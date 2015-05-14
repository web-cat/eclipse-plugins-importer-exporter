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

package org.webcat.eclipse.projectlink.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.i18n.Messages;

import static org.webcat.eclipse.projectlink.util.SWTUtil.*;

//--------------------------------------------------------------------------
/**
 * A dialog that asks the user to enter their username and password.
 * 
 * @author Tony Allevato
 */
public class AuthenticationDialog extends TitleAreaDialog
{
	//~ Instance/static variables .............................................

	private Text username;
	private Text password;
	private Button rememberPassword;

	private boolean remembersPassword;


	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AuthenticationDialog(Shell parentShell)
	{
		super(parentShell);
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	public boolean remembersPassword()
	{
		return remembersPassword;
	}


	// ----------------------------------------------------------
	/**
	 * Create contents of the dialog.
	 *
	 * @param parent the parent
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		setTitle(Messages.AuthenticationDialog_Title);
		setMessage(Messages.AuthenticationDialog_Message);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.horizontalSpacing = 15;
		gl_container.marginHeight = 15;
		gl_container.marginWidth = 15;
		gl_container.verticalSpacing = 10;
		container.setLayout(gl_container);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblUsername = new Label(container, SWT.NONE);
		lblUsername.setText(Messages.AuthenticationDialog_Username);

		username = new Text(container, SWT.BORDER);
		GridData gd_username = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_username.widthHint = 200;
		username.setLayoutData(gd_username);

		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText(Messages.AuthenticationDialog_Password);

		password = new Text(container, SWT.BORDER | SWT.PASSWORD);
		GridData gd_password = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_password.widthHint = 200;
		password.setLayoutData(gd_password);

		rememberPassword = new Button(container, SWT.CHECK);
		rememberPassword.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		rememberPassword.setText(Messages.AuthenticationDialog_Remember_Password);

		setText(username, Activator.getDefault().getStoredUsername());
		setText(password, Activator.getDefault().getLastEnteredPassword());
		rememberPassword.setSelection(remembersPassword);

		if (Activator.getDefault().getStoredUsername().length() > 0)
		{
			password.setFocus();
			password.selectAll();
		}

		return area;
	}


	// ----------------------------------------------------------
	/**
	 * Create contents of the button bar.
	 *
	 * @param parent the parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}


	// ----------------------------------------------------------
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize()
	{
		return new Point(482, 246);
	}


	// ----------------------------------------------------------
	@Override
	protected void okPressed()
	{
		Activator.getDefault().setStoredUsername(getText(username));
		Activator.getDefault().setLastEnteredPassword(getText(password));
		remembersPassword = rememberPassword.getSelection();
		

		super.okPressed();
	}
}
