/*
 * VMFSTools.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fluidops.base.Version;
import com.fluidops.tools.vmfs.VMFSDriver.FSInfo;
import com.fluidops.tools.vmfs.VMFSDriver.FileMetaInfo;
import com.fluidops.util.HexDump;
import com.fluidops.util.StringUtil;
import com.fluidops.util.logging.Debug;

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
            System.out.println("  VMFSVolume filecopy path position size");
            System.out.println("  VMFSVolume filedump path position size");
            System.out.println();
            System.out.println("VMFSVolume can be any mounted VMFS volume, or a volume reachable by SFTP.");
            System.out.println("Examples:");
            System.out.println("  \\\\sambaserver\\luns\\bigdisk dir /Linux_VMs");
            System.out.println("  ssh://root:passwd@linuxhost/mnt/vmfslun fileinfo /disks/SwapDisk-flat.vmdk");
            return;
        }

        String f = args[0];
        String cmd = args[1];
            
        vi = new VMFSDriver();
        vi.openVolume(f);
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
        vi.closeVolume();        
    }

    void showVMFSInfo() throws Exception
    {
        VMFSDriver.VolumeInfo volInfo = this.vi.vi;
        VMFSDriver.VMFSSuperBlock superBlock = this.vi.sb;
        VMFSDriver.FSInfo fs = this.vi.fs;
        
        System.out.println("VMFS label         = "+fs.label__128);
        System.out.println("VMFS creation date = "+new java.util.Date(1000L*fs.timestamp) );
        System.out.println("VMFS capacity      = "+ StringUtil.displaySizeInBytes((long)this.vi.fbbBmp.bmp.itemCount*this.vi.blockSize) );
        System.out.println("VMFS UUID          = "+fs.uuid);
        System.out.println("VMFS block size    = "+StringUtil.displaySizeInBytes(fs.blocksize__0xa1));
        System.out.println("VMFS version       = 3."+fs.version);
        System.out.println("VMFS # of FD/PB/SB = "+vi.fdcBmp.bmp.itemCount+" / "+vi.pbcBmp.bmp.itemCount+" / "+vi.sbcBmp.bmp.itemCount);

        System.out.println("VMFS volume type   = "+ volInfo.name__28_0x12 );
        System.out.println("VMFS volume UUID   = "+ volInfo.uuid__0x82);
        System.out.println("VMFS volume size   = "+ StringUtil.displaySizeInBytes(superBlock.size) );
        System.out.println("VMFS volume ver    = "+fs.volumeVersion); // 3, 4 or 5        
    }
    
    void showFileDump(String file, long pos, int sz) throws Exception
    {
        IOAccess io = vi.openFile( file );
        System.out.println("Size = "
                + StringUtil.displaySizeInBytes(io.getSize()));
        
        HexDump.dump( io.read(pos, sz) );
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
