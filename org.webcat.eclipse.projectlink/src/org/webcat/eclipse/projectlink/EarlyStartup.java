package org.webcat.eclipse.projectlink;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.webcat.eclipse.projectlink.i18n.Messages;
import org.webcat.eclipse.projectlink.preferences.IPreferencesConstants;

public class EarlyStartup implements IStartup {

	/**
	 * This gets called on Eclipse startup. Instantiates sensors and displays
	 * notification message if it hasn't been already.
	 */
	public void earlyStartup() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (!store.getBoolean(IPreferencesConstants.DATA_NOTIFICATION_SHOWN)) {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			workbench.getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = workbench
							.getActiveWorkbenchWindow();
					if (window != null) {
						MessageDialog.openInformation(window.getShell(),
								"Web-CAT Data Collection",
								Messages.Startup_Data_Collection_Notification);
					}
				}
			});
			store.setValue(IPreferencesConstants.DATA_NOTIFICATION_SHOWN, true);
		}
	}

}
