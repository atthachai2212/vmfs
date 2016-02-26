# Introduction #

This section describes a sample session that shows how to recover a virtual machine from a snapshotted VMFS LUN.

# Scenario #

The productive virtual machines are running on vSphere connected to a centralized storage system. The VMs are backed up by periodically taking snapshots of the LUN where the VMFS of the virtual machines are stored.

# Recovery #

A productive virtual machine was broken and must be recovered from a past snapshot.
To achieve this, the snapshot must be made available to the recovery system.

In our example, the VMFS consists of two disk extents, and are mapped to the recovery system via iSCSI. The two extents are mapped to the local disks `\\.\PhysicalDrive2` and `\\.\PhysicalDrive3`

The storage hardware in this example is a NetApp storage filer, and the recovery station runs Windows 7 with the iSCSI software initiator. Network connectivity is 1 GBit ethernet.

## List the VMFS ##

First, we list the contents of the virtual machine in question.
```
java -jar fvmfs.jar \\.\PhysicalDrive2,\\.\PhysicalDrive3 dir "/Uli01 Virtual Machine"

01/20/10 16:38:06           (dir) /Uli01 Virtual Machine
01/20/10 16:38:04   1.079.210.182 /Uli01 Virtual Machine/Uli01 Virtual Machine-ec5263bc.vmss
01/20/10 16:37:40   8.589.934.592 /Uli01 Virtual Machine/Uli01 Virtual Machine-flat.vmdk
01/20/10 16:38:05           8.684 /Uli01 Virtual Machine/Uli01 Virtual Machine.nvram
01/20/10 16:32:54             486 /Uli01 Virtual Machine/Uli01 Virtual Machine.vmdk
01/20/10 16:07:30               0 /Uli01 Virtual Machine/Uli01 Virtual Machine.vmsd
01/20/10 16:38:06           3.026 /Uli01 Virtual Machine/Uli01 Virtual Machine.vmx
01/20/10 16:32:48             276 /Uli01 Virtual Machine/Uli01 Virtual Machine.vmxf
01/20/10 16:38:07          82.247 /Uli01 Virtual Machine/vmware.log
```

## Virtual Disk Recovery ##

Now, we recover the virtual machine disk file:

```
java -jar fvmfs.jar \\.\PhysicalDrive2,\\.\PhysicalDrive3 filecopy "/Uli01 Virtual Machine/Uli01 Virtual Machine-flat.vmdk"

VMFSTools (C) by fluid Operations (v8.8.8.8888 r?? / 8888-12-31_23-59-59)
http://www.fluidops.com

Size = 8.00 GB
Copying file -- bytes left=8437366784 throughput=60759 KB/s ETA=138s
Copying file -- bytes left=8344305664 throughput=48900 KB/s ETA=170s
Copying file -- bytes left=8230535168 throughput=47703 KB/s ETA=172s
Copying file -- bytes left=8129085440 throughput=45800 KB/s ETA=177s
Copying file -- bytes left=8033140736 throughput=44284 KB/s ETA=181s
Copying file -- bytes left=7929331712 throughput=43792 KB/s ETA=181s
Copying file -- bytes left=7818182656 throughput=43703 KB/s ETA=178s
Copying file -- bytes left=7713587200 throughput=43448 KB/s ETA=177s
Copying file -- bytes left=7537426432 throughput=46402 KB/s ETA=162s
Copying file -- bytes left=7196901376 throughput=54615 KB/s ETA=131s
Copying file -- bytes left=7124549632 throughput=52041 KB/s ETA=136s
Copying file -- bytes left=7026769920 throughput=50917 KB/s ETA=138s
Copying file -- bytes left=6939738112 throughput=48883 KB/s ETA=141s
Copying file -- bytes left=6916669440 throughput=45837 KB/s ETA=150s
Copying file -- bytes left=6855065600 throughput=44466 KB/s ETA=154s
Copying file -- bytes left=6774325248 throughput=43443 KB/s ETA=155s
Copying file -- bytes left=6658719744 throughput=43346 KB/s ETA=153s
Copying file -- bytes left=6537609216 throughput=43606 KB/s ETA=149s
Copying file -- bytes left=6415450112 throughput=43820 KB/s ETA=146s
Copying file -- bytes left=6306398208 throughput=43695 KB/s ETA=144s
Copying file -- bytes left=6185811968 throughput=43485 KB/s ETA=142s
Copying file -- bytes left=6088294400 throughput=43259 KB/s ETA=140s
Copying file -- bytes left=6037962752 throughput=42097 KB/s ETA=143s
Copying file -- bytes left=5947785216 throughput=41767 KB/s ETA=142s
Copying file -- bytes left=5827198976 throughput=41446 KB/s ETA=140s
Copying file -- bytes left=5741215744 throughput=40953 KB/s ETA=140s
Copying file -- bytes left=5507383296 throughput=42659 KB/s ETA=129s
Copying file -- bytes left=5370544128 throughput=43057 KB/s ETA=124s
Copying file -- bytes left=5194907648 throughput=43930 KB/s ETA=118s
Copying file -- bytes left=5005115392 throughput=44777 KB/s ETA=111s
Copying file -- bytes left=4780720128 throughput=46133 KB/s ETA=103s
Copying file -- bytes left=4554489856 throughput=47430 KB/s ETA=96s
Copying file -- bytes left=4329832448 throughput=48634 KB/s ETA=89s
Copying file -- bytes left=4107796480 throughput=49743 KB/s ETA=82s
Copying file -- bytes left=3872653312 throughput=50882 KB/s ETA=76s
Copying file -- bytes left=3560964096 throughput=52813 KB/s ETA=67s
Copying file -- bytes left=3278110720 throughput=54349 KB/s ETA=60s
Copying file -- bytes left=3042705408 throughput=55336 KB/s ETA=54s
Copying file -- bytes left=2648178688 throughput=57823 KB/s ETA=45s
Copying file -- bytes left=2421161984 throughput=58600 KB/s ETA=41s
Copying file -- bytes left=2128871424 throughput=59782 KB/s ETA=35s
Copying file -- bytes left=1795424256 throughput=61439 KB/s ETA=29s
Copying file -- bytes left=1444937728 throughput=63174 KB/s ETA=22s
Copying file -- bytes left=1029701632 throughput=65393 KB/s ETA=15s
Copying file -- bytes left=608436224 throughput=67569 KB/s ETA=9s
Copying file -- bytes left=193200128 throughput=69604 KB/s ETA=2s
Copied 8589934592 bytes in 121s throughput was 70495 KB/s
```

# Conclusions #

The disk file was recovered at a decent rate of 70 MBytes per second, or roughly 4 GBytes per minute!

The performance is obviously depending on the source storage system, on the type of connection between storage and recovery system, and on the destination storage system. And of course on the load of both the source and the destination system. Hence, your mileage may vary!

Side note: of course, we could have achieved the same result by running the webdav server (using the webdav CLI command), and recovering the file from the web UI. This is slightly slower, because the data flows through the WebDAV engine and the HTTP stack before being delivered to the recovery station.