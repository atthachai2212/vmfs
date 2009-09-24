/*
 * IOAccess.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generic IO device and file access interface.
 * @author Uli
 */
public abstract class IOAccess
{
    public abstract long getPosition();
    public abstract void setPosition( long pos );
    public abstract int read( byte[] buffer, int offset, int size ) throws IOException;
    public abstract void write( byte[] buffer, int offset, int size ) throws IOException;
    public abstract long getSize();
    public abstract void setSize( long newSize );
    public abstract void close();
    
    public void open( String file, java.util.Properties props ) throws IOException
    {
    	throw new IOException("Not implemented");
    }
    
    public int read( long pos, byte[] buffer, int offset, int size ) throws IOException
    {
        setPosition( pos );
        return read( buffer, offset, size );
    }
    public void write( long pos, byte[] buffer, int offset, int size ) throws IOException
    {
        setPosition( pos );
        write( buffer, offset, size );
    }
    
    public byte[] read( long pos, int size ) throws IOException
    {
        byte[] res = new byte[ size ];
        read( pos, res, 0, res.length );
        return res;
    }
    
    public void write( long pos, byte[] buffer ) throws IOException
    {
        write( pos, buffer, 0, buffer.length );
    }
    
    public InputStream getInputStream()
    {
        return new InputStream()
        {
            @Override
            public int read(byte[] b, int off, int len) throws IOException
            {
                return IOAccess.this.read(b, off, len);
            }

            @Override
            public int read() throws IOException
            {
                byte[] b = new byte[1];
                int r = read( b, 0, 1);
                if ( r==1 )
                    return (int) (b[0]&0xff);
                else
                    return -1;
            }            
        };
    }
}
