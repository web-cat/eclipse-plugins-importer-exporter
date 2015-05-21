package org.webcat.eclipse.deveventtracker.sensorshell.command;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellProperties;
import org.webcat.eclipse.deveventtracker.sensorshell.SingleSensorShell;

/**
 * Implements the Ping command, which ensures that the SensorBase is reachable.
 * 
 * @author Philip Johnson
 */
public class PingCommand extends Command {

	/** The timeout in milliseconds, initialized from sensorshell.properties. */
	private int timeout;

	/**
	 * Creates the PingCommand.
	 * 
	 * @param shell
	 *            The sensorshell.
	 * @param properties
	 *            The sensorproperties.
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
	 * @return True if the server could be pinged.
	 */
	public boolean isPingable() {
		return this.isPingable(this.timeout);
	}

	/**
	 * Does a ping on the hackystat server and returns true if the server was
	 * accessible. A ping-able server indicates the data will be sent to it,
	 * while a non-pingable server indicates that data will be stored offline.
	 * If the server is not reachable, or does not respond with given time
	 * frame, false will be returned.
	 *
	 * @param timeout
	 *            Maximum seconds to wait for server response. A 0 value or
	 *            negative value is equivalent to set time out to infinity.
	 * @return True if the server could be pinged.
	 */
	public boolean isPingable(int timeout) {

		// We were unable to get the necessary information from the preference
		// store, so store everything offline til we can send it.
		if (this.host.equals("dummyHost") || this.email.equals("dummyUser")) {
			return false;
		}
		//System.out.println("pinging with host: " + this.host + ", and email " + this.email);

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(host)
					.openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				// Not OK.
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
