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

import java.util.Date;

//-------------------------------------------------------------------------
/**
 * Represents the availability of a node -- whether it can be viewed and/or
 * downloaded.
 * 
 * @author Tony Allevato
 */
public abstract class NodeAvailability
{
	// ----------------------------------------------------------
	public abstract boolean isVisible();
	
	
	// ----------------------------------------------------------
	public abstract boolean isDownloadable();
	
	
	// ----------------------------------------------------------
	public static final NodeAvailability NONE = new NodeAvailability() {
		public boolean isVisible()      { return false; }
		public boolean isDownloadable() { return false; }		
	};

	
	// ----------------------------------------------------------
	public static final NodeAvailability HIDDEN = new NodeAvailability() {
		public boolean isVisible()      { return false; }
		public boolean isDownloadable() { return true; }		
	};

	
	// ----------------------------------------------------------
	public static final NodeAvailability AVAILABLE = new NodeAvailability() {
		public boolean isVisible()      { return true; }
		public boolean isDownloadable() { return true; }		
	};

	
	// ----------------------------------------------------------
	public static DateBasedNodeAvailability after(Date date)
	{
		return new DateBasedNodeAvailability(date);
	}
}
