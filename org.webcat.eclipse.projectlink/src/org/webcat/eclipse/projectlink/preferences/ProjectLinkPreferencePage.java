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
 * @author  Tony Allevato
 */
public class ProjectLinkPreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage
{
	private Text downloadURL;
	private Text submitURL;


	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the preference page.
	 */
	public ProjectLinkPreferencePage()
	{
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.ProjectLinkPreferencePage_Description);
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	public void init(IWorkbench workbench)
	{
		// Does nothing; required by the IWorkbenchPreferencePage interface.
	}


	// ----------------------------------------------------------
	@Override
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FormLayout());
		
		Label lblDownloadUrl = new Label(composite, SWT.NONE);
		lblDownloadUrl.setText(Messages.ProjectLinkPreferencePage_Download_URL);
		FormData fd_lblDownloadUrl = new FormData();
		fd_lblDownloadUrl.left = new FormAttachment(0);
		lblDownloadUrl.setLayoutData(fd_lblDownloadUrl);
		
		downloadURL = new Text(composite, SWT.BORDER);
		fd_lblDownloadUrl.top = new FormAttachment(downloadURL, 3, SWT.TOP);
		FormData fd_downloadURL = new FormData();
		fd_downloadURL.left = new FormAttachment(lblDownloadUrl, 14);
		fd_downloadURL.right = new FormAttachment(100, -10);
		fd_downloadURL.top = new FormAttachment(0, 10);
		downloadURL.setLayoutData(fd_downloadURL);
		
		Label lblSubmitUrl = new Label(composite, SWT.NONE);
		lblSubmitUrl.setText(Messages.ProjectLinkPreferencePage_Submit_URL);
		FormData fd_lblSubmitUrl = new FormData();
		fd_lblSubmitUrl.left = new FormAttachment(lblDownloadUrl, 0, SWT.LEFT);
		lblSubmitUrl.setLayoutData(fd_lblSubmitUrl);
		
		submitURL = new Text(composite, SWT.BORDER);
		fd_lblSubmitUrl.top = new FormAttachment(submitURL, 3, SWT.TOP);
		FormData fd_submitURL = new FormData();
		fd_submitURL.top = new FormAttachment(downloadURL, 6);
		fd_submitURL.left = new FormAttachment(downloadURL, 0, SWT.LEFT);
		fd_submitURL.right = new FormAttachment(100, -10);
		submitURL.setLayoutData(fd_submitURL);
		setText(downloadURL, getPreferenceStore().getString(
				IPreferencesConstants.DOWNLOAD_URL));
		setText(submitURL, getPreferenceStore().getString(
				IPreferencesConstants.SUBMIT_URL));

		//String urlType = getPreferenceStore().getString(
		//		IPreferencesConstants.URL_TYPE);
		
		/*if (IPreferencesConstants.URL_TYPE_WEBCAT.equals(urlType))
		{
			useWebCAT.setSelection(true);
		}
		else
		{
			useSeparateURLs.setSelection(true);
		}*/

		return composite;
	}
	
	
	// ----------------------------------------------------------
	@Override
	public boolean performOk()
	{
		//getPreferenceStore().setValue(IPreferencesConstants.WEBCAT_URL,
		//		getText(webCatURL));
		getPreferenceStore().setValue(IPreferencesConstants.DOWNLOAD_URL,
				getText(downloadURL));
		getPreferenceStore().setValue(IPreferencesConstants.SUBMIT_URL,
				getText(submitURL));

		//if (useSeparateURLs.getSelection())
		{
			getPreferenceStore().setValue(IPreferencesConstants.URL_TYPE,
					IPreferencesConstants.URL_TYPE_SEPARATE);
		}
		//else
		//{
		//	getPreferenceStore().setValue(IPreferencesConstants.URL_TYPE,
		//			IPreferencesConstants.URL_TYPE_WEBCAT);
		//}

		return super.performOk();
	}
}
