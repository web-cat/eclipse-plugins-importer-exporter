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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

//--------------------------------------------------------------------------
/**
 * An Eclipse menu action that presents the user with the Download Assignment
 * wizard.
 * 
 * @author Ellen Boyd, Tony Allevato
 */
public class DownloadAssignmentAction
	implements IWorkbenchWindowActionDelegate
{
	//~ Static/instance variables .............................................

	/* The workbench window to which this action belongs. */
	private IWorkbenchWindow window;


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	public void run(IAction action)
	{
		DownloadAssignmentWizard wizard = new DownloadAssignmentWizard();
		wizard.init(window.getWorkbench(), null);
		
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}


	// ----------------------------------------------------------
	public void selectionChanged(IAction action, ISelection selection)
	{
		// Do nothing.
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
