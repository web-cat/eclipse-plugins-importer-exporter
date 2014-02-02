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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

//--------------------------------------------------------------------------
/**
 * The action that represents the "Submit..." menu item on popups for the
 * IProject resource type.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 */
public class SubmitAssignmentAction
	implements IWorkbenchWindowActionDelegate
{
	//~ Static/instance variables .............................................

	/* The workbench window to which this action belongs. */
	private IWorkbenchWindow window;

	/* The project that is currently selected in the workbench (in the
	   Navigator, Package Explorer, or similar view). */
	private IProject selectedProject;

	/* The project to which the file in the currently active workbench editor
	   belongs. */
	private IProject activeEditorProject;

	
	// ----------------------------------------------------------
	public void run(IAction action)
	{
		IProject projectToSubmit = null;

		if(selectedProject == activeEditorProject)
		{
			projectToSubmit = selectedProject;
		}
		else if(selectedProject != null && activeEditorProject == null)
		{
			projectToSubmit = selectedProject;
		}
		else if(selectedProject == null && activeEditorProject != null)
		{
			projectToSubmit = activeEditorProject;
		}
		else
		{
			// The current workspace selection (Package Explorer,
			// Navigator, etc.) is one project, but the active editor
			// contains a file in another project. Ask the user to
			// choose which of the two projects they want to submit.

			AmbiguousProjectToSubmitDialog dialog =
				AmbiguousProjectToSubmitDialog.createWithProjects(
						window.getShell(),
						selectedProject, activeEditorProject);

			int result = dialog.open();

			if(result == 0)
			{
				projectToSubmit = dialog.getSelectedProject();
			}
			else
			{
				return;
			}
		}

		SubmitAssignmentWizard wizard = new SubmitAssignmentWizard();
		wizard.init(window.getWorkbench(), null);
		wizard.setSelectedProject(projectToSubmit);
		
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}


	// ----------------------------------------------------------
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection)selection;
			Object obj = ss.getFirstElement();

			if (obj instanceof IAdaptable)
			{
				IAdaptable adapt = (IAdaptable)obj;
				selectedProject = (IProject)adapt
				        .getAdapter(IProject.class);
			}
		}

		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null)
		{
			IEditorPart activeEditor = activePage.getActiveEditor();

			if (activeEditor != null)
			{
				IEditorInput editorInput = activeEditor.getEditorInput();
				if (editorInput instanceof IFileEditorInput)
				{
					IFile file = ((IFileEditorInput) editorInput).getFile();
					activeEditorProject = file.getProject();
				}
			}
		}
	}


	// ----------------------------------------------------------
	public void dispose()
	{
		// Do nothing.
	}


	// ----------------------------------------------------------
	public void init(IWorkbenchWindow aWindow)
	{
		this.window = aWindow;
	}
}
