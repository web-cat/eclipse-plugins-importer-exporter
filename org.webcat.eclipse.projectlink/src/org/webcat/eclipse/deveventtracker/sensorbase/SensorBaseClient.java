package org.webcat.eclipse.deveventtracker.sensorbase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.TimerTask;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.webcat.eclipse.deveventtracker.EclipseSensor;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.preferences.IPreferencesConstants;

/**
 * Provides a high-level interface for Clients wishing to communicate with a
 * SensorBase.
 * 
 * Is now a singleton class, accessed by getInstance().
 * 
 * Adapted from Hackystat project.
 * 
 * @author Philip Johnson
 * @author Joseph Luke
 * 
 */
public class SensorBaseClient
{
	/** The singleton instance */
	private static SensorBaseClient theInstance;

	/** The Restlet Client instance used to communicate with the server. */
	private Client client;
	/** The preferred representation type. */
	private Preference<MediaType> htmlMedia = new Preference<MediaType>(
	    MediaType.TEXT_HTML);

	public final static String SENSORBASECLIENT_TIMEOUT_KEY = "CLIENT_TIMEOUT";

	/** To facilitate debugging of problems using this system. */
	private boolean isTraceEnabled = true;

	/**
	 * The default timeout for the Restlet client.
	 */
	private static final int DEFAULT_TIMEOUT = 2000;

	private SensorBaseClient()
	{
		this.client = new Client(new Context(), Protocol.HTTP);
		try
		{
			client.start();
		}
		catch (Exception e)
		{
			Activator.getDefault().log(e);
		}
		setClientTimeout(DEFAULT_TIMEOUT);
		Activator.getDefault().getPreferenceStore()
			.addPropertyChangeListener(new DevEventPreferenceListener());
	}

	/**
	 * Returns the singleton instance.
	 * 
	 * @return The singleton SensorBaseClient instance.
	 */
	public static SensorBaseClient getInstance()
	{
		if (theInstance == null)
		{
			theInstance = new SensorBaseClient();
		}
		return theInstance;
	}

	/**
	 * Attempts to provide a timeout value for this SensorBaseClient.
	 * 
	 * @param milliseconds
	 *            The number of milliseconds to wait before timing out.
	 */
	public final synchronized void setClientTimeout(int milliseconds)
	{
		setClientTimeout(this.client, milliseconds);
	}

	/**
	 * When passed true, future HTTP calls using this client instance will print
	 * out information on the request and response.
	 * 
	 * @param enable
	 *            If true, trace output will be generated.
	 */
	public synchronized void enableHttpTracing(boolean enable)
	{
		this.isTraceEnabled = enable;
	}

	/**
	 * Retrieves a user UUID, either from preferences, or from the server if not
	 * in preferences.
	 * 
	 * @param email
	 *            The user email.
	 * @return The UUID of the user.
	 * @throws SensorBaseClientException
	 *             If the server does not successfully respond.
	 */
	public synchronized UUID retrieveUser(String email)
			throws SensorBaseClientException
	{
		// Check preferences first
		String uuidStringFromPref = Activator.getDefault().getPreferenceStore()
			.getString(IPreferencesConstants.STORED_USER_UUID);
		if (uuidStringFromPref != null && !uuidStringFromPref.isEmpty())
		{
			return UUID.fromString(uuidStringFromPref);
		}

		// If not in preferences, retrieve from server.
		// If we don't have a user email, ask the server and it will give us a
		// UUID;
		// we will confirm this later once we have the user.
		Response response;
		if (email != null && !email.isEmpty())
		{
			response =
                makeRequest(Method.GET, "retrieveUser?email=" + email, null);
		}
		else
		{
			response = makeRequest(Method.GET, "retrieveUser", null);
		}

		if (!response.getStatus().isSuccess())
		{
			throw new SensorBaseClientException(response.getStatus());
		}
		String responseText;
		try
		{
			responseText = response.getEntity().getText();
		}
		catch (IOException e)
		{
			Activator.getDefault().log(e);
			return null;
		}
		if (responseText == null || !responseText.contains("<uuid>"))
		{
			return null;
		}
		else
		{
			String uuidString = parseUUID(responseText);
			Activator.getDefault()
				.getPreferenceStore()
				.setValue(IPreferencesConstants.STORED_USER_UUID, uuidString);
			return UUID.fromString(uuidString);
		}
	}

