package org.webcat.eclipse.deveventtracker.sensorshell.command;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellProperties;
import org.webcat.eclipse.deveventtracker.sensorshell.SingleSensorShell;

/**
 * Implements the Ping command, which ensures that the SensorBase is reachable. 
 * @author Philip Johnson
 */
public class PingCommand extends Command {
  
  /** The timeout in milliseconds, initialized from sensorshell.properties.*/
  private int timeout;
  
  /**
   * Creates the PingCommand. 
   * @param shell The sensorshell. 
   * @param properties The sensorproperties. 
   */
  public PingCommand(SingleSensorShell shell, SensorShellProperties properties) {
    super(shell, properties);
    this.timeout = properties.getPingTimeout() * 1000;
  }
  
  /**
   * Does a ping on the hackystat server and returns true if the server was
   * accessible. A ping-able server indicates the data will be sent to it,
   * while a non-pingable server indicates that data will be stored offline.
   *
   * @return   True if the server could be pinged.
   */
  public boolean isPingable() {
    return this.isPingable(this.timeout);
  }

  /**
   * Does a ping on the hackystat server and returns true if the server was
   * accessible. A ping-able server indicates the data will be sent to it,
   * while a non-pingable server indicates that data will be stored offline.
   * If the server is not reachable, or does not respond with given time frame, false will
   * be returned.
   *
   * @param timeout Maximum seconds to wait for server response. A 0 value or negative
   * value is equivalent to set time out to infinity.
   * @return   True if the server could be pinged.
   */
  public boolean isPingable(int timeout) {
    try {
		InetAddress inet = InetAddress.getByName(this.host);
		return inet.isReachable(timeout);
	} catch (UnknownHostException e) {
		e.printStackTrace();
		return false;
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
  }
}
