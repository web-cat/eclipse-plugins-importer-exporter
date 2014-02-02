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

package org.webcat.eclipse.projectlink.importer;

//--------------------------------------------------------------------------
/**
 * Thrown when there is an error parsing the importer XML file.
 * 
 * @author Ellen Boyd
 */
public class ImporterParseException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3189236136560273913L;
	
	
	public ImporterParseException()
	{
		super();
	}
	
	
	public ImporterParseException(String message)
	{
		super(message);
	}
}
