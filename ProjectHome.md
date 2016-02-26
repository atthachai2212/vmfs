# Open Source VMFS Driver #

This driver enables read-only access to files and folders on partitions formatted with the [Virtual Machine File System (VMFS)](http://en.wikipedia.org/wiki/VMFS).
VMFS is a clustered file system that is used by the VMware ESX hosts to store virtual machines and virtual disk files.

The VMFS driver comes with [command line interface (CLI)](CLI.md) tools to mount and analyze VMFS volumes. The VMFS driver was tested on Linux and Windows based hosts, but should work on any platform that supports Java. The driver supports VMFS version 3 (VMFS3).

VMFS volumes can also be browsed or mounted using a [WebDAV client.](CLI_webdav.md)

**NEW FEATURES in Release [r95](http://code.google.com/p/vmfs/downloads/detail?name=fvmfs_r95_dist.zip&can=2&q=#makechanges)**:
  * Support for LVM using multiple extents (VMFS that were extended)
  * Support for sparse files (a.k.a. "thin provisioned" virtual disks)
  * Auto-detection of VMFS stored in partitions
  * Enhanced display of symbolic links and raw device mapped (RDM) virtual disks
  * New CLI commands **cat** and **showheartbeats**
  * Display of cluster heartbeat information
  * Many issues resolved

Many people asked questions about the driver's performance, and wanted to have a more useful sample scenario - we have heard you, please [take a look here](CLI_recoverysession.md).

Also have a look at the [Frequently Asked Questions](FAQ.md) for more details.

The VMFS driver is developed and maintained by [fluid Operations](http://www.fluidops.com) and is included in upcoming releases of the [eCloudManager](http://www.ecloudmanager.com) product, where it is used to allow enhanced features like offloaded backups of virtual machines hosted on VMware ESX hosts.

# How to Run #

<a href='Hidden comment: 
To start the VMFS Driver CLI Tools, type
```
java -jar fvmfs.jar
```

You should see the following CLI help:
```
VMFSTools (C) by fluid Operations (v0.9.8.18 r95 / 2010-01-25_15-57-35)
http://www.fluidops.com

SFTP failed, fallback to SSH
Arguments:
  VMFSVolume info
  VMFSVolume dir path
  VMFSVolume dirall path
  VMFSVolume cat path
  VMFSVolume fileinfo path
  VMFSVolume filecopy path [newname position size]
  VMFSVolume filedump path position size
  VMFSVolume showheartbeats
  VMFSVolume webdav [host port]

VMFSVolume can be any mounted VMFS volume, or a volume reachable by SSH/SFTP.
Multiple VMFS extents can be specified using a comma-separated list.
Examples:
  \\sambaserver\luns\bigdisk dir /Linux_VMs
  ssh://root:passwd@linuxhost/mnt/vmfslun fileinfo /disks/SwapDisk-flat.vmdk
  \\.\PhysicalDrive3,\\.\PhysicalDrive4 filecopy /Windows-Template/W2008.vmdk x:\recover\W2008.vmdk
```

== Command Line Arguments ==
'></a>
Please refer to the [CLI reference](CLI.md) for more details.

# How to Build #

Please refer to the [build instructions](HowToBuild.md) for more details.

# News and Updates #

Follow fluidOps on Twitter: http://twitter.com/fluidops

# Acknowledgements #

The VMFS WebDAV server is based on the [Jetty](http://www.mortbay.org) web server and the [Milton WebDAV API](http://milton.ettrema.com). To enable remote volume access over SSH/SFTP the [Trilead SSH library](http://www.trilead.com/Products/Trilead_SSH_for_Java/) is used.

# Copyright #

Copyright by [fluid Operations](http://www.fluidops.com)

# Licensing #

The source code is placed under the GPL. None of it is shareware. You don't have to pay anyone to use it but you should be sure to read the copyright section of the FAQ for more information on how the GNU General Public License may affect your use of these tools.

In particular, if you intend to use the code in a proprietary (non-GPL'd) application, you will need a proprietary-use license for the VMFS library. This is available for purchase; please [contact fluid Operations](http://www.fluidops.com/contact.html) for more information.

Note that when we say "free" we mean freedom, not price. The goal of such freedom is that the people who use a given piece of software should be able to change it to fit their needs, learn from it, share it with their friends, etc. This license allows you those freedoms, so it is free software.