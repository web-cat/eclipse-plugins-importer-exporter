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

// -------------------------------------------------------------------------
/**
 * Constants representing the elements and attributes in the xml file.
 *
 * @author Ellen Boyd, Tony Allevato
 * @version Jan 18, 2012
 */
public class Xml
{
    // ----------------------------------------------------------
    /**
     * Elements (nodes) in the XML definition file.
     */
    public static class Elements
    {
        public static final String IMPORT_TARGETS = "project-imports";
        public static final String PROJECT_GROUP = "project-group";
        public static final String PROJECT = "project";
        public static final String PREFERENCES = "preferences";
    }


    // ----------------------------------------------------------
    /**
     * Attributes of elements in the XML file.
     */
    public static class Attributes
    {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String URI = "uri";
        public static final String AVAILABILITY = "availability";
        public static final String DEPENDS = "depends";
    }
}
