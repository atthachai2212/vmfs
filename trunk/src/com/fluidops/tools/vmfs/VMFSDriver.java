/*
 * VMFSInfo.java // com.fluidops.tools.vmfs
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.tools.vmfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import com.fluidops.base.Version;
import com.fluidops.util.StringUtil;
import com.fluidops.util.logging.Debug;

/**
 * The VMware VMFS driver.
 * Provides directory and file access to VMFS volumes.
 * 
 * @author Uli
 */
public class VMFSDriver
{
    public static class UUID
    {
        public byte[] uuid__16;

        String twoDigitHex( byte b )
        {
            String s = Integer.toHexString( (b&0xff) );
            if ( s.length()<2 )
                s = "0"+s;
            return s;
        }
        
        public String toString()
        {
            if ( uuid__16==null )
                return "(null)";
            
            String s = "";
            for (int p=3; p>=0; p--)
                s += twoDigitHex( uuid__16[p] );
            s += "-";
            for (int p=3; p>=0; p--)
                s += twoDigitHex( uuid__16[p+4] );
            s += "-";
            for (int p=1; p>=0; p--)
                s += twoDigitHex( uuid__16[p+8] );
            s += "-";
            for (int p=0; p<=5; p++)
                s += twoDigitHex( uuid__16[p+10] );
            return s;
        }
    }
    
    public static class VolumeInfo
    {
        public int magic;
        public int version;
        public String name__28_0x12;
        public UUID uuid__0x82;
        public long unknown__0x92;
        public long unknown__0x9a;
    }
    
    public static class VMFSSuperBlock
    {
        public long size;
        public long blocks;
        public int unknown;
        public String uuidString__35;
        byte[] fill__0x1d;
        public UUID uuid__0x54;
        public int unknown2;
        public long l1;
        public long l2;
        public long l3;
        public long l4;        
        public long l5;
    }
    
    public static class FSInfo
    {
        public int magic;
        public int volumeVersion;
        public byte version;
        public UUID uuid;
        public int unknown2;
        public String label__128;
        public long blocksize__0xa1;
        public int timestamp;
        public int unknownFlag; // only saw 1 in here
        public UUID volumeUuid__0xb1;
    }
    
    public static class Record
    {
        public int magic;
        public long number;
    }
    
    
    public static class FileRecord
    {
        public int type;
        public int blockId;
        public int recordId;
        public String name__128;
        
        public boolean isFolder()
        {
            return type==2 || type==4;
        }
        
        public boolean equals( Object other )
        {
            return other instanceof FileRecord &&
                ((FileRecord)other).blockId==blockId &&
                ((FileRecord)other).recordId==recordId &&
                ((FileRecord)other).name__128.equals(name__128);
        }
        
        public int hashCode()
        {
            return name__128.hashCode();
        }
        
        public String toString()
        {
            return name__128 + " (" + (isFolder() ? "folder":"file") + ")";            
        }
    }
    
    public static class FileMetaHeader
    {
        public int groupId;
        public int position;        
    }
    
    public static class FileMetaRecord
    {
        public int id;
        public int id2;
        public int unknown1, type, unknown3;
//        public byte[] unknown__6;
        public long size;
        public int timeStamp1__0x2c;
        public int timeStamp2;
        public int timeStamp3;
    }
    
    public static class RDMMetaRecord
    {
        public int unknownid;
        public short unknownid2;
        public String lunType__28;
        public int blocks;
        public String lunUuid__17;
    }
    
    public static class BlockID
    {
        public int id;
        public int subgroup;
        public int type;
        public int number;
        
        public BlockID(int id)
        {
            this.id = id;
            type = id & 7;
            toString(); // TODO: clean init
        }
        
        public String toString()
        {
            String s = null;
            switch( type )
            {
                case 0:
                    // type==0: NULL
                    s = "NULL";
                    if ( id!=0 )
                        s = "NULL(INVALID)";
                    break;
                case 1:
                    // type==1: full block address            
                    // tbz, blk
                    s = "FullBlock";
                    subgroup = (id&32)!=0 ? 1 : 0;
                    number = ( id&0xffffffff ) >> 6;
                    break;
                case 2:
                    // type==2: sub block address
                    // number==cnum, subgroup=rnum
                    s = "SubBlock";
                    subgroup = (id>>28) & 0xf;
                    number = ( id&0xfffffff ) >> 6;
                    break;
                
                case 3:
                    // type==3: pointer block address
                    // cnum, rnum
                    s = "PointerBlock";
                    subgroup = (id>>28) & 0xf;
                    number = ( id&0xfffffff ) >> 6;
                    break;
                    
                case 4:
                    // type==4: file descriptor
                    // cnum, rnum
                    s = "FileDescriptor";
                    subgroup = (id>>6) & 0x7fff;
                    number = ( id >> 22 ) & 0x3ff;
                    break;
                default:
                    s = "UNKNOWN";
                    break;
            }
                        
            return "Block " + s + "("+Long.toHexString(id & 0xffffffffL)+") type="+type
                +" number="+number+" subgroup="+subgroup;
        }
        
    }
    
