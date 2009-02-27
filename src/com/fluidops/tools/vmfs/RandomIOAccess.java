/*
 * RandomIOAccess.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.fluidops.util.logging.Debug;

/**
 * Random IO access backed by random access file.
 * @author Uli
 */
public class RandomIOAccess extends IOAccess
{
    RandomAccessFile raf;
    String file, mode;

    public RandomIOAccess( String file, String mode ) throws IOException
    {
    	this.file = file;
    	this.mode = mode;
        raf = new RandomAccessFile( file, mode );
    }
    
    @Override
    public long getPosition()
    {
        try
        {
            return raf.getFilePointer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int size) throws IOException
    {
    	Debug.out.println( "reading at "+ Long.toHexString(raf.getFilePointer())+" amount="+size);

    	long pos = raf.getFilePointer();
    	int ALIGN = 512;
    	int smod = (int)(pos%ALIGN), emod=(int)((pos+size)%ALIGN);
    	
    	if ( smod!=0 || emod!=0 )
    	{
    		// Unaligned access to the device, now align the access
    		long tpos = pos;
    		int tsize = size;
    		if ( smod!=0 )
    		{
    			tpos -= smod;
    			tsize += smod;
    		}
    		if ( emod!=0 )
    		{
    			tsize += ALIGN-emod;
    		}

    		// Create a temp buffer for the aligned access
    		byte[] tbuffer = new byte[ tsize ];
    		raf.seek( tpos );
        	Debug.out.println( "aligned reading at "+ Long.toHexString(raf.getFilePointer())+" amount="+tsize);
    		int tres = raf.read( tbuffer, 0, tsize );
            Debug.out.println( "aligned reading res="+tres );    		

            // Adjust the result
    		tres -= smod;
    		if ( tres<0 ) tres = 0;
    		if ( tres>size ) tres = size;
            Debug.out.println("reading res="+tres);    		
    		raf.seek( pos + tres );
    		
    		// Copy the resulting data into the buffer
    		System.arraycopy(tbuffer, smod, buffer, offset, tres);
    		return tres;
    	}

    	// Regular access, just read
        int res = raf.read(buffer, offset, size);
        Debug.out.println("reading res="+res);
        return res;
    }

    @Override
    public void setPosition(long pos)
    {
        try
        {
            raf.seek( pos );
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int size) throws IOException
    {
        raf.write( buffer, offset, size );
    }

    @Override
    public long getSize()
    {
        try
        {
            return raf.length();
        }
        catch (IOException e)
        {
            Debug.out.println( "Error getting size: "+ e );
            return -1;
        }
    }

    @Override
    public void setSize(long newSize)
    {
        try
        {
            raf.setLength( newSize );
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public void close()
    {
        try
        {
            raf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }        
    }
    
    @Override
    public String toString()
    {
    	return "RandomIOAccess "+file+"("+mode+") pos="+getPosition()+" size="+getSize();
    }
}
