package org.webcat.eclipse.deveventtracker.sensorbase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.preferences.IPreferencesConstants;

/**
 * Provides a high-level interface for Clients wishing to communicate with a
 * SensorBase.
 * 
 * @author Philip Johnson
 * 
 */
public class SensorBaseClient {

	/** The singleton instance */
	private static SensorBaseClient theInstance;

	/** The Restlet Client instance used to communicate with the server. */
	private Client client;
	/** The preferred representation type. */
	private Preference<MediaType> htmlMedia = new Preference<MediaType>(
			MediaType.TEXT_HTML);

	/** To facilitate debugging of problems using this system. */
	private boolean isTraceEnabled = false;

	/**
	 * The System property key used to retrieve the default timeout value in
	 * milliseconds.
	 */
	public static final String SENSORBASECLIENT_TIMEOUT_KEY = "sensorbaseclient.timeout";
	private static final int DEFAULT_TIMEOUT = 2000;

	private SensorBaseClient() {
		this.client = new Client(new Context(), Protocol.HTTP);
		try {
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setClientTimeout(DEFAULT_TIMEOUT);
		Activator.getDefault().getPreferenceStore()
				.addPropertyChangeListener(new DevEventPreferenceListener());
	}

	public static SensorBaseClient getInstance() {
		if (theInstance == null) {
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
	public final synchronized void setClientTimeout(int milliseconds) {
		setClientTimeout(this.client, milliseconds);
	}

	/**
	 * When passed true, future HTTP calls using this client instance will print
	 * out information on the request and response.
	 * 
	 * @param enable
	 *            If true, trace output will be generated.
	 */
	public synchronized void enableHttpTracing(boolean enable) {
		this.isTraceEnabled = enable;
	}

	/**
	 * Returns the user UUID from this server based on their email.
	 * 
	 * @param email
	 *            The user email.
	 * @return The UUID of the user.
	 * @throws SensorBaseClientException
	 *             If the server does not successfully respond.
	 */
	public synchronized UUID retrieveUser(String email)
			throws SensorBaseClientException {
		// Check preferences first
		String uuidStringFromPref = Activator.getDefault().getPreferenceStore()
				.getString(IPreferencesConstants.STORED_USER_UUID);
		if (uuidStringFromPref != null && uuidStringFromPref != "") {
			return UUID.fromString(uuidStringFromPref);
		}

		// If not in preferences, retrieve from server.
		// If we don't have a user email, ask the server and it will give us a
		// UUID;
		// we will confirm this later once we have the user.
		Response response;
		if (email != null && !email.equals("")) {
			response = makeRequest(Method.GET, "retrieveUser?email=" + email,
					null);
		} else {
			response = makeRequest(Method.GET, "retrieveUser", null);
		}

		if (!response.getStatus().isSuccess()) {
			throw new SensorBaseClientException(response.getStatus());
		}
		String responseText;
		try {
			responseText = response.getEntity().getText();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (responseText == null || !responseText.contains("<uuid>")) {
			return null;
		} else {
			String uuidString = parseUUID(responseText);
			Activator
					.getDefault()
					.getPreferenceStore()
					.setValue(IPreferencesConstants.STORED_USER_UUID,
							uuidString);
			return UUID.fromString(uuidString);
		}
	}

	public synchronized UUID retrieveStudentProject(String projectUri)
			throws SensorBaseClientException {
		// Check the file location to see if we already have the studentProject
		// UUID.
		UUID projectUuid;
		String storedProjectUri;
		try {
			FileReader fr = new FileReader(projectUri + "/.uuid");
			BufferedReader br = new BufferedReader(fr);
			try {
				projectUuid = UUID.fromString(br.readLine());
				storedProjectUri = br.readLine();
				br.close();
				fr.close();
				if (projectUuid != null && storedProjectUri != null
						&& storedProjectUri.equals(projectUri)) {
					return projectUuid;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
		}

		// We don't have the UUID in the file system, or the file's projectUri
		// disagrees with the one passed to us, so retrieve it from the server.

		// Retrieve the stored user UUID from preferences, or from the server if
		// not present.
		String userUuid = retrieveUser(getEmail()).toString();

		// Ask the server for the studentProject UUID.
		String requestString = "retrieveStudentProject?projectUri="
				+ projectUri + "&userUuid=" + userUuid;
		Response response = makeRequest(Method.GET, requestString, null);
		if (!response.getStatus().isSuccess()) {
			throw new SensorBaseClientException(response.getStatus());
		}
		String responseText;
		try {
			responseText = response.getEntity().getText();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (responseText == null || !responseText.contains("<uuid>")) {
			return null;
		} else {
			String uuidString = parseUUID(responseText);
			// Create new file containing UUID and projectUri
			File studentProjectUUIDFileToCreate = new File(projectUri
					+ "/.uuid");
			FileWriter fw;
			try {
				fw = new FileWriter(studentProjectUUIDFileToCreate);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uuidString);
				out.newLine();
				out.write(projectUri);
				out.flush();
				fw.close();
				out.close();
			} catch (IOException e) {
				// e.printStackTrace();
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
	private String parseUUID(String html) {

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
			throws SensorBaseClientException {
		// Retrieve the stored user UUID from preferences, or from the server if
		// not present.
		String userUuid = retrieveUser(getEmail()).toString();

		String studentProjectUuid = retrieveStudentProject(data.getProjectUri())
				.toString();

		String requestString = "postSensorData?studentProjectUuid="
				+ studentProjectUuid + "&userUuid=" + userUuid + "&time="
				+ data.timestamp + "&runtime=" + data.runtime + "&tool="
				+ data.tool + "&sensorDataType=" + data.sensorDataType
				+ "&uri=" + data.uri;
		if (data.findProperty("CommitHash") != null) {
			requestString += "&CommitHash="
					+ data.findProperty("CommitHash").value;
		}
		Response response = makeRequest(Method.GET, requestString, null);
		if (!response.getStatus().isSuccess()) {
			throw new SensorBaseClientException(response.getStatus());
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
	public synchronized void putSensorDataBatch(SensorDatas batch)
			throws SensorBaseClientException {
		// Retrieve the stored user UUID from preferences, or from the server if
		// not present.
		String userUuid = retrieveUser(getEmail()).toString();

		for (SensorData data : batch.sensorData) {
			String studentProjectUuid = retrieveStudentProject(
					data.getProjectUri()).toString();
			String requestString = "postSensorData?studentProjectUuid="
					+ studentProjectUuid + "&userUuid=" + userUuid + "&time="
					+ data.timestamp + "&runtime=" + data.runtime + "&tool="
					+ data.tool + "&sensorDataType=" + data.sensorDataType
					+ "&uri=" + data.uri;
			if (data.findProperty("CommitHash") != null) {
				requestString += "&commitHash="
						+ data.findProperty("CommitHash").value;
			}
			Response response = makeRequest(Method.GET, requestString, null);
			if (!response.getStatus().isSuccess()) {
				throw new SensorBaseClientException(response.getStatus());
			}
		}
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
			Representation entity) {
		System.out.println("requesting from:" + getEventUrl()
				+ " with string: " + requestString);
		Reference reference = new Reference(getEventUrl() + requestString);
		Request request = (entity == null) ? new Request(method, reference)
				: new Request(method, reference, entity);
		request.getClientInfo().getAcceptedMediaTypes().add(htmlMedia);
		if (this.isTraceEnabled) {
			System.out.println("SensorBaseClient Tracing: " + method + " "
					+ reference);
			if (entity != null) {
				try {
					System.out.println(entity.getText());
				} catch (Exception e) {
					System.out.println("  Problems with getText() on entity.");
				}
			}
		}
		Response response = this.client.handle(request);
		if (this.isTraceEnabled) {
			Status status = response.getStatus();
			System.out.println("  => " + status.getCode() + " "
					+ status.getDescription());
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
	private static void setClientTimeout(Client client, int milliseconds) {
		client.getContext().getParameters()
				.add("socketTimeout", new Integer(milliseconds).toString());
	}

	public RevCommit commitSnapshot(String projectUri, Git git, String message,
			boolean needsPull) {
		System.out.println("attempting to push from project " + projectUri
				+ " in sensorbaseclient");
		if (isPingable()) {
			try {
				String studentProjectUuid = retrieveStudentProject(projectUri)
						.toString();

				String gitUrl = getGitUrl() + "StudentProject/"
						+ studentProjectUuid;

				StoredConfig config = git.getRepository().getConfig();
				config.setString("remote", "origin", "url", gitUrl);
				config.setString("remote", "origin", "fetch",
						"+refs/heads/*:refs/remotes/origin/*");
				config.save();

				// Credentials are userUuid, projectUuid
				UsernamePasswordCredentialsProvider cred = new UsernamePasswordCredentialsProvider(
						this.retrieveUser(getEmail()).toString(),
						studentProjectUuid);

				System.out.println(gitUrl);

				RevCommit commit = git.commit().setMessage(message).call();
				git.checkout().addPath(".gitignore").call();
				// Pull files from server if this is the first time we've used
				// this repository.
				if (needsPull) {
					System.out.println("pulling first");
					git.pull().setRemote("origin").setCredentialsProvider(cred)
							.call();
					// Update .gitignore file to include /bin directory
					File gitignore = new File(projectUri, "/.gitignore");
					FileWriter fw;
					try {
						fw = new FileWriter(gitignore, true);
						BufferedWriter out = new BufferedWriter(fw);
						out.write("/bin/");
						out.newLine();
						out.flush();
						out.close();
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					git.add().addFilepattern(".gitignore").call();
					git.rm().addFilepattern("README.txt").call();
					git.commit().setMessage("Updating .gitignore file.").call();
				}
				git.push().setRemote("origin").setCredentialsProvider(cred)
						.call();
				return commit;
			} catch (GitAPIException e) {
				e.printStackTrace();
			} catch (SensorBaseClientException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		// If no connection, still need to make local commit and preserve
		// needsPull value.
		else {
			RevCommit commit = null;
			try {
				commit = git.commit().setMessage(message).call();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
			// If we needed a pull, but had no connection, create a .needspull
			// file so we can
			// tell next time.
			if (needsPull) {
				File needsPullFile = new File(projectUri, "/.needspull");
				if (!needsPullFile.exists()) {
					try {
						needsPullFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return commit;
		}
		return null;
	}

	public void submissionHappened(String projectUri)
			throws SensorBaseClientException {
		UUID userUuid = retrieveUser(getEmail());
		UUID studentProjectUuid = retrieveStudentProject(projectUri);
		String requestString = "submissionHappened?projectUuid="
				+ studentProjectUuid + "&userUuid=" + userUuid;
		Response response = makeRequest(Method.GET, requestString, null);
		if (!response.getStatus().isSuccess()) {
			throw new SensorBaseClientException(response.getStatus());
		}
	}

	public void downloadStarterProjectHappened(String projectUri,
			String projectName, String courseName)
			throws SensorBaseClientException {
		String requestString = "projectDownload?projectUri=" + projectUri
				+ "&projectName=" + projectName + "&userUuid="
				+ retrieveUser(getEmail()).toString() + "&courseName="
				+ courseName;
		Response response = makeRequest(Method.GET, requestString, null);
		if (!response.getStatus().isSuccess()) {
			throw new SensorBaseClientException(response.getStatus());
		}
		String responseText;

		try {
			responseText = response.getEntity().getText();
			String uuidString = parseUUID(responseText);
			// Create new file containing UUID and projectUri
			File studentProjectUUIDFileToCreate = new File(projectUri
					+ "/.uuid");
			FileWriter fw = new FileWriter(studentProjectUUIDFileToCreate);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(uuidString);
			out.newLine();
			out.write(projectUri);
			out.flush();
			fw.close();
			out.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	public void pluginExceptionHappened(Exception e) {
		try {
			String userUuid = retrieveUser(getEmail()).toString();
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
		} catch (SensorBaseClientException e1) {
		}
	}

	public boolean isPingable() {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					getHost()).openConnection();
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

	public String getHost() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String submitUrl = store.getString(IPreferencesConstants.SUBMIT_URL);
		String host = submitUrl.split("/wa/")[0];
		return host;
	}

	public String getEventUrl() {
		return getHost() + "/wa/event/";
	}

	public String getGitUrl() {
		return getHost() + "/git/";
	}

	public String getEmail() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String email = store.getString(IPreferencesConstants.STORED_EMAIL);
		return email;
	}

	public class DevEventPreferenceListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IPreferencesConstants.STORED_EMAIL)) {
				if (event.getOldValue().equals("")) {
					// If we already had a UUID stored and the user email was
					// just entered,
					// we need to send a confirmation action to the server.
					System.out.println("just changed email to "
							+ event.getNewValue());
					String storedUserUuid = Activator.getDefault()
							.getPreferenceStore()
							.getString(IPreferencesConstants.STORED_USER_UUID);
					if (!storedUserUuid.equals("")) {
						String requestString = "confirmUuid?userUuid ="
								+ storedUserUuid + "&email="
								+ event.getNewValue();
						Response response = makeRequest(Method.GET,
								requestString, null);
						if (response.getStatus().isSuccess()) {
							String responseText = null;
							try {
								responseText = response.getEntity().getText();
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (responseText != null
									&& responseText.contains("<uuid>")) {
								String uuidString = parseUUID(responseText);
								Activator
										.getDefault()
										.getPreferenceStore()
										.setValue(
												IPreferencesConstants.STORED_USER_UUID,
												uuidString);
							}
						}
					}
				}
			}
		}
	}

	public void stopClient() {
		try {
			client.stop();
		} catch (Exception e) {
		}
	}
}
