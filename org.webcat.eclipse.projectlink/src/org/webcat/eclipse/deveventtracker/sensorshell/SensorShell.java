package org.webcat.eclipse.deveventtracker.sensorshell;

import java.io.File;
import java.util.Map;

import org.webcat.eclipse.deveventtracker.sensorbase.SensorData;


/**
 * Provides "middleware" for accumulating and sending notification (sensor)
 * data to Hackystat. SensorShell has two modes of interaction: command line and
 * programmatic. 
 * <p> 
 * Command line mode is entered by invoking the main() method, and
 * is intended to be used as a kind of subshell to which commands to add and
 * send notification data of various types can be sent. The SensorShell can be invoked
 * without any additional arguments as follows:
 * 
 * <pre>java -jar sensorshell.jar</pre>
 * 
 * Or you can invoke it with one, two, or three additional arguments:
 * 
 * <pre>java -jar sensorshell.jar [tool] [sensorshell.properties] [command file]</pre>
 * <p>
 * Programmatic mode involves creating an instance of SensorShell, retrieving the 
 * appropriate command instance (Ping, Add, etc.) and invoking the appropriate method.
 *
 * Imported from Hackystat project.
 *
 * @author    Philip M. Johnson
 */
public class SensorShell implements Shell {

  /** The underlying SingleSensorShell or MultiSensorShell. */
  private Shell shell = null;


/**
   * Constructs a new SensorShell instance that can be provided with
   * notification data to be sent eventually to a specific user key and host.
   * The toolName field in the log file name is set to "interactive" if the tool
   * is invoked interactively and "tool" if it is invoked programmatically.
   *
   * @param properties  The sensor properties instance for this run.
   * @param isInteractive     Whether this SensorShell is being interactively invoked or not.
   */
  public SensorShell(SensorShellProperties properties, boolean isInteractive) {
    this(properties, isInteractive, (isInteractive) ? "interactive" : "tool", null);
  }


  /**
   * Constructs a new SensorShell instance that can be provided with
   * notification data to be sent eventually to a specific user key and host.
   *
   * @param properties  The sensor properties instance for this run.
   * @param isInteractive     Whether this SensorShell is being interactively invoked or not.
   * @param tool          Indicates the invoking tool that is added to the log file name.
   */
  public SensorShell(SensorShellProperties properties, boolean isInteractive, String tool) {
    this(properties, isInteractive, tool, null);
  }


  /**
   * Constructs a new SensorShell instance that can be provided with
   * notification data to be sent eventually to a specific user key and host.
   *
   * @param properties   The sensor properties instance for this run.
   * @param isInteractive Whether this SensorShell is being interactively invoked or not.
   * @param toolName  The invoking tool that is added to the log file name.
   * @param commandFile A file containing shell commands, or null if none provided.
   */
  public SensorShell(SensorShellProperties properties, boolean isInteractive, String toolName,
      File commandFile) {
    if (properties.isMultiShellEnabled()) {
      // Note that isInteractive, commandFile are ignored if MultiSensorShell is specified.
      shell = new MultiSensorShell(properties, toolName);
    }
    else {
      shell = new SingleSensorShell(properties, isInteractive, toolName, commandFile);
    }
  }
  
  /** {@inheritDoc} */
  public void add(Map<String, String> keyValMap) throws SensorShellException {
    this.shell.add(keyValMap);
  }
  
  /** {@inheritDoc} */
  public void add(SensorData sensorData) throws SensorShellException {
    this.shell.add(sensorData);
  }
  
  /** {@inheritDoc} */
  public int send() throws SensorShellException {
    return this.shell.send();
  }
  
  /** {@inheritDoc} */
  public void quit() throws SensorShellException {
    this.shell.quit();
  }
  
  /** {@inheritDoc} */
  public boolean hasOfflineData() {
    return this.shell.hasOfflineData();
  }
  
  /** {@inheritDoc} */
  public boolean ping() {
    return this.shell.ping();
  }
  
  /** {@inheritDoc} */
  public SensorShellProperties getProperties() {
    return this.shell.getProperties();
  }
  
  /** {@inheritDoc} */
  public void statechange(long resourceCheckSum, Map<String, String> keyValMap) throws Exception {
    this.shell.statechange(resourceCheckSum, keyValMap);
  }
}