    public static class BitmapMetaHeader
    {
        public int blocks;
        public int count;
        public int dataSize;
        public int subDataSize;
        public short metaDataSize;
        public short dataItems;
        public int itemCount;
        public int bitmaps;
    }
    
    public static class BitmapHeader
    {
        public int magic;
        public int id;
        public int flag__0x1c; // unknown, seems always 1
        
        public int bitmapId__0x200;
        public int total;
        public int free;
        public int used;
        
        public byte[] bitmap__0x100;
    }

    IOAccess rf;
    long vmfsBase;
    int blockSize;
 
    VolumeInfo vi;
    VMFSSuperBlock sb;
    FSInfo fs;
    
    boolean hyperverbose;
    
    /**
     * Reads a block from specified location.
     * @param pos
     * @param size
     * @return
     * @throws IOException
     */
    byte[] readBlock( long pos, int size ) throws IOException
    {
        return rf.read( pos, size);
    }

    /**
     * This class encapsulates the VMFS allocation bitmaps.
     * Virtually all meta data is managed using these allocation tables.
     * 
     * @author Uli
     */
    class BitmappedBlockAllocation
    {
        IOAccess io;
        long basePos;
        long firstBitmapPos;
        BitmapMetaHeader bmp;
        long dataBlockSize;
        
        BitmappedBlockAllocation( IOAccess io ) throws Exception
        {
            this( io, 0 );
        }
        
        BitmappedBlockAllocation( IOAccess io, long basePos ) throws Exception
        {
            this.io = io;
            this.basePos = basePos;
            readBitmapInfo();
            
            /* DEBUG
            if ( getDataBlockAreaSize()>0 )
            for ( int i=0; i<bmp.itemCount; i+=16 )
            {
                Debug.out.println( "BlockName[" + i + " = " + Long.toHexString( getDataBlockAddress(i) ) );
                if ( i>100*16 ) break;
            }
            */
        }

        long getDataBlockAreaSize()
        {
            return dataBlockSize;
        }
        
        long getDataBlockSize()
        {
            return bmp.subDataSize;
        }
        
        long getDataBlockAddress( long block )
        {
            long addr = block * getDataBlockSize();
            long area = addr / getDataBlockAreaSize();
            long areaAddr = getDataBlockAreaAddress( area );
            
            long perBlock = getDataBlockAreaSize() / getDataBlockSize();
            long mod = block % perBlock;
            
            return areaAddr + mod * getDataBlockSize();
        }
        
        long getDataBlockAreaAddress( long area )
        {
            long gr = area;
            
            long bmpMetaSize = 0x400 * bmp.count;
            long grAddress = firstBitmapPos + (gr+1) * bmpMetaSize + gr * dataBlockSize;
            
            return basePos + grAddress;
        }
        
        /**
         * Reads bitmap info.
         * @throws Exception
         */
        void readBitmapInfo() throws Exception
        {
            long pos = basePos;
            
            Debug.out.println("Reading block allocation bitmap from "+io);
            Debug.out.println("Bitmap @"+Long.toHexString(pos));
            byte[] header = io.read( pos, 0x800 );
            
            bmp = new BitmapMetaHeader();
            NativeStruct.fromNative( bmp, header, 0);
            Debug.out.println("Blocks per bitmap = " + bmp.blocks );
            Debug.out.println("Managed items = "+bmp.itemCount);
            Debug.out.println("Count = "+bmp.count);

            Debug.out.println("Data block size = "+bmp.subDataSize);
            Debug.out.println("Data items = "+bmp.dataItems );
            Debug.out.println("Data size = "+bmp.dataSize);

            
            dataBlockSize = (long)(bmp.dataItems & 0xffff) * (long)bmp.dataSize;
            Debug.out.println("Data area size = "+Long.toHexString(dataBlockSize) );

            firstBitmapPos = 0x10000;
            pos += firstBitmapPos;
        }
        
