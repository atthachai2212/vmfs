/*
 * FileUtil.java // com.fluidops.util
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.util;

import java.net.URL;
import java.net.URLDecoder;

/**
 * Miscellaneous file, directory & location utility functions.
 * 
 * @author vasu
 */
public class FileUtil
{
    /**
     * Returns the absolute path of the resource. All special characters like
     * spaces etc. are decoded properly.
     * 
     * @param class
     * @param urlResource
     * @return
     */
    public static String getAbsolutePathFromResource(String resource)
    {
        return getAbsolutePathFromResource(Object.class, resource);
    }
    
    /**
     * Returns the absolute path of the resource. All special characters like
     * spaces etc. are decoded properly.
     * 
     * @param urlResource
     * @return
     */
    public static String getAbsolutePathFromResource(Class reference, String resource)
    {
        URL urlResource = reference.getResource(resource);
        if (urlResource == null)
            return null;

        //    return URI.create(urlResource.toString()).getPath();
        //    problem: URI can't parse spaces in URL at all.

        String urlString = URLDecoder.decode(urlResource.toString());

        if (urlString.startsWith("jar:"))
            urlString = urlString.substring("jar:".length());
        if (urlString.startsWith("file:"))
            urlString = urlString.substring("file:".length());

        //linux: /path/dir
        //windows: /C:/path/dir , also weg mit lead /
        if (urlString.indexOf(':') == 2)
            urlString = urlString.substring(1);

        //other ideas?

        return urlString;
    }
}
