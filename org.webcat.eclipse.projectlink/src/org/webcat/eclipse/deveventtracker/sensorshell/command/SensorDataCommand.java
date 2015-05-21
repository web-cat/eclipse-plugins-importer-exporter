package org.webcat.eclipse.deveventtracker.sensorshell.command;

import java.util.Map;
import java.util.logging.Level;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.webcat.eclipse.deveventtracker.sensorbase.Properties;
import org.webcat.eclipse.deveventtracker.sensorbase.Property;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClientException;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorData;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorDatas;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellProperties;
import org.webcat.eclipse.deveventtracker.sensorshell.SingleSensorShell;

/**
 * Implements the SensorData commands, of which there is "add", "send", and "statechange".
 * @author Philip Johnson
 */
public class SensorDataCommand extends Command { 
    
  /** The list of unsent SensorData instances. */
  private SensorDatas sensorDatas = new SensorDatas();
  /** The Ping Command. */
  private PingCommand pingCommand;
  /** The sensorbase client. */
  private SensorBaseClient client;
  /** Holds the Resource value from the last StateChange event. */
  private String lastStateChangeResource = "";
  /** Holds the bufferSize value from the last StateChange event. */
  private long lastStateChangeResourceCheckSum = 0;
  /** Holds the total number of sensor data sent to the server. */
  private long totalSent = 0;
  
  
  /**
   * Creates the SensorDataCommand. 
   * @param shell The sensorshell. 
   * @param properties The sensorproperties.
   * @param pingCommand The Ping Command. 
   * @param client The SensorBase client.
   */
  public SensorDataCommand(SingleSensorShell shell, SensorShellProperties properties, 
      PingCommand pingCommand, SensorBaseClient client) {
    super(shell, properties);
    this.pingCommand = pingCommand;
    this.client = client;
  }
  
  /**
   * Sends accumulated data, including offline and current data from the AddCommand.
   * If server not pingable, then the offline data is saved for a later attempt.
   * @return The number of sensor data instances that were sent. 
   * @throws SensorShellException If problems occur sending the data. 
   */
  public int send() throws SensorShellException {
    System.out.println("sending in sendcommand");
    System.out.println("with host: " + this.host);
    int numDataSent = 0;

    // Return right away if there is no data to send
    if (sensorDatas.getSensorData().isEmpty()) {
      return 0;
    }
    // Indicate we're sending if in interactive mode.
    if (!this.shell.isInteractive()) {
      this.shell.getLogger().info("#> send" + cr);
    }
    
    // Do a ping to see that we can connect to the server. 
    if (this.pingCommand.isPingable()) {
    	System.out.println("pingable");
      // We can connect, and there is data, so attempt to send.
      try {
        this.shell.println("Attempting to send " + sensorDatas.getSensorData().size() 
            + " sensor data instances. Available memory (bytes): " + getAvailableMemory());
        //long startTime = new Date().getTime(); 
        this.client.putSensorDataBatch(sensorDatas);
        //this.shell.println("Successful send to " + this.properties.getSensorBaseHost() +
        //    " Elapsed time: " + (new Date().getTime() - startTime) + " ms.");
        numDataSent = sensorDatas.getSensorData().size();
        totalSent += numDataSent;
        this.sensorDatas.getSensorData().clear();
        return numDataSent;
      }
      catch (SensorBaseClientException e) {
        this.shell.println("Error sending data: " + e);
        this.sensorDatas.getSensorData().clear();
        throw new SensorShellException("Could not send data: error in SensorBaseClient", e);
      }
    }
	System.out.println("not pingable");

    // If we got here, then the server was not available.
    if (this.properties.isOfflineCacheEnabled()) {
      this.shell.println("Server " + this.properties.getSensorBaseHost() + " not available." + 
          " Storing sensor data offline.");
      this.shell.getOfflineManager().store(this.sensorDatas);
      this.sensorDatas.getSensorData().clear();
      return 0;
    }
    else {
      String msg = "Server not available and offline storage disabled. Sensor Data lost.";
      this.shell.println(msg);
      this.sensorDatas.getSensorData().clear();
      throw new SensorShellException(msg);
    }
  }
  