        void scanAllBitmaps() throws Exception
        {
            long pos = basePos + firstBitmapPos;
            
            int totalItems = bmp.itemCount;
            int expectedBitmapId = 0;
            
            int dataBlockCount = 0;
            int magic = 0;
            while ( totalItems > 0 )
            {
                io.setPosition( pos );
                Debug.out.println("Bitmap headers @"+Long.toHexString(io.getPosition()));
    
                byte[] bmpBuf = new byte[ 0x400 ];
                for ( int bitmapCount=0; bitmapCount<bmp.count; bitmapCount++)
                {
                    io.read( bmpBuf, 0, bmpBuf.length );
                    BitmapHeader bmpHdr = new BitmapHeader();
                    NativeStruct.fromNative( bmpHdr, bmpBuf, 0);
                    
                    // We learn the magic from the 1st header
                    if ( magic==0 )
                    {
                        magic = bmpHdr.magic;
                    }
                    else
                    {
                        if ( magic!=bmpHdr.magic )
                            throw new IOException("Wrong magic in bitmap: "+magic+" expected was "+bmpHdr.magic);
                    }
            
                    if ( expectedBitmapId!=bmpHdr.bitmapId__0x200 )
                        throw new IOException("Wrong ID in bitmap: "+bmpHdr.bitmapId__0x200+" expected was "+expectedBitmapId);
                    
                    expectedBitmapId++;
                    
                    //Debug.out.println("Bitmap "+bmpHdr.bitmapId__0x200+" total="+bmpHdr.total+" free="+bmpHdr.free);
    
                    totalItems -= bmpHdr.total;
                    if ( totalItems <= 0 )
                        break;
                }
                
                long dataAddr = getDataBlockAreaAddress( dataBlockCount );
                if ( dataAddr!=io.getPosition() )
                    Debug.err.println("POsition of block "+dataBlockCount+": "+dataAddr+" != "+io.getPosition());
                dataBlockCount ++;
                
                pos = io.getPosition() + dataBlockSize;
        
                Debug.out.println("XXX@"+Long.toHexString(io.getPosition())+" itemsLeft="+totalItems);
            }
        }    
    }

    /**
     * Reads bitmap info.
     * @throws Exception
     */
    void readBitmapInfo( IOAccess io, long pos ) throws Exception
    {
        Debug.out.println("Reading block allocation bitmap from "+io);
        Debug.out.println("Bitmap @"+Long.toHexString(pos));
        byte[] header = io.read( pos, 0x800 );
        
        BitmapMetaHeader bmp = new BitmapMetaHeader();
        NativeStruct.fromNative( bmp, header, 0);
        Debug.out.println("Blocks per bitmap = " + bmp.blocks );
        Debug.out.println("Managed items = "+bmp.itemCount);
        Debug.out.println("Count = "+bmp.count);

        long dataBlockSize = (long)(bmp.dataItems & 0xffff) * (long)bmp.dataSize;
        Debug.out.println("Data item size = "+Long.toHexString(dataBlockSize) );
        
        pos += 0x10000;
        
        int totalItems = bmp.itemCount;
        int expectedBitmapId = 0;
        
        int magic = 0;
        while ( totalItems > 0 )
        {
            io.setPosition( pos );
            Debug.out.println("Bitmap headers @"+Long.toHexString(io.getPosition()));

            byte[] bmpBuf = new byte[ 0x400 ];
            for ( int bitmapCount=0; bitmapCount<bmp.count; bitmapCount++)
            {
                io.read( bmpBuf, 0, bmpBuf.length );
                BitmapHeader bmpHdr = new BitmapHeader();
                NativeStruct.fromNative( bmpHdr, bmpBuf, 0);
                
                // We learn the magic from the 1st header
                if ( magic==0 )
                {
                    magic = bmpHdr.magic;
                }
                else
                {
                    if ( magic!=bmpHdr.magic )
                        throw new IOException("Wrong magic in bitmap: "+magic+" expected was "+bmpHdr.magic);
                }
        
                if ( expectedBitmapId!=bmpHdr.bitmapId__0x200 )
                    throw new IOException("Wrong ID in bitmap: "+bmpHdr.bitmapId__0x200+" expected was "+expectedBitmapId);
                
                expectedBitmapId++;
                
                //Debug.out.println("Bitmap "+bmpHdr.bitmapId__0x200+" total="+bmpHdr.total+" free="+bmpHdr.free);

                totalItems -= bmpHdr.total;
                if ( totalItems <= 0 )
                    break;
            }
            
            pos = io.getPosition() + dataBlockSize;
    
            Debug.out.println("XXX@"+Long.toHexString(io.getPosition())+" itemsLeft="+totalItems);
        }
    }

