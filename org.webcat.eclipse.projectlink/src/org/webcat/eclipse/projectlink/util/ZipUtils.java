/*==========================================================================*\
 |  Copyright (C) 2012 Virginia Tech
 |
 |  This file is part of Web-CAT Eclipse Plugins.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License along
 |  with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package org.webcat.eclipse.projectlink.util;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

// -------------------------------------------------------------------------
/**
 * Utility class for reading compressed files from a stream and unzipping
 * them to the users hard drive.
 *
 * @author Ellen Boyd
 * @version Apr 2, 2012
 */
public class ZipUtils
{
	//~ Constructors ..........................................................

    // ----------------------------------------------------------
	/**
	 * Prevent instantiation.
	 */
	private ZipUtils()
	{
		// Do nothing.
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	/**
	 * Checks if the specified archive contains a single directory at its root,
	 * and returns its name if so. Otherwise, it returns null.
	 * 
	 * @param file the zip file
	 * @return the name of the single directory at the root, or null
	 * @throws IOException if an error occurs
	 */
	public static String directoryAtArchiveRoot(File file) throws IOException
	{
		ZipFile zipFile = new ZipFile(file);

		String lastPrefix = null;

		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = entries.nextElement();
			
			int slash = entry.getName().indexOf('/');

			if (slash != -1)
			{
				String thisPrefix = entry.getName().substring(0, slash + 1);
				
				if (lastPrefix != null && !thisPrefix.equals(lastPrefix))
				{
					return null;
				}
				
				lastPrefix = thisPrefix;
			}
			else
			{
				return null;
			}
		}

		if (lastPrefix != null)
		{
			return lastPrefix.substring(0, lastPrefix.length() - 1);
		}
		else
		{
			return null;
		}
	}


	// ----------------------------------------------------------
    /**
     * Goes through the contents of the zip file (via the stream), storing
     * each individual element at the given location on the harddrive.
     * 
     * @param destPath the path of the destination file
     * @param stream the input stream given the url of the host
     * @throws IOException if the given filepath doesn't exist
     */
    public static void unpack(File destPath, File zipFile)
        throws IOException
    {
    	FileInputStream stream = new FileInputStream(zipFile);
	    ZipInputStream zipStream = new ZipInputStream(stream);
	
	    ZipEntry zipEntry = zipStream.getNextEntry();
	    while (zipEntry != null)
	    {
	        String name = zipEntry.getName();

	        if (zipEntry.isDirectory())
	        {
	            if (!"__MACOSX".equals(name))
	            {
	                File destDir = new File(destPath, name);
	
	                if (!destDir.exists())
	                {
	                    destDir.mkdirs();
	                }
	            }
	        }
	        else if (name != null
	                  && !(name.equals(".DS_Store")
	                       || name.startsWith("__MACOSX/")
	                       || name.endsWith("/.DS_Store")))
	        {
	            File destFile = new File(destPath, name);
	            File destParent = destFile.getParentFile();
	
	            if (destParent != null  &&  !destParent.exists())
	            {
	                destParent.mkdirs();
	            }
	
	            copyStreamToFile(zipStream, destFile, zipEntry.getTime());
	        }
	
	        zipStream.closeEntry();
	        zipEntry = zipStream.getNextEntry();
	    }
	    
	    stream.close();
	}

    
    // ----------------------------------------------------------
    public static void copyStreamToFile(InputStream stream, File destFile,
        long fileTime) throws IOException
    {
        OutputStream outStream = new FileOutputStream(destFile);
        copyStream(stream, outStream);
        outStream.flush();
        outStream.close();

        destFile.setLastModified(fileTime);
    }


    // ----------------------------------------------------------
    public static void copyStream(InputStream in, OutputStream out)
        throws IOException
    {
        final int BUFFER_SIZE = 65536;

        // read in increments of BUFFER_SIZE
        byte[] b = new byte[BUFFER_SIZE];
        int count = in.read(b);
        while (count > -1)
        {
            out.write(b, 0, count);
            count = in.read(b);
        }

        out.flush();
    }
}
