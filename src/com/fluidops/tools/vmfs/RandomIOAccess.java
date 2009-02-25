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

/**
 * Random IO access backed by random access file.
 * @author Uli
 */
public class RandomIOAccess extends IOAccess
{
    RandomAccessFile raf;

    public RandomIOAccess( String file, String mode ) throws IOException
    {
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
        return raf.read(buffer, offset, size);
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
            e.printStackTrace();
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
}
