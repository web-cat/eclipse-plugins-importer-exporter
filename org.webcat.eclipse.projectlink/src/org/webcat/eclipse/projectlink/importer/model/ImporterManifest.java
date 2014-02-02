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
 * This class stores references to several objects that are required in
 * various places during the import process, so they can easily be passed
 * between functions.
 *
 * @author Ellen Boyd
 * @version Mar 23, 2012
 */
public class ImporterManifest
{
    /* The project or project group that the user is importing. */
    private ImportNode importNode;

    /* The ID of the user. */
    private String username;

    /* The password used to log into the submission target system, if
     * required.
     */
    private String password;


    // ----------------------------------------------------------
    /**
     * Create a new ImporterManifest object.
     */
    public ImporterManifest()
    {
    }


    // ----------------------------------------------------------
    /**
     * Gets the project referred to by this object.
     *
     * @return a ProjectTarget representing the project to submit.
     */
    public ImportNode getImportNode()
    {
        return importNode;
    }

    
    // ----------------------------------------------------------
    /**
     * Sets the projet value.
     * @param val the new project
     */
    public void setImportNode(ImportNode node)
    {
        importNode = node;
    }

    
    // ----------------------------------------------------------
    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    
    // ----------------------------------------------------------
    /**
     * @param username the username to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    
    // ----------------------------------------------------------
    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    
    // ----------------------------------------------------------
    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }


    // ----------------------------------------------------------
    /**
     * @param project the project to set
     */
    public void setProject(ProjectNode project)
    {
        this.importNode = project;
    }
}
