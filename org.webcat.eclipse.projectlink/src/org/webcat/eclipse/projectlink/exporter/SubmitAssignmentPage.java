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

package org.webcat.eclipse.projectlink.exporter;

import static org.webcat.eclipse.projectlink.util.SWTUtil.getText;
import static org.webcat.eclipse.projectlink.util.SWTUtil.setText;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.dialogs.AuthenticationDialog;
import org.webcat.eclipse.projectlink.dialogs.ExceptionDialog;
import org.webcat.eclipse.projectlink.i18n.Messages;
import org.webcat.submitter.SubmissionTargetException;
import org.webcat.submitter.Submitter;
import org.webcat.submitter.targets.AssignmentTarget;
import org.webcat.submitter.targets.SubmissionTarget;

//--------------------------------------------------------------------------
/**
 * The main page of the submission wizard contains the assignment tree and
 * other user-input fields.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 */
public class SubmitAssignmentPage extends WizardPage
{
	//~ Static/instance variables .............................................

	/* The submission engine instance that should be used by this wizard to
	   submit the project. */
	private Submitter submitter;

	/* The currently selected project that will be submitted by the wizard. */
	private IProject project;

	/* A text field that displays the name of the currently selected
	   project. */
	private Text projectField;

	/* A tree that displays the submission targets that can be selected for
	   submission. */
	private TreeViewer assignmentTree;

	/* A text field that optionally contains a comma-separated list of user
	   IDs that represent partners who should be attached to the
	   assignment. */
	private Text partners;

