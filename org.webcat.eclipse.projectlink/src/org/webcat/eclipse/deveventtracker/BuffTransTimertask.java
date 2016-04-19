package org.webcat.eclipse.deveventtracker;

import java.util.TimerTask;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.projectlink.Activator;


/**
 * Implements Eclipse sensor timer task that can be executed by a timer. The timer task checks
 * buffer transition, which represents the event where a developer moves from one buffer
 * (containing a file) to another buffer (containing a different file).
 *
 * Imported from Hackystat project.
 *
 * @author Hongbing Kou
 */
public class BuffTransTimertask extends TimerTask {
  /**
   * Processes the state change activity and computes file metrics in a time based interval.
   */
  public void run() {
    try {
      EclipseSensor sensor = EclipseSensor.getInstance();
      // process buffer transactions.
      sensor.processBuffTrans();
    }
    catch (SensorShellException e) {
      Activator.getDefault().log("Couldn't get EclipseSensor instance.", e);
    } catch (Exception e) {
    	Activator.getDefault().log(e);
    }
  }
}
