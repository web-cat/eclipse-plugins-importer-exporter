package org.webcat.eclipse.deveventtracker.sensorshell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorData;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorDatas;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.preferences.IPreferencesConstants;

/**
 * Provides a facility for: (a) persisting buffered SensorData instances locally
 * when the SensorBase host is not available and (b) recovering them during a
 * subsequent invocation of SensorShell.
 * 
 * @author Philip Johnson
 */
public class OfflineManager {

	/** The directory where offline data is stored. */
	private File offlineDir;

	/** Holds the sensorShellProperties instance from the parent sensor shell. */
	private SensorShellProperties properties;

	/** The shell that created this offline manager. **/
	private SingleSensorShell parentShell;

	/** The tool that was created the parent shell. */
	private String tool;

	/** Whether or not data has been stored offline. */
	boolean hasOfflineData = false;

	/**
	 * Creates an OfflineManager given the parent shell and the tool.
	 * 
	 * @param shell
	 *            The parent shell.
	 * @param tool
	 *            The tool.
	 */
	public OfflineManager(SingleSensorShell shell, String tool) {
		this.parentShell = shell;
		this.properties = shell.getProperties();
		this.tool = tool;
		this.offlineDir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocationURI().getPath(), "/deveventtracker/offline/");
		boolean dirOk = this.offlineDir.mkdirs();
		if (!dirOk && !this.offlineDir.exists()) {
			throw new RuntimeException("mkdirs failed");
		}
	}

	/**
	 * Stores a SensorDatas instance to a serialized file in the offline
	 * directory. Does nothing if there are no sensordata instances in the
	 * SensorDatas instance.
	 * 
	 * @param sensorDatas
	 *            The SensorDatas instance to be stored.
	 */
	public void store(SensorDatas sensorDatas) {
		if (sensorDatas.getSensorData().size() > 0) {
			SimpleDateFormat fileTimestampFormat = new SimpleDateFormat(
					"yyyy.MM.dd.HH.mm.ss.SSS", Locale.US);
			String fileStampString = fileTimestampFormat.format(new Date());
			File outFile = new File(this.offlineDir, fileStampString + ".data");
			try {
				FileWriter fw = new FileWriter(outFile);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(sensorDatas.getFileString());
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				parentShell.println("Stored "
						+ sensorDatas.getSensorData().size()
						+ " sensor data instances in: "
						+ outFile.getAbsolutePath());
				this.hasOfflineData = true;
			} catch (Exception e) {
				parentShell.println("Error writing the offline file "
						+ outFile.getName() + " " + e);
			}
		}
	}

	/**
	 * Returns true if this offline manager has successfully stored any data
	 * offline.
	 * 
	 * @return True if offline data has been stored.
	 */
	public boolean hasOfflineData() {
		return this.hasOfflineData;
	}

	/**
	 * Attempts to resend any previously stored SensorDatas instances from their
	 * serialized files. Creates a new sensorshell instance to do the sending.
	 * Each SensorDatas instance is deserialized, then each SensorData instance
	 * inside is sent to the SensorShell. This gives the SensorShell an
	 * opportunity to send batches off at whatever interval it chooses. All
	 * serialized files are deleted after being processed if successful.
	 * 
	 * @throws SensorShellException
	 *             If problems occur sending the recovered data.
	 */
	public void recover() throws SensorShellException {
		// Return immediately if there are no offline files to process.
		File[] xmlFiles = this.offlineDir.listFiles(new ExtensionFileFilter(
				".data"));
		if (xmlFiles.length == 0) {
			return;
		}
		// Tell the parent shell log that we're going to try to do offline
		// recovery.
		parentShell.println("Invoking offline recovery on " + xmlFiles.length
				+ " files.");

		// Create a new properties instance with offline recovery/storage
		// disabled.
		Properties props = new Properties();
		String falseStr = "false";
		props.setProperty(
				SensorShellProperties.SENSORSHELL_MULTISHELL_ENABLED_KEY,
				falseStr);
		props.setProperty(
				SensorShellProperties.SENSORSHELL_OFFLINE_CACHE_ENABLED_KEY,
				falseStr);
		props.setProperty(
				SensorShellProperties.SENSORSHELL_OFFLINE_RECOVERY_ENABLED_KEY,
				falseStr);
		props.setProperty(
				SensorShellProperties.SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY,
				"0.0");
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		String host = store.getString(IPreferencesConstants.SUBMIT_URL);
		host = host.split("/wa/")[0];
		String email = store.getString(IPreferencesConstants.STORED_EMAIL);

		if (host == null || host.equals(""))
		{
			host = "dummyHost";
		}
		if (email == null || email.equals(""))
		{
			email = "dummyUser";
		}
		
		
		SensorShellProperties shellProps = new SensorShellProperties(props,
				host, email);
		// Provide a separate log file for this offline recovery.
		String offlineTool = this.tool + "-offline-recovery";
		// Create the offline sensor shell to be used for sending this data.
		SingleSensorShell shell = new SingleSensorShell(shellProps, false,
				offlineTool);
		shell.println("Invoking offline recovery on " + xmlFiles.length
				+ " files.");
		FileInputStream fileStream = null;

		// For each offline file to recover
		for (int i = 0; i < xmlFiles.length; i++) {
			try {
				// Reconstruct the SensorDatas instances from the serialized
				// files.
				shell.println("Recovering offline data from: "
						+ xmlFiles[i].getName());
				fileStream = new FileInputStream(xmlFiles[i]);
				SensorDatas sensorDatas = makeSensorDatasFromFile(xmlFiles[i]);
				shell.println("Found " + sensorDatas.getSensorData().size()
						+ " instances.");
				for (SensorData data : sensorDatas.getSensorData()) {
					shell.add(data);
				}
				// Try to send the data.
				shell.println("About to send data");
				int numSent = shell.send();
				shell.println("Successfully sent: " + numSent + " instances.");
				// If all the data was successfully sent, then we delete the
				// file.
				fileStream.close();
				if (numSent == sensorDatas.getSensorData().size()) {
					boolean isDeleted = xmlFiles[i].delete();
					System.out.println(xmlFiles[i].getName() + " was deleted: " + isDeleted);
					shell.println("Trying to delete " + xmlFiles[i].getName()
							+ ". Success: " + isDeleted);
				} else {
					shell.println("Did not send all instances. " + xmlFiles[i]
							+ " not deleted.");
				}
			} catch (Exception e) {
				shell.println("Error recovering data from: " + xmlFiles[i]);
				try {
					fileStream.close();
				} catch (Exception f) {
					shell.println("Failed to close: " + fileStream.toString()
							+ " " + e);
				}
			}
		}
		shell.quit();
	}

	private SensorDatas makeSensorDatasFromFile(File file) {
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		SensorData currentData = new SensorData();
		SensorDatas datas = new SensorDatas();
		String lastPropertyKey = null;
		
		String currentLine;
		try {
			currentLine = br.readLine();
			while(!currentLine.equals("</SensorDatas>"))
			{
				if (currentLine.equals("<Timestamp>")) {
					currentData.setTimestamp(Long.parseLong(br.readLine()));
				}
				else if (currentLine.equals("<Runtime>")) {
					currentData.setRuntime(Long.parseLong(br.readLine()));
				}
				else if (currentLine.equals("<Tool>")) {
					currentData.setTool(br.readLine());
				}
				else if (currentLine.equals("<SensorDataType>")) {
					currentData.setSensorDataType(br.readLine());
				}
				else if (currentLine.equals("<URI>")) {
					currentData.setUri(br.readLine());
				}
				else if (currentLine.equals("<ProjectURI>")) {
					currentData.setProjectUri(br.readLine());
				}
				else if (currentLine.equals("<Name>")) {
					lastPropertyKey = br.readLine();
				}
				else if (currentLine.equals("<Value>")) {
					if (lastPropertyKey != null)
					{
						currentData.addProperty(lastPropertyKey, br.readLine());
						lastPropertyKey = null;
					}
				}
				else if (currentLine.equals("</SensorData>"))
				{
					datas.getSensorData().add(currentData);
					currentData = new SensorData();
				}
				currentLine = br.readLine();
			}
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return datas;
	}
}
