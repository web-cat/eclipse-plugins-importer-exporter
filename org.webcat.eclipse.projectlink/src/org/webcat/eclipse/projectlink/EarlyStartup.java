package org.webcat.eclipse.projectlink;

import org.eclipse.ui.IStartup;

public class EarlyStartup implements IStartup{

	/**
	 * This gets called on Eclipse startup. Use it to instantiate sensors.
	 */
	public void earlyStartup() {
		System.out.println("Early startup happening.");
		Activator.getDefault();
	}

}
