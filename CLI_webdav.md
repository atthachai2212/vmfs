

The webdav command opens a VMFS volume, and serves the contents of the file system as WebDAV/HTTP. The volume can be directly mounted in most operating systems.

The WebDAV server binds on localhost (127.0.0.1) and port 50080 by default. This can be changed by optionally specifying the host IP and port to bind to.

## Starting the WebDAV server ##

The following command starts a WebDAV server for the VMFS volume on /dev/sda5:
```
>java -jar fvmfs.jar /dev/sda5 webdav

VMFSTools (C) by fluid Operations (v0.9.8.13 r66 / 2009-03-05_22-18-06)
http://www.fluidops.com

*** Serving WebDAV/HTTP at http://localhost:50080/vmfs
```

By pointing your Web browser to the URL, you can now browse the VMFS file system.

## Mounting the volume on Windows ##

On Windows, this volume can now be mounted as a drive letter as follows:

```
> net use * http://localhost:50080/vmfs

Drive Z: is now connected to http://localhost:50080/vmfs.

The command completed successfully.
```

The VMFS volume is now visible under drive Z. Executing a dir Z:\ shows the root directory. In Windows Vista, this works out of the box.

Alternatively, you can also use the "Map Network Drive" wizard in Explorer.

Note: If you want to use Vista's "Add a Network Location" feature, the drive location needs to be specified as "\\host@port\vmfs", using the HTTP URL is not accepted.

## Mounting on Linux ##

On Linux, the volume can be mounted using the davfs2 driver as follows:

```
> mount -t davfs -o ro http://localhost:50080/vmfs/ /mount/vmfs
Please enter the username to authenticate with server
http://localhost:50080/vmfs/ or hit enter for none.
Username:
mount.davfs: warning: the server does not support locks
```

Just press enter when asked for the username (no authentication required).
The volume is now accessible under the given mount point (/mount/vmfs in the above example).


## Mounting Success List ##

Successfully mounting volumes seems to depend on whether WebDAV is serving on the default port (80), and whether there is a path prefix (like "/vmfs") or not.

Here's the current "success list" for mounting with different operating systems. More feedback welcome:
  * Windows Vista works
  * Mac OS X (10.5.6) works
  * Linux works (with davfs2)
> > This was tested on Ubuntu 8.
  * 3rd party NetDrive client on Windows mounts fine
  * Windows Server 2008 works fine without path prefix
  * Windows Server 2003 server works fine with default port
> > This requires the WebFolders upgrade, see Microsoft KB907306.
  * Windows XP mounts **with** port and **with** path prefix
> > Use the "Add Network Place" wizard (this does not show up as drive letter though).