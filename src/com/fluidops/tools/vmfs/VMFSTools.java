/*
 * VMFSTools.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fluidops.base.Version;
import com.fluidops.tools.vmfs.VMFSDriver.FileMetaInfo;
import com.fluidops.util.HexDump;
import com.fluidops.util.StringUtil;

/**
 * VMFS CLI tools.
 * @author Uli
 */
public class VMFSTools
{
    VMFSDriver vi;
    
    /**
     * Recursively reads directories.
     * @param path
     * @return
     * @throws Exception
     */
    List<FileMetaInfo> dirRecursive(String path)
            throws Exception
    {
        System.err.println("recur " + path);
        List<FileMetaInfo> fmis = vi.dir(path);
        List<FileMetaInfo> res = new ArrayList<FileMetaInfo>();
        for (FileMetaInfo fmi : fmis)
            if (fmi.fr.isFolder())
            {
                if (!path.endsWith("/"))
                    path += "/";
                try
                {
                    res.addAll(dirRecursive(path + fmi.fr.name__128));
                }
                catch (Exception ex)
                {
                    System.err.println("Error recursing directory "+path+fmi.fr.name__128+" : "+ex);
                }
            }
        fmis.addAll(res);
        return fmis;
    }

    /**
     * IO Access that gathers analysis data.
     * @author Uli
     */
    static class AnalysisIOAccess extends IOAccess
    {
        IOAccess io;
        List<Long> readAddress;
        List<Long> readSize;

        AnalysisIOAccess(IOAccess io)
        {
            this.io = io;
            readAddress = new ArrayList<Long>();
            readSize = new ArrayList<Long>();
        }

        public void close()
        {
            io.close();
        }

        public long getPosition()
        {
            return io.getPosition();
        }

        public long getSize()
        {
            return io.getSize();
        }

        public int read(byte[] buffer, int offset, int size) throws IOException
        {
            if (buffer == null)
            {
                if (!readAddress.isEmpty())
                {
                    // Can we merge the last read operation with this one?
                    long ladr = readAddress.get(readAddress.size() - 1);
                    long lsize = readSize.get(readSize.size() - 1);
                    if (ladr + lsize == io.getPosition())
                        readSize.set(readSize.size() - 1, lsize + size);
                    else
                    {
                        readAddress.add(io.getPosition());
                        readSize.add((long) size);
                    }
                }
                else
                {
                    readAddress.add(io.getPosition());
                    readSize.add((long) size);
                }
                return size;
            }

            return io.read(buffer, offset, size);
        }

        public void setPosition(long pos)
        {
            io.setPosition(pos);
        }

        public void setSize(long newSize)
        {
            io.setSize(newSize);
        }

        public void write(byte[] buffer, int offset, int size)
                throws IOException
        {
            io.write(buffer, offset, size);
        }
    }

    static Properties parseCommaSeparatedProps( String file )
    {    	
    	Properties res = null;
    	if ( file.indexOf(',')>0 )
    	{
    		res = new Properties();
    		try
			{
				res.load( new StringReader( file.replace(',', '\r') ) );
			}
			catch (IOException e)
			{
				res = null;
			}
    	}
    	return res;
    }
    
    public static IOAccess openIOAccess( String ioProvider, String ioFile, Properties ioProps ) throws IOException
    {
    	IOAccess io = null;
    	try
		{
			io = (IOAccess) Class.forName( ioProvider ).newInstance();
		}
		catch (Exception e)
		{
			throw new IOException("Provider class "+ioProvider+" not found");
		}
		io.open( ioFile, ioProps );
		return io;
    }
    
    public static VMFSDriver getVMFSDriver( String volume ) throws Exception
    {
        Properties props = parseCommaSeparatedProps( volume );
        VMFSDriver vi = new VMFSDriver();
        if ( props!=null )
        {
        	vi.setVolumeIOAccess(
        		openIOAccess( props.getProperty("provider"), props.getProperty("file"), props )
        	);
        }
        else
        	vi.openVolume( volume );
        return vi;
    }
    
    void cli( String[] args) throws Throwable
    {
        System.out.println("VMFSTools (C) by fluid Operations (v"+Version.getVersion()+" r"+Version.getRevision()+" / " +Version.getBuildDate() + ")");
        System.out.println("http://www.fluidops.com");
        System.out.println();
        if ( args.length<2 )
        {
            System.out.println("Arguments:");
            System.out.println("  VMFSVolume info");
            System.out.println("  VMFSVolume dir path");
            System.out.println("  VMFSVolume dirall path");
            System.out.println("  VMFSVolume fileinfo path");
            System.out.println("  VMFSVolume filecopy path [newname position size]");
            System.out.println("  VMFSVolume filedump path position size");
            System.out.println("  VMFSVolume webdav [host port]");
            System.out.println();
            System.out.println("VMFSVolume can be any mounted VMFS volume, or a volume reachable by SFTP.");
            System.out.println("Examples:");
            System.out.println("  \\\\sambaserver\\luns\\bigdisk dir /Linux_VMs");
            System.out.println("  ssh://root:passwd@linuxhost/mnt/vmfslun fileinfo /disks/SwapDisk-flat.vmdk");
            return;
        }

        String f = args[0];
        String cmd = args[1];
            
        vi = getVMFSDriver( f );
        vi.openVmfs();

        if ( "info".equals(cmd) )
        {
            showVMFSInfo();
        }
        if ( "dirall".equals(cmd) )
        {
            String path = args[2];
            if ( path==null )
                throw new Exception("Path missing");
            showDirRecursive(path);
        }
        else if ( "dir".equals(cmd) )
        {
            String path = args[2];
            if ( path==null )
                throw new Exception("Path missing");
            showDir(path);            
        }
        else if ( "fileinfo".equals(cmd) )
        {
            String file = args[2];
            if ( file==null )
                throw new Exception("File missing");
            showFileInfo(file);
        }
        else if ( "filedump".equals(cmd) )
        {
            String file = args[2];
            long pos = Long.valueOf(args[3]);
            int sz = Integer.parseInt(args[4]);
            showFileDump(file, pos, sz);
        }
        else if ( "filecopy".equals(cmd) )
        {
            String file = args[2];
            String localname = args.length>3 ? args[3] : null;
            long pos = args.length>4 ? Long.valueOf(args[4]) : 0;
            int sz = args.length>5 ? Integer.parseInt(args[5]) : 0;
            doFileCopy(file, pos, sz, localname);
        }
        else if ( "webdav".equals(cmd) )
        {
        	String bind = args.length>2 ? args[2] : "localhost";
        	int port = args.length>3 ? Integer.parseInt(args[3]) : 50080;
        	runWebDAVServer( bind, port );
        }
        vi.closeVolume();        
    }

