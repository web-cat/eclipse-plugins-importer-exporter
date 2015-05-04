package org.webcat.eclipse.deveventtracker.sensorbase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.jgit.revwalk.RevCommit;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ObjectRepresentation;
import org.restlet.resource.Representation;
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

	/** Holds the userEmail to be associated with this client. */
	private String userEmail;

	/** The Web-CAT URL. */
	private String webCatUrl;

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

	/**
	 * Initializes a new SensorBaseClient, given the Web-CAT URL and userEmail.
	 * 
	 * @param host
	 *            The URL of the Web-CAT server to connect to.
	 * @param email
	 *            The user's email that we will use for authentication.
	 */
	public SensorBaseClient(String host, String email) {
		validateArg(host);
		validateArg(email);
		this.userEmail = email;
		this.webCatUrl = host;
		if (!this.webCatUrl.endsWith("/")) {
			this.webCatUrl = this.webCatUrl + "/";
		}
		this.webCatUrl = this.webCatUrl + "wa/event/";

		this.client = new Client(Protocol.HTTP);
		setTimeout(getDefaultTimeout());
	}

	/**
	 * Attempts to provide a timeout value for this SensorBaseClient.
	 * 
	 * @param milliseconds
	 *            The number of milliseconds to wait before timing out.
	 */
	public final synchronized void setTimeout(int milliseconds) {
		setClientTimeout(this.client, milliseconds);
	}

	/**
	 * Returns the default timeout in milliseconds. The default timeout is set
	 * to 2000 ms, but clients can change this by creating a System property
	 * called sensorbaseclient.timeout and set it to a String indicating the
	 * number of milliseconds.
	 * 
	 * @return The default timeout.
	 */
	private static int getDefaultTimeout() {
		String systemTimeout = System.getProperty(SENSORBASECLIENT_TIMEOUT_KEY,
				"2000");
		int timeout = 2000;
		try {
			timeout = Integer.parseInt(systemTimeout);
		} catch (Exception e) {
			timeout = 2000;
		}
		return timeout;
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
		Response response = makeRequest(Method.GET, "retrieveUser?email="
				+ email, null);
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
		if (responseText == null
				|| responseText.equals("No user found for that email")) {
			return null;
		} else {
			String uuidString = parseUUID(responseText);
			UUID userUUID = UUID.fromString(uuidString);
			Activator
					.getDefault()
					.getPreferenceStore()
					.setValue(IPreferencesConstants.STORED_USER_UUID,
							userUUID.toString());
			return userUUID;
		}
	}
	


	public synchronized UUID retrieveStudentProject(String projectUri)
			throws SensorBaseClientException {
		// Check the file location to see if we already have the studentProject
		// UUID.
		UUID projectUUID;
		try {
			FileReader fr = new FileReader(projectUri + "/projectUUID.uuid");
			BufferedReader br = new BufferedReader(fr);
			try {
				projectUUID = UUID.fromString(br.readLine());
				br.close();
				if (projectUUID != null) {
					return projectUUID;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// Retrieve the stored user UUID from preferences, or from the server if
		// not present.
		String userUUID = Activator.getDefault().getPreferenceStore()
				.getString(IPreferencesConstants.STORED_USER_UUID);
		if (userUUID.equals("")) {
			userUUID = retrieveUser(userEmail).toString();
		}

		// Ask the server for the studentProject UUID.
		String requestString = "retrieveStudentProject?projectUri="
				+ projectUri + "&userUuid=" + userUUID;
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
		if (responseText == null
				|| responseText.equals("No user found for that email")) {
			return null;
		} else {
			String uuidString = parseUUID(responseText);
			// Create new file containing UUID
			File studentProjectUUIDFileToCreate = new File(projectUri
					+ "/projectUUID.txt");
			FileWriter fw;
			try {
				fw = new FileWriter(studentProjectUUIDFileToCreate);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(uuidString);
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return UUID.fromString(uuidString);
		}
	}
	
	/**
	 * Parses out a UUID (in string form) from html text which contains the form:
	 * "...elementName="uuid">UUID<..."
	 * @param html The html string.
	 * @return The String representation of a UUID embedded in the html.
	 */
	private String parseUUID(String html)
	{
		int index = html.indexOf("=\"uuid\">");
		int index2 = html.indexOf("<", index);
		return html.substring(index+8, index2);
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
		String userUUID = Activator.getDefault().getPreferenceStore()
				.getString(IPreferencesConstants.STORED_USER_UUID);
		if (userUUID.equals("")) {
			userUUID = retrieveUser(userEmail).toString();
		}
		String studentProjectUUID = retrieveStudentProject(data.getProjectUri())
				.toString();

		String requestString = "postSensorData?studentProjectUuid="
				+ studentProjectUUID + "&userUuid=" + userUUID + "&time="
				+ data.timestamp + "&runtime=" + data.runtime + "&tool="
				+ data.tool + "&sensorDataType=" + data.sensorDataType
				+ "&uri=" + data.uri;
		Response response = makeRequest(Method.GET, requestString, null);
		if (!response.getStatus().isSuccess()) {
			throw new SensorBaseClientException(response.getStatus());
		}
		String responseText = "";
		try {
			responseText = response.getEntity().getText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		responseText = parseUUID(responseText);
		if (responseText == null || responseText.equals("No user found for that email")
				|| responseText
						.equals("No student project found for that UUID")
				|| responseText.equals("Invalid SensorDataType")) {
			throw new SensorBaseClientException(responseText);
		}
	}

	/**
	 * Creates the passed batch of SensorData on the server.
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
		String userUUID = Activator.getDefault().getPreferenceStore()
				.getString(IPreferencesConstants.STORED_USER_UUID);
		if (userUUID.equals("")) {
			userUUID = retrieveUser(userEmail).toString();
		}

		for (SensorData data : batch.sensorData) {
			String studentProjectUUID = retrieveStudentProject(
					data.getProjectUri()).toString();
			String requestString = "postSensorData?studentProjectUuid="
					+ studentProjectUUID + "&userUuid=" + userUUID + "&time="
					+ data.timestamp + "&runtime=" + data.runtime + "&tool="
					+ data.tool + "&sensorDataType=" + data.sensorDataType
					+ "&uri=" + data.uri;
			if(data.findProperty("CommitHash") != null)
			{
				requestString += "&commitHash=" + data.findProperty("CommitHash").value;
			}
			Response response = makeRequest(Method.GET, requestString, null);
			if (!response.getStatus().isSuccess()) {
				throw new SensorBaseClientException(response.getStatus());
			}
			String responseText = "";
			try {
				responseText = response.getEntity().getText();
			} catch (IOException e) {
				e.printStackTrace();
			}
			responseText = parseUUID(responseText);
			if (responseText.equals("No user found for that email")
					|| responseText
							.equals("No student project found for that UUID")
					|| responseText.equals("Invalid SensorDataType")) {
				throw new SensorBaseClientException(responseText);
			}
		}
	}

	/**
	 * Throws an unchecked illegal argument exception if the arg is null or
	 * empty.
	 * 
	 * @param arg
	 *            The String that must be non-null and non-empty.
	 */
	private void validateArg(String arg) {
		if ((arg == null) || ("".equals(arg))) {
			throw new IllegalArgumentException(arg
					+ " cannot be null or the empty string.");
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
		Reference reference = new Reference(this.webCatUrl + requestString);
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
		client.setConnectTimeout(milliseconds);
	}

	public void commitSnapshot(RevCommit commit) {
		Representation rep = new ObjectRepresentation<RevCommit>(commit);
		makeRequest(Method.PUT, this.webCatUrl + "/push", rep);
	}
}
