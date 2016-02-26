

# Prerequisites #

A Java Virtual Machine of at least version 5 must be installed (version 6 is preferred, as most tests happen on it).
Please verify that it is correctly installed by executing "java -version" on the command line.

Download the latest fvmfs.jar (or [build your own](HowToBuild.md)) distribution, and unzip it into your location of choice.
<a href='Hidden comment: 
If you want to use the remote host features that require SSH/SFTP, you must also download the trilead-ssh library [http://code.google.com/p/vmfs/source/browse/#svn/trunk/lib from the project"s lib folder] and put it into the same folder as fvmfs.jar. Otherwise, you will see a NoClassDefFoundError or a similar exception.
'></a>

# How to Run #

To start the VMFS Driver CLI Tools, type
```
java -jar fvmfs.jar
```

You should see the following CLI help:
```
VMFSTools (C) by fluid Operations (v0.9.8.13 r66 / 2009-03-05_22-18-06)
http://www.fluidops.com

Arguments:
  VMFSVolume info
  VMFSVolume dir path
  VMFSVolume dirall path
  VMFSVolume fileinfo path
  VMFSVolume filecopy path [newname position size]
  VMFSVolume filedump path position size
  VMFSVolume webdav [host port]

VMFSVolume can be any mounted VMFS volume, or a volume reachable by SFTP.

Examples:
  /path/to/vmfs info
  \\sambaserver\luns\bigvmfslun dir /Linux_VMs
  ssh://root:passwd@linuxhost/mnt/vmfslun fileinfo /disks/SwapDisk-flat.vmdk
```

# Command Line Arguments #

The first argument (VMFSVolume) always specifies the location of the VMFS volume. The second argument is the command. Arguments in brackets are optional.

If the volume is mounted on the local host, this is a path to the device, i.e. /dev/sda5 under Linux or \\.\PhysicalDrive5 under Windows.

The VMFS tools also support remotely mounted volumes on hosts with SSH access. It uses SFTP, or an SSH compatible mode if SFTP is not supported, to access the volumes on these remote hosts. The volume is then specified using an SSH URI like
```
ssh://user:passwd@linuxhost/mnt/vmfslun
```
The URI specifies user and password, the host to connect to using SSH/SFTP, and the path of the VMFS volume as mounted on the remote host.

## info ##

The [info](CLI_info.md) command shows general information about the VMFS volume, like the FS label, UUID, creation date, and internal meta data information (FD/PB/SB).

## dir path or dirall path ##

The [dir](CLI_dir.md) and [dirall](CLI_dirall.md) commands list the contents of the volume (dirall lists recursively).
They require a second argument, the path to list from. To list from the root folder, just specify "/".

## fileinfo file ##

The [fileinfo](CLI_fileinfo.md) command shows detailed information about a file (or virtual disk) hosted on the VMFS volume.

This includes the file size, and all allocated blocks on the volume (list of allocation extents).

## filecopy path (newname position size) ##

The [filecopy](CLI_filecopy.md) command copies the file's content, starting at position, into a file called newname.
Using this command, files can be copied from the VMFS volume to the local file system.

## filedump path position size ##

The [filedump](CLI_filedump.md) command prints the file's content as a hex dump to stdout.
This is useful for test and debugging purposes.

## webdav (host port) ##

The [webdav](CLI_webdav.md) command starts a WebDAV/HTTP server (defaults to port 50080 on localhost).

The VMFS volume can then be browsed using any HTTP browser, or it can be mounted as a file system on many operating systems.