/*
 * Version.java // com.fluidops.base
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.base;

import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.fluidops.util.FileUtil;

/**
 * Handles Versioning of the product.
 * 
 * @author vasu
 */
public class Version
{
	private static String Version = "8.8.8.8888";
	private static String MajorVersion;
    private static String MinorVersion;
    private static String PatchVersion;    
	private static String BuildDate = "8888-12-31_23-59-59";
	private static String BuildNumber = "8888";
	private static String Revision = "r8888";
	
    private static String ProductName ="vmfs";
    private static String ProductLongName ="fluidOps VMFS Driver";
    private static String CompanyName ="fluid Operations";
    private static String ProductVersion ="8.8.8.8888";
    private static String ProductContact ="info@fluidops.com";

    static
    {
        try
        {
            JarFile jar;

            String jarpath = FileUtil.getAbsolutePathFromResource(Version.class, "/com/fluidops/base/Version.class");            
            if (jarpath!=null && jarpath.indexOf('!')>=0)
                jarpath = jarpath.substring(0,jarpath.indexOf('!'));

            if (jarpath==null)
                jarpath = FileUtil.getAbsolutePathFromResource(Version.class, "/fvmfs.jar");
            
            jar = new JarFile(jarpath);
            
            Manifest buildManifest = jar.getManifest();
    	    if (buildManifest!=null)
    	    {
    	        Version = buildManifest.getMainAttributes().getValue("version");
    	        BuildDate = buildManifest.getMainAttributes().getValue("date");
    	        Revision = buildManifest.getMainAttributes().getValue("revision");
    	        ProductName = buildManifest.getMainAttributes().getValue("ProductName");
    	        ProductLongName = buildManifest.getMainAttributes().getValue("ProductLongName");
    	        CompanyName = buildManifest.getMainAttributes().getValue("CompanyName");
                ProductVersion = buildManifest.getMainAttributes().getValue("ProductVersion");
                ProductContact = buildManifest.getMainAttributes().getValue("ProductContact");
    	    }
        }
        catch (Throwable t)
        {
            //ignore
        }

        try
        {
	        if (Version!=null)
	        {
                int i1,i2;
                i1 = 0; 
                i2= Version.indexOf('.');
	            MajorVersion = Version.substring(i1, i2);
                i1 = Version.indexOf('.', i2+1);
                MinorVersion = Version.substring(i2+1, i1);
                i2 = Version.indexOf('.', i1+1);
                PatchVersion = Version.substring(i1+1, i2);
	        }
        }
        catch(Throwable t)
        {
            //ignore
        }
    }
    
    /**
     * Returns the version.
     * @return
     */
	public static String getVersion()
	{	    
		return Version;
	}

    /**
     * Returns the major version.
     * @return
     */
	public static String getMajorVersion()
	{	    
		return MajorVersion;
	}

    /**
     * Returns the minor version.
     * @return
     */
	public static String getMinorVersion()
	{
	    return MinorVersion;
	}

    /**
     * Returns the patch version.
     * @return
     */
    public static String getPatchVersion()
    {
        return PatchVersion;
    }

    /**
     * Returns the build date.
     * @return
     */
	public static String getBuildDate()
	{
	    return BuildDate;
	}

    /**
     * Returns the build number.
     * @return
     */
	public static String getBuildNumber()
	{
	    return BuildNumber;
	}

    /**
     * @return Returns the companyName.
     */
    public static String getCompanyName()
    {
        return CompanyName;
    }

    /**
     * @return Returns the productLongName.
     */
    public static String getProductLongName()
    {
        return ProductLongName;
    }

    /**
     * @return Returns the productName.
     */
    public static String getProductName()
    {
        return ProductName;
    }

    /**
     * @return Returns the productVersion.
     */
    public static String getProductVersion()
    {
        return ProductVersion;
    }

    /**
     * @return Returns an email
     */
    public static String getProductContact()
    {
        return ProductContact;
    }

    /**
     * Returns the revision.
     * @return
     */
	public static String getRevision()
	{	    
		return Revision;
	}
	
    /**
     * Prints the version info.
     * @param args
     */
	public static void main(String[] args)
	{
	    System.out.println("Version Information");
		System.out.println("Version: " + getVersion());
		System.out.println("Major: " + getMajorVersion());
        System.out.println("Minor: " + getMinorVersion());
        System.out.println("Patch: " + getPatchVersion());
		System.out.println("Build Date: " + getBuildDate());
        System.out.println("Build: " + getBuildNumber());
        System.out.println("Revision: " + getRevision());
        System.out.println("ProductName: " + getProductName());
        System.out.println("ProductLongName: " + getProductLongName());
        System.out.println("CompanyName: " + getCompanyName());
        System.out.println("ProductVersion: " + getProductVersion());
        System.out.println("ProductContact: " + getProductContact());
	}
}
