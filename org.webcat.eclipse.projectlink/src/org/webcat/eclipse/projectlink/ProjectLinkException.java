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

package org.webcat.eclipse.projectlink;

import java.io.IOException;

//--------------------------------------------------------------------------
/**
 * A general exception type used by the Importer that wraps other more
 * specific exceptions, such as I/O exceptions and XML parsing exceptions, so
 * that they can be handled in a common manner.
 *
 * @author  Ellen Boyd (Virginia Tech Computer Science)
 */
public class ProjectLinkException extends IOException
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new instance of this exception with no inner exception. This
     * is only used by subclasses that don't represent their errors using
     * wrapped exceptions.
     */
    protected ProjectLinkException()
    {
        // Does nothing.
    }


    // ----------------------------------------------------------
    /**
     * Creates a new instance of this exception that wraps the specified
     * exception.
     *
     * @param cause a Throwable that represents the actual exception that was
     *     thrown
     */
    public ProjectLinkException(Throwable cause)
    {
        super(cause);
    }


    //~ Static/instance variables .............................................

    private static final long serialVersionUID = 1L;
}
