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

import org.webcat.eclipse.projectlink.Activator;

//--------------------------------------------------------------------------
/**
 * Constants that represent keys and values used in the Eclipse preferences
 * store.
 * 
 * @author Tony Allevato
 */
public interface IPreferencesConstants {
	// ----------------------------------------------------------
	/**
	 * Preferences store key for storing the Web-CAT URL.
	 */
	public static final String URL_TYPE = Activator.PLUGIN_ID
			+ ".preferences.urlType";

	// ----------------------------------------------------------
	public static final String URL_TYPE_WEBCAT = "webcat";

	// ----------------------------------------------------------
	public static final String URL_TYPE_SEPARATE = "separate";

	// ----------------------------------------------------------
	/**
	 * Preferences store key for storing the Web-CAT URL.
	 */
	public static final String WEBCAT_URL = Activator.PLUGIN_ID
			+ ".preferences.webcatURL";

	// ----------------------------------------------------------
	/**
	 * Preferences store key for storing the separate download URL.
	 */
	public static final String DOWNLOAD_URL = Activator.PLUGIN_ID
			+ ".preferences.downloadURL";

	// ----------------------------------------------------------
	/**
	 * Preferences store key for storing the separate submit URL.
	 */
	public static final String SUBMIT_URL = Activator.PLUGIN_ID
			+ ".preferences.submitURL";

	public static final String STORED_USERNAME = Activator.PLUGIN_ID
			+ ".preferences.username";

	public static final String STORED_EMAIL = Activator.PLUGIN_ID
			+ ".preferences.email";

	public static final String OUTGOING_MAIL_SERVER = Activator.PLUGIN_ID
			+ ".preferences.mail_server";

	public static final String IMPORTED_PREFERENCES = Activator.PLUGIN_ID
			+ ".preferences.imported_preferences";

	public static final String DOWNLOADED_PROJECTS = Activator.PLUGIN_ID
			+ ".preferences.downloaded_projects";

	// ----------------------------------------------------------
	/**
	 * Preferences store keys for DevEventTracker.
	 */
	public static final String STORED_USER_UUID = Activator.PLUGIN_ID
			+ ".preferences.user_uuid";

	public static final String OFFLINE_STORAGE_DIRECTORY = Activator.PLUGIN_ID
			+ ".preferences.offline_storage_dir";

	public static final String PUSH_TO_SERVER = Activator.PLUGIN_ID
			+ ".preferences.push_to_server";

	public static final String DATA_NOTIFICATION_SHOWN = Activator.PLUGIN_ID
			+ ".preferences.data_notification_shown";

}
