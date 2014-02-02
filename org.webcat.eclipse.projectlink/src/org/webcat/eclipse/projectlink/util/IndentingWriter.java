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

import java.io.IOException;
import java.io.Writer;

//--------------------------------------------------------------------------
/**
 * A writer that lets you control the indentation of each line; for example,
 * when writing XML content.
 * 
 * @author Tony Allevato
 */
public class IndentingWriter extends Writer
{
	private Writer writer;
	private int indentSize;
	private int depth;
	private boolean startingLine;
	private char[] leader;


	// ----------------------------------------------------------
	public IndentingWriter(Writer writer, int indentSize)
	{
		this.writer = writer;
		this.indentSize = indentSize;

		depth = 0;
		startingLine = true;
		leader = new char[0];
	}


	// ----------------------------------------------------------
	public void indent()
	{
		depth++;
		updateLeader();
	}

	
	// ----------------------------------------------------------
	public void dedent()
	{
		if (depth > 0)
		{
			depth--;
			updateLeader();
		}
		else
		{
			throw new IllegalStateException(
					"Cannot dedent; depth already at 0");
		}
	}


	// ----------------------------------------------------------
	@Override
	public void close() throws IOException
	{
		writer.close();
	}

	
	// ----------------------------------------------------------
	@Override
	public void flush() throws IOException
	{
		writer.flush();
	}


	// ----------------------------------------------------------
	@Override
	public void write(char[] chars, int start, int length) throws IOException
	{
		StringBuffer buffer = new StringBuffer(length);

		for (int i = start; i < start + length; i++)
		{
			if (startingLine)
			{
				buffer.append(leader);
				startingLine = false;
			}

			char ch = chars[i];

			buffer.append(ch);
			
			if (ch == '\n')
			{
				startingLine = true;
			}
		}

		writer.write(buffer.toString().toCharArray(), 0, buffer.length());
	}
	

	// ----------------------------------------------------------
	public void writeEscaped(String str) throws IOException
	{
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			
			switch (ch)
			{
				case '<': buffer.append("&lt;"); break;
				case '>': buffer.append("&gt;"); break;
				case '&': buffer.append("&amp;"); break;
				case '"': buffer.append("&quot;"); break;
				default:  buffer.append(ch); break;
			}
		}

		write(buffer.toString());
	}


	// ----------------------------------------------------------
	private void updateLeader()
	{
		StringBuffer buffer = new StringBuffer(depth * indentSize);
		for (int i = 0; i < depth * indentSize; i++)
		{
			buffer.append(' ');
		}

		leader = buffer.toString().toCharArray();
	}
}