    /**
     * Detects/reads VMFS info structs.
     * @throws Exception
     */
    void readVmfsInfo() throws Exception
    {
        vi = null;
        long pos = 0x100000;
        byte[] header = new byte[ 0x800 ];
        while ( pos<0x400000 )
        {
            rf.setPosition( pos );
            rf.read( header, 0, header.length );
            
            vi = new VolumeInfo();
            NativeStruct.fromNative( vi, header, 0 );
            if ( vi.magic==(int)0xc001d00d )
                break;
            
            pos += 0x10000;
        }
        if ( pos>=0x400000 )
            throw new Exception("No VMware File System detected");
        
        vmfsBase = pos;
        Debug.out.println("VMFS base @"+Long.toHexString(vmfsBase));
        
        sb = new VMFSSuperBlock();
        NativeStruct.fromNative( sb, header, 0x200 );
        
        Debug.out.println("VMFS Volume at "+Long.toHexString(pos) );
        Debug.out.println("Volume type = "+ vi.name__28_0x12 );
        Debug.out.println("Volume UUID = "+ vi.uuid__0x82);
        Debug.out.println("Size = "+ StringUtil.displaySizeInBytes(sb.size) );
        Debug.out.println("Blocks = "+sb.blocks);
        //Debug.out.println("UUIDString = "+sb.uuidString__35);
        Debug.out.println("UUID       = "+sb.uuid__0x54);
                
        pos = vmfsBase + 0x1200000;
        Debug.out.println("@"+Long.toHexString(pos));
        rf.setPosition( pos );
        rf.read( header, 0, header.length );
        
        fs = new FSInfo();
        NativeStruct.fromNative( fs, header, 0 );
        
        if ( fs.magic!=0x2fabf15e )
            throw new Exception("VMFS FSInfo block not found");
        
        Debug.out.println("VMFS version = 3."+fs.version);
        Debug.out.println("VMFS label = "+fs.label__128);
        Debug.out.println("VMFS creation date = "+new java.util.Date(1000L*fs.timestamp) );
        Debug.out.println("VMFS UUID = "+fs.uuid);
        Debug.out.println("VMFS block size = "+fs.blocksize__0xa1+" / "+Long.toHexString(fs.blocksize__0xa1));
        Debug.out.println("VMFS volume version = "+fs.volumeVersion); // 3, 4 or 5

        pos = vmfsBase + 0x1300000;
        
        // 512-byte records with magic 0xabcdef01
        // What are these used for???
        rf.setPosition( pos );
        Debug.out.println("@"+Long.toHexString(pos));
        rf.read( header, 0, header.length );

        Record r = new Record();
        NativeStruct.fromNative( r, header, 0 );
        if ( r.magic!=(int)0xabcdef01 )
            throw new Exception("VMFS records not found");
        Debug.out.println( "Record first serial = "+r.number);   
        
        blockSize = (int) fs.blocksize__0xa1;
    }
 
    List<FileRecord> frs;
    List<FileMetaInfo> fmis;
    
    /**
     * Searches the file record for the given file name.
     * @param fileName
     * @return Null if record not found
     */
    FileRecord getFileRecord( List<FileRecord> frs, String fileName )
    {
        for ( FileRecord fr : frs )
        {
            if ( fr.name__128.equals(fileName) )
            {
                return fr;
            }
        }                
        return null;
    }
    
