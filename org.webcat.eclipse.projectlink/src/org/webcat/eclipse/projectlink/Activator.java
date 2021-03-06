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

package org.webcat.eclipse.projectlink;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.webcat.eclipse.projectlink.preferences.IPreferencesConstants;

//--------------------------------------------------------------------------
/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Ellen Boyd, Tony Allevato
 */
public class Activator extends AbstractUIPlugin
{
	// The plug-in ID
	public static final String PLUGIN_ID =
			"org.webcat.eclipse.projectlink"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private String lastEnteredPartners = "";
	private String lastEnteredPassword;
	private String lastSubmittedAssignmentPath;


	// ----------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}


	// ----------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}


	// ----------------------------------------------------------
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return plugin;
	}

	
	// ----------------------------------------------------------
	/**
	 * Gets an image descriptor for the specified image in the plug-in's
	 * "icons" directory.
	 * 
	 * @param path the path to the icon that should be loaded, relative to
	 *     the "icons" folder in the plug-in
	 * @return an ImageDescriptor for the image
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		try
		{
			URL base = Platform.getBundle(PLUGIN_ID).getEntry(
					"/icons/"); //$NON-NLS-1$
			URL url = new URL(base, path);

			return ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e)
		{
			// Do nothing.
		}

		return null;
	}
	
	
	// ----------------------------------------------------------
	public String getDownloadURL()
	{
		IPreferenceStore prefs = getPreferenceStore();
		String urlType = prefs.getString(IPreferencesConstants.URL_TYPE);

		if (IPreferencesConstants.URL_TYPE_SEPARATE.equals(urlType))
		{
			return prefs.getString(IPreferencesConstants.DOWNLOAD_URL);
		}
		else if (IPreferencesConstants.URL_TYPE_WEBCAT.equals(urlType))
		{
			// FIXME
			if (!prefs.isDefault(IPreferencesConstants.WEBCAT_URL))
			{
				return prefs.getString(IPreferencesConstants.WEBCAT_URL)
						+ "/FIXME";
			}
			else
			{
				return "";
			}
		}
		else
		{
			return "";
		}
	}
	
	
	// ----------------------------------------------------------
	public String getSubmitURL()
	{
		IPreferenceStore prefs = getPreferenceStore();
		String urlType = prefs.getString(IPreferencesConstants.URL_TYPE);

		if (IPreferencesConstants.URL_TYPE_SEPARATE.equals(urlType))
		{
			return prefs.getString(IPreferencesConstants.SUBMIT_URL);
		}
		else if (IPreferencesConstants.URL_TYPE_WEBCAT.equals(urlType))
		{
			// FIXME
			if (!prefs.isDefault(IPreferencesConstants.WEBCAT_URL))
			{
				return prefs.getString(IPreferencesConstants.WEBCAT_URL)
						+ "/FIXME";
			}
			else
			{
				return "";
			}
		}
		else
		{
			return "";
		}
	}


	// ----------------------------------------------------------
	public String getStoredUsername()
	{
		return getPreferenceStore().getString(
				IPreferencesConstants.STORED_USERNAME);
	}


	// ----------------------------------------------------------
	public void setStoredUsername(String username)
	{
		getPreferenceStore().setValue(
				IPreferencesConstants.STORED_USERNAME, username);
	}


	// ----------------------------------------------------------
	public String getLastEnteredPassword()
	{
		return lastEnteredPassword;
	}


	// ----------------------------------------------------------
	public void setLastEnteredPassword(String password)
	{
		lastEnteredPassword = password;
	}


	// ----------------------------------------------------------
	public void clearCredentials()
	{
		setStoredUsername(null);
		setLastEnteredPassword(null);
	}


	// ----------------------------------------------------------
	public boolean hasFullCredentials()
	{
		return getStoredUsername().length() > 0
				&& getLastEnteredPassword() != null;
	}


	// ----------------------------------------------------------
	/**
	 * Gets the most recently entered partner usernames in the submission
	 * wizard.
	 * 
	 * @return the most recently entered partner usernames
	 */
	public String getLastEnteredPartners()
	{
		return lastEnteredPartners;
	}


	// ----------------------------------------------------------
	/**
	 * Sets the most recently entered partner usernames in the submission
	 * wizard.
	 * 
	 * @param partners the most recently entered partner usernames
	 */
	public void setLastEnteredPartners(String partners)
	{
		lastEnteredPartners = partners;
	}


	// ----------------------------------------------------------
	/**
	 * Gets the path to the most recently selected assignment in the
	 * submission wizard.
	 * 
	 * @return the path to the most recently selected assignment
	 */
	public String getLastSubmittedAssignmentPath()
	{
		return lastSubmittedAssignmentPath;
	}


	// ----------------------------------------------------------
	/**
	 * Sets the path to the most recently selected assignment in the
	 * submission wizard.
	 * 
	 * @param path the path to the most recently selected assignment
	 */
	public void setLastSubmittedAssignmentPath(String path)
	{
		lastSubmittedAssignmentPath = path;
	}
}
