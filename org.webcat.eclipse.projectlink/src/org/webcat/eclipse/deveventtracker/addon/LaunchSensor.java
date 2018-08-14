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
 * Detects normal, test, and debug launches and adds dev events accordingly.
 * 
 * @author Ayaan Kazerouni
 * @version 12/1/2017
 */
public class LaunchSensor implements ILaunchListener {

	/**
	 * EclipseSensor stuff
	 */
	private EclipseSensor eclipseSensor;
	private URI projectURI;
	private URI fileURI;
	
	/**
	 * For getting test case information
	 */
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
	 * There are three possible Subtypes:
	 * 		* Test
	 * 		* Normal
	 * 		* Debug
	 */
	public void launchAdded(ILaunch launch) {
		String name = launch.getLaunchConfiguration().getName();
		String launchMode = launch.getLaunchMode();
		
		Map<String, String> keyValueMap = new HashMap<String, String>();
		
		if (launchMode.equals(ILaunchManager.RUN_MODE)) {
			if (name.toLowerCase().contains("test")) {
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

	public void launchChanged(ILaunch launch) {
		// no op
	}

	/**
	 * Overridden from ILaunchListener. Gets called when a launch is removed from the listener.
	 * This method will add a DevEvent about the termination.
	 * If the termination was of Type "Test", will also emit some information about the test cases
	 * and their results (Success, Failure, Error).
	 */
	public void launchRemoved(ILaunch launch) {
		String name = launch.getLaunchConfiguration().getName();
		String launchMode = launch.getLaunchMode();
		
		Map<String, String> keyValueMap = new HashMap<String, String>();
		keyValueMap.put("Class-Name", name);
		
		if (launchMode.equals(ILaunchManager.RUN_MODE)) {
			if (name.toLowerCase().contains("test") || name.toLowerCase().contains("junit")) {
				processTestTerminations(keyValueMap);
			} else {
				keyValueMap.put("Subtype", "Normal");
				keyValueMap = this.setLaunchStatus(launch, keyValueMap);
			}
		} else if (launchMode.equals(ILaunchManager.DEBUG_MODE)) {
			keyValueMap.put("Subtype", "Debug");
			keyValueMap = this.setLaunchStatus(launch, keyValueMap);
		}
		
		keyValueMap = this.putLaunchOutput(keyValueMap);
		
		this.eclipseSensor.addDevEvent("Termination", this.projectURI, this.fileURI, keyValueMap, "Program Terminated");
	}

	private void processTestTerminations(Map<String, String> keyValueMap) {
		keyValueMap.put("Subtype", "Test");
		StringBuilder testMethodNames = new StringBuilder("|");
		StringBuilder results = new StringBuilder("|");
		for (ITestCaseElement currentElement : this.testCases) { // List got populated by the TestCaseRunListener
			testMethodNames.append(currentElement.getTestMethodName() + "|");
			Result result = currentElement.getTestResult(true);
			if (result.equals(Result.OK)) {
				results.append("Success|");
			} else if (result.equals(Result.FAILURE)) {
				results.append("Failure|");
			} else if (result.equals(Result.ERROR)) {
				FailureTrace failureTrace = currentElement.getFailureTrace();
				if (failureTrace != null) {
					String stackTrace = failureTrace.getTrace();
					String exception = stackTrace.split("\n")[0];
					results.append("Error: " + exception + "|");
				} else {
					results.append("Error|");
				}
			}
			
		}
		
		// Each runs data is output as a single string, with values delimited by the pipe character (|)
		keyValueMap.put("Unit-Name", testMethodNames.toString());
		keyValueMap.put("Subsubtype", results.toString());
		
		this.testCases = new ArrayList<ITestCaseElement>();
	}
	
	// For normal and debug launch removals, tells whether terminated with an error or not
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
	
	private Map<String, String> putLaunchOutput(Map<String, String> keyValueMap) {
		String output = OutputSensor.getOutput();
		if (output.length() > 0) {
			keyValueMap.put("ConsoleOutput", output);
		}
		return keyValueMap;
	}
}
