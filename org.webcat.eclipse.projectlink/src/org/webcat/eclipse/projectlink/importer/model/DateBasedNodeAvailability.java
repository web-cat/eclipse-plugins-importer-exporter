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

//--------------------------------------------------------------------------
/**
 * Makes an assignment available only if a certain date has passed.
 * 
 * @author Tony Allevato
 */
public class DateBasedNodeAvailability
	extends NodeAvailability
{
	private Date date;


	// ----------------------------------------------------------
	public DateBasedNodeAvailability(Date date)
	{
		this.date = date;
	}


	// ----------------------------------------------------------
	public Date getDate()
	{
		return date;
	}


	// ----------------------------------------------------------
	@Override
	public boolean isVisible()
	{
		return isDownloadable();
	}


	// ----------------------------------------------------------
	@Override
	public boolean isDownloadable()
	{
		return new Date().after(date);
	}
}
