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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.preferences.IPreferencesConstants;

//--------------------------------------------------------------------------
/**
 * Tracks the projects that have been downloaded (so they aren't redownloaded if
 * multiple projects have it as a dependency, for example).
 * 
 * @author Ellen Boyd
 */
public class ProjectTracker {
	private static ProjectTracker instance;

	private Properties history;

	// ----------------------------------------------------------
	private ProjectTracker() {
		history = new Properties();
		loadPluginPreferences();
	}

	// ----------------------------------------------------------
	public synchronized static ProjectTracker getInstance() {
		if (instance == null) {
			instance = new ProjectTracker();
		}

		return instance;
	}

	// ----------------------------------------------------------
	public String projectNameForUri(String uri) {
		return history.getProperty(uri);
	}

	// ----------------------------------------------------------
	public void setProjectNameForUri(String uri, String projectName) {
		history.setProperty(uri, projectName);
		updatePluginPreferences();
	}

	// ----------------------------------------------------------
	private void loadPluginPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();

		String historyString = prefs
				.getString(IPreferencesConstants.DOWNLOADED_PROJECTS);

		if (historyString.length() > 0) {
			StringReader reader = new StringReader(historyString);
			history = new Properties();

			try {
				history.load(reader);
			} catch (IOException e) {
				Activator.getDefault().log(e);
			}
		}
	}

	// ----------------------------------------------------------
	private void updatePluginPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();

		try {
			StringWriter writer = new StringWriter();
			history.store(writer, null);

			prefs.setValue(IPreferencesConstants.DOWNLOADED_PROJECTS,
					writer.toString());
		} catch (IOException e) {
			Activator.getDefault().log(e);
		}
	}
}