	/**
	 * Retrieves a studentProject UUID from the file in the project's location,
	 * or from the server if not present. Also sets the preference value for
	 * whether or not we should push events and code snapshots to the server or
	 * hold them locally.
	 * 
	 * @param projectUri
	 *            The URI for the project directory.
	 * @return The project UUID
	 * @throws SensorBaseClientException
	 *             If the server does not successfully respond.
	 */
	public synchronized UUID retrieveStudentProject(String projectUri)
			throws SensorBaseClientException
	{
		// Check the file location to see if we already have the studentProject
		// UUID.
		UUID projectUuid = null;
		String storedProjectUri = null;
		File uuidFile = new File(projectUri + "/.uuid");
		if (uuidFile.exists()) {
			try
			{
				FileReader fr = new FileReader(uuidFile);
				BufferedReader br = new BufferedReader(fr);
				projectUuid = UUID.fromString(br.readLine());
				storedProjectUri = br.readLine();
				br.close();
				fr.close();
				if (projectUuid != null && storedProjectUri != null
					&& storedProjectUri.equals(projectUri))
				{
					return projectUuid;
				}
			}
			catch (IOException e)
			{
				Activator.getDefault().log(e);
			}
		}

		// We don't have the UUID in the file system, or the file's projectUri
		// disagrees with the one passed to us, so retrieve it from the server.

		// Retrieve the stored user UUID from preferences, or from the server if
		// not present.
		UUID userUuid = retrieveUser(getEmail());

		// Ask the server for the studentProject UUID.
		String requestString = "retrieveStudentProject?projectUri="
				+ projectUri + "&userUuid=" + userUuid;
		Response response = makeRequest(Method.GET, requestString, null);
		if (!response.getStatus().isSuccess())
		{
			throw new SensorBaseClientException(response.getStatus());
		}
		String responseText = null;
		try
		{
			responseText = response.getEntity().getText();
		}
		catch (IOException e)
		{
			Activator.getDefault().log(e);
			return null;
		}
		if (responseText == null || !responseText.contains("<uuid>"))
		{
			return null;
		}
		else
		{
			String uuidString = parseUUID(responseText);
			String pushLogsString = responseText.split("<pushLogs>")[1]
					.split("</pushLogs>")[0];
			Activator
					.getDefault()
					.getPreferenceStore()
					.setValue(IPreferencesConstants.PUSH_TO_SERVER,
							Boolean.parseBoolean(pushLogsString));
			// Create new file containing UUID and projectUri
			File studentProjectUUIDFileToCreate = new File(projectUri
					+ "/.uuid");
			FileWriter fw;
			try
			{
				fw = new FileWriter(studentProjectUUIDFileToCreate);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uuidString);
				out.newLine();
				out.write(projectUri);
				out.flush();
				fw.close();
				out.close();
			}
			catch (IOException e)
			{
				Activator.getDefault().log(e);
			}
			return UUID.fromString(uuidString);
		}
	}

	/**
	 * Parses out a UUID (in string form) from html text which contains the
	 * form: "<uuid>UUID</uuid>"
	 * 
	 * @param html
	 *            The html string.
	 * @return The String representation of a UUID embedded in the html.
	 */
	private String parseUUID(String html)
	{
		String afterOpen = html.split("<uuid>")[1];
		return afterOpen.split("</uuid>")[0];
	}

	/**
	 * Creates the passed SensorData on the server.
	 * 
	 * @param data
	 *            The sensor data to create.
	 * @throws SensorBaseClientException
	 *             If problems occur posting this data.
	 */
	public synchronized void putSensorData(SensorData data)
			throws SensorBaseClientException
	{
		if (getPushToServer())
		{
			// Retrieve the stored user UUID from preferences, or from the
			// server if
			// not present.
			String userUuid = retrieveUser(getEmail()).toString();

			String studentProjectUuid = retrieveStudentProject(
					data.getProjectUri()).toString();

			String requestString = "postSensorData?studentProjectUuid="
					+ studentProjectUuid + "&userUuid=" + userUuid + "&time="
					+ data.timestamp + "&runtime=" + data.runtime + "&tool="
					+ data.tool + "&sensorDataType=" + data.sensorDataType
					+ "&uri=" + data.uri;
			int counter = 1;
			for (Property p : data.getProperties().property) {
				try {
					requestString += "&name" + counter + "=" + URLEncoder.encode(p.getKey(), "UTF-8");
					requestString += "&value" + counter + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
					counter++;
				} catch (UnsupportedEncodingException e) {
					Activator.getDefault().log(e);
				}
			}
			Response response = makeRequest(Method.GET, requestString, null);
			if (!response.getStatus().isSuccess())
			{
				throw new SensorBaseClientException(response.getStatus());
			}
		}
	}

	/**
	 * Creates the passed batch of SensorDatas on the server.
	 * 
	 * @param data
	 *            The sensor data batch to create, represented as a SensorDatas
	 *            instance.
	 * @throws SensorBaseClientException
	 *             If problems occur posting this data.
	 */
	public synchronized SensorDatas putSensorDataBatch(SensorDatas batch)
			throws SensorBaseClientException
	{
		SensorDatas unsent = new SensorDatas();
		if (getPushToServer())
		{
			// Retrieve the stored user UUID from preferences, or from the
			// server if
			// not present.
			String userUuid = retrieveUser(getEmail()).toString();

			for (SensorData data : batch.sensorData)
			{
				UUID studentProjectUuid = retrieveStudentProject(
						data.getProjectUri());
				
				if (studentProjectUuid == null) {
					unsent.getSensorData().add(data);
					continue;
				}
				String requestString = "postSensorData?studentProjectUuid="
						+ studentProjectUuid.toString() + "&userUuid=" + userUuid
						+ "&time=" + data.timestamp + "&runtime="
						+ data.runtime + "&tool=" + data.tool
						+ "&sensorDataType=" + data.sensorDataType + "&uri="
						+ data.uri;
				
				int counter = 1;
				for (Property p : data.getProperties().property) {
					try {
						requestString += "&name" + counter + "=" + URLEncoder.encode(p.getKey(), "UTF-8");
						requestString += "&value" + counter + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
						counter++;
					} catch (UnsupportedEncodingException e) {
						Activator.getDefault().log(e);
					}
				}
				
				Response response =
	                makeRequest(Method.GET, requestString, null);
				if (!response.getStatus().isSuccess())
				{
					throw new SensorBaseClientException(response.getStatus());
				}
			}
		}
		
		return unsent;
	}

	/**
	 * Does the housekeeping for making HTTP requests to the SensorBase by a
	 * test or admin user.
	 * 
	 * @param method
	 *            The type of Method.
	 * @param requestString
	 *            A string, such as "users". No preceding slash.
	 * @param entity
	 *            The representation to be sent with the request, or null if not
	 *            needed.
	 * @return The Response instance returned from the server.
	 */
	private Response makeRequest(Method method, String requestString,
			Representation entity)
	{
		Reference reference = new Reference(getEventUrl() + requestString);
		Request request = (entity == null) ? new Request(method, reference)
				: new Request(method, reference, entity);
		request.getClientInfo().getAcceptedMediaTypes().add(htmlMedia);
		if (this.isTraceEnabled)
		{
			System.out.println(Thread.currentThread().getName());
			System.out.println(
                "SensorBaseClient Tracing: " + method + " " + reference);
			if (entity != null)
			{
				try
				{
					System.out.println(entity.getText());
				}
				catch (Exception e)
				{
					Activator.getDefault().log(e);
				}
			}
		}
        Response response = null;
		try
		{
		    response = this.client.handle(request);
	        if (this.isTraceEnabled)
	        {
	            Status status = response.getStatus();
	            System.out.println("  => " + status.getCode() + " "
	                    + status.getDescription());
	        }
		}
		catch (Exception e)
		{
            Activator.getDefault().log(
                "REST request \"" + requestString + "\" failed", e);
            response = new Response(request);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return response;
	}

	/**
	 * Attempts to set timeout values for the passed client.
	 * 
	 * @param client
	 *            The client .
	 * @param milliseconds
	 *            The timeout value.
	 */
	private static void setClientTimeout(Client client, int milliseconds)
	{
		client.getContext().getParameters()
				.add("socketTimeout", new Integer(milliseconds).toString());
	}

	/**
	 * Commits a project locally to its git repo and pushes it to the server if
	 * possible.
	 * 
	 * @param projectUri
	 *            The URI of the project directory.
	 * @param git
	 *            The JGit Git object to use for Git actions.
	 * @param message
	 *            The commit message to use.
	 * @param needsPull
	 *            Whether we need to pull from the server first to prevent merge
	 *            issues.
	 * @return The Commit object itself so that we can pull its hash and store
	 *         this in its associated event.
	 */
	public RevCommit commitSnapshot(
        String projectUri, Git git, String message, boolean needsPull)
	{
		if (isPingable() && getPushToServer())
		{
		    try
		    {
				String studentProjectUuid =
	                retrieveStudentProject(projectUri).toString();

				String gitUrl =
	                getGitUrl() + "StudentProject/" + studentProjectUuid;

				StoredConfig config = git.getRepository().getConfig();
				config.setString("remote", "origin", "url", gitUrl);
				config.setString("remote", "origin", "fetch",
						"+refs/heads/*:refs/remotes/origin/*");
				config.save();

				// Credentials are userUuid, projectUuid
				UsernamePasswordCredentialsProvider cred =
				    new UsernamePasswordCredentialsProvider(
						this.retrieveUser(getEmail()).toString(),
						studentProjectUuid);


				RevCommit commit = git.commit().setMessage(message).call();
				git.checkout().addPath(".gitignore").call();
				// Pull files from server if this is the first time we've used
				// this repository.
				if (needsPull)
				{
					git.pull().setRemote("origin").setCredentialsProvider(cred)
						.call();
					// Update .gitignore file to include /bin directory
					File gitignore = new File(projectUri, "/.gitignore");
					FileWriter fw;
					try
					{
						fw = new FileWriter(gitignore, true);
						BufferedWriter out = new BufferedWriter(fw);
						out.write("/bin/");
						out.newLine();
						out.flush();
						out.close();
						fw.close();
					}
					catch (IOException e)
					{
						Activator.getDefault().log(e);
					}
					git.add().addFilepattern(".gitignore").call();
					git.rm().addFilepattern("README.txt").call();
					git.commit().setMessage("Updating .gitignore file.").call();
				}
				git.push().setRemote("origin").setCredentialsProvider(cred)
					.call();
				return commit;
			}
		    catch (GitAPIException e)
		    {
				Activator.getDefault().log(e);
			}
		    catch (SensorBaseClientException e)
		    {
				Activator.getDefault().log(e);
			}
		    catch (IOException e1)
		    {
				Activator.getDefault().log(e1);
			}
		}
		// If no connection or only storing locally, still need to make local
		// commit and preserve needsPull value.
		else
		{
			RevCommit commit = null;
			try
			{
				commit = git.commit().setMessage(message).call();
			}
			catch (GitAPIException e)
			{
				Activator.getDefault().log(e);
			}
			// If we needed a pull, but had no connection, create a .needspull
			// file so we can
			// tell next time.
			if (needsPull)
			{
				File needsPullFile = new File(projectUri, "/.needspull");
				if (!needsPullFile.exists())
				{
					try
					{
						needsPullFile.createNewFile();
					}
					catch (IOException e)
					{
						Activator.getDefault().log(e);
					}
				}
			}
			return commit;
		}
		return null;
	}

	/**
	 * Informs the server that a project was submitted to Web-CAT.
	 * 
	 * @param project
	 *            The project being submitted.
	 * @throws SensorBaseClientException
	 *             If the server does not respond.
	 */
	public void submissionHappened(IProject project)
			throws SensorBaseClientException
	{
	    String projectUri = project.getLocationURI().getPath();
		UUID userUuid = retrieveUser(getEmail());
		UUID studentProjectUuid = retrieveStudentProject(projectUri);
		String requestString = "submissionHappened?studentProjectUuid="
				+ studentProjectUuid + "&userUuid=" + userUuid;
		Response response = makeRequest(Method.GET, requestString, null);
		if (response.getStatus().isSuccess())
		{
	        Activator.getDefault().getPreferenceStore()
                .setValue(IPreferencesConstants.PUSH_TO_SERVER, true);
		}
		else
		{
		    Activator.getDefault().log(
		        "submissionHappened(): unable to push event: " + requestString);
		}
	}

	/**
	 * Informs the server that a starter project was downloaded.
	 * 
	 * @param projectUri
	 *            The URI of the project directory.
	 * @param projectName
	 *            The name of the project (i.e. the assignment it is for).
	 * @throws SensorBaseClientException
	 *             If the server does not respond.
	 */
	public void downloadStarterProjectHappened(
	    String projectUri, String projectName)
	    throws SensorBaseClientException
	{
		String requestString = "projectDownload?projectUri=" + projectUri
			+ "&projectName=" + projectName + "&userUuid="
			+ retrieveUser(getEmail()).toString();
		Response response = makeRequest(Method.GET, requestString, null);
		if (response.getStatus().isSuccess())
		{
			Activator.getDefault().getPreferenceStore().setValue(IPreferencesConstants.PUSH_TO_SERVER, true);
		} else {
			throw new SensorBaseClientException(response.getStatus());
		}
		
		String responseText;

		try
		{
			responseText = response.getEntity().getText();
			if (responseText.contains("<uuid>")) {
				String uuidString = parseUUID(responseText);
				// Create new file containing UUID and projectUri
				File studentProjectUUIDFileToCreate = new File(projectUri + "/.uuid");
				FileWriter fw = new FileWriter(studentProjectUUIDFileToCreate);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uuidString);
				out.newLine();
				out.write(projectUri);
				out.flush();
				fw.close();
				out.close();
			}
		}
		catch (IOException e)
		{
			Activator.getDefault().log(e);
		}
	}

	/**
	 * Inform the server that an exception occurred in the plugin.
	 * 
	 * @param e
	 *            The exception to log.
	 */
	public void pluginExceptionHappened(final Exception e)
	{
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				
				// Wrap run in catch-all to avoid Timer cancellations.
				try
				{
					UUID userUuid = retrieveUser(getEmail());
					String exceptionClass = e.getClass().getCanonicalName();
					String exceptionMessage = e.getMessage();

					StackTraceElement topStackElement = e.getStackTrace()[0];
					String className = topStackElement.getClassName();
					String methodName = topStackElement.getMethodName();
					String fileName = topStackElement.getFileName();
					int lineNumber = topStackElement.getLineNumber();

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String stackTrace = errors.toString();

					String requestString = "pluginExceptionHappened" + "?userUuid="
						+ userUuid + "&exceptionClass=" + exceptionClass
						+ "&exceptionMessage=" + exceptionMessage + "&className="
						+ className + "&methodName=" + methodName + "&fileName="
						+ fileName + "&lineNumber=" + lineNumber + "&stackTrace="
						+ stackTrace;
					makeRequest(Method.GET, requestString, null);
				} catch (Exception e1) {
					// Don't want to loop infinitely, so don't log this exception.
				}
			}
		};
		
		try {
			EclipseSensor.getInstance().scheduleOneTimeTask(task);
		} catch (SensorShellException e1) {
			System.out.println("Couldn't get EclipseSensor instance.");
		}
	}

	/**
	 * Determine whether the Web-CATURL from preferences is pingable.
	 * 
	 * @return Whether the Web-CAT URL is pingable.
	 */
	public boolean isPingable()
	{
	    String host = getHost();
	    if (host == null || host.isEmpty())
	    {
	        return false;
	    }
		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getHost()).openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if (responseCode != 200)
			{
				// Not OK.
				return false;
			}
		}
		catch (Exception e)
		{
			Activator.getDefault().log(e);
			return false;
		}
		return true;
	}

	/**
	 * @return The base host name for Web-CAT.
	 */
	public String getHost()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String submitUrl = store.getString(IPreferencesConstants.SUBMIT_URL);
		String host = submitUrl.split("/wa/")[0];
		return host;
	}

	/**
	 * @return The Web-CAT URL for DevEventTracker subsytem.
	 */
	public String getEventUrl()
	{
		return getHost() + "/wa/event/";
	}

	/**
	 * @return The Web-CAT url for Git requests.
	 */
	public String getGitUrl()
	{
		return getHost() + "/git/";
	}

	/**
	 * @return The email stored in preferences.
	 */
	public String getEmail()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(IPreferencesConstants.STORED_EMAIL);
	}

	/**
	 * @return Whether we should push events and code snapshots to the server.
	 */
	public boolean getPushToServer()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getBoolean(IPreferencesConstants.PUSH_TO_SERVER);
	}

	/**
	 * Listens for preference changes in the preference store.
	 * 
	 * @author Joseph Luke
	 */
	public class DevEventPreferenceListener implements IPropertyChangeListener
	{
		/**
		 * Listens for a newly-entered user email so we can inform the server
		 * and associate this work with a user.
		 */
		public void propertyChange(PropertyChangeEvent event)
		{
			if (event.getProperty().equals(IPreferencesConstants.STORED_EMAIL))
			{
				if (event.getOldValue().equals(""))
				{
					// If we already had a UUID stored and the user email was
					// just entered,
					// we need to send a confirmation action to the server.
					String storedUserUuid = Activator.getDefault()
						.getPreferenceStore()
						.getString(IPreferencesConstants.STORED_USER_UUID);
					if (!storedUserUuid.equals(""))
					{
						final String requestString = "confirmUuid?userUuid ="
							+ storedUserUuid + "&email="
							+ event.getNewValue();
						TimerTask confirmUUIDTask = new TimerTask() {
							
							@Override
							public void run() {
								
								// Wrap run in catch-all to avoid Timer cancellations.
								try {
									Response response = makeRequest(Method.GET,
											requestString, null);
									if (response.getStatus().isSuccess())
									{
										String responseText = null;
										try
										{
											responseText = response.getEntity().getText();
										}
										catch (IOException e)
										{
											Activator.getDefault().log(e);
										}
										if (responseText != null
											&& responseText.contains("<uuid>"))
										{
											String uuidString = parseUUID(responseText);
											Activator.getDefault()
												.getPreferenceStore()
												.setValue(
												    IPreferencesConstants.STORED_USER_UUID,
													uuidString);
										}
									}
								} catch (Exception e) {
									Activator.getDefault().log(e);
								}
							}
						};
						
						try {
							EclipseSensor.getInstance().scheduleOneTimeTask(confirmUUIDTask);
						} catch (SensorShellException e) {
							Activator.getDefault().log("Couldn't get EclipseSensor instance.", e);
						}
					}
				}
			}
		}
	}

	/**
	 * Stops the Restlet client.
	 */
	public void stopClient()
	{
		try
		{
			client.stop();
		}
		catch (Exception e)
		{
			Activator.getDefault().log(e);
		}
	}
}
