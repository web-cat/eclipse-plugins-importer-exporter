package org.webcat.eclipse.deveventtracker.sensorshell.command;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellProperties;
import org.webcat.eclipse.deveventtracker.sensorshell.SingleSensorShell;

/**
 * A class providing access to information useful for all Command instances. 
 * 
 * Imported from Hackystat project.
 * 
 * @author Philip Johnson
 */
public class Command {
  
  /** The sensorshell. */
  protected SingleSensorShell shell;
  /** The SensorProperties. */
  protected SensorShellProperties properties;
  /** The line separator. */
  protected String cr = System.getProperty("line.separator");
  
  /**
   * Constructs a Command instance.  Only subclasses call this method. 
   * @param shell The sensorshell. 
   * @param properties The properties. 
   */
  public Command(SingleSensorShell shell, SensorShellProperties properties) {
    this.shell = shell;
    this.properties = properties;
  }
}
