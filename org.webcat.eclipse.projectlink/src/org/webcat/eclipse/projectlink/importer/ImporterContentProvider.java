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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.importer.model.ImportNode;

// -------------------------------------------------------------------------
/**
 * The content provider for the tree that displays the import targets in the
 * wizard.
 * 
 * @author Ellen Boyd
 * @version Feb 2, 2012
 */
public class ImporterContentProvider implements ITreeContentProvider {

	/* The root of the import target tree. */
	private ImportNode root;

	/**
	 * (from org.eclipse.jface.viewers.ITreeContentProvider: Disposes of this
	 * content provider. This is called by the viewer when it is disposed. The
	 * viewer should not be updated during this call as it is in the process of
	 * being disposed.
	 */
	public void dispose() {
		// Do nothing.
	}

	/**
	 * (from org.eclipse.jface.viewers.IContentProvider): Notifies this content
	 * provider that the given viewer's input has been switched to a different
	 * element.
	 * 
	 * A typical use for this method is registering the content provider as a
	 * listener to changes on the new input (using -model specific means), and
	 * deregistering the viewer from the older input. In response to these
	 * change notifications, the content provider should update the viewer (see
	 * the add, remove, update and refresh methods on the viewers.)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		root = (ImportNode) newInput;
	}

	/**
	 * (from org.eclipse.jface.viewers.ITreeContentProvider) Returns the
	 * elements to display in the viewer when its input is set to the given
	 * element. These elements can be presented as rows in the table, items in a
	 * list, etc. The result is not modified by the viewer.
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(root);
	}

	/**
	 * (from org.eclipse.jface.viewers.ITreeContentProvider): Returns the child
	 * elements of the given parent element.
	 * 
	 * The difference between this method and the
	 * IstructuredContentProvider.getElements is that getElements is called to
	 * obtain the tree viewer's root elements, whereas get children is used to
	 * obtain the children of a given parent element in the tree (including a
	 * root)
	 */
	public Object[] getChildren(Object parentElement) {
		ImportNode obj = (ImportNode) parentElement;
		ArrayList<ImportNode> children = new ArrayList<ImportNode>();

		computeChildren(obj, children);
		return children.toArray();
	}

	/**
	 * Computes the visible children of the specified node, displaying a message
	 * to the user if an error occurs.
	 */
	private void computeChildren(ImportNode obj, ArrayList<ImportNode> list) {
		try {
			List<ImportNode> children = obj.getLogicalChildren();
			for (ImportNode child : children) {
				if (child.getAvailability().isVisible()) {
					if (child.isContainer() && !child.isNested()) {
						computeChildren(child, list);
					} else {
						list.add(child);
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(e);
			// SubmissionParserErrorDialog dlg = new
			// SubmissionParserErrorDialog(null, e);
		}
	}

	/**
	 * (from org.eclipse.jface.viewers.ITreeContentProvider): Returns the parent
	 * for the given element. or null indicating that the parent can't be
	 * computed. In this case the tree-structured viewer can't expand a given
	 * node correctly if requested.
	 */
	public Object getParent(Object element) {
		return ((ImportNode) element).parent();
	}

	/**
	 * (from org.eclipse.jface.viewers.ITreeContentProvider):
	 * 
	 * Returns whether the given element has children.
	 * 
	 * Intended as an optimazation for when the viewer does not need the actual
	 * children. Clients may be able to implement this more efficiently than
	 * getChildren
	 */
	public boolean hasChildren(Object element) {
		ArrayList<ImportNode> children = new ArrayList<ImportNode>();
		computeChildren((ImportNode) element, children);
		return children.size() > 0;
	}

}
