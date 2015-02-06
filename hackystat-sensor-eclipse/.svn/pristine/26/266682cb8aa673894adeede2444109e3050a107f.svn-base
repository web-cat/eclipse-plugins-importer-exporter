package org.hackystat.sensor.eclipse;

import org.eclipse.ui.IStartup;
import org.hackystat.sensorshell.SensorShellException;

/**
 * This is a helper class to start Eclipse Sensor Plugin at startup time. This class stood out 
 * because EclipseSensorPlugin can't serve as the startup; otherwise, two instances (bundle) will 
 * be created. 
 * 
 * @author Hongbing Kou
 *
 */
public class SensorStartup implements IStartup {
  /**
   * Instantiates EclipseSensor class so that the collection for necessary data is ready. Note that
   * this is called when workbench starts up. This method must be overridden due to IStartup
   * interface. Because of this method, this class is instantiated on startup.
   *
   * @see IStartup
   */
  public void earlyStartup() {
    //To initialize the sensor.
    try {
      EclipseSensor.getInstance();
    }
    catch (SensorShellException e) {
      e.printStackTrace();
    }
  }

}
