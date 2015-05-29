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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
 * @author Joseph Luke
 */
public class ProjectLinkPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private Text submitURL;
	private Text email;
	private Text username;
	private Text mailServer;
	private Text downloadURL;

	private boolean popupHappenedThisLoad = false;

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

		Group submissionGroup = new Group(composite, SWT.SHADOW_NONE);
		submissionGroup
				.setText(Messages.ProjectLinkPreferencePage_SubmissionTitle);
		FormData fd_submissionGroup = new FormData();
		fd_submissionGroup.left = new FormAttachment(0);
		fd_submissionGroup.top = new FormAttachment(0);
		fd_submissionGroup.right = new FormAttachment(100, -10);
		submissionGroup.setLayout(new FormLayout());
		submissionGroup.setLayoutData(fd_submissionGroup);

		// Submission description
		Label lblElectronicSubmissionDesc = new Label(submissionGroup, SWT.NONE);
		lblElectronicSubmissionDesc
				.setText(Messages.ProjectLinkPreferencePage_SubmissionDescription);
		FormData fd_lblElectronicSubmissionDesc = new FormData();
		fd_lblElectronicSubmissionDesc.left = new FormAttachment(0);
		fd_lblElectronicSubmissionDesc.top = new FormAttachment(0);
		lblElectronicSubmissionDesc
				.setLayoutData(fd_lblElectronicSubmissionDesc);

		// Submission label
		Label lblSubmitUrl = new Label(submissionGroup, SWT.NONE);
		lblSubmitUrl.setText(Messages.ProjectLinkPreferencePage_Submit_URL);
		FormData fd_lblSubmitUrl = new FormData();
		fd_lblSubmitUrl.left = new FormAttachment(0);
		lblSubmitUrl.setLayoutData(fd_lblSubmitUrl);

		// Submission field
		submitURL = new Text(submissionGroup, SWT.BORDER);
		fd_lblSubmitUrl.top = new FormAttachment(submitURL, 3, SWT.TOP);
		FormData fd_submitURL = new FormData();
		fd_submitURL.top = new FormAttachment(lblElectronicSubmissionDesc, 14);
		fd_submitURL.right = new FormAttachment(100, -10);
		submitURL.setLayoutData(fd_submitURL);

		// Email label
		Label lblEmail = new Label(submissionGroup, SWT.NONE);
		lblEmail.setText(Messages.ProjectLinkPreferencePage_Email);
		FormData fd_lblEmail = new FormData();
		fd_lblEmail.left = new FormAttachment(0);
		lblEmail.setLayoutData(fd_lblEmail);

		// Email field
		email = new Text(submissionGroup, SWT.BORDER);
		fd_lblEmail.top = new FormAttachment(email, 3, SWT.TOP);
		FormData fd_email = new FormData();
		fd_email.top = new FormAttachment(submitURL, 14);
		fd_email.right = new FormAttachment(100, -10);
		email.setLayoutData(fd_email);

		// Username label
		Label lblUsername = new Label(submissionGroup, SWT.NONE);
		lblUsername.setText(Messages.ProjectLinkPreferencePage_Username);
		FormData fd_lblUsername = new FormData();
		fd_lblUsername.left = new FormAttachment(0);
		lblUsername.setLayoutData(fd_lblUsername);

		// Username field
		username = new Text(submissionGroup, SWT.BORDER);
		fd_lblUsername.top = new FormAttachment(username, 3, SWT.TOP);
		FormData fd_username = new FormData();
		fd_username.top = new FormAttachment(email, 14);
		fd_username.right = new FormAttachment(100, -10);
		username.setLayoutData(fd_username);

		// Mail Server label
		Label lblMailServer = new Label(submissionGroup, SWT.NONE);
		lblMailServer.setText(Messages.ProjectLinkPreferencePage_MailServer);
		FormData fd_lblMailServer = new FormData();
		fd_lblMailServer.left = new FormAttachment(0);
		lblMailServer.setLayoutData(fd_lblMailServer);

		// Mail Server field
		mailServer = new Text(submissionGroup, SWT.BORDER);
		fd_lblMailServer.top = new FormAttachment(mailServer, 3, SWT.TOP);

		fd_submitURL.left = new FormAttachment(mailServer, 0, SWT.LEFT);
		fd_email.left = new FormAttachment(mailServer, 0, SWT.LEFT);
		fd_username.left = new FormAttachment(mailServer, 0, SWT.LEFT);

		FormData fd_mailServer = new FormData();
		fd_mailServer.top = new FormAttachment(username, 14);
		fd_mailServer.left = new FormAttachment(lblMailServer, 10);
		fd_mailServer.right = new FormAttachment(100, -10);
		mailServer.setLayoutData(fd_mailServer);

		Group downloadGroup = new Group(composite, SWT.SHADOW_NONE);
		downloadGroup.setText(Messages.ProjectLinkPreferencePage_DownloadTitle);
		FormData fd_downloadGroup = new FormData();
		fd_downloadGroup.top = new FormAttachment(submissionGroup, 20);
		fd_downloadGroup.left = new FormAttachment(0);
		fd_downloadGroup.right = new FormAttachment(100, -10);
		downloadGroup.setLayout(new FormLayout());
		downloadGroup.setLayoutData(fd_downloadGroup);

		// Download description
		Label lblDownloadDesc = new Label(downloadGroup, SWT.NONE);
		lblDownloadDesc
				.setText(Messages.ProjectLinkPreferencePage_DownloadDescription);
		FormData fd_lblDownloadDesc = new FormData();
		fd_lblDownloadDesc.left = new FormAttachment(0);
		fd_lblDownloadDesc.top = new FormAttachment(0);
		lblDownloadDesc.setLayoutData(fd_lblDownloadDesc);

		// Download URL label
		Label lblDownloadUrl = new Label(downloadGroup, SWT.NONE);
		lblDownloadUrl.setText(Messages.ProjectLinkPreferencePage_Download_URL);
		FormData fd_lblDownloadUrl = new FormData();
		fd_lblDownloadUrl.left = new FormAttachment(0);
		lblDownloadUrl.setLayoutData(fd_lblDownloadUrl);

		// Download URL field
		downloadURL = new Text(downloadGroup, SWT.BORDER);
		fd_lblDownloadUrl.top = new FormAttachment(downloadURL, 3, SWT.TOP);
		FormData fd_downloadURL = new FormData();
		fd_downloadURL.top = new FormAttachment(lblDownloadDesc, 14);
		fd_downloadURL.left = new FormAttachment(lblDownloadUrl, 10);
		fd_downloadURL.right = new FormAttachment(100, -10);
		downloadURL.setLayoutData(fd_downloadURL);

		setText(submitURL,
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
		// Validate each URL is not malformed (missing or incorrect protocol).

		// Validate downloadURL.
		validateURL(downloadURL, IPreferencesConstants.DOWNLOAD_URL);

		// Validate submitURL.
		validateURL(submitURL, IPreferencesConstants.SUBMIT_URL);

		getPreferenceStore().setValue(IPreferencesConstants.STORED_EMAIL,
				getText(email));
		getPreferenceStore().setValue(IPreferencesConstants.STORED_USERNAME,
				getText(username));

		validateURL(mailServer, IPreferencesConstants.OUTGOING_MAIL_SERVER);

		// getPreferenceStore().setValue(IPreferencesConstants.WEBCAT_URL,
		// getText(webCatURL));
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
		popupHappenedThisLoad = false;
		return super.performOk();
	}

	/**
	 * Validates the URL contained in the given text field and stores it into
	 * the given preference field if it is well-formed, otherwise shows a
	 * notification to the user (but only once per performOk()).
	 * 
	 * @param textField
	 *            The textField whose value we are validating.
	 * @param prefConstant
	 *            The preference store key to store the value into.
	 */
	private void validateURL(Text textField, String prefConstant) {
		IPreferenceStore store = getPreferenceStore();

		String validationText = getText(textField);
		if (!validationText.equals("")) {
			try {
				new URL(validationText);
				store.setValue(prefConstant, validationText);
			} catch (MalformedURLException e) {
				// Check to see if just adding http:// fixes it.
				try {
					new URL("http://" + validationText);
					textField.setText("http://" + validationText);
					store.setValue(prefConstant, "http://" + validationText);
				} catch (MalformedURLException e1) {
					// Notification just once for multiple malformed URLs.
					if (!popupHappenedThisLoad) {
						MessageDialog.openInformation(getShell(),
								"Malformed URL",
								"A URL you entered was malformed.");
						popupHappenedThisLoad = true;
					}
				}
			}
		}
	}
}
