# What is the VMFS Driver? #

It is the industry's first open solution to access data on VMFS.
It's an open source driver to access VMFS formatted volumes from any Linux, Windows or OS-X operating system (actually from any OS that is capable of running Java and mounting a volume). It currently supports VMFS version 3 (VMFS3), version 1 and version 2 are not supported.

# Why is it useful? #

Accessing offline (e.g. backed up, unused, legacy) VMFS volumes is not a trivial task. Especially if there are only non-VMware hosts around, there is no open way of accessing the files. But even if VMware hosts are available, one needs to take care of "volume resignaturing" and other challenges.

fluidOps strives for openness and therefore provides this technology as a free solution to make backups and other offline VMFS volumes readable, from any environment.

# What is the performance? #

It depends. If all goes well, performance is pretty decent, we regularly achieve throughputs of up to 4 GBytes per minute. Have look at this [CLI example session](CLI_recoverysession.md) to get an idea.

# Do you have the VMFS specification? #

No. The driver is the result of extensively analyzing the on-disk format, and comparing changes after certain file system operations. [This is an ongoing process.](HowCanIHelp.md)

# Is it safe to access an already mounted VMFS volume? #

Absolutely. Since we are only reading from the volume, there is no risk of interfering or data loss. Accessing a volume that is mounted on a VMware host is an easy way of [verifying the driver code.](HowCanIHelp.md)

<a href='Hidden comment: 
= Do you plan to add write support? =

Writing into existing files (i.e. virtual disks) is easy and will be included soon. Creating files and folders, or changing file allocation, is hard, and requires [HowCanIHelp a lot more work to be done.]
'></a>

# How can I help? #

You can contribute in many ways, please [have a look here.](HowCanIHelp.md)

# This is no "driver". It is just a Java app! #

Right and wrong:

  * Right, it is no driver running in **kernel** space. It is a user space file system library.

  * Wrong, because you can easily hook for example a WebDAV frontend into the VMFS driver lib, making it a "full" file system driver. Then you can regularly mount the VMFS as a file system under the major operating systems and use all regular file operations, from all applications. (Expect a checkin soon that enables this feature...)