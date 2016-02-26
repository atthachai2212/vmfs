# Introduction #

The driver is actively used and does, to our knowledge, not have any "hard issues".

But since this file system driver is the result of extensive on-disk format analysis, there is no guarantee that all the assumptions that were made are correct.

# Help by analyzing and adding more support #

If you are a file system expert, and you are eager to contribute to a driver for a FS that is the de facto standard in virtualization hosting, please go ahead - we have a couple of unsupported features regarding VMFS:

  * Extents are not supported
  * Creating and deleting files or folders is not supported
  * Changing file sizes (allocation) is not supported
  * The clustered sync/lock algorithm is mostly unknown

# Help by testing #

That said, the current driver might still have problems with certain VMFS parameters (i.e. certain sizes, certain Unicode characters in names, rare conditions, ...) which we can solve by testing on as many VMFS volumes as possible.

It's easiest to test the driver on a volume that is mounted by a VMware host ([this is safe](FAQ.md)), because then we can immediately compare our output with the actual VMFS info.

Testing the driver code can be made with two easy steps:
First, run the
```
java -jar fvmfs.jar /vmfs/volume info
```
command to verify that the volume is correctly opened. Also validate that the shown size, date, volume name and type etc. is correct.

Then, run the following command to verify that all directory structures can be read:
```
java -jar fvmfs.jar /vmfs/volume dirall /
```
This iterates over all directories and lists all files (like a "ls -lR").

# What if I find an error? #

If you find an issue while testing, please run the commands again enabling debug information. Debug logging is enabled by setting the following property
```
com.fluidops.util.logging.debug=true
```

Now please run all of the commands with the following option:
```
java -Dcom.fluidops.util.logging.debug=true -jar fvmfs.jar ...
```
If you are a file system expert, you can help by analyzing the issue yourself. Otherwise, please send us the output of the command, and we will try to figure out what's wrong, which is many times not possible without having access to the volume. There are remote ways of access, so maybe we can work something out.

Here is the sample output of the info command with debugging enabled:
```
VMFSTools (C) by fluid Operations (8888 / 8888-12-31_23-59-59)
http://www.fluidops.com

VMFS base @110000
VMFS Volume at 110000
Volume type = HP      MSA VOLUME      7.00
Volume UUID = 4938c991-bc235236-12c2-0018fe8a17d4
Size = 551.00 GB
Blocks = 2205
UUID       = 4938c98e-f28a871d-fe82-0018fe8a17d4
@1310000
VMFS version = 3.31
VMFS label = MSA20LUN7
VMFS creation date = Fri Dec 05 07:26:27 CET 2008
VMFS UUID = 4938c993-1b2e5c12-86ff-0018fe8a17d4
VMFS block size = 2097152 / 200000
VMFS volume version = 4
@1410000
Record first serial = 3145728
Reading block allocation bitmap from com.fluidops.tools.vmfs.RemoteSSHIOAccess@763f5d
Bitmap @1510000
Blocks per bitmap = 200
Managed items = 30720
Count = 32
Data block size = 2048
Data items = 200
Data size = 65536
Data area size = c80000
FDC base zero = 1528000
@1710000
@171008c
@1710118
@17101a4
@1710230
@17102bc
@1710348
@17103d4
@1710460
@17104ec
@1528000
Type=2 id=Block FileDescriptor(4) type=4 number=0 subgroup=0 size=0x4ec / 1260 date=Wed Dec 10 16:26:21 CET 2008
Blocks = 1
@1528800
Type=5 id=Block FileDescriptor(400004) type=4 number=1 subgroup=0 size=0x178000 / 1540096 date=Fri Dec 05 07:26:28 CET 2008
Blocks = 1
@1529000
Type=5 id=Block FileDescriptor(800004) type=4 number=2 subgroup=0 size=0x3c38000 / 63143936 date=Fri Dec 05 07:26:28 CET 2008
Blocks = 31
@1529800
Type=5 id=Block FileDescriptor(c00004) type=4 number=3 subgroup=0 size=0xf3d0000 / 255655936 date=Fri Dec 05 07:26:28 CET 2008
Blocks = 122
@152a000
Type=5 id=Block FileDescriptor(1000004) type=4 number=4 subgroup=0 size=0xf850000 / 260374528 date=Fri Dec 05 07:26:28 CET 2008
Blocks = 125
@152a800
Type=5 id=Block FileDescriptor(1400004) type=4 number=5 subgroup=0 size=0x400000 / 4194304 date=Fri Dec 05 07:26:28 CET 2008
Blocks = 2
@152b000
Type=0 id=Block NULL(0) type=0 number=0 subgroup=0 size=0x0 / 0 date=Thu Jan 01 01:00:00 CET 1970
Blocks = 0
Reading block allocation bitmap from FileIOAccess Name=?/.fbb.sf Size=1540096 Date=Fri Dec 05 07:26:28 CET 2008 Position=0
Bitmap @0
Blocks per bitmap = 200
Managed items = 282112
Count = 32
Data block size = 0
Data items = 0
Data size = 65536
Data area size = 0
Reading block allocation bitmap from FileIOAccess Name=?/.fdc.sf Size=63143936 Date=Fri Dec 05 07:26:28 CET 2008 Position=0
Bitmap @0
Blocks per bitmap = 200
Managed items = 30720
Count = 32
Data block size = 2048
Data items = 200
Data size = 65536
Data area size = c80000
Reading block allocation bitmap from FileIOAccess Name=?/.pbc.sf Size=255655936 Date=Fri Dec 05 07:26:28 CET 2008 Position=0
Bitmap @0
Blocks per bitmap = 16
Managed items = 61440
Count = 32
Data block size = 4096
Data items = 32
Data size = 65536
Data area size = 200000
Reading block allocation bitmap from FileIOAccess Name=?/.sbc.sf Size=260374528 Date=Fri Dec 05 07:26:28 CET 2008 Position=0
Bitmap @0
Blocks per bitmap = 16
Managed items = 3968
Count = 32
Data block size = 65536
Data items = 512
Data size = 65536
Data area size = 2000000
VMFS label         = MSA20LUN7
VMFS creation date = Fri Dec 05 07:26:27 CET 2008
VMFS capacity      = 551.00 GB
VMFS UUID          = 4938c993-1b2e5c12-86ff-0018fe8a17d4
VMFS block size    = 2.00 MB
VMFS version       = 3.31
VMFS # of FD/PB/SB = 30720 / 61440 / 3968
VMFS volume type   = HP      MSA VOLUME      7.00
VMFS volume UUID   = 4938c991-bc235236-12c2-0018fe8a17d4
VMFS volume size   = 551.00 GB
VMFS volume ver    = 4
```