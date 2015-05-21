/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

package org.webcat.eclipse.projectlink.i18n;

import org.eclipse.osgi.util.NLS;

//--------------------------------------------------------------------------
/**
 * The message bundle used by the submitter.ui plug-in.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
@SuppressWarnings("all")
public class Messages extends NLS
{
	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Prevent instantiation.
	 */
	private Messages()
	{
		// Prevent instantiation.
	}


	//~ Constants .............................................................

	public static String AmbiguousProjectToSubmitDialog_Cancel;
	public static String AmbiguousProjectToSubmitDialog_Message;
	public static String AmbiguousProjectToSubmitDialog_OK;
	public static String AmbiguousProjectToSubmitDialog_Option_1;
	public static String AmbiguousProjectToSubmitDialog_Option_2;
	public static String AmbiguousProjectToSubmitDialog_Title;
	public static String AuthenticationDialog_Message;
	public static String AuthenticationDialog_Password;
	public static String AuthenticationDialog_Remember_Password;
	public static String AuthenticationDialog_Title;
	public static String AuthenticationDialog_Username;
	public static String ChooseProjectDialog_Title;
	public static String DownloadAssignmentPage_Description;
	public static String DownloadAssignmentPage_Group_Has_No_Assignments;
	public static String DownloadAssignmentPage_Projects_to_Download;
	public static String DownloadAssignmentPage_Select_Assignment;
	public static String DownloadAssignmentPage_Select_Assignment_or_Group;
	public static String DownloadAssignmentPage_Title;
	public static String DownloadAssignmentPage_No_URL_Title;
	public static String DownloadAssignmentPage_No_URL_Description;
	public static String DownloadAssignmentWizard_Download_Error_Description;
	public static String DownloadAssignmentWizard_Download_Errors_Title;
	public static String DownloadAssignmentWizard_Project_Error;
	public static String ExceptionDialog_General_Error;
	public static String ExceptionDialog_Parse_Errors;
	public static String ExceptionDialog_Title;
	public static String ProjectLinkPreferencePage_Description;
	public static String ProjectLinkPreferencePage_SubmissionTitle;
	public static String ProjectLinkPreferencePage_DownloadTitle;
	public static String ProjectLinkPreferencePage_SubmissionDescription;
	public static String ProjectLinkPreferencePage_DownloadDescription;
	public static String ProjectLinkPreferencePage_Download_URL;
	public static String ProjectLinkPreferencePage_Submit_URL;
	public static String ProjectLinkPreferencePage_Use_Separate_URLs;
	public static String ProjectLinkPreferencePage_Use_WebCAT;
	public static String ProjectLinkPreferencePage_WebCAT_URL;
	public static String ProjectLinkPreferencePage_Email;
	public static String ProjectLinkPreferencePage_Username;
	public static String ProjectLinkPreferencePage_MailServer;
	public static String SubmitAssignmentPage_ChangeUsernamePassword;
	public static String SubmitAssignmentPage_Enter_Partners;
	public static String SubmitAssignmentPage_EnterPartnerNames;
	public static String SubmitAssignmentPage_ChooseProject;
	public static String SubmitAssignmentPage_Currently_Logged_in_as;
	public static String SubmitAssignmentPage_No_Assignment_Error;
	public static String SubmitAssignmentPage_No_Project_Error;
	public static String SubmitAssignmentPage_Not_an_Assignment_Error;
	public static String SubmitAssignmentPage_SelectAssignment;
	public static String SubmitAssignmentPage_Page_Description;
	public static String SubmitAssignmentPage_Page_Title;
	public static String SubmitAssignmentPage_Partners_not_Supported;
	public static String SubmitAssignmentPage_Project;
	public static String SubmitAssignmentPage_No_URL_Title;
	public static String SubmitAssignmentPage_No_URL_Description;


	//~ Static/instance variables .............................................

	private static final String BUNDLE_NAME =
		"org.webcat.eclipse.projectlink.i18n.messages"; //$NON-NLS-1$

	static
	{
		// Initialize the strings.

		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
