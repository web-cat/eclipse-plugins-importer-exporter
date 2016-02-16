/**
 * 
 */
package org.webcat.eclipse.deveventtracker;

import java.util.TimerTask;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.projectlink.Activator;

/**
 * Implements Eclipse sensor timer task that can be executed by a timer. The
 * timer task attempts to recover offline data (if any) and send it to the server.
 * 
 * @author Ayaan Kazerouni
 *
 */
public class PostToServerTimerTask extends TimerTask {
	
	public PostToServerTimerTask() {
		super();
	}

	/**
	 * Attempts to recover offline data and sent it to the server.
	 */
	public void run() {
		
		// Wrap run in a catch-call to avoid Timer cancellations.
		try {
			if (!EclipseSensor.POST_HAPPENING) {
				EclipseSensor.POST_HAPPENING = true;
				EclipseSensor.getInstance().processOfflineRecovery();
				EclipseSensor.POST_HAPPENING = false;
			}
		} catch (SensorShellException e) {
			Activator.getDefault().log("Could't get EclipseSensor instance", e);
		} catch (Exception e) {
			Activator.getDefault().log(e);
		}
	}
}