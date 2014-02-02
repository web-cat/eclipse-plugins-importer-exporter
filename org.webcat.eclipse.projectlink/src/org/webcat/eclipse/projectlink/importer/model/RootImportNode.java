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
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;
import org.webcat.eclipse.projectlink.importer.ImporterParseException;
import org.webcat.eclipse.projectlink.util.IndentingWriter;

// -------------------------------------------------------------------------
/**
 *  Represents the root of the import definition tree. The root
 *  contains settings that are inherited down throughout the entire
 *  tree.
 *
 *  @author Ellen Boyd
 *  @version Jan 22, 2012
 */
public class RootImportNode
    extends ImportNode
{
	private HashMap<String, ProjectNode> projectTable;


    // ----------------------------------------------------------
    /**
     * Create a new RootTarget object.
     */
    public RootImportNode()
    {
        super(null);

        projectTable = new HashMap<String, ProjectNode>();
    }


    // ----------------------------------------------------------
    /**
     * Adds a project to the table for easy dependency reference.
     * 
     * @param project the project to add
     */
    public void addProjectNode(ProjectNode project)
    {
    	projectTable.put(project.getID(), project);
    }
    
    
    // ----------------------------------------------------------
    /**
     * Gets the project node with the specified ID.
     * 
     * @param id the ID
     * @return the node
     */
    public ProjectNode getProjectNode(String id)
    {
    	return projectTable.get(id);
    }


    // ----------------------------------------------------------
    /**
     * See ImportTarget {@link #isContainer()}
     */
    @Override
    public boolean isContainer()
    {
        return true;
    }

    
    // ----------------------------------------------------------
    /**
     * See ImportTarget {@link #isNested()}
     */
    @Override
    public boolean isNested()
    {
        return false;
    }

    
    // ----------------------------------------------------------
    /**
     * See ImportTarget {@link #isDownloadable()}
     */
    @Override
    public boolean isDownloadable()
    {
        return false;
    }

    
    // ----------------------------------------------------------
    /**
     * See ImportTarget {@link #isLoaded()}
     */
    @Override
    public boolean isLoaded()
    {
        return true;
    }

    
    // ----------------------------------------------------------
    /**
     * See ImportTarget {@link #parse(Node node)}
     */
    @Override
    public void parse(Node node)
    {
        String nodeName = node.getLocalName();

        if (Xml.Elements.IMPORT_TARGETS.equals(nodeName))
        {
            parseImportTargets(node);
        }
        else
        {
        	throw new ImporterParseException("The data retrieved from the "
        			+ "download URL is not valid.");
        }
    }

    
    // ----------------------------------------------------------
    /**
     * Parses the children of a import-target root node.
     *
     * @param parentNode the node to parse.
     */
    private void parseImportTargets(Node parentNode)
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
     * @return the new project group
     */
    private ImportNode parseProjectGroup(Node node)
    {
        ProjectGroupNode group = new ProjectGroupNode(this);
        group.parse(node);
        return group;
    }

    
    // ----------------------------------------------------------
    /**
     * Called if node represents a project group to create the new
     * group and parse it.
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
		writer.write("<?xml version=\"1.0\"?>\n");
		writer.write("<project-imports>\n");
		
		writer.indent();

		for (ImportNode child : getChildren())
		{
			child.writeIndented(writer);
		}

		writer.dedent();
		
		writer.write("</project-imports>\n");
	}
}
