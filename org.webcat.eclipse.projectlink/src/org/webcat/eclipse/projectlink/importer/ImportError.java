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

import org.webcat.eclipse.projectlink.importer.model.ProjectNode;

//--------------------------------------------------------------------------
/**
 * Describes an error that occurred when parsing the importer XML file and the
 * node it happened in.
 * 
 * @author Ellen Boyd
 */
public class ImportError
{
	private ProjectNode project;
	private String message;
	

	public ImportError(ProjectNode project, String message)
	{
		this.project = project;
		this.message = message;
	}
	

	public ProjectNode getProject()
	{
		return project;
	}


	public String getMessage()
	{
		return message;
	}
}
