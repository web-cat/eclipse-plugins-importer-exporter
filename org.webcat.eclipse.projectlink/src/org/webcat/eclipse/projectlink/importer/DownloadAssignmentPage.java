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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.dialogs.ExceptionDialog;
import org.webcat.eclipse.projectlink.i18n.Messages;
import org.webcat.eclipse.projectlink.importer.model.ImportNode;
import org.webcat.eclipse.projectlink.importer.model.ProjectGroupNode;
import org.webcat.eclipse.projectlink.importer.model.ProjectNode;
import org.webcat.submitter.Submitter;

//--------------------------------------------------------------------------
/**
 * The main page of the submission wizard contains the assignment tree and
 * other user-input fields.
 *
 * @author Ellen Boyd, Tony Allevato
 */
public class DownloadAssignmentPage extends WizardPage
{
	//~ Instance/static variables .............................................

	private Importer importer;

	private TreeViewer assignmentTree;

	/* Set to false while control initialization occurs so that an error
	   message will not be displayed in the wizard until actual user input
	   occurs, as per Eclipse user interface guidelines. */
	private boolean initializationComplete = false;
	private Label infoLabel;

	
	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the main wizard page.
	 * 
	 * @param submitter the {@link Submitter} to use to submit
	 * @param project the project being submitted
	 */
	protected DownloadAssignmentPage(Importer importer)
	{
		super(Messages.DownloadAssignmentPage_Title);

		this.importer = importer;

		setTitle(Messages.DownloadAssignmentPage_Title);
		setDescription(Messages.DownloadAssignmentPage_Description);
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 10;
		gl.marginWidth = 10;
		composite.setLayout(gl);
		
		setControl(composite);
		
		Label lblSelectAnAssignment = new Label(composite, SWT.NONE);
		lblSelectAnAssignment.setText(Messages.DownloadAssignmentPage_Select_Assignment);

		assignmentTree = new TreeViewer(composite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.heightHint = 200;
		assignmentTree.getTree().setLayoutData(
				gridData);
		
		infoLabel = new Label(composite, SWT.WRAP);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		assignmentTree.setContentProvider(
				new ImporterContentProvider());
		assignmentTree.setLabelProvider(new ImporterLabelProvider());
		assignmentTree.addSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e)
					{
						assignmentTreeSelectionChanged();
					}
				});

		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				initializeImporter();
			}
		});

		updatePageComplete();

		initializationComplete = true;
	}


	// ----------------------------------------------------------
	private void initializeImporter()
	{
		if (Activator.getDefault().getDownloadURL().length() == 0)
		{
			MessageDialog.openInformation(getContainer().getShell(),
					Messages.DownloadAssignmentPage_No_URL_Title,
					Messages.DownloadAssignmentPage_No_URL_Description);
			return;
		}

		try
		{
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException
				{
					try
					{
						URL url = new URL(Activator.getDefault().getDownloadURL());
						importer.readSchema(url, monitor);
	
						Display.getDefault().syncExec(new Runnable() {
							public void run()
							{
								assignmentTree.setInput(importer.getRoot());
								expandAllLocalGroups(importer.getRoot(), getContainer());
							}
						});
					}
					catch (IOException e)
					{
						showErrorDialog(e);
					}
				}
			});
		}
		catch (InterruptedException e)
		{
			// Do nothing.
		}
		catch (InvocationTargetException e)
		{
			showErrorDialog(e);
		}
	}


	// ----------------------------------------------------------
	private void showErrorDialog(final Exception e)
	{
		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				new ExceptionDialog(getContainer().getShell(), e).open();
			}			
		});
	}


	// ----------------------------------------------------------
	/**
	 * Gets the assignment currently selected in the tree.
	 * 
	 * @return The IDefinitionObject representing the currently selected
	 *         assignment.
	 */
	public ImportNode getSelectedNode()
	{
		IStructuredSelection sel =
				(IStructuredSelection) assignmentTree.getSelection();

		if (sel.getFirstElement() instanceof ProjectNode
				|| sel.getFirstElement() instanceof ProjectGroupNode)
		{
			return (ImportNode) sel.getFirstElement();
		}
		else
		{
			return null;
		}
	}


	// ----------------------------------------------------------
	private void expandAllLocalGroups(ImportNode obj,
			                          IRunnableContext context)
	{
		if (obj != null)
		{
			for (ImportNode child : obj.getLogicalChildren())
			{
				if (child.isLoaded())
				{
					if (assignmentTree.isExpandable(child))
					{
						assignmentTree.setExpandedState(child, true);
						expandAllLocalGroups(child, context);
					}
				}
			}
		}
	}


	// ----------------------------------------------------------
	private void assignmentTreeSelectionChanged()
	{
		updatePageComplete();
	}


	// ----------------------------------------------------------
	private static String join(Iterable<ProjectNode> iterable)
	{
		StringBuffer buffer = new StringBuffer();

		Iterator<ProjectNode> it = iterable.iterator();
		while (it.hasNext())
		{
			buffer.append(it.next().getName());
			
			if (it.hasNext())
			{
				buffer.append(", ");
			}
		}
		
		return buffer.toString();
	}


	// ----------------------------------------------------------
	private void setErrorMessageIfInitialized(String msg)
	{
		if (initializationComplete)
		{
			setErrorMessage(msg);
		}
	}


	// ----------------------------------------------------------
	private void updatePageComplete()
	{
		if (assignmentTree.getSelection().isEmpty())
		{
			setPageComplete(false);
			setErrorMessageIfInitialized(Messages.DownloadAssignmentPage_Select_Assignment_or_Group);
			return;
		}

		ImportNode node = getSelectedNode();

		if (node == null || !node.isDownloadable())
		{
			infoLabel.setText("");
			getContainer().getShell().layout(true, true);

			setPageComplete(false);
			setErrorMessageIfInitialized(Messages.DownloadAssignmentPage_Select_Assignment_or_Group);
			return;
		}

		if (node != null)
		{
			HashSet<ProjectNode> depends = new HashSet<ProjectNode>();
			importer.gatherProjects(depends, node);
			importer.gatherDependencies(depends, node, true);
	
			if (depends.size() > 0)
			{
				infoLabel.setText(MessageFormat.format(
						Messages.DownloadAssignmentPage_Projects_to_Download, join(depends)));
			}
			else
			{
				infoLabel.setText("");
				getContainer().getShell().layout(true, true);
				
				setPageComplete(false);
				setErrorMessageIfInitialized(Messages.DownloadAssignmentPage_Group_Has_No_Assignments);
				return;
			}
		}

		getContainer().getShell().layout(true, true);

		setPageComplete(true);
		setErrorMessage(null);
	}
}
