package org.webcat.eclipse.deveventtracker;

import java.util.TimerTask;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.projectlink.Activator;

/**
 * Implements Eclipse sensor timer task that can be executed by a timer. The
 * timer task checks whether the state of Eclipse buffer has been changed since
 * its last invocation.
 * 
 * Imported from Hackystat project.
 * 
 * @author Hongbing Kou
 */
public class StateChangeTimerTask extends TimerTask {
	/**
	 * Processes the state change activity in a time based interval.
	 */
	public void run() {

		try {
			EclipseSensor sensor = EclipseSensor.getInstance();
			sensor.processStateChangeActivity();
		} catch (SensorShellException e) {
			Activator.getDefault().log(e);
		}
	}
}