    FileRecord getFileRecord( String path ) throws Exception
    {
        List<FileRecord> rs = frs;
        StringTokenizer st = new StringTokenizer( path, "/" );
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            
            FileRecord fr = getFileRecord( rs, token );
            if ( fr==null )
                return null;

            if ( fr.isFolder() )
            {
                rs = getFileRecordsForDirectory(fr);
            }
            else
            {
                if ( st.hasMoreTokens() )
                    throw new IOException("Illegal path: "+path);
                
                return fr;
            }
        }
        throw new IOException("File not found: "+path);
    }

    Map<FileRecord, List<FileRecord> > dirCache = new HashMap<FileRecord, List<FileRecord> >();
    
    List<FileRecord> getFileRecordsForDirectory(FileRecord fr)
            throws IOException, Exception
    {
        List<FileRecord> rs = dirCache!=null ? dirCache.get(fr) : null;
        if ( rs!=null )
            return rs;
        
        IOAccess io = new FileIOAccess( getMetaInfo(fr) );
        if ( io.getSize()==0 )
            // empty directory
            rs = new ArrayList();
        else
            rs = readFileRecords(io, 0 );
        io.close();
     
        if ( dirCache!=null )
            dirCache.put( fr, rs );
        
        return rs;
    }
    
    /**
     * Lists the given directory.
     * @param path Full path to the directory.
     * @return The list of file records in the given directory
     * @throws Exception
     */
    List<FileMetaInfo> dir( String path ) throws Exception
    {
        List<FileMetaInfo> fmis = new ArrayList<FileMetaInfo>();
        List<FileRecord> frs = _dir( path );
        if ( frs!=null )
        for ( FileRecord fr : frs )
        {
            if ( ".".equals(fr.name__128) || "..".equals(fr.name__128) )
                continue;

            FileMetaInfo fmi = getMetaInfo(fr);
            if ( fmi!=null )
            {
                fmi.fullPath = path;
                fmis.add( fmi );
            }
        }
        return fmis;            
    }
    
    List<FileRecord> _dir( String path ) throws Exception
    {
        List<FileRecord> rs = frs;
        StringTokenizer st = new StringTokenizer( path, "/" );
        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            
            FileRecord fr = getFileRecord( rs, token );
            if ( fr==null )
                return null;

            if ( fr.isFolder() )
            {
                rs = getFileRecordsForDirectory(fr);
            }
            else
            {
                throw new IOException("Illegal path: "+path);
            }
        }
        return rs;
    }
    
    /**
     * Returns the meta info for the given file name.
     * @param fileName
     * @return Null if not found
     */
    FileMetaInfo getMetaInfoForFile( String fileName )
    {
        FileRecord fr;
        try
        {
            fr = getFileRecord( fileName );
            return getMetaInfo( fr );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    FileMetaInfo getMetaInfo( FileRecord fr )
    {
        if ( fr!=null )
        {
            for ( FileMetaInfo fmi : fmis )
            if ( fmi.fmr.id==fr.blockId )
            {
                fmi.fr = fr;
                return fmi;
            }
            
            // Read file record
            BlockID bid = new BlockID( fr.blockId );
            try
            {                                
                long addr = bid.number*0x800 + fdcBmp.getDataBlockAddress( bid.subgroup * fdcBmp.bmp.blocks );
                FileMetaInfo fmi = readFileMetaInfo( fdc, addr );
                
                //IOAccess fdc = openFile(".fdc.sf");
                //int addr = bid.number * 0x800 + (blockSize==0x100000 ? 0x1a54000 : 0xef8000);
                //FileMetaInfo fmi = readFileMetaInfo( fdc, addr );
                //fdc.close();

                if ( fmi.fmr.id==fr.blockId )
                {
                    fmi.fr = fr;
                    fmis.add( fmi );
                    return fmi;
                }
                else
                {
                    System.err.println("Illegal file record -- fmr.id="+fmi.fmr.id+" blockId="+fr.blockId);
                }
            }
            catch (Exception ex)
            {
                System.err.println("Cannot read file record: "+bid+" error was: "+ex);
            }
        }        
        return null;
    }
    
    /**
     * Opens the VMFS
     * @throws Exception
     */
    void openVmfs() throws Exception
    {
        // Read the basic VMFS meta data
        readVmfsInfo();

        // search the FDC bitmap base
        long ofs = 0;
        for (;;)
        {
        try
        {
            // Calculate the base address of the file descriptor area
            BitmappedBlockAllocation tempFdcBmp = new BitmappedBlockAllocation( rf, vmfsBase + 0x1400000 + ofs );
            long fdcBaseZero = tempFdcBmp.getDataBlockAddress( 0 );
            Debug.out.println("FDC base zero = " + Long.toHexString( fdcBaseZero ) );
            
            // Read the superblock file records and meta info
            frs = readFileRecords( rf, vmfsBase + 0x1400000 + ofs + blockSize );
            fmis = readFileMetaInfos( rf, fdcBaseZero );
            if ( !frs.isEmpty() && !fmis.isEmpty() ) break;
        }
        catch (Exception ex)
        {}
        ofs += 0x100000;
        if ( ofs>=0x1000000 )
            throw new IOException("VMFS FDC base not found");
        }
        // Open the "super meta" files
        readMetaFiles();
    }

    /**
     * IO access of a file hosted on VMFS
     * @author Uli
     */
    class FileIOAccess extends IOAccess
    {
        FileMetaInfo fmi;
        long pos;
        
        FileIOAccess( FileMetaInfo fmi ) throws IOException
        {
            this.fmi = fmi;
            fmi.resolvePointerBlocks();
        }
        
        public String toString()
        {
            return "FileIOAccess "+fmi+" Position="+getPosition();            
        }
        
        @Override
        public void close()
        {
            // TODO Auto-generated method stub            
        }

        @Override
        public long getPosition()
        {
            return pos;
        }

        @Override
        public long getSize()
        {
            return fmi.fmr.size;
        }

        @Override
        public int read(byte[] buffer, int offset, int size)
                throws IOException
        {
            int done = 0;
            
            while ( size>0 )
            {
                int bn = (int) (pos / blockSize);
                
                if ( fmi.blockTab==null )
                    throw new IOException("This file has no allocation table (RDM)");
                if ( fmi.blockTab.length<=bn )
                    throw new IOException("Read beyond end of file: pos="+pos+" file="+fmi);
                
                BlockID bid = new BlockID( fmi.blockTab[ bn ] );                
                int posInBlock = (int) (pos % blockSize);
                int maxAmountInBlock = (int) ( blockSize - posInBlock  );
                
                IOAccess io = rf;
                long base = vmfsBase + 0x1000000L;
                long blockAddr = (long)bid.number * blockSize;

                //Debug.out.println("Reading from block "+bid.number);
                long posInDevice = base + blockAddr + posInBlock;

                switch ( bid.type )
                {
                    case 1:
                        break;
                    case 2:
                    {
                        io = sbc;
                        posInBlock = (int) (pos % sbcBmp.getDataBlockSize() );
                        maxAmountInBlock = (int)sbcBmp.getDataBlockSize() - posInBlock;
                        posInDevice = sbcBmp.getDataBlockAddress( bid.number*16 + bid.subgroup ) + posInBlock;
                        break;
                    }
                    case 3:
                        throw new IOException("Internal error: PointerBlock should already be resolved");
                    default:
                        throw new IOException("Internal error: block type "+bid.type+" unexpected");
                }
                
                int thisTime = Math.min(size, maxAmountInBlock);

                int res = io.read( posInDevice, buffer, offset, thisTime );
                
                done += res;                
                pos += thisTime;
                offset += thisTime;
                size -= thisTime;
                
                if ( res<thisTime )
                    break;
            }
            
            return done;
        }

        @Override
        public void setPosition(long pos)
        {
            this.pos = pos;
        }

        @Override
        public void setSize(long newSize)
        {
            throw new IllegalArgumentException("setSize not supported");
        }

        @Override
        public void write(byte[] buffer, int offset, int size)
                throws IOException
        {
            throw new IOException("Write not supported");
        }   
    }

    /**
     * Opens the file with the given name.
     * @param name Full path to the file.
     * @return IOAccess handle of the file
     * @throws IOException
     */
    IOAccess openFile( String name ) throws IOException
    {
        FileMetaInfo fmi = getMetaInfoForFile( name );
        if ( fmi==null )
            throw new FileNotFoundException( name );
        
        return new FileIOAccess( fmi );
    }
    
    IOAccess fbb, fdc, pbc, sbc, vh;
    BitmappedBlockAllocation fbbBmp, fdcBmp, pbcBmp, sbcBmp;
    
    /**
     * Reads the VMFS meta files.
     * @throws Exception
     */
    void readMetaFiles() throws Exception
    {
        // Open the VMFS meta data files
        fbb = openFile(".fbb.sf");
        fdc = openFile(".fdc.sf");
        pbc = openFile(".pbc.sf");
        sbc = openFile(".sbc.sf");
        vh = openFile(".vh.sf");

        // Open the bitmap allocation tables
        fbbBmp = new BitmappedBlockAllocation( fbb );
        fdcBmp = new BitmappedBlockAllocation( fdc );
        pbcBmp = new BitmappedBlockAllocation( pbc );
        sbcBmp = new BitmappedBlockAllocation( sbc );
        //sbcBmp.scanAllBitmaps();
    }

    /**
     * Reads the file record at the specified location.
     * @param pos
     * @return
     * @throws Exception
     */
    FileRecord readFileRecord( IOAccess io, long pos ) throws Exception
    {
        Debug.out.println("@"+Long.toHexString(pos));
        
        NativeStruct frn = new NativeStruct( FileRecord.class );
        //Debug.out.println( frn );
        int recordSize = frn.getSize();
        
        byte[] frBuffer = io.read( pos, recordSize );
        
        FileRecord fr = new FileRecord();
        
        if ( frBuffer==null || frBuffer.length < frn.size )
            return fr;
        
        frn.decode( fr, frBuffer, 0 );

        if ( hyperverbose )
        {
            if ( fr.isFolder() )
                Debug.out.println("Folder: "+fr.name__128+" Block="+new BlockID(fr.blockId)+ " ID="+Long.toHexString(fr.recordId));
            else
                Debug.out.println("File: "+fr.name__128+" Block="+new BlockID(fr.blockId)+" ID="+Long.toHexString(fr.recordId));
        }
        
        return fr;
    }

    /**
     * Reads file records from the specific location, stops when type==0 is detected.
     * @param pos
     * @return
     * @throws Exception
     */
    List<FileRecord> readFileRecords( IOAccess io, long pos ) throws Exception
    {
        List<FileRecord> res = new ArrayList<FileRecord>();
        
        FileRecord fr = null;
        int sz = new NativeStruct(FileRecord.class).getSize();
        do
        {
            fr = readFileRecord( io, pos );
            pos += sz;
            
            if ( fr.type!=0 )
                res.add( fr );
        }
        while ( fr.type!=0 ); 
        
        return res;
    }

    /**
     * Class that holds the file meta structs. 
     * @author Uli
     */
    class FileMetaInfo
    {
        FileRecord fr;
        FileMetaHeader fmh;
        FileMetaRecord fmr;
        RDMMetaRecord rdm;
        int[] blockTab;
        boolean resolvedPointers;
        String fullPath="?";
        
        /**
         * Checks if this file meta data has pointer blocks,
         * and resolves them.
         */
        synchronized void resolvePointerBlocks() throws IOException
        {
            if ( resolvedPointers )
                return;

            resolvedPointers = true;

            if ( blockTab==null )
                // RDM files have no block allocations
                return;

            List<Integer> res = new ArrayList<Integer>();
            
            for ( int b : blockTab )
            {
                BlockID bid = new BlockID( b );
                if ( bid.type==3 )
                {
                    Debug.out.println("Resolving "+this+" => "+bid);
                    
                    // Pointer block
                    long addr = pbcBmp.getDataBlockAddress( bid.number*16 + bid.subgroup );
                    byte[] p = pbc.read( addr, (int)pbcBmp.getDataBlockSize() );
                    for ( int i=0; i<p.length/4; i++ )
                    {
                        int block = (p[4*i] & 0xff)
                            + ((p[4*i+1] & 0xff) << 8)
                            + ((p[4*i+2] & 0xff) << 16)
                            + ((p[4*i+3] & 0xff) << 24);
                        res.add( block );                        
                    }
                }
                else
                    res.add( b );
            }
            
            if ( res.size()!=blockTab.length )
            {
                blockTab = new int[ res.size() ];
                for ( int i=0; i<blockTab.length; i++ )
                    blockTab[i] = res.get(i);
            }
        }
        
        public String toString()
        {
            String p = fullPath;
            if ( p.endsWith("/") )
                p = p.substring( 0, p.length()-1 );
            return "Name="+p+"/"+fr.name__128+" Size=" + fmr.size + " Date="+new java.util.Date(fmr.timeStamp1__0x2c*1000L);
        }
    }

    /**
     * Reads file meta info at the specified location.
     * @param pos
     * @return
     * @throws Exception
     */
    FileMetaInfo readFileMetaInfo( IOAccess io, long pos ) throws Exception
    {
        FileMetaInfo res = new FileMetaInfo();
        
        Debug.out.println("@"+Long.toHexString(pos));
        if ( pos+0x800 > io.getSize() )
        {
            throw new IOException("File meta info after EOF @Pos="+pos+" : "+io);
        }
        
        byte[] header = io.read( pos, 0x800 );
        
        FileMetaHeader fmh = new FileMetaHeader();
        FileMetaRecord fmr = new FileMetaRecord();
        
        res.fmh = fmh;
        res.fmr = fmr;
        
        NativeStruct.fromNative( fmh, header, 0 );
        
        /* this validation might not always be true!
        
        long relPos = pos - 0x1000000L - vmfsBase; // - 0x3000000L - vmfsBase;
        if ( (long)fmh.position != relPos )
        {
            Debug.out.println("Address error: "+Long.toHexString(fmh.position)+" does not match "+Long.toHexString(relPos) );
            throw new IOException("No file record at location "+pos);
        }
        
        */
        
        NativeStruct.fromNative( fmr, header, 0x200 );
        //HexDump.dump( header );
        Debug.out.println( "Type="+fmr.type+" id="+ new BlockID(fmr.id) + " size=0x"+ Long.toHexString(fmr.size)+" / "+fmr.size+" date="+new java.util.Date(1000L*fmr.timeStamp1__0x2c) );

        if ( fmr.type==6 )
        {
            Debug.out.println( "RDM mapped file" );
            
            RDMMetaRecord rdmRec = new RDMMetaRecord();
            res.rdm = rdmRec;
            
            NativeStruct.fromNative( rdmRec, header, 0x400 );
            Debug.out.println("RDM type = "+rdmRec.lunType__28);
            Debug.out.println("RDM UUID = "+rdmRec.lunUuid__17);
            Debug.out.println("RDM blocks = "+rdmRec.blocks);            
        }
        else
        {
            int blocks = (int) ( (fmr.size + blockSize - 1) / blockSize );
            Debug.out.println("Blocks = " + blocks);
            if ( blocks>256 )
            {
                // Each pointer represents 1024 blocks
                // TODO: do we have a block size dependency?
                blocks = (blocks+1023)/1024;
                
                if ( hyperverbose )
                    Debug.out.println("Blocks to pointers = "+blocks);
            }
            if ( blocks<256 )
            {
                int[] blockTab = new int[blocks];
                res.blockTab = blockTab;
                for (int i = 0; i < blocks; i++)
                {
                    blockTab[i] = (header[4*i+0x400] & 0xff)
                            + ((header[4*i+0x401] & 0xff) << 8)
                            + ((header[4*i+0x402] & 0xff) << 16)
                            + ((header[4*i+0x403] & 0xff) << 24);

                    BlockID b = new BlockID( blockTab[i] );
                    
                    if ( hyperverbose )
                        Debug.out.println("Block" + i + " = " + b );
                }
            }
        }
        pos += 0x800;

        return res;
    }

    /**
     * Reads file meta infos from the given location, stops at id==0.
     * @param pos
     * @return
     * @throws Exception
     */
    List<FileMetaInfo> readFileMetaInfos( IOAccess io, long pos ) throws Exception
    {
        List<FileMetaInfo> res = new ArrayList<FileMetaInfo>();
        FileMetaInfo fmi;
        do
        {
            fmi = readFileMetaInfo( io, pos );
            if ( fmi.fmr.id!=0 )
                res.add( fmi );
            pos += 0x800;
        } while ( fmi.fmr.id!=0 );
        
        return res;
    }

    /**
     * Closes the volume.
     * @throws IOException
     */
    void closeVolume() throws IOException
    {
        if ( rf!=null ) rf.close();
    }
    
    /**
     * Opens the specified volume.
     * @param file
     * @throws IOException
     * @throws URISyntaxException 
     */
    void openVolume( String file ) throws IOException, URISyntaxException
    {
        closeVolume();
        
        // Open the volume for VMFS analysis
        if ( file.startsWith("ssh://"))
            rf = new RemoteSSHIOAccess( file, false );
        else
            rf = new RandomIOAccess( file, "r" );
    }
    
    /**
     * Analyzes the given VMFS volume and prints to dbgout.
     * Internal debug function.
     * @param args
     */    
    public static void main(String[] args) throws Throwable
    {
        System.out.println("VMFSInfo (C) by fluid Operations ("+Version.getBuildNumber()+" / " +Version.getBuildDate() + ")");
        System.out.println("http://www.fluidops.com");
        System.out.println();
        
        String f = args!=null && args.length==1 ? args[0] : null;
        
        VMFSDriver vmfsInfo = new VMFSDriver();
        vmfsInfo.openVolume( f );
        vmfsInfo.openVmfs();
        vmfsInfo.closeVolume();
    }
}
