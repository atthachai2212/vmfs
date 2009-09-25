/*
 * RemoteSSHIOAccess.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import com.fluidops.util.logging.Debug;
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
    
    Session s;
    
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

        // Enable auth without passwd
        if ( passwd!=null )
        	c.authenticateWithPassword(user, passwd);
        else
        	c.authenticateWithNone(user);
        
        try
        {
        	sftp = new SFTPv3Client( c );
            openDevice();
            SFTPv3FileAttributes attr = sftp.fstat( deviceHandle );
            closeDevice();
            size = attr.size;
        }
        catch (IOException ex)
        {
        	System.out.println("SFTP failed, fallback to SSH");
        	// Fall back to SSH only
        	sftp = null;
        	
            s = c.openSession();
            s.execCommand("stat -t \""+path+"\"");
            String res = readUrl( s.getStdout() );
            String err = readUrl( s.getStderr() );
            if ( err!=null && err.length()>0 )
                throw new IOException( err );
            
            StringTokenizer st = new StringTokenizer(res, " ");
            /* String statPath = */ st.nextToken();
            size = Long.parseLong( st.nextToken() );        	
        }        
    }

	/**
	 * read URL into string
	 */
    public static ByteArrayOutputStream readUrlToBuffer( InputStream in ) throws IOException
    {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (;;)
        {
            int len = in.read( buffer );
            if ( len == -1 )
                break;
            out.write( buffer, 0, len );
        }
        in.close();
        return out;
    }
    
    /**
     * read URL into string
     */
    public static String readUrl( InputStream in ) throws IOException
    {
        return readUrlToBuffer( in ).toString();
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
        if ( sftp!=null )
        {
            closeDevice();
        	sftp.close();
        }
        if ( s!=null )
        	s.close();

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
        if ( sftp!=null )
        {
        	// We have SFTP
            openDevice();
            try
            {
                int res = sftp.read( deviceHandle, pos, buffer, offset, size);
                Debug.out.println("SSH: read "+buffer.length+" @"+Long.toHexString(pos));
                pos += res;
                return res;
            }
            finally
            {
                closeDevice();
            }
        }
        else
        {
        	// SSH fallback
            Session s = c.openSession();
            String ddCmd;
            // If pos and size are on block boundaries, make use of bs=512 to reach up to 2TB
            if ( pos%512==0 && size%512==0 )
            	ddCmd = "dd if=\""+path+"\" bs=512 skip="+(pos/512L)+" count="+(size/512);
            else
            	ddCmd = "dd if=\""+path+"\" bs=1 skip="+pos+" count="+size;
            	
            s.execCommand( ddCmd );
            byte[] output = readUrlToBuffer( s.getStdout() ).toByteArray();
            Debug.out.println("SSH: read "+output.length+" @"+Long.toHexString(pos));
            System.arraycopy( output, 0, buffer, offset, output.length );
            pos += output.length;
            s.close();
            return output.length;
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
    	return "RemoteSSHIOAccess "+url.getHost()+":"+url.getPath()+" pos="+getPosition()+" size="+getSize()+" SFTP="+(sftp!=null);
    }
}
