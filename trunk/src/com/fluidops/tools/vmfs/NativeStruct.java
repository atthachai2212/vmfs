/*
 * NativeStruct.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * A utility class to decode native binary data structures.
 * 
 * @author Uli
 */
public class NativeStruct
{
    Class<?> type;
    static Map<Class<?>, Integer> sizeOfType;
    List<FieldInfo> fields;
    int size;
    
    static
    {
        // Initialize the type <=> size map
        Object[] l = {
                byte.class,     1,
                short.class,    2,
                int.class,      4,
                long.class,     8,
                float.class,    4,
                double.class,   8,                
        };
        
        sizeOfType = new HashMap<Class<?>, Integer>();
        for ( int i=0; i<l.length; i+=2 )
            sizeOfType.put( (Class<?>)l[i], (Integer)l[i+1] );        
    }

    /**
     * Info about a field.
     * @author Uli
     */
    static class FieldInfo
    {
        Field field;
        int pos = -1, size = -1;
        String SEPARATOR = "__";
     
        FieldInfo( Field field )
        {
            this.field = field;
            getMetaInfo();
        }
        
        int lengthOfField()
        {
            Integer _size = sizeOfType.get( field.getType() );
            if ( _size!=null )
                return _size;
            else
            {
                if ( !field.getType().equals(String.class) )
                {
                    NativeStruct n = new NativeStruct( field.getType() );
                    if ( n.getSize()>0 )
                        return n.getSize();
                }
                return -1;
            }
        }
        
        void getMetaInfo()
        {
            pos = -1;
            size = lengthOfField();
            
            String name = field.getName();
            int ix = name.lastIndexOf( SEPARATOR );
            if ( ix>0 )
            {
                String meta = name.substring( ix+2 );
                StringTokenizer st = new StringTokenizer( meta, "_" );
                List<Integer> res = new ArrayList<Integer>();
                while ( st.hasMoreTokens() )
                {
                    String s = st.nextToken();
                    res.add( Integer.decode( s ) );
                }
                if ( res.size()==1 )
                {
                    if ( size!=-1 )
                        pos = res.get(0);
                    else
                        size = res.get(0);
                }
                else if ( res.size()==2 )
                {
                    size = res.get(0);
                    pos = res.get(1);                    
                }
                else
                    throw new IllegalArgumentException("Invalid meta information in field "+ field );
            }
        }
        
        public String toString()
        {
            return field.getName()+" pos=0x"+Integer.toHexString(pos)+" size="+size;
        }
    }

    /**
     * Constructs a NativeStruct for the given type.
     * @param type
     */
    public NativeStruct( Class<?> type )
    {
        this.type = type;
        fields = new ArrayList<FieldInfo>();
        int pos = 0;
        size = 0;
        for ( Field f : type.getFields() )
        {            
            FieldInfo fi = new FieldInfo( f );
            
            if ( fi.size<0 )
                throw new IllegalArgumentException("Type="+type+": Field has unknown length: "+f);

            if ( fi.pos<0 )
                fi.pos = pos;
            else
                pos = fi.pos;
            
            fields.add( fi );
            
            //System.out.println( " >>> " + fi );
            
            pos += fi.size;
            if ( pos>size )
                size = pos;
        }
    }

    /**
     * Returns the size (in bytes) of this struct.
     * @return
     */
    public int getSize()
    {
        return size;
    }
    
    /**
     * toString
     */
    public String toString()
    {
        return "NativeStruct type="+type.getName()+" size="+size+" fields="+fields;
    }

    /**
     * Decodes at the given offset from the buffer.
     * Unmarshals into the given object (which must be of the type with
     * which this NativeStruct instance was created).
     *  
     * @param obj
     * @param buffer
     * @param offset
     * @throws Exception
     */
    public void decode( Object obj, byte[] buffer, int offset ) throws Exception
    {
        ByteBuffer bb = ByteBuffer.wrap( buffer );
        bb.order( ByteOrder.LITTLE_ENDIAN );
        for ( FieldInfo fi : fields )
        {
            if ( fi.field.getType().equals(byte.class) )
            {                
                fi.field.set( obj, bb.get( offset+fi.pos ) );
            }
            else
            if ( fi.field.getType().equals(short.class) )
            {                
                fi.field.set( obj, bb.getShort( offset+fi.pos ) );
            }
            else
            if ( fi.field.getType().equals(int.class) )
            {                
                fi.field.set( obj, bb.getInt( offset+fi.pos ) );
            }
            else
            if ( fi.field.getType().equals(long.class) )
            {                
                fi.field.set( obj, bb.getLong( offset+fi.pos ) );
            }
            else
            if ( fi.field.getType().equals(byte[].class) )
            {
                byte[] b = new byte[ fi.size ];
                System.arraycopy( buffer, offset+fi.pos, b, 0, fi.size );
                fi.field.set( obj, b );
            }
            else
            if ( fi.field.getType().equals(String.class) )
            {
                int l;
                for ( l=0; l<fi.size; l++ )
                    if ( buffer[offset+fi.pos+l]==0 ) break;
                
                fi.field.set( obj, new String(buffer, offset+fi.pos, l, "UTF-8") );
            }
            else
            {
                // A compound sub-struct
                Class _type = fi.field.getType();
                NativeStruct n = new NativeStruct( _type );
                int _size = n.getSize();
                Object _obj = _type.newInstance();
                n.decode( _obj, buffer, offset+fi.pos );
                fi.field.set( obj, _obj );
            }
        }        
    }

    /**
     * Unmarshals the native struct at the given offset from the given buffer into
     * the provided object.
     * 
     * @param obj
     * @param buffer
     * @param offset
     * @throws Exception
     */
    public static void fromNative( Object obj, byte[] buffer, int offset ) throws Exception
    {
        NativeStruct ns = new NativeStruct(obj.getClass());
        ns.decode( obj, buffer, offset );        
    }

    /**
     * Test main.
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable
    {
        NativeStruct ns = new NativeStruct( VMFSDriver.VolumeInfo.class );
        System.out.println( ns );
    }
}
