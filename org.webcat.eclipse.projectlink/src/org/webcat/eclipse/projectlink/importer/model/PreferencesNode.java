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

package org.webcat.eclipse.projectlink.importer.model;

import java.io.IOException;

import org.w3c.dom.Node;
import org.webcat.eclipse.projectlink.util.IndentingWriter;

//--------------------------------------------------------------------------
/**
 * A node that allows Eclipse workspace preferences to be imported. This is
 * useful to equalize users' code format settings at the beginning of a course.
 * Provide a preferences node at the root of your course's import XML file and
 * those settings will be imported when the assignments are imported.
 * 
 * @author Tony Allevato
 */
public class PreferencesNode extends ImportNode
{
	private String uri;


	// ----------------------------------------------------------
	public PreferencesNode(ImportNode parent)
	{
		super(parent);
	}

	
	// ----------------------------------------------------------
	@Override
	public boolean isContainer()
	{
		return false;
	}


	// ----------------------------------------------------------
	@Override
	public NodeAvailability getAvailability()
	{
		return NodeAvailability.HIDDEN;
	}


	// ----------------------------------------------------------
	@Override
	public boolean isNested()
	{
		return true;
	}


	// ----------------------------------------------------------
	@Override
	public boolean isDownloadable()
	{
		return true;
	}


	// ----------------------------------------------------------
	@Override
	public boolean isLoaded()
	{
		return true;
	}
	
	
	// ----------------------------------------------------------
	public String getURI()
	{
		return uri;
	}
	
	
	// ----------------------------------------------------------
	public void setURI(String newURI)
	{
		uri = newURI;
	}


	// ----------------------------------------------------------
	@Override
	public void parse(Node node)
	{
        Node uriNode = node.getAttributes().getNamedItem(Xml.Attributes.URI);

        if (uriNode != null)
        {
            setURI(uriNode.getNodeValue());
        }
	}


	// ----------------------------------------------------------
	@Override
	protected void writeIndented(IndentingWriter writer) throws IOException
	{
		writer.write("<preferences");
		
		if (getURI() != null)
		{
			writer.write(" uri=\"");
			writer.writeEscaped(getURI());
			writer.write("\"");
		}

		writer.write("/>\n");
	}
}
