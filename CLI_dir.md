The dir command opens a VMFS volume and lists the files and subfolders of the given folder.

Here is a sample session that gathers the info from a remote host:
```
>java -jar fvmfs.jar ssh://root:password@vmfs-01.test.com/vmfs/devices/disks/vmhba32:5:0:0 dir /
```

The output is:
```
Name=/.fbb.sf Size=98304 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.fdc.sf Size=22708224 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.pbc.sf Size=6520832 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.sbc.sf Size=260374528 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.vh.sf Size=4194304 Date=Mon Feb 16 10:26:02 CET 2009
Name=/Lefthand-VM Size=1260 Date=Mon Feb 16 10:27:58 CET 2009
```