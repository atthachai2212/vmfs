# Eclipse #

The Eclipse .classpath and .project files are included in the source code distribution (from Eclipse version 3.4).

Eclipse automatically recognizes this and properly sets up the project.

# Building with Ant #

The VMFS driver comes with an ant build file in {root}/build/build.xml

Executing ant will compile all sources, build the binary in {root}/deploy/fvmfs.jar and the documentation in {root}/javadoc.

Here is a sample output of the build process. Yes, the warnings might be fixed some time in the future :-)

```
Buildfile: build.xml

manualprops:
     [echo] Generating module fvmfs
     [echo] Generating in Schedule manual
     [echo] Generating Version 9.9.0.9999
     [echo] Generating Build 9999
     [echo] Generating Date 2009-02-25_02-09-34

clean:
   [delete] Deleting directory C:\uli\fworkspace\vmfs\bin
    [mkdir] Created dir: C:\uli\fworkspace\vmfs\bin
   [delete] Deleting directory C:\uli\fworkspace\vmfs\deploy
    [mkdir] Created dir: C:\uli\fworkspace\vmfs\deploy
   [delete] Deleting directory C:\uli\fworkspace\vmfs\javadoc
    [mkdir] Created dir: C:\uli\fworkspace\vmfs\javadoc
   [delete] Deleting directory C:\uli\fworkspace\vmfs\build\junit-tests
    [mkdir] Created dir: C:\uli\fworkspace\vmfs\build\junit-tests
   [delete] Deleting directory C:\uli\fworkspace\vmfs\testbin
    [mkdir] Created dir: C:\uli\fworkspace\vmfs\testbin

compile:
    [javac] Compiling 10 source files to C:\uli\fworkspace\vmfs\bin
    [javac] C:\uli\fworkspace\vmfs\src\com\fluidops\tools\vmfs\IOAccess.java:67: warning: [cast] redundant cast to int
    [javac]                     return (int) (b[0]&0xff);
    [javac]                            ^
    [javac] C:\uli\fworkspace\vmfs\src\com\fluidops\tools\vmfs\VMFSDriver.java:631: warning: [unchecked] unchecked conversion
    [javac] found   : java.util.ArrayList
    [javac] required: java.util.List<com.fluidops.tools.vmfs.VMFSDriver.FileRecord>
    [javac]             rs = new ArrayList();
    [javac]                  ^
    [javac] C:\uli\fworkspace\vmfs\src\com\fluidops\tools\vmfs\VMFSDriver.java:844: warning: [cast] redundant cast to int
    [javac]                 int maxAmountInBlock = (int) ( blockSize - posInBlock  );
    [javac]                                        ^
    [javac] C:\uli\fworkspace\vmfs\src\com\fluidops\tools\vmfs\VMFSDriver.java:515: warning: [cast] redundant cast to int
    [javac]             if ( vi.magic==(int)0xc001d00d )
    [javac]                            ^
    [javac] C:\uli\fworkspace\vmfs\src\com\fluidops\tools\vmfs\VMFSDriver.java:565: warning: [cast] redundant cast to int
    [javac]         if ( r.magic!=(int)0xabcdef01 )
    [javac]                       ^
    [javac] 5 warnings

jar:
      [jar] Building jar: C:\uli\fworkspace\vmfs\deploy\fvmfs.jar

javadoc:
  [javadoc] Generating Javadoc
  [javadoc] Javadoc execution
  [javadoc] Loading source files for package com.fluidops.base...
  [javadoc] Loading source files for package com.fluidops.tools.vmfs...
  [javadoc] Loading source files for package com.fluidops.util...
  [javadoc] Loading source files for package com.fluidops.util.logging...
  [javadoc] Constructing Javadoc information...
  [javadoc] Standard Doclet version 1.6.0_02
  [javadoc] Building tree for all the packages and classes...
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\base\Version.java:63: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\base\Version.java:72: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\base\Version.java:36: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\base\Version.java:45: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\base\Version.java:54: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\base\Version.java:27: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\tools\vmfs\NativeStruct.java:154: warning - @return tag has no arguments.
  [javadoc] C:\uli\fworkspace\vmfs\src\com\fluidops\util\StringUtil.java:32: warning - @return tag has no arguments.
  [javadoc] Generating C:\uli\fworkspace\vmfs\javadoc\src-html\com\fluidops\util\logging\Debug.NullStream.html...
  [javadoc] Copying file C:\uli\fworkspace\vmfs\build\doc\stylesheet_fbase.css to file C:\uli\fworkspace\vmfs\javadoc\stylesheet_fbase.css...
  [javadoc] Building index for all the packages and classes...
  [javadoc] Building index for all classes...
  [javadoc] Generating C:\uli\fworkspace\vmfs\javadoc\help-doc.html...
  [javadoc] 8 warnings

manual:

BUILD SUCCESSFUL
Total time: 3 seconds
```