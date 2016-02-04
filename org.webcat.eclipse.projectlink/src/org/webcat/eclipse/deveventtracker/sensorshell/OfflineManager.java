package org.webcat.eclipse.deveventtracker.sensorshell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.ResourcesPlugin;
import org.webcat.eclipse.deveventtracker.EclipseSensor;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorData;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorDatas;
import org.webcat.eclipse.projectlink.Activator;

/**
 * Provides a facility for: (a) persisting buffered SensorData instances locally
 * when the SensorBase host is not available and (b) recovering them during a
 * subsequent invocation of SensorShell.
 * 
 * Imported from Hackystat project.
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
	
	/**
	 * Constants for the filenames of the files used for offline storage and data to post,
	 * respectively.
	 */
	private static final String OFFLINE_DATA_FILE = "offlineData.data";
	private static final String POST_DATA_FILE = "postData.data";

	/** Whether or not data has been stored offline. */
	boolean hasOfflineData = false;
	
	private volatile SensorDatas unsentData;
	
	private LinkedBlockingQueue<SensorData> blockingQueue;

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
		this.unsentData = new SensorDatas();
		this.blockingQueue = new LinkedBlockingQueue<SensorData>();
		boolean dirOk = this.offlineDir.mkdirs();
		if (!dirOk && !this.offlineDir.exists()) {
			throw new RuntimeException("mkdirs failed");
		}
		this.startWriteOfflineTask();
	}

	/**
	 * Stores SensorDatas inside a blocking in-memory data structure
	 * that is polled on a background thread for data to send to the
	 * server.
	 * 
	 * @param sensorDatas
	 *            The SensorDatas instance to be stored.
	 */
	public void store(SensorDatas sensorDatas) {
		
		if (!this.unsentData.getSensorData().isEmpty()) {
			synchronized (this) {
				sensorDatas.getSensorData().addAll(this.unsentData.getSensorData());
				this.unsentData.getSensorData().clear();
			} 
		}
		if (sensorDatas.getSensorData().size() > 0) {
			
			for (SensorData current : sensorDatas.getSensorData()) {
				this.blockingQueue.add(current);
			}		
			
			try {
				parentShell.println("Stored "
						+ sensorDatas.getSensorData().size()
						+ " sensor data instances in memory.");
				this.hasOfflineData = true;
			} catch (Exception e) {
				parentShell.println("Error storing the offline data.");
				Activator.getDefault().log(e);
			}
		}
	}
	
	private void startWriteOfflineTask() {
		Thread offlineThread = new Thread(new Runnable() {
			public void run() {
				OfflineManager.this.writeOffline();
			}
		});
		offlineThread.setName("OfflineWritingThread");
		offlineThread.start();
	}
	
	private void writeOffline()
	{
		File offline = new File(this.offlineDir, OFFLINE_DATA_FILE);
		BufferedWriter writer = null;
		try
		{
			while (true)
			{
				SensorData data = this.blockingQueue.poll(2, TimeUnit.SECONDS);
				if (data != null)
				{
	                if (writer == null)
	                {
	                    writer = new BufferedWriter(
	                        new FileWriter(offline, true));
	                }
					this.parentShell.println(Thread.currentThread().getName()
					    + ": Got data, writing to file.");
					writer.write(data.getFileString());
	                writer.flush();
				}
				
				if (EclipseSensor.FILE_REQUESTED)
				{
				    if (writer != null)
				    {
				        writer.close();
				        writer = null;
				    }
				    if (offline.exists())
				    {
				        offline.renameTo(
				            new File(this.offlineDir, POST_DATA_FILE));
				        this.parentShell.println(
				            Thread.currentThread().getName()
                            + ": File requested. Releasing semaphore.");
				        EclipseSensor.getInstance().releaseSemaphore();
				    }
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				Activator.getDefault().log(e);
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
	 * Attempts to send SensorData from the postData.data file. If the
	 * server is pingable and the data is sent, the file is deleted. This
	 * entire process takes place in a background thread.
	 * 
	 * @throws SensorShellException
	 *             If problems occur sending the recovered data.
	 */
	public void recover() throws SensorShellException {
		// Return immediately if there are no offline files to process.
		File postData = new File(this.offlineDir, POST_DATA_FILE);
		
		if (!postData.exists()) {
			return;
		}
		
		if (!SensorBaseClient.getInstance().isPingable()) {
			return;
		}
		
		this.parentShell.println(Thread.currentThread().getName() + ": posting to server.");

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
		SensorShellProperties shellProps = new SensorShellProperties(props);
		// Provide a separate log file for this offline recovery.
		String offlineTool = this.tool + "-offline-recovery";
		// Create the offline sensor shell to be used for sending this data.
		SingleSensorShell shell = new SingleSensorShell(shellProps, false,
				offlineTool);
		FileInputStream fileStream = null;

		try {
			// Reconstruct the SensorDatas instances from the serialized
			// files.
			shell.println("Recovering offline data from: "
					+ postData.getName());
			fileStream = new FileInputStream(postData);
			SensorDatas sensorDatas = makeSensorDatasFromFile(postData);
			shell.println("Found " + sensorDatas.getSensorData().size()
					+ " instances.");
			SensorDatas unsent = SensorBaseClient.getInstance().putSensorDataBatch(sensorDatas);
			if (!unsent.getSensorData().isEmpty()) {
				synchronized (this) {
					this.unsentData.getSensorData().addAll(unsent.getSensorData());
				} 
			}
			int numSent = sensorDatas.getSensorData().size() - unsent.getSensorData().size();
			shell.println("Successfully sent: " + numSent + " instances.");
			fileStream.close();
				boolean isDeleted = postData.delete();
				System.out.println(postData.getName() + " was deleted: " + isDeleted);
				shell.println("Trying to delete " + postData.getName()
						+ ". Success: " + isDeleted);
		} catch (Exception e) {
			shell.println("Error recovering data from: " + postData);
			Activator.getDefault().log(e);
			try {
				fileStream.close();
			} catch (Exception f) {
				shell.println("Failed to close: " + fileStream.toString()
						+ " " + e);
				Activator.getDefault().log(e);
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
			while((currentLine = br.readLine()) != null)
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
			}
			br.close();
			
		} catch (IOException e) {
			Activator.getDefault().log(e);
		}
		return datas;
	}
}