  /**
   * Returns the data instance in a formatted string.
   * @param data The sensor data instance. 
   * @return A string displaying the instance.
   */
  private String formatSensorData(SensorData data) {
    StringBuffer buffer = new StringBuffer(75);
    buffer.append('<');
    buffer.append(data.getTimestamp());
    buffer.append(' ');
    buffer.append(data.getSensorDataType());
    buffer.append(' ');
    buffer.append(data.getTool());
    buffer.append(' ');
    buffer.append(data.getUri());
    buffer.append(' ');
    buffer.append(data.getRuntime());
    buffer.append(' ');
    for (Property property : data.getProperties().getProperty()) {
      buffer.append(property.getKey());
      buffer.append('=');
      buffer.append(property.getValue());
      buffer.append(' ');
    }
    buffer.append('>');
    return buffer.toString();
  }
 
  
  /**
   * Given a Map containing key-value pairs corresponding to SensorData fields and properties,
   * constructs a SensorData instance and stores it for subsequent sending to the SensorBase.
   * @param keyValMap The map of key-value pairs. 
   * @throws SensorShellException If problems occur sending the data.  
   */
  public void add(Map<String, String> keyValMap) throws SensorShellException {
    // Begin by creating the sensor data instance. 
    try {
    SensorData data = new SensorData();
    data.setProjectUri(getMap(keyValMap, "ProjectURI", ""));
    data.setUri(getMap(keyValMap, "URI", ""));
    data.setTimestamp(Long.parseLong(getMap(keyValMap, "Timestamp", "")));
    data.setRuntime(Long.parseLong(getMap(keyValMap, "Runtime", "")));
    data.setSensorDataType(getMap(keyValMap, "SensorDataType", ""));
    data.setTool(getMap(keyValMap, "Tool", ""));

    data.setProperties(new Properties());
    // Add all non-standard key-val pairs to the property list. 
    for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
      Property property = new Property();
      String key = entry.getKey();
      if (isProperty(key)) {
        property.setKey(key);
        property.setValue(entry.getValue());
        data.getProperties().getProperty().add(property);
      }
    }
    add(data);
    }
    catch (Exception e) {
      throw new SensorShellException("Error adding sensor data instance.", e);
    }
  }
  
  /**
   * Adds the SensorData instance, invoking send if the max buffer size has been exceeded.
   * @param data The SensorData instance to be added.
   * @throws SensorShellException If problems occur sending the data. 
   */
  public void add(SensorData data) throws SensorShellException {
    sensorDatas.getSensorData().add(data);
    if (this.shell.getLogger().isLoggable(Level.FINE)) {
      this.shell.println("Adding: " + formatSensorData(data));
    }
    // If that makes the buffer size too big, then send this data. 
    if (sensorDatas.getSensorData().size() > properties.getAutoSendMaxBuffer()) {
      this.shell.println("Invoking send(); buffer size > " + properties.getAutoSendMaxBuffer());
      try {
        this.send();
      }
      catch (SensorShellException e) {
        this.shell.println("Exception during send(): " + e);
      }
    }
  }
  
  /**
   * Provides an easy way for sensors to implement "StateChange" behavior.  From the sensor side,
   * StateChange can be implemented by creating a timer process that wakes up at regular 
   * intervals and gets the file (resource) that the current active buffer is attached to, plus
   * a "checksum" (typically the size of the resource in characters or bytes or whatever).  
   * Then, the timer process simply creates the
   * appropriate keyValMap as if for an 'add' event, and provides it to this method along with 
   * the buffer size.  This method will check the passed buffer size and the Resource field 
   * against the values for these two fields passed in the last call of this method, and if 
   * either one has changed, then the "add" method is called with the keyValMap.  
   * @param resourceCheckSum Indicates the state of the resource, typically via its size. 
   * @param keyValMap The map of key-value pairs. 
   * @throws Exception If problems occur while invoking the 'add' command. 
   */
  public void statechange(long resourceCheckSum, Map<String, String> keyValMap) throws Exception {
    // Get the Resource attribute, default it to "" if not found.
    String resource = (keyValMap.get("URI") == null) ? "" : keyValMap.get("URI");
    // Do an add if the resource or buffer size has changed.
    if (!this.lastStateChangeResource.equals(resource)) { //NOPMD
      this.shell.println("Invoking add: Resource has changed to: " + resource);
      this.add(keyValMap);
    }
    else if (this.lastStateChangeResourceCheckSum != resourceCheckSum) { //NOPMD
      this.shell.println("Invoking add: CheckSum has changed to: " + resourceCheckSum);
      this.add(keyValMap);
    }
    else {
      this.shell.println("No change in resource: " + resource + ", checksum: " + resourceCheckSum);
    }
    // Always update the 'last' values.
    this.lastStateChangeResourceCheckSum = resourceCheckSum;
    this.lastStateChangeResource = resource;
  }

  /**
   * Returns the value associated with key in keyValMap, or the default if the key is not present.
   * @param keyValMap The map
   * @param key The key
   * @param defaultValue The value to return if the key has no mapping.
   * @return The value to be used.
   */
  private String getMap(Map<String, String> keyValMap, String key, String defaultValue) {
    return (keyValMap.get(key) == null) ? defaultValue : keyValMap.get(key);
  }
  
  /**
   * Returns true if the passed key is not one of the standard Sensor Data fields including
   * "Timestamp", "Runtime", "Tool", "Resource", "Owner", or "SensorDataType".
   * @param key The key.
   * @return True if the passed key indicates a property, not a standard sensor data field.
   */
  private boolean isProperty(String key) {
    return 
        (!"Timestamp".equals(key)) &&
        (!"Runtime".equals(key)) &&
        (!"Tool".equals(key)) &&
        (!"URI".equals(key)) &&
        (!"ProjectURI".equals(key)) &&
        (!"SensorDataType".equals(key));
  }

  /**
   * Returns the total number of sensor data instances sent so far. 
   * @return The total sent so far by this instance of SensorDataCommand. 
   */
  public long getTotalSent() {
    return this.totalSent;
  }
  
  /**
   * Returns true if this SensorDataCommand instance has remaining unsent data. 
   * @return True if there is data remaining to be sent. 
   */
  public boolean hasUnsentData() {
    return ((this.sensorDatas.getSensorData() != null) &&
        !this.sensorDatas.getSensorData().isEmpty());
  }
  
  /**
   * Helper method to return the available memory in bytes.
   * @return The available memory.
   */
  private long getAvailableMemory() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();
    long allocatedMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    return freeMemory + (maxMemory - allocatedMemory);
  }

  public void commitSnapshot(String projectUri, Git git)
  {
	  client.commitSnapshot(projectUri, git);
  }
}
