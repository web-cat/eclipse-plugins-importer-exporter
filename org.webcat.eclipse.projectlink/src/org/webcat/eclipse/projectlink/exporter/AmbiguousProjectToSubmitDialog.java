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

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.webcat.eclipse.projectlink.i18n.Messages;

//--------------------------------------------------------------------------
/**
 * A dialog used to allow the user to choose which project should be submitted
 * in the submission wizard if the selection in the Navigator differs from the
 * project that owns the file in the currently active editor.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 */
public class AmbiguousProjectToSubmitDialog extends MessageDialog
{
	//~ Static/instance variables .............................................

	/* The first project that is selected (the one in the Navigator). */
	private IProject project1;

	/* The second project that is selected (the one owning the active
	   editor). */
	private IProject project2;

	/* The project that the user has selected in the dialog. */
	private IProject selectedProject;

	
	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the dialog. This method is protected because
	 * pre-processing must occur before the superclass constructor can be
	 * called. Use the static createWithProjects method to create a new dialog
	 * instead.
	 * 
	 * @param parentShell the shell that owns this dialog
	 * @param dialogMessage the message to display in the dialog
	 * @param project1 a project
	 * @param project2 a project
	 */
	protected AmbiguousProjectToSubmitDialog(Shell parentShell,
	        String dialogMessage, IProject project1, IProject project2)
	{
		super(parentShell, Messages.AmbiguousProjectToSubmitDialog_Title, null,
		        dialogMessage, MessageDialog.INFORMATION, new String[] {
		                Messages.AmbiguousProjectToSubmitDialog_OK,
		                Messages.AmbiguousProjectToSubmitDialog_Cancel }, 0);

		this.project1 = project1;
		this.project2 = project2;
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the AmbiguousProjectToSubmitDialog class.
	 * 
	 * @param parentShell the Shell that will own the dialog box
	 * @param project1 the first project that is selected
	 * @param project2 the second project that is selected
	 * @return an instance of AmbiguousProjectToSubmitDialog
	 */
	public static AmbiguousProjectToSubmitDialog createWithProjects(
	        Shell parentShell, IProject project1, IProject project2)
	{
		String message = MessageFormat.format(
		        Messages.AmbiguousProjectToSubmitDialog_Message,
		        project1.getName(), project2.getName());

		return new AmbiguousProjectToSubmitDialog(parentShell, message,
		        project1, project2);
	}


	// ----------------------------------------------------------
	/**
	 * Creates the radio buttons in the dialog box to allow the user to select
	 * which project should be submitted.
	 */
	@Override
	protected Control createCustomArea(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		Button radio1 = new Button(container, SWT.RADIO);
		radio1.setText(MessageFormat.format(
		        Messages.AmbiguousProjectToSubmitDialog_Option_1, project1.getName()));
		radio1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				selectedProject = project1;
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});

		Button radio2 = new Button(container, SWT.RADIO);
		radio2.setText(MessageFormat.format(
		        Messages.AmbiguousProjectToSubmitDialog_Option_2, project2.getName()));
		radio2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				selectedProject = project2;
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});
		
		return container;
	}
	
	
	// ----------------------------------------------------------
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}


	// ----------------------------------------------------------
	/**
	 * Gets the project that was selected by the user after the dialog is
	 * dismissed.
	 * 
	 * @return the project that was selected by the user
	 */
	public IProject getSelectedProject()
	{
		return selectedProject;
	}
}
