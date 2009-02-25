/*
 * Debug.java // com.fluidops.util.logging
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.util.logging;

import java.io.*;


/**
 * Debug print functions.
 * Can be disabled/enabled using global property.
 * 
 * @author Uli
 */
public class Debug
{
    public static PrintStream out, err;
    public static boolean debug;
    
    public static class NullStream extends OutputStream
    {
        public NullStream() {}

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
        }

        @Override
        public void write(int b) throws IOException
        {
        }
    }
 
    /**
     * Enables or disables debugging.
     * @param debug
     */
    public static void setDebug( boolean debug )
    {
        Debug.debug = debug;
        if ( debug )
        {
            out = System.out;
            err = System.err;
        }
        else
        {
            out = new PrintStream( new NullStream() );
            err = new PrintStream( new NullStream() );
        }
        
    }
    
    static
    {
        setDebug( Boolean.getBoolean("com.fluidops.util.logging.debug") );
    }
}
