/*
 * RemoteSSHIOAccess.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.fluidops.util.HexDump;
import com.trilead.ssh2.*;

/**
 * IO access based on SSH.
 * @author Uli
 */
public class RemoteSSHIOAccess extends IOAccess
{
    URI url;
    boolean rw;
    String host, user, passwd, path;
    int port;
    Connection c;
    ConnectionInfo ci;
    SFTPv3Client sftp;
    SFTPv3FileHandle deviceHandle;
    long pos;
    long size;
    
    /**
     * Creates a new Remote SSH IO access.
     * @param sshUri SSH URI of the device/file
     * @param rw Open the file in RW mode
     * @throws IOException
     * @throws URISyntaxException
     */
    public RemoteSSHIOAccess( String sshUri, boolean rw ) throws IOException, URISyntaxException
    {
        this( new URI(sshUri), rw );
    }

    /**
     * Creates a new Remote SSH IO Access
     * @param url SSH URI of the device/file
     * @param rw Open the file in RW mode
     * @throws IOException
     */
    public RemoteSSHIOAccess( URI url, boolean rw ) throws IOException
    {
        this.url = url;
        this.rw = rw;
        host = url.getHost();
        
        if ( url.getPort()<0 )
            port = 22;
        else
            port = url.getPort();

        user = url.getUserInfo();
        if ( user.indexOf(':')>0 )
        {
            int ix = user.indexOf(':');
            passwd = user.substring(ix+1);
            user = user.substring( 0, ix );
        }
        path = url.getPath();
        
        c = new Connection( host, port );
        ci = c.connect();
        c.authenticateWithPassword(user, passwd);
        sftp = new SFTPv3Client( c );
        
        openDevice();
        SFTPv3FileAttributes attr = sftp.fstat( deviceHandle );
        closeDevice();
        size = attr.size;
    }

    void openDevice() throws IOException
    {
        deviceHandle = rw ? sftp.openFileRW( path ) : sftp.openFileRO( path );
    }

    void closeDevice()
    {
        if ( deviceHandle!=null )
        {
            try
            {
                sftp.closeFile( deviceHandle );
                deviceHandle = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void close()
    {
        closeDevice();
        sftp.close();
        c.close();
    }

    @Override
    public long getPosition()
    {
        return pos;
    }

    @Override
    public long getSize()
    {
        return size;
    }

    @Override
    public synchronized int read(byte[] buffer, int offset, int size) throws IOException
    {
        if ( size==0 ) return 0;
        openDevice();
        try
        {
            int res = sftp.read( deviceHandle, pos, buffer, offset, size);
            pos += res;
            return res;
        }
        finally
        {
            closeDevice();
        }
    }

    @Override
    public void setPosition(long pos)
    {
        this.pos = pos;
    }

    @Override
    public void setSize(long newSize)
    {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public synchronized void write(byte[] buffer, int offset, int size) throws IOException
    {
        openDevice();
        try
        {
            sftp.write( deviceHandle, pos, buffer, offset, size);
            pos += size;
        }
        finally
        {
            closeDevice();
        }
    }
    
    @Override
    public String toString()
    {
    	return "RemoteSSHIOAccess "+url.getHost()+":"+url.getPath()+" pos="+getPosition()+" size="+getSize();
    }
}
