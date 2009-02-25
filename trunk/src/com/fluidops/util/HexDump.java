/*
 * HexDump.java // com.fluidops.util
 *
 * Copyright (C) by Fluid Operations.
 * All rights reserved.
 *
 * For more information go to http://www.fluidops.com
 */
package com.fluidops.util;

import java.io.*;

/**
 * A hex dump utility class useful for debugging native/binary structures.
 * 
 * @author Uli
 */
public class HexDump
{    
    /**
     * Copy/clone the given array.
     * 
     * @param a the source array
     * 
     * @return the cloned byte[]
     */
    public static byte[] copy(byte[] a)
    {
        byte[] b = new byte[a.length];
        System.arraycopy(a, 0, b, 0, b.length);
        return b;
    }

    /**
     * Sleep.
     * 
     * @param ms the ms
     */
    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Converts the given byte to 2-digit hex.
     * 
     * @param b the byte
     * 
     * @return the hex string
     */
    public static String toHex(byte b)
    {
        String s = Integer.toHexString(b);
        if (s.length() > 2)
        {
            s = s.substring(s.length() - 2);
        }
        if (s.length() < 2)
        {
            s = "0" + s;
        }

        return s;
    }

    /**
     * Converts the given int to hex word.
     * 
     * @param b the given int
     * 
     * @return the hex string
     */
    public static String toHexWord(int b)
    {
        String s = Integer.toHexString(b);
        if (s.length() > 4)
        {
            s = s.substring(s.length() - 4);
        }
        while (s.length() < 4)
        {
            s = "0" + s;
        }

        return s;
    }

    /**
     * Dump hex.
     * 
     * @param out the output stream
     * @param a the array to dump
     * @param from offset
     * @param len length
     */
    public static void dumpHex(PrintStream out, byte[] a, int from, int len)
    {
        if (from < 0 || from > a.length)
        {
            return;
        }
        int to = from + len;
        if (to > a.length)
        {
            to = a.length;
        }

        for (int i = from; i < to; i++)
        {
            out.print(toHex(a[i]));
            if (i < to - 1)
            {
                out.print(",");
            }
        }
    }

    /**
     * Dump ascii.
     * 
     * @param out the output stream.
     * @param a the array to dump
     * @param from the offset
     * @param len the length
     */
    public static void dumpAscii(PrintStream out, byte[] a, int from, int len)
    {
        if (from < 0 || from > a.length)
        {
            return;
        }
        int to = from + len;
        if (to > a.length)
        {
            to = a.length;
        }

        for (int i = from; i < to; i++)
        {
            char ch = (char) a[i];
            if (Character.getType(ch) != Character.CONTROL)
            {
                out.print(ch);
            }
            else
            {
                out.print('.');
            }
        }
    }

    /**
     * Dumps the array into the given stream.
     * 
     * @param out the output stream
     * @param a the array
     */
    public static void dump(PrintStream out, byte[] a)
    {
        dump(out, a, 0, a.length);
    }

    /**
     * Dumps the array to stdout.
     * 
     * @param a the array
     */
    public static void dump(byte[] a)
    {
        dump(System.out, a);
    }

    /**
     * Dumps the array range to the given stream.
     * 
     * @param out the output stream
     * @param a the array
     * @param from the offset
     * @param len the length
     */
    public static void dump(PrintStream out, byte[] a, int from, int len)
    {
        if (from < 0 || from > a.length)
        {
            return;
        }
        int to = from + len;
        if (to > a.length)
        {
            to = a.length;
        }

        for (int i = 0; i < to - from; i += 16)
        {
            out.print(toHexWord(i) + ": ");
            int _len = (i + 16) >= (to - from) ? (to - from - i) : 16;
            dumpHex(out, a, i + from, _len);
            int _temp = _len;
            while (_temp++ < 16)
            {
                out.print("   ");
            }

            out.print(" : ");
            dumpAscii(out, a, i + from, _len);
            out.println();
        }
    }

    /**
     * Dumps the array range to stdout.
     * 
     * @param a the array
     * @param from the offset
     * @param len the length
     */
    public static void dump(byte[] a, int from, int len)
    {
        dump(System.out, a, from, len);
    }
}
