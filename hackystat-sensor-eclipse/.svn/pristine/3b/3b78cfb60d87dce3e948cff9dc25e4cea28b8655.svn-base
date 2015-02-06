package org.hackystat.sensor.eclipse;

import java.util.TimerTask;

import org.hackystat.sensorshell.SensorShellException;

/**
 * Implements Eclipse sensor timer task that can be executed by a timer. The timer task checks
 * whether the state of Eclipse buffer has been changed since its last invocation.
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
    }
    catch (SensorShellException e) {
      e.printStackTrace();
    }
  }
}
