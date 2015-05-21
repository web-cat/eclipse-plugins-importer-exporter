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

package org.webcat.eclipse.projectlink.preferences;

import static org.webcat.eclipse.projectlink.util.SWTUtil.getText;
import static org.webcat.eclipse.projectlink.util.SWTUtil.setText;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.i18n.Messages;

//--------------------------------------------------------------------------
/**
 * The preference page used to edit settings for the electronic submission
 * plug-in.
 *
 * @author Tony Allevato
 */
public class ProjectLinkPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private Text submissionDefinitionURL;
	private Text email;
	private Text username;
	private Text mailServer;
	private Text downloadURL;

	// ~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the preference page.
	 */
	public ProjectLinkPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.ProjectLinkPreferencePage_Description);
	}

	// ~ Methods ...............................................................

	// ----------------------------------------------------------
	public void init(IWorkbench workbench) {
		// Does nothing; required by the IWorkbenchPreferencePage interface.
	}

	// ----------------------------------------------------------
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FormLayout());

		//Submission subtitle
		Label lblElectronicSubmissionSubtitle = new Label(composite, SWT.BOLD);
		lblElectronicSubmissionSubtitle.setText(Messages.ProjectLinkPreferencePage_SubmissionTitle);
		FormData fd_lblElectronicSubmissionSubtitle= new FormData();
		fd_lblElectronicSubmissionSubtitle.left = new FormAttachment(0);
		fd_lblElectronicSubmissionSubtitle.top = new FormAttachment(0, 10);
		lblElectronicSubmissionSubtitle.setLayoutData(fd_lblElectronicSubmissionSubtitle);
		
		//Submission description
		Label lblElectronicSubmissionDesc = new Label(composite, SWT.NONE);
		lblElectronicSubmissionDesc.setText(Messages.ProjectLinkPreferencePage_SubmissionDescription);
		FormData fd_lblElectronicSubmissionDesc= new FormData();
		fd_lblElectronicSubmissionDesc.left = new FormAttachment(0);
		fd_lblElectronicSubmissionDesc.top = new FormAttachment(lblElectronicSubmissionSubtitle, 14);
		lblElectronicSubmissionDesc.setLayoutData(fd_lblElectronicSubmissionDesc);
		
		//Submission label
		Label lblSubmitUrl = new Label(composite, SWT.NONE);
		lblSubmitUrl.setText(Messages.ProjectLinkPreferencePage_Submit_URL);
		FormData fd_lblSubmitUrl = new FormData();
		fd_lblSubmitUrl.left = new FormAttachment(0);
		lblSubmitUrl.setLayoutData(fd_lblSubmitUrl);

		//Submission field
		submissionDefinitionURL = new Text(composite, SWT.BORDER);
		fd_lblSubmitUrl.top = new FormAttachment(submissionDefinitionURL, 3,
				SWT.TOP);
		FormData fd_submitURL = new FormData();
		fd_submitURL.top = new FormAttachment(lblElectronicSubmissionDesc, 14);
		fd_submitURL.right = new FormAttachment(100, -10);
		submissionDefinitionURL.setLayoutData(fd_submitURL);

		//Email label
		Label lblEmail = new Label(composite, SWT.NONE);
		lblEmail.setText(Messages.ProjectLinkPreferencePage_Email);
		FormData fd_lblEmail = new FormData();
		fd_lblEmail.left = new FormAttachment(0);
		lblEmail.setLayoutData(fd_lblEmail);
		
		//Email field
		email = new Text(composite, SWT.BORDER);
		fd_lblEmail.top = new FormAttachment(email, 3,
				SWT.TOP);
		FormData fd_email = new FormData();
		fd_email.top = new FormAttachment(submissionDefinitionURL, 14);
		fd_email.right = new FormAttachment(100, -10);
		email.setLayoutData(fd_email);

		//Username label
		Label lblUsername = new Label(composite, SWT.NONE);
		lblUsername.setText(Messages.ProjectLinkPreferencePage_Username);
		FormData fd_lblUsername = new FormData();
		fd_lblUsername.left = new FormAttachment(0);
		lblUsername.setLayoutData(fd_lblUsername);
		
		//Username field
		username = new Text(composite, SWT.BORDER);
		fd_lblUsername.top = new FormAttachment(username, 3,
				SWT.TOP);
		FormData fd_username = new FormData();
		fd_username.top = new FormAttachment(email, 14);
		fd_username.right = new FormAttachment(100, -10);
		username.setLayoutData(fd_username);

		//Mail Server label
		Label lblMailServer = new Label(composite, SWT.NONE);
		lblMailServer.setText(Messages.ProjectLinkPreferencePage_MailServer);
		FormData fd_lblMailServer = new FormData();
		fd_lblMailServer.left = new FormAttachment(0);
		lblMailServer.setLayoutData(fd_lblMailServer);
		
		//Mail Server field
		mailServer = new Text(composite, SWT.BORDER);
		fd_lblMailServer.top = new FormAttachment(mailServer, 3,
				SWT.TOP);

		fd_submitURL.left = new FormAttachment(mailServer, 0, SWT.LEFT);
		fd_email.left = new FormAttachment(mailServer, 0, SWT.LEFT);
		fd_username.left = new FormAttachment(mailServer, 0, SWT.LEFT);
		
		FormData fd_mailServer = new FormData();
		fd_mailServer.top = new FormAttachment(username, 14);
		fd_mailServer.left = new FormAttachment(lblMailServer, 10);
		fd_mailServer.right = new FormAttachment(100, -10);
		mailServer.setLayoutData(fd_mailServer);
		
		
		//Download subtitle
		Label lblDownloadSubtitle = new Label(composite, SWT.BOLD);
		lblDownloadSubtitle.setText(Messages.ProjectLinkPreferencePage_DownloadTitle);
		FormData fd_lblDownloadSubtitle= new FormData();
		fd_lblDownloadSubtitle.left = new FormAttachment(0);
		fd_lblDownloadSubtitle.top = new FormAttachment(mailServer, 14);
		lblDownloadSubtitle.setLayoutData(fd_lblDownloadSubtitle);
		
		//Download description
		Label lblDownloadDesc = new Label(composite, SWT.NONE);
		lblDownloadDesc.setText(Messages.ProjectLinkPreferencePage_DownloadDescription);
		FormData fd_lblDownloadDesc= new FormData();
		fd_lblDownloadDesc.left = new FormAttachment(0);
		fd_lblDownloadDesc.top = new FormAttachment(lblDownloadSubtitle, 14);
		lblDownloadDesc.setLayoutData(fd_lblDownloadDesc);

		//Download URL label
		Label lblDownloadUrl = new Label(composite, SWT.NONE);
		lblDownloadUrl.setText(Messages.ProjectLinkPreferencePage_Download_URL);
		FormData fd_lblDownloadUrl = new FormData();
		fd_lblDownloadUrl.left = new FormAttachment(0);
		lblDownloadUrl.setLayoutData(fd_lblDownloadUrl);

		
		//Download URL field
		downloadURL = new Text(composite, SWT.BORDER);
		fd_lblDownloadUrl.top = new FormAttachment(downloadURL, 3, SWT.TOP);
		FormData fd_downloadURL = new FormData();
		fd_downloadURL.top = new FormAttachment(lblDownloadDesc, 14);
		fd_downloadURL.left = new FormAttachment(mailServer, 0, SWT.LEFT);
		fd_downloadURL.right = new FormAttachment(100, -10);
		downloadURL.setLayoutData(fd_downloadURL);

		setText(submissionDefinitionURL,
				getPreferenceStore()
						.getString(IPreferencesConstants.SUBMIT_URL));
		setText(email,
				getPreferenceStore().getString(
						IPreferencesConstants.STORED_EMAIL));
		setText(username,
				getPreferenceStore().getString(
						IPreferencesConstants.STORED_USERNAME));
		setText(mailServer,
				getPreferenceStore().getString(
						IPreferencesConstants.OUTGOING_MAIL_SERVER));

		setText(downloadURL,
				getPreferenceStore().getString(
						IPreferencesConstants.DOWNLOAD_URL));

		// String urlType = getPreferenceStore().getString(
		// IPreferencesConstants.URL_TYPE);

		/*
		 * if (IPreferencesConstants.URL_TYPE_WEBCAT.equals(urlType)) {
		 * useWebCAT.setSelection(true); } else {
		 * useSeparateURLs.setSelection(true); }
		 */

		return composite;
	}

	// ----------------------------------------------------------
	@Override
	public boolean performOk() {
		// getPreferenceStore().setValue(IPreferencesConstants.WEBCAT_URL,
		// getText(webCatURL));
		getPreferenceStore().setValue(IPreferencesConstants.DOWNLOAD_URL,
				getText(downloadURL));
		getPreferenceStore().setValue(IPreferencesConstants.SUBMIT_URL,
				getText(submissionDefinitionURL));

		// if (useSeparateURLs.getSelection())
		{
			getPreferenceStore().setValue(IPreferencesConstants.URL_TYPE,
					IPreferencesConstants.URL_TYPE_SEPARATE);
		}
		// else
		// {
		// getPreferenceStore().setValue(IPreferencesConstants.URL_TYPE,
		// IPreferencesConstants.URL_TYPE_WEBCAT);
		// }

		return super.performOk();
	}
}
