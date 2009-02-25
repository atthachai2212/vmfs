/*
 * StringUtil.java // com.fluidops.util
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Miscellaneous String utility functions.
 * 
 * @author aeb, uli, vasu
 */
public class StringUtil
{    
    static long UNITSIZE[] = { 1l, 1024l, 1024l*1024l, 1024l*1024l*1024l, 1024l*1024l*1024l*1024l };
    static String UNITLABEL[] = { "Bytes", "KB", "MB", "GB", "TB" };
    static DecimalFormat df = new DecimalFormat( "0.00", new DecimalFormatSymbols(Locale.US) );

    /**
     * Converts the given size using suitable unit.
     * 
     * @param size
     * @return
     */
    public static String displaySizeInBytes(Long size) 
    {
        if ( size == null )
            return null;
        
        int i;
        for (i=0; i<UNITSIZE.length && UNITSIZE[i]<=size; i++)
            ;
        if (i>0) i--;

        return df.format( (double)size / (double)UNITSIZE[i] ) + " " + UNITLABEL[i];        
    }
}
