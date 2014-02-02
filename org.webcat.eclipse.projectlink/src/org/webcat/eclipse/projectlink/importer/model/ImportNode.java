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
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.webcat.eclipse.projectlink.util.IndentingWriter;

// -------------------------------------------------------------------------
/**
 * An abstract base class from which all objects in the import target tree
 * are derived.
 *
 * @author Ellen Boyd
 * @version Jan 22, 2012
 */
public abstract class ImportNode
{
	//~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new ImportTarget object.
     * 
     * @param parent represents this node's parent in the tree
     */
    protected ImportNode(ImportNode parent)
    {
        this.parent = parent;
        
        if (parent != null)
        {
        	parent.children.add(this);
        }
        
        name = null;
        availability = NodeAvailability.AVAILABLE;

        otherAttributes = new LinkedHashMap<String, String>();
        
        children = new ArrayList<ImportNode>();
    }

    
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the parent node to this node in the tree.
     * 
     * @return the ImportTarget that is the parent of this tree
     */
    public ImportNode parent()
    {
        return parent;
    }


    // ----------------------------------------------------------
    /**
     * Gets the root of the tree that this object is contained in.
     * 
     * @return the root of the tree, or null if it could not be
     *     found or if the top level target was not a RootTarget
     */
    public RootImportNode getRoot()
    {
        ImportNode par = this;
        
        while (par.parent() != null)
        {
            par = par.parent();
        }
        
        if (par instanceof RootImportNode)
        {
            return (RootImportNode) par;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Overridden by derived classes to specify whether the node may
     * contain children.
     *
     * @return true if the node may contain children; otherwise, false.
     */
    public abstract boolean isContainer();

    
    // ----------------------------------------------------------
    /**
     * Overridden by derived classes to specify whether the node should be
     * displayed at the same level as its parent, or if it should be nested
     * at a lower-level. Typically a target that is a container will not be
     * nested if it does not have a name, but subclasses can provide their own
     * behavior.
     *
     * @return true if the node should be nested at a lower level in the tree;
     *      false if it should be displayed at same level as parent.
     */
    public abstract boolean isNested();

    
    // ----------------------------------------------------------
    /**
     * Overridden by derived classes to specify whether an action can be taken
     * on this node. In a wizard for example, this would enable the "next"
     * or "finish" button so the user can download/import an assignment.
     *
     * @return true if the node is actionable; otherwise, false
     */
    public abstract boolean isDownloadable();

    
    // ----------------------------------------------------------
    /**
     * Overridden by derived classes to specify whether the node has been
     * loaded into local memory. This is always true for most nodes.
     * @return true if the node is local or if it has been delay-loaded;
     * false if
     */
    public abstract boolean isLoaded();


    // ----------------------------------------------------------
    /**
     * Gets the name of the target.
     *
     * @return the name of the target
     */
    public String getName()
    {
        return name;
    }

    
    // ----------------------------------------------------------
    /**
     * Sets the name of the target
     *
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }


    // ----------------------------------------------------------
    /**
     * Gets the value indicating whether the target should be hidden
     * in the user interface.
     *
     * @return true if the target should be hidden; false otherwise
     */
    public NodeAvailability getAvailability()
    {
        return availability;
    }

    // ----------------------------------------------------------
    /**
     * Sets the value indicating whether the target should be hidden in
     * the user interface.
     *
     * @param value true if the target should be hidden; otherwise, false.
     */
    public void setAvailability(NodeAvailability value)
    {
        availability = value;
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of an attribute for import targets at this level
     * in the tree. This function walks up the tree to find an inherited
     * attribute if necessary
     *
     * @param attribute the name of the attribute
     * @return a String containing the value of the attribute, or null
     *      if not present
     */
    public String getAttribute(String attribute)
    {
        String localAttribute = getLocalAttribute(attribute);
        if (localAttribute != null)
        {
            return localAttribute;
        }
        else if (parent != null)
        {
            return parent.getAttribute(attribute);
        }
        else
        {
            return null;
        }
    }

    
    // ----------------------------------------------------------
    /**
     * Gets the value of an attribute for import targets at this level in
     * the tree. This function does not walk up the tree to find an inherited
     * attribute--it returns the attribute specified for this node only.
     *
     * @param attribute the name of the attribute
     * @return a String containing the value of the attribute or null
     *      if not present.
     */
    public String getLocalAttribute(String attribute)
    {
        return otherAttributes.get(attribute);
    }

    
    // ----------------------------------------------------------
    /**
     * Sets an attribute for this node.
     *
     * @param attribute the name of the attribute
     * @param value the value for the attribute
     */
    public void setAttribute(String attribute, String value)
    {
        if (value != null)
        {
            otherAttributes.put(attribute, value);
        }
        else
        {
            otherAttributes.remove(attribute);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the children of this node. This function only considers the
     * link structure of the target tree, not the nested state of any
     * of the nodes
     *
     * @return an array of ImportTargets representing the children of the
     *      node
     */
    public List<ImportNode> getChildren()
    {
        return children;
    }
    

    // ----------------------------------------------------------
    public void insertBefore(ImportNode before)
    {
    	int index = before.parent.children.indexOf(before);
    	
    	if (index != -1)
    	{
    		this.parent = before.parent;
    		this.parent.children.add(index, this);
    	}
    }


    // ----------------------------------------------------------
    public void insertAfter(ImportNode after)
    {
    	int index = after.parent.children.indexOf(after);
    	
    	if (index != -1)
    	{
    		this.parent = after.parent;
    		this.parent.children.add(index + 1, this);
    	}
    }


    // ----------------------------------------------------------
    public void addToParent(ImportNode newParent)
    {
		this.parent = newParent;
		newParent.children.add(this);
    }


    // ----------------------------------------------------------
    /**
     * Gets the "logical" children of this node, respecting the nested state of
     * any children (so that children of a non-nested child are "pushed up"
     * into the parent). This method is appropriate for determining the
     * children of a node as they should be displayed in a user interface.
     *
     * @return an array of ImportTargets that represent the logical
     *     children of the node
     */
    public List<ImportNode> getLogicalChildren()
    {
       List<ImportNode> childList = new ArrayList<ImportNode>();

       computeLogicalChildren(this, childList);

       return childList;
    }


    // ----------------------------------------------------------
    public void remove()
    {
    	if (parent != null)
    	{
    		parent.children.remove(this);
    	}
    }


    // ----------------------------------------------------------
    public Set<PreferencesNode> getPreferences()
    {
        Set<PreferencesNode> prefs = new HashSet<PreferencesNode>();
        
        for (ImportNode child : getChildren())
        {
        	if (child instanceof PreferencesNode)
        	{
        		prefs.add((PreferencesNode) child);
        	}
        }

        if (parent() != null)
        {
        	prefs.addAll(parent().getPreferences());
        }

        return prefs;
    }


    // ----------------------------------------------------------
    /**
     * Recursively computes the children for the specified target, taking into
     * account the nested state of each target.
     *
     * @param target the target whose children should be computed
     * @param list a list that will hold the children upon returning
     */
    private static void computeLogicalChildren(ImportNode target,
            List<ImportNode> list)
    {
        List<ImportNode> children = target.getChildren();

        for (ImportNode child : children)
        {
            if (child.getAvailability().isVisible())
            {
                if (child.isContainer() && !child.isNested())
                {
                    computeLogicalChildren(child, list);
                }
                else
                {
                    list.add(child);
                }
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Parses the specified xml node and builds a subtree from the data.
     *
     * @param node the XML document Node to parese from
     */
    public abstract void parse(Node node);


    // ----------------------------------------------------------
    /**
     * Parses the common attributes for the specified XML node and adds them to
     * the target.
     *
     * @param node the XML document node to parse from.
     */
    protected void parseCommonAttributes(Node node)
    {
        Node nameNode = node.getAttributes().getNamedItem(Xml.Attributes.NAME);
        Node availabilityNode = node.getAttributes().getNamedItem(
        		Xml.Attributes.AVAILABILITY);

        String availabilityString = null;

        if (nameNode != null)
        {
            setName(nameNode.getNodeValue());
        }

        if (availabilityNode != null)
        {
        	availabilityString = availabilityNode.getNodeValue();
        }
        else
        {
        	availabilityString = "available";
        }

        if ("hidden".equals(availabilityString))
        {
        	setAvailability(NodeAvailability.HIDDEN);
        }
        else if ("none".equals(availabilityString))
        {
        	setAvailability(NodeAvailability.NONE);
        }
        else if ("available".equals(availabilityString))
        {
        	setAvailability(NodeAvailability.AVAILABLE);
        }
        else
        {
        	DateFormat format = DateFormat.getDateTimeInstance(
        			DateFormat.SHORT, DateFormat.SHORT);

        	try
        	{
        		Date date = format.parse(availabilityString);
        		setAvailability(NodeAvailability.after(date));
        	}
        	catch (ParseException e)
        	{
        		setAvailability(NodeAvailability.AVAILABLE);
        	}
        }

        for (int i = 0; i < node.getAttributes().getLength(); i ++)
        {
            Node attribute = node.getAttributes().item(i);
            String attributeName = attribute.getNodeName();

            String value = attribute.getNodeValue();
            otherAttributes.put(attributeName, value);
        }
    }
    

    // ----------------------------------------------------------
    public final void write(Writer writer) throws IOException
    {
    	writeIndented(new IndentingWriter(writer, 4));
    }

    
    // ----------------------------------------------------------
    protected abstract void writeIndented(IndentingWriter writer)
    		throws IOException;
    
    
    // ----------------------------------------------------------
    protected void writeAvailability(IndentingWriter writer) throws IOException
    {
    	if (getAvailability() != NodeAvailability.AVAILABLE)
    	{
    		writer.write(" availability=\"");

    		if (getAvailability() == NodeAvailability.HIDDEN)
    		{
    			writer.write("hidden");
    		}
    		else if (getAvailability() == NodeAvailability.NONE)
    		{
    			writer.write("none");
    		}
    		else if (getAvailability() instanceof DateBasedNodeAvailability)
    		{
    			DateBasedNodeAvailability avail =
    					(DateBasedNodeAvailability) getAvailability();
    			
    			writer.write(avail.getDate().toString());
    		}

    		writer.write("\"");
    	}
    }

    
    //~ Instance/static variables .............................................

    /* The parent object to this object in the tree. */
    private ImportNode parent;

    /* The name of the target */
    private String name;

    /* Indicates whether the target should be hidden */
    private NodeAvailability availability;

    /* The list of child nodes to this node in the import target tree. */
    private List<ImportNode> children;

    /* Other attributes associated with an import target*/
    private LinkedHashMap<String, String> otherAttributes;
}