	/* Set to false while control initialization occurs so that an error
	   message will not be displayed in the wizard until actual user input
	   occurs, as per Eclipse user interface guidelines. */
	private boolean initializationComplete = false;
	private Composite loginComposite;
	private Composite partnerComposite;
	private Composite treeComposite;
	private Composite projectComposite;
	private Label partnerLabel;

	
	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the main wizard page.
	 * 
	 * @param submitter the {@link Submitter} to use to submit
	 * @param project the project being submitted
	 */
	protected SubmitAssignmentPage(Submitter submitter, IProject project)
	{
		super(Messages.SubmitAssignmentPage_Page_Title);

		setTitle(Messages.SubmitAssignmentPage_Page_Title);
		setDescription(Messages.SubmitAssignmentPage_Page_Description);

		this.submitter = submitter;
		this.project = project;
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	/**
	 * Gets the assignment currently selected in the tree.
	 * 
	 * @return The IDefinitionObject representing the currently selected
	 *         assignment.
	 */
	public AssignmentTarget getSelectedAssignment()
	{
		IStructuredSelection sel = (IStructuredSelection) assignmentTree
		        .getSelection();
		
		if (sel.getFirstElement() instanceof AssignmentTarget)
		{
			return (AssignmentTarget) sel.getFirstElement();
		}
		else
		{
			return null;
		}
	}


	// ----------------------------------------------------------
	public IProject getProject()
	{
		return project;
	}


	// ----------------------------------------------------------
	public String getPartners()
	{
		AssignmentTarget target = getSelectedAssignment();
		
		boolean usesPartners =
			(target != null && target.usesParameter("partners")); //$NON-NLS-1$

		if (usesPartners)
		{
			return getText(partners);
		}
		else
		{
			return null;
		}
	}


	// ----------------------------------------------------------
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 10;
		gl.marginHeight = 10;
		gl.marginWidth = 10;
		composite.setLayout(gl);
		
		projectComposite = new Composite(composite, SWT.NONE);
		projectComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_projectComposite = new GridLayout(3, false);
		gl_projectComposite.marginWidth = 0;
		gl_projectComposite.marginHeight = 0;
		gl_projectComposite.verticalSpacing = 0;
		projectComposite.setLayout(gl_projectComposite);
		
		Label lblProject = new Label(projectComposite, SWT.NONE);
		lblProject.setBounds(0, 0, 59, 14);
		lblProject.setText(Messages.SubmitAssignmentPage_Project);
		
		projectField = new Text(projectComposite, SWT.BORDER | SWT.READ_ONLY);
		projectField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						
		Button projectChoose = new Button(projectComposite, SWT.NONE);
		projectChoose.setText(Messages.SubmitAssignmentPage_ChooseProject);
		projectChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				chooseProjectToSubmit();
			}
		});
		
		treeComposite = new Composite(composite, SWT.NONE);
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		GridLayout gl_treeComposite = new GridLayout(1, false);
		gl_treeComposite.marginWidth = 0;
		gl_treeComposite.marginHeight = 0;
		treeComposite.setLayout(gl_treeComposite);
		
		Label lblNewLabel = new Label(treeComposite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel.setText(Messages.SubmitAssignmentPage_SelectAssignment);
				
		assignmentTree = new TreeViewer(treeComposite,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Tree tree = assignmentTree.getTree();
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tree.heightHint = 150;
		tree.setLayoutData(gd_tree);
		
		partnerComposite = new Composite(composite, SWT.NONE);
		partnerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_partnerComposite = new GridLayout(1, false);
		gl_partnerComposite.marginWidth = 0;
		gl_partnerComposite.marginHeight = 0;
		partnerComposite.setLayout(gl_partnerComposite);
		
		partnerLabel = new Label(partnerComposite, SWT.WRAP);
		partnerLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		partnerLabel.setText(Messages.SubmitAssignmentPage_EnterPartnerNames);

		partners = new Text(partnerComposite, SWT.BORDER);
		partners.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		loginComposite = new Composite(composite, SWT.NONE);
		loginComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_loginComposite = new GridLayout(2, false);
		gl_loginComposite.marginHeight = 0;
		gl_loginComposite.marginWidth = 0;
		loginComposite.setLayout(gl_loginComposite);
		
		Label lblCurrentlyLoggedIn = new Label(loginComposite, SWT.NONE);
		lblCurrentlyLoggedIn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button changeUser = new Button(loginComposite, SWT.NONE);
		changeUser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				askForLoginCredentials();
			}
		});
		changeUser.setText(Messages.SubmitAssignmentPage_ChangeUsernamePassword);

		setControl(composite);

		// Not auto-generated code

		if (project != null)
		{
			projectField.setText(project.getName());
		}

		assignmentTree.setContentProvider(
				new SubmitterContentProvider());
		assignmentTree.setLabelProvider(new SubmitterLabelProvider());
		assignmentTree
		        .addSelectionChangedListener(new ISelectionChangedListener() {
			        public void selectionChanged(SelectionChangedEvent e)
			        {
				        assignmentTreeSelectionChanged();
			        }
		        });

		String lastPartners = Activator.getDefault().getLastEnteredPartners();
		setText(partners, lastPartners);

		String username = Activator.getDefault().getStoredUsername();
		if (username.length() > 0)
		{
			lblCurrentlyLoggedIn.setText(MessageFormat.format(
					Messages.SubmitAssignmentPage_Currently_Logged_in_as,
					username));
		}
		else
		{
			includeComposite(loginComposite, false);
		}

		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				initializeSubmitter();
			}
		});

		updatePageComplete();

		initializationComplete = true;
	}


	// ----------------------------------------------------------
	private void initializeSubmitter()
	{
		if (Activator.getDefault().getSubmitURL().length() == 0)
		{
			MessageDialog.openInformation(getContainer().getShell(),
					Messages.SubmitAssignmentPage_No_URL_Title,
					Messages.SubmitAssignmentPage_No_URL_Description);
			return;
		}

		try
		{
			URL url = new URL(Activator.getDefault().getSubmitURL());
			submitter.readSubmissionTargets(url);

			Display.getDefault().syncExec(new Runnable() {
				public void run()
				{
					assignmentTree.setInput(submitter.getRoot());
					expandAllLocalGroups(submitter.getRoot(), getContainer());
					selectLastSelectedAssignmentInTree();
					
					updatePageComplete();
				}
			});
		}
		catch (IOException e)
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
	private void chooseProjectToSubmit()
	{
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot()
		        .getProjects();
		ArrayList<IProject> filteredProjects = new ArrayList<IProject>();

		for (int i = 0; i < allProjects.length; i++)
		{
			if (allProjects[i].isOpen())
			{
				filteredProjects.add(allProjects[i]);
			}
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
		        getShell(), new WorkbenchLabelProvider());

		dialog.setElements(filteredProjects.toArray());
		dialog.setMessage(Messages.ChooseProjectDialog_Title);
		dialog.setMatchEmptyString(true);
		dialog.setMultipleSelection(false);

		if (project != null)
		{
			dialog.setInitialSelections(new IProject[] { project });
		}

		int result = dialog.open();
		if (result == Window.OK)
		{
			project = (IProject) dialog.getResult()[0];
			projectField.setText(project.getName());
		}

		updatePageComplete();
	}


	// ----------------------------------------------------------
	private void expandAllLocalGroups(SubmissionTarget obj,
			                          IRunnableContext context)
	{
		try
		{
			SubmissionTarget[] children = obj.getLogicalChildren();

			for (int i = 0; i < children.length; i++)
			{
				SubmissionTarget child = children[i];

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
		catch (SubmissionTargetException e)
		{
			// Do nothing.
		}
	}


	// ----------------------------------------------------------
	private void assignmentTreeSelectionChanged()
	{
		updatePageComplete();
	}


	// ----------------------------------------------------------
	public boolean canFlipToNextPage()
	{
		return isPageComplete();
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
		updatePartnerEnablement();

		if (project == null)
		{
			setErrorMessageIfInitialized(Messages.SubmitAssignmentPage_No_Project_Error);
			setPageComplete(false);
			return;
		}

		IStructuredSelection sel = (IStructuredSelection) assignmentTree
		        .getSelection();
		if (sel.isEmpty())
		{
			setPageComplete(false);
			setErrorMessageIfInitialized(Messages.SubmitAssignmentPage_No_Assignment_Error);
			return;
		}

		SubmissionTarget object = getSelectedAssignment();

		if (object == null || !object.isActionable())
		{
			setPageComplete(false);
			setErrorMessageIfInitialized(Messages.SubmitAssignmentPage_Not_an_Assignment_Error);
			return;
		}

		setPageComplete(true);
		setErrorMessage(null);
	}


	// ----------------------------------------------------------
	private void updatePartnerEnablement()
	{
		AssignmentTarget target = getSelectedAssignment();
		
		boolean usesPartners =
			(target != null && target.usesParameter("partners")); //$NON-NLS-1$

		partners.setEnabled(usesPartners);
		
		if (usesPartners)
		{
			partnerLabel.setText(Messages.SubmitAssignmentPage_Enter_Partners);
		}
		else
		{
			partnerLabel.setText(Messages.SubmitAssignmentPage_Partners_not_Supported);
		}
	}


	// ----------------------------------------------------------
	public void includeComposite(Composite composite, boolean include)
	{
		composite.setVisible(include);
		((GridData) composite.getLayoutData()).exclude = !include;

		getContainer().getShell().layout(true, true);
	}


	// ----------------------------------------------------------
	private void askForLoginCredentials()
	{
		AuthenticationDialog dialog = new AuthenticationDialog(
				getContainer().getShell());
		dialog.open();
	}


	// ----------------------------------------------------------
	private void selectLastSelectedAssignmentInTree()
	{
		String path = 
			Activator.getDefault().getLastSubmittedAssignmentPath();

		Tree tree = assignmentTree.getTree();
		TreeItem item = null;

		if (path != null)
		{
			String[] components = path.split("/\\$#\\$/"); //$NON-NLS-1$
			TreeItem[] children = tree.getItems();

			for (String component : components)
			{
				item = findTreeItemWithText(component, children);
				
				if (item == null)
				{
					return;
				}
				else
				{
					item.setExpanded(true);
					children = item.getItems();
				}
			}

			if (item != null)
			{
				tree.select(item);
			}
		}
	}


	// ----------------------------------------------------------
	private TreeItem findTreeItemWithText(String text, TreeItem[] items)
	{
		for (TreeItem item : items)
		{
			if (item.getText().equals(text))
			{
				return item;
			}
		}
		
		return null;
	}


	// ----------------------------------------------------------
	public void saveState()
	{
		Activator.getDefault().setLastEnteredPartners(
				partners.getText());

		Tree tree = assignmentTree.getTree();
		
		TreeItem[] selItems = tree.getSelection();
		
		if (selItems != null && selItems.length > 0)
		{
			TreeItem item = selItems[0];
			StringBuffer buffer = new StringBuffer();

			buffer.append(item.getText());

			while ((item = item.getParentItem()) != null)
			{
				buffer.insert(0, "/$#$/"); //$NON-NLS-1$
				buffer.insert(0, item.getText());
			}

			Activator.getDefault().setLastSubmittedAssignmentPath(
				buffer.toString());
		}
		else
		{
			Activator.getDefault().setLastSubmittedAssignmentPath(null);
		}
	}
}
