/**
 * 
 */
package org.webcat.eclipse.deveventtracker.addon;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.webcat.eclipse.deveventtracker.EclipseSensor;

/**
 * Detects program and test launches and adds dev events accordingly.
 * 
 * @author ayaan
 * @version 1/12/2016
 */
public class LaunchSensor implements ILaunchListener {

	private EclipseSensor eclipseSensor;
	private URI projectURI;
	private URI fileURI;
	private ILaunchManager launchManager;
	
	/**
	 * Creates a new LaunchSensor that will be added as a listener
	 * to the specified launchManager.
	 * 
	 * @param sensor
	 * 		The singleton instance of the EclipseSensor class
	 * @param projectURI
	 * 		The URI of the project currently worked on in the workspace
	 * @param launchManager
	 * 		The LaunchManager to which this sensor will be added as an instance
	 */
	public LaunchSensor(EclipseSensor sensor, URI projectURI, ILaunchManager launchManager) {
		this.eclipseSensor = sensor;
		this.projectURI = projectURI;
		this.launchManager = launchManager;
		this.fileURI = this.eclipseSensor.getActiveFile();
	}

	/**
	 * Overridden from ILaunchListener. Gets called when a launch is added to the listener.
	 * (More simply, when a program is launched from the workspace). 
	 * If the launch comes from a test class, then a DevEvent with LaunchType "TestLaunch" 
	 * is added. If not, then "NormalLaunch".
	 */
	public void launchAdded(ILaunch arg0) {
		String name = this.launchManager.getLaunches()[0].getLaunchConfiguration().getName();
		Map<String, String> keyValueMap = new HashMap<String, String>();
		if (name.toLowerCase().contains("test")) {
			keyValueMap.put("LaunchType", "TestLaunch");
		} else {
			keyValueMap.put("LaunchType", "NormalLaunch");
		}
		
		this.eclipseSensor.addDevEvent("Launch", this.projectURI, this.fileURI, keyValueMap, "Launch happened");
	}

	public void launchChanged(ILaunch arg0) {
		// no op
	}

	/**
	 * Overridden from ILaunchListener. Gets called when a launch is removed from the listener.
	 * This method will add a DevEvent about the termination.
	 */
	public void launchRemoved(ILaunch arg0) {
		this.eclipseSensor.addDevEvent("Termination", this.projectURI, this.fileURI, null, "Program Terminated");
	}
}