    static void showVMFSInfo( java.io.PrintStream out, VMFSDriver vi ) throws Exception
    {
        VMFSDriver.VolumeInfo volInfo = vi.vi;
        VMFSDriver.VMFSSuperBlock superBlock = vi.sb;
        VMFSDriver.FSInfo fs = vi.fs;
        
        out.println("VMFS label         = "+fs.label__128);
        out.println("VMFS creation date = "+new java.util.Date(1000L*fs.timestamp) );
        out.println("VMFS capacity      = "+ StringUtil.displaySizeInBytes((long)vi.fbbBmp.bmp.itemCount*vi.blockSize) );
        out.println("VMFS UUID          = "+fs.uuid);
        out.println("VMFS block size    = "+StringUtil.displaySizeInBytes(fs.blocksize__0xa1));
        out.println("VMFS version       = 3."+fs.version);
        out.println("VMFS # of FD/PB/SB = "+vi.fdcBmp.bmp.itemCount+" / "+vi.pbcBmp.bmp.itemCount+" / "+vi.sbcBmp.bmp.itemCount);

        out.println("VMFS volume type   = "+ volInfo.name__28_0x12 );
        out.println("VMFS volume UUID   = "+ volInfo.uuid__0x82);
        out.println("VMFS volume size   = "+ StringUtil.displaySizeInBytes(superBlock.size) );
        out.println("VMFS volume ver    = "+fs.volumeVersion); // 3, 4 or 5        
    }

    void showVMFSInfo() throws Exception
    {
    	showVMFSInfo( System.out, vi );
    }

    void showFileDump(String file, long pos, int sz) throws Exception
    {
        IOAccess io = vi.openFile( file );
        System.out.println("Size = "
                + StringUtil.displaySizeInBytes(io.getSize()));
        
        HexDump.dump( io.read(pos, sz) );
        io.close();
    }

    void doFileCopy(String file, long pos, long sz, String localfile) throws Exception
    {
        IOAccess io = vi.openFile( file );
        long size = io.getSize();
        System.out.println("Size = "
                + StringUtil.displaySizeInBytes(size) );

        if ( localfile==null )
        	localfile = new File(file).getName();
        
        io.setPosition(pos);
        if ( sz==0 )
        	sz = size;
        
        FileOutputStream out = new FileOutputStream( localfile );
        
        long todo = sz;
        int CHUNK = 16384;
        byte[] buffer = new byte[CHUNK];
        long t0 = System.currentTimeMillis();
        while ( todo>0 )
        {
        	int now = todo>CHUNK ? CHUNK : (int)todo;
        	int res = io.read( buffer, 0, now );
        	out.write( buffer, 0, res );
        	todo -= res;
        	
        	if ( System.currentTimeMillis()-t0 > 2500 )
        	{
        		t0 = System.currentTimeMillis();
        		
        		System.out.println("Copying file -- bytes left="+todo);
        	}
        }
        
        out.close();
        io.close();
    }

    void showDirRecursive(String path) throws Exception
    {
        List<FileMetaInfo> dir = dirRecursive( path );
        if (dir != null)
            for (FileMetaInfo fr : dir)
            {
                System.out.println(fr);
            }
    }

    void showDir(String path) throws Exception
    {
        List<FileMetaInfo> dir = vi.dir( path );
        if (dir != null)
            for (FileMetaInfo fr : dir)
            {
                System.out.println(fr);
            }
    }

    void showFileInfo(String file) throws IOException
    {
        IOAccess io = vi.openFile( file );
        System.out.println("Size = "
                + StringUtil.displaySizeInBytes(io.getSize()));
   
        // Read over the whole file to gather the block allocation analysis
        long todo = io.getSize();
        IOAccess device = vi.rf;
        AnalysisIOAccess analysis = new AnalysisIOAccess(device);
        vi.rf = analysis;
   
        int bs = vi.blockSize;
        while (todo > 0)
        {
            long thisTime = Math.min(bs, todo);
            int done = io.read(null, 0, (int) thisTime);
            todo -= done;
        }
   
        vi.rf = device;
   
        for (int i = 0; i < analysis.readAddress.size(); i++)
        {
            long adr = analysis.readAddress.get(i);
            long sz = analysis.readSize.get(i);
            System.out.println("Extent[" + i + "] = @" + Long.toHexString(adr)
                    + "(" + adr + ") size=" + Long.toHexString(sz) + "(" + sz
                    + ")");
        }
        io.close();
    }
    
    void runWebDAVServer( String host, int port ) throws Exception
    {
    	VMFSWebDAV.runWebDAVServer( vi, host, port );
    }
    
    /**
     * Main entry point.
     * @param args
     */
    public static void main(String[] args) throws Throwable
    {
        VMFSTools vt = new VMFSTools();
        vt.cli( args );
    }
}
