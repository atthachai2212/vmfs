The dir command opens a VMFS volume and recursively lists all the files and subfolders of the given folder.

Here is a sample session that gathers the info from a remote host:
```
>java -jar fvmfs.jar ssh://root:password@vmfs-01.test.com/vmfs/devices/disks/vmhba32:5:0:0 dirall /
```

The output is:
```
Name=/.fbb.sf Size=98304 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.fdc.sf Size=22708224 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.pbc.sf Size=6520832 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.sbc.sf Size=260374528 Date=Mon Feb 16 10:26:02 CET 2009
Name=/.vh.sf Size=4194304 Date=Mon Feb 16 10:26:02 CET 2009
Name=/Lefthand-VM Size=1260 Date=Mon Feb 16 10:27:58 CET 2009
Name=/Lefthand-VM/Lefthand-VM.vmx Size=1426 Date=Mon Feb 16 10:27:26 CET 2009
Name=/Lefthand-VM/Lefthand-VM.vmxf Size=266 Date=Mon Feb 16 10:27:20 CET 2009
Name=/Lefthand-VM/Lefthand-VM.vmsd Size=0 Date=Mon Feb 16 10:27:19 CET 2009
Name=/Lefthand-VM/Lefthand-VM-flat.vmdk Size=8388608 Date=Mon Feb 16 10:27:20 CET 2009
Name=/Lefthand-VM/Lefthand-VM.vmdk Size=371 Date=Mon Feb 16 10:27:20 CET 2009
Name=/Lefthand-VM/vmware.log Size=21442 Date=Mon Feb 16 10:27:58 CET 2009
Name=/Lefthand-VM/Lefthand-VM.nvram Size=8684 Date=Mon Feb 16 10:27:57 CET 2009
Name=/Lefthand-VM/Lefthand-VM.nvram Size=8684 Date=Mon Feb 16 10:27:57 CET 2009
```