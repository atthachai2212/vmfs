The info command opens a VMFS volume and displays the VMFS meta information.

Here is a sample session that gathers the info from a remote host:
```
>java -jar fvmfs.jar ssh://root:password@vmfs-01.test.com/vmfs/devices/disks/vmhba32:5:0:0 info
```

This shows the following information:
```
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

The volume has a size of 551 Gigabytes, the block size is 2 MB. It was created on Dec 05, 2008.

Another, somewhat smaller volume of 1.75 GB with a 1 MB block size, shows this:
```
VMFS label         = lefthand-vmfs
VMFS creation date = Mon Feb 16 10:26:01 CET 2009
VMFS capacity      = 1.75 GB
VMFS UUID          = 49993129-456163d8-fc31-0018fe8a17d4
VMFS block size    = 1.00 MB
VMFS version       = 3.31
VMFS # of FD/PB/SB = 11024 / 1544 / 3968
VMFS volume type   = LEFTHANDiSCSIDisk       8000
VMFS volume UUID   = 49993129-3dff5c2e-95f8-0018fe8a17d4
VMFS volume size   = 1.75 GB
VMFS volume ver    = 4
```