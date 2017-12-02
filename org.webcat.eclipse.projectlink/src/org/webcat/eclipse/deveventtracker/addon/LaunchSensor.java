/**
 * 
 */
package org.webcat.eclipse.deveventtracker.addon;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement.FailureTrace;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.webcat.eclipse.deveventtracker.EclipseSensor;
import org.webcat.eclipse.projectlink.Activator;

/**
 * Detects program and test launches and adds dev events accordingly.
 * 
 * @author Ayaan Kazerouni
 * @version 12/1/2017
 */
public class LaunchSensor implements ILaunchListener {

	private EclipseSensor eclipseSensor;
	private URI projectURI;
	private URI fileURI;
	private TestRunListener listener;
	private List<ITestCaseElement> testCases;
	
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
	public LaunchSensor(EclipseSensor sensor, URI projectURI) {
		this.eclipseSensor = sensor;
		this.projectURI = projectURI;
		this.fileURI = this.eclipseSensor.getActiveFile();
		this.testCases = new ArrayList<ITestCaseElement>();
		this.listener = new TestRunListener() {
			
			public void testCaseFinished(ITestCaseElement element) {
				LaunchSensor.this.testCases.add(element);
			}
		};
		
		JUnitCore.addTestRunListener(this.listener);
	}

	/**
	 * Overridden from ILaunchListener. Gets called when a launch is added to the listener.
	 * (More simply, when a program is launched from the workspace). 
	 * If the launch comes from a test class, then a DevEvent with LaunchType "TestLaunch" 
	 * is added. If not, then "NormalLaunch".
	 */
	public void launchAdded(ILaunch arg0) {
		String name = arg0.getLaunchConfiguration().getName();
		String launchMode = arg0.getLaunchMode();
		
		Map<String, String> keyValueMap = new HashMap<String, String>();
		
		if (launchMode.equals(ILaunchManager.RUN_MODE)) {
			if (name.toLowerCase().contains("test")) {
				System.out.println("Launch added");
				keyValueMap.put("Subtype", "Test");
			} else {
				keyValueMap.put("Subtype", "Normal");
			}
		} else if (launchMode.equals(ILaunchManager.DEBUG_MODE)) {
			keyValueMap.put("Subtype", "Debug");
		}
		
		keyValueMap.put("Unit-Name", name);
		
		this.eclipseSensor.addDevEvent("Launch", this.projectURI, this.fileURI, keyValueMap, "Program Launched");
	}

	public void launchChanged(ILaunch arg0) {
		// no op
	}

	/**
	 * Overridden from ILaunchListener. Gets called when a launch is removed from the listener.
	 * This method will add a DevEvent about the termination.
	 */
	public void launchRemoved(ILaunch arg0) {
		String name = arg0.getLaunchConfiguration().getName();
		String launchMode = arg0.getLaunchMode();
		
		Map<String, String> keyValueMap = new HashMap<String, String>();
		keyValueMap.put("Unit-Name", name);
		
		if (launchMode.equals(ILaunchManager.RUN_MODE)) {
			if (name.toLowerCase().contains("test") || name.toLowerCase().contains("junit")) {
				keyValueMap.put("Subtype", "Test");
				for (ITestCaseElement currentElement : this.testCases) {
					keyValueMap.put("TestMethodName", currentElement.getTestMethodName());
					Result result = currentElement.getTestResult(true);
					if (result.equals(Result.OK)) {
						keyValueMap.put("Subsubtype", "TestSuccess");
					} else if (result.equals(Result.FAILURE)) {
						keyValueMap.put("Subsubtype", "TestFailure");
					} else if (result.equals(Result.ERROR)) {
						FailureTrace failureTrace = currentElement.getFailureTrace();
						if (failureTrace != null) {
							String stackTrace = failureTrace.getTrace();
							String exception = stackTrace.split("\n")[0];
							keyValueMap.put("Subsubtype", exception);
						} else {
							keyValueMap.put("Subsubtype", "Error");
						}
					}
					
					this.eclipseSensor.addDevEvent("Termination", this.projectURI, this.fileURI, keyValueMap, "Program Terminated");
				}
				this.testCases = new ArrayList<ITestCaseElement>();
				return; // don't add dev-events for normal and debug launch removals
			} else {
				keyValueMap.put("Subtype", "Normal");
			}
		} else if (launchMode.equals(ILaunchManager.DEBUG_MODE)) {
			keyValueMap.put("Subtype", "Debug");
		}
		
		keyValueMap = this.setLaunchStatus(arg0, keyValueMap);
		this.eclipseSensor.addDevEvent("Termination", this.projectURI, this.fileURI, keyValueMap, "Program Terminated");
	}
	
	private Map<String, String> setLaunchStatus(ILaunch arg0, Map<String, String> keyValueMap) {
		try {
			int exitValue = arg0.getProcesses()[0].getExitValue();
			if (exitValue == 0) {
				keyValueMap.put("Subsubtype", "Normal");
			} else {
				keyValueMap.put("Subsubtype", "Error");
			}
			
		} catch (DebugException e) {
			Activator.getDefault().log(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			Activator.getDefault().log(e);
		}
		
		return keyValueMap;
	}
}
