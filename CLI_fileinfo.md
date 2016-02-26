The fileinfo command opens a VMFS volume, then opens the specified file on the volume and shows the allocation information (which extents/blocks are used on the disk by the file).

Here is a sample session that gathers the info from a remote host:
```
>java -jar fvmfs.jar /dev/sda5 fileinfo /ExchangeServer/ExchangeServer-flat.vmdk
```

The output is:
```
Size = 15.00 GB
Extent[0] = @5711510000(373952675840) size=56c00000(1455423488)
Extent[1] = @5777b10000(375670243328) size=9600000(157286400)
Extent[2] = @5768110000(375408099328) size=fa00000(262144000)
Extent[3] = @5781110000(375827529728) size=350400000(14231273472)
```

This means that this virtual disk file, which is 15 Gigabytes in size, is made up of four extents on the disk. The extent location is given in bytes from the start of the volume, and the volume size is also given in bytes. These are hexadecimal numbers, decimal numbers are in brackets.