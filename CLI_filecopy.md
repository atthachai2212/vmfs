The filecopy command opens a VMFS volume, then copies the specified file (or parts of it) from the VMFS volume to the local disk.

Here is a sample session that copies a disk file from /dev/sda5:
```
>java -jar fvmfs.jar /dev/sda5 filecopy /ExchangeServer/ExchangeServer-flat.vmdk
```
If the copy process runs for a longer period of time, a progress indicator will be shown.

To copy the disk file into the local file called "exch.vmdk":
```
>java -jar fvmfs.jar /dev/sda5 filecopy /ExchangeServer/ExchangeServer-flat.vmdk exch.vmdk
```

And to just copy one megabyte, starting at location 100000:
```
>java -jar fvmfs.jar /dev/sda5 filecopy /ExchangeServer/ExchangeServer-flat.vmdk exch.vmdk 100000 1048576
```