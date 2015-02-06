package org.hackystat.sensor.eclipse;

import java.util.TimerTask;

import org.hackystat.sensorshell.SensorShellException;

/**
 * Implements Eclipse sensor timer task that can be executed by a timer. The timer task checks
 * buffer transition, which represents the event where a developer moves from one buffer
 * (containing a file) to another buffer (containing a different file).
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
      e.printStackTrace();
    }
  }
}
