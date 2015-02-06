package org.hackystat.sensor.eclipse;

import org.junit.Assert;
import org.junit.Test;

/**
 * A dummy test for the Eclipse Sensor.
 * 
 * @author hongbing
 *
 */
public class TestEclipseSensor {
  /**
   * Test the sensor initializations. 
   * 
   */
  @Test public void testSingletonSensor() {
    Assert.assertTrue("Singletonness of Eclipse sensor.", true);
  }
}
