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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.webcat.eclipse.projectlink.util.IndentingWriter;

// -------------------------------------------------------------------------
/**
 * Represents a project group in the import definition tree. A
 * project group is a container for other groups and projects, and
 * contains common settings that can be inherited by its children.
 *
 * @author Ellen Boyd
 * @version Jan 22, 2012
 */
public class ProjectGroupNode extends ImportNode
{
	//~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new project group node with the specified parent.
     *
     * @param parent the node assigned parent of new node
     */
    public ProjectGroupNode(ImportNode parent)
    {
        super(parent);
    }

    
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean isContainer()
    {
        return true;
    }


    // ----------------------------------------------------------
    @Override
    public boolean isNested()
    {
        return (getName() != null);
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
    @Override
    public void parse(Node parentNode)
    {
        parseCommonAttributes(parentNode);
        Node node = parentNode.getFirstChild();

        while (node != null)
        {
            String nodeName = node.getLocalName();

            if (Xml.Elements.PROJECT_GROUP.equals(nodeName))
            {
                parseProjectGroup(node);
            }
            else if (Xml.Elements.PROJECT.equals(nodeName))
            {
                parseProject(node);
            }
            else if (Xml.Elements.PREFERENCES.equals(nodeName))
            {
            	parsePreferences(node);
            }

            node = node.getNextSibling();
        }
    }


    // ----------------------------------------------------------
    /**
     * Called if node represents a project group to create the new
     * group and parse it.
     *
     * @param node the project-group node to parse
     * @return the new project-group
     */
    private ImportNode parseProjectGroup(Node node)
    {
        ProjectGroupNode group = new ProjectGroupNode(this);
        group.parse(node);
        return group;
    }

    
    // ----------------------------------------------------------
    /**
     * Called if node represents a project to create the new assignment and
     * parse it.
     *
     * @param node the project node to parse
     * @return the new project
     */
    private ImportNode parseProject(Node node)
    {
        ProjectNode project = new ProjectNode(this);
        project.parse(node);
        return project;
    }


    // ----------------------------------------------------------
    /**
     * Called if node represents a preferences node to create the new
     * node and parse it.
     *
     * @param node the node to parse
     * @return the new node
     */
    private ImportNode parsePreferences(Node node)
    {
        PreferencesNode prefs = new PreferencesNode(this);
        prefs.parse(node);
        return prefs;
    }


    // ----------------------------------------------------------
	@Override
	protected void writeIndented(IndentingWriter writer) throws IOException
	{
		writer.write("<project-group ");
		writer.write("name=\"");
		writer.writeEscaped(getName());
		writer.write("\"");
		
		writeAvailability(writer);
		
		writer.write(">\n");

		writer.indent();

		for (ImportNode child : getChildren())
		{
			child.writeIndented(writer);
		}

		writer.dedent();
		
		writer.write("</project-group>\n");
	}
}
