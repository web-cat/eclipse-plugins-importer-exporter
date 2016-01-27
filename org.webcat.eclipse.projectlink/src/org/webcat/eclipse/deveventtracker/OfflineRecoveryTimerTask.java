/**
 * 
 */
package org.webcat.eclipse.deveventtracker;

import java.util.TimerTask;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.projectlink.Activator;

/**
 * @author ayaan
 *
 */
public class OfflineRecoveryTimerTask extends TimerTask {
	
	public OfflineRecoveryTimerTask() {
		super();
	}

	public void run() {
		try {
			EclipseSensor.POST_HAPPENING = true;
			EclipseSensor.getInstance().processOfflineRecovery();
			EclipseSensor.POST_HAPPENING = false;
		} catch (SensorShellException e) {
			Activator.getDefault().log("Could't get EclipseSensor instance", e);
		}
	}
}