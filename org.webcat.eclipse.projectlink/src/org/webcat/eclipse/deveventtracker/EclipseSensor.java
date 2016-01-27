package org.webcat.eclipse.deveventtracker;

import static org.webcat.eclipse.deveventtracker.EclipseSensorConstants.PROP_CLASS_NAME;
import static org.webcat.eclipse.deveventtracker.EclipseSensorConstants.PROP_CURRENT_METHODS;
import static org.webcat.eclipse.deveventtracker.EclipseSensorConstants.PROP_CURRENT_SIZE;
import static org.webcat.eclipse.deveventtracker.EclipseSensorConstants.PROP_CURRENT_STATEMENTS;
import static org.webcat.eclipse.deveventtracker.EclipseSensorConstants.PROP_CURRENT_TEST_ASSERTIONS;
import static org.webcat.eclipse.deveventtracker.EclipseSensorConstants.PROP_CURRENT_TEST_METHODS;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.webcat.eclipse.deveventtracker.addon.BreakPointerSensor;
import org.webcat.eclipse.deveventtracker.addon.BuildErrorSensor;
import org.webcat.eclipse.deveventtracker.addon.DebugSensor;
import org.webcat.eclipse.deveventtracker.addon.JavaStatementMeter;
import org.webcat.eclipse.deveventtracker.addon.JavaStructureChangeDetector;
import org.webcat.eclipse.deveventtracker.addon.LaunchSensor;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellProperties;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.util.GitIgnoreUtils;

/**
 * Provides all the necessary sensor initialization and collects data in this
 * singleton class. A client can use one static method to get this instance:
 * 
 * <p>
 * A client can set Eclipse sensor by calling <code>getInstance()</code>, ant
 * can use the following process methods: Because of lazy instantiation, any
 * activity was not set until the initial call for <code>getInstance()</code>.
 * </p>
 * 
 * Adapted from Hackystat project.
 * 
 * @author Hongbing Kou
 * @author Takuya Yamashita
 * @author Joseph Luke
 */
public class EclipseSensor {
	/** A singleton instance. */
	private static EclipseSensor theInstance;
	
	public static String IMPORT = "true";
	
	public static boolean POST_HAPPENING = false;

	/**
	 * The number of seconds of the state change after which timer will wake up
	 * again.
	 */
	private long timerStateChangeInterval = 30;

	/**
	 * The number of seconds of the buffer transition after which time will wake
	 * up again.
	 */
	private long timeBuffTransInterval = 5;
	
	private long timerOfflineRecoveryInterval = 10;

	/**
	 * The ITextEdtior instance to hold the active editor's (file's)
	 * information.
	 */
	private ITextEditor activeTextEditor;

	/** The active buffer to hold the buffer size of the active file. */
	private int activeBufferSize;

	/**
	 * The ITextEditor instance to hold the previous active editor's
	 * information.
	 */
	private ITextEditor previousTextEditor;

	/**
	 * The ITextEdtior instance to hold the de-active editor's (file's)
	 * information. to see if several partDeactivated call backs occur in the
	 * same time.
	 */
	private ITextEditor deactivatedTextEditor;

	/**
	 * The threshold buffer size at an file activation to be compared with
	 * activeBufferSize.
	 */
	private int thresholdBufferSize;

	/** The boolean value to check if an previous file is modified. */
	private boolean isModifiedFromFile;

	/** The boolean value to check if the current opened window is active. */
	private boolean isActivatedWindow;

	/** The SensorShell wrapper class for eclipse. */
	private SensorShellWrapper sensorShellWrapper;

	/**
	 * 12 characters hackystat directory key to check if the new sensor shell
	 * should be set.
	 */
	private Timer timer;

	/**
	 * The TimerTask instance to do the task of the state change when the timer
	 * wakes up.
	 */
	private TimerTask stateChangeTimerTask;

	/**
	 * The TimerTask instance to do the task of the buffer transitions when the
	 * timer wakes up.
	 */
	private TimerTask buffTransTimerTask;
	
	private TimerTask offlineRecoveryTimerTask;

	/**
	 * The WindowListerAdapter instance to check if this instance is added or
	 * not.
	 */
	private WindowListenerAdapter windowListener;
	
	private ILaunchManager launchManager;
	private LaunchSensor launchListener;
	
	/** Build error sensor. */
	private BuildErrorSensor buildErrorSensor;

	/** Keep track of the last buffer trans data in case of the repeation. */
	private String latestBuffTrans = "";

	private static Integer JLSVersion = null;

	/**
	 * Provides instantiation of SensorProperties, which has information in the
	 * sensor.properties file, and executes <code>doCommand</code> to activate
	 * sensor. Note that the Eclipse instance is lazily instantiated when static
	 * <code>getInstance()</code> was called.
	 * 
	 * @throws SensorShellException
	 *             If error in getting sensorshell.
	 */
	private EclipseSensor() throws SensorShellException {
		this.timer = new Timer();
		this.stateChangeTimerTask = new StateChangeTimerTask();
		this.buffTransTimerTask = new BuffTransTimertask();
		this.offlineRecoveryTimerTask = new OfflineRecoveryTimerTask();

		// Load sensor's setting.
		this.sensorShellWrapper = new SensorShellWrapper(
				new SensorShellProperties());

		// Adds this EclipseSensorPlugin instance to IResourceChangeListener
		// so that project event and file save event is notified.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// Adds IResourceChangeListener-implemented ResourceChangeAdapter before
		// checking isEnabled
		// for piemontese sensor because this listener is used in piemontese
		// sensor even though
		// the main sensor.properties does not exist in the <hackystat_home>.

		workspace.addResourceChangeListener(new ResourceChangeAdapter(),
				IResourceChangeEvent.POST_CHANGE);

		// Adds element changed listener to get the corresponding change of
		// refactoring.
		JavaCore.addElementChangedListener(new JavaStructureChangeDetector(this));
		initialize();
	}

	/**
	 * Returns the (singleton) EclipseSensor instance. This method is initially
	 * called by EclipseSensorPlugin client class for instantiation.
	 * 
	 * @return The (singleton) instance.
	 * @throws SensorShellException
	 *             If problem occurred in instantiating the sensor.
	 */
	public static synchronized EclipseSensor getInstance()
			throws SensorShellException {
		if (theInstance == null) {
			theInstance = new EclipseSensor();
		}

		return theInstance;
	}

	/**
	 * Initializes sensor and JUnitListener instance if the sensor is enabled.
	 * Note that JUnit listener instance is added only when the instance is not
	 * instantiated.
	 */
	private void initialize() {
		// Sets state change time schedule.
		if (this.stateChangeTimerTask.scheduledExecutionTime() == 0) {
			this.timer.schedule(this.stateChangeTimerTask,
					this.timerStateChangeInterval * 1000,
					this.timerStateChangeInterval * 1000);
		}

		// Sets buffer transition time schedule.
		if (this.buffTransTimerTask.scheduledExecutionTime() == 0) {
			this.timer.schedule(this.buffTransTimerTask,
					this.timeBuffTransInterval * 1000,
					this.timeBuffTransInterval * 1000);
		}
		
		if (this.offlineRecoveryTimerTask.scheduledExecutionTime() == 0) {
			this.timer.schedule(this.offlineRecoveryTimerTask, 
					this.timerOfflineRecoveryInterval * 1000, 
					this.timerOfflineRecoveryInterval * 1000);
		}
		
		registerListeners();
	}

	/**
	 * Provide the initialization of the listeners additions. The Window, Part,
	 * and Document Listener are added. Note that sensor shell should be
	 * instantiated before this method is called because
	 * <code>processActivity()</code> method uses sensor shell instance.
	 */
	private void registerListeners() {
		IWorkbench workbench = Activator.getDefault().getWorkbench();

		// :RESOLVED: JULY 1, 2003
		// Supports the multiple window for sensor collection.
		IWorkbenchWindow[] activeWindows = workbench.getWorkbenchWindows();

		// Check if window listener is not added yet. Otherwise multi instances
		// are notified.
		if (this.windowListener == null) {
			this.windowListener = new WindowListenerAdapter();
			workbench.addWindowListener(new WindowListenerAdapter());
		}

		for (int i = 0; i < activeWindows.length; i++) {
			IWorkbenchPage activePage = activeWindows[i].getActivePage();
			activePage.addPartListener(new PartListenerAdapter());
			IEditorPart activeEditorPart = activePage.getActiveEditor();

			// Adds this EclipseSensorPlugin instance to IDocumentListener
			// only when activeEditorPart is the instance of ITextEditor
			// so that null case is also ignored.
			if (activeEditorPart instanceof ITextEditor) {
				// Sets activeTextEditor. Otherwise a first activated file would
				// not be recorded.
				this.activeTextEditor = (ITextEditor) activeEditorPart;

				// Gets opened file since the initial opened file is not
				// notified from IPartListener.
				URI fileResource = EclipseSensor.this
						.getFileResource(this.activeTextEditor);
				URI projectURI = EclipseSensor.this
						.getProjectURI(this.activeTextEditor);
				final Map<String, String> keyValueMap = new HashMap<String, String>();
				keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Open");
				keyValueMap.put(EclipseSensorConstants.UNIT_TYPE,
						EclipseSensorConstants.FILE);
				keyValueMap.put(EclipseSensorConstants.UNIT_NAME,
						EclipseSensor.this.extractFileName(fileResource));

				this.addDevEvent(EclipseSensorConstants.DEVEVENT_EDIT,
						projectURI, fileResource, keyValueMap, "Opened "
								+ fileResource.toString());

				IDocumentProvider provider = this.activeTextEditor
						.getDocumentProvider();
				IDocument document = provider.getDocument(activeEditorPart
						.getEditorInput());
				
				// Initially sets active buffer and threshold buffer.
				// Otherwise a first activated buffer would not be recorded.
				this.activeBufferSize = document.getLength();
				this.thresholdBufferSize = document.getLength();
				document.addDocumentListener(new DocumentListenerAdapter());
			}
		}
		
		if (this.launchManager == null) {
			this.launchManager = DebugPlugin.getDefault().getLaunchManager();
		}
		
		this.launchListener = new LaunchSensor(this, this.getProjectURI(this.getActiveTextEditor()));
		
		this.launchManager.addLaunchListener(this.launchListener);

		// Handles breakpoint set/unset event.
		IBreakpointManager bpManager = DebugPlugin.getDefault()
				.getBreakpointManager();
		bpManager.addBreakpointListener(new BreakPointerSensor(this));

		// Listens to debug event.
		DebugPlugin.getDefault().addDebugEventListener(new DebugSensor(this));

		// Creates instance to handle build error.
		this.buildErrorSensor = new BuildErrorSensor(this);
	}

	/**
	 * Processes development events that occur within the Eclipse browser. The
	 * Browser event classes will invoke this method to process the development
	 * event and send the data to Web-CAT.
	 * 
	 * @param type
	 *            The type of development event, eg "ProgramUnit:New".
	 * @param fileResource
	 *            The associated path with the event, may be file or directory.
	 * @param moreKeyValueMap
	 *            Additional development event data, eg
	 *            "unit-name=EclipseSensor".
	 * @param message
	 *            DevEvent message to be used for logging and display.
	 */
	public void addDevEvent(String type, URI projectUri, URI fileResource,
			Map<String, String> moreKeyValueMap, String message) {
		// Sometimes projectUri is null when registering listeners, these are
		// not events we want to track.
		if (projectUri == null) {
			return;
		}
		Map<String, String> keyValueMap = new HashMap<String, String>();
		keyValueMap.put("Tool", "Eclipse");
		keyValueMap.put("SensorDataType", "DevEvent");
		keyValueMap.put("Type", type);
		keyValueMap.put("Timestamp",
				new Long(System.currentTimeMillis()).toString());
		keyValueMap.put("Runtime",
				new Long(System.currentTimeMillis()).toString());
		keyValueMap.put("URI", "");
		if (fileResource != null) {
			keyValueMap.put("URI", fileResource.getPath());
		}
		keyValueMap.put("ProjectURI", "");
		if (projectUri != null) {
			keyValueMap.put("ProjectURI", projectUri.getPath());
		}
		if (moreKeyValueMap != null) {
			keyValueMap.putAll(moreKeyValueMap);
		}
		this.sensorShellWrapper.add(keyValueMap, message);
	}

	/**
	 * Extracts file name from a file resource URI.
	 * 
	 * @param fileResource
	 *            File name path.
	 * @return File name.
	 */
	public String extractFileName(URI fileResource) {
		String fileString = fileResource.toString();
		if (fileString != null && fileString.indexOf('/') > 0) {
			return fileString.substring(fileString.lastIndexOf('/') + 1);
		} else {
			return fileString;
		}
	}

	/**
	 * Keep track of the latest state change file to avoid sending out repeated
	 * data.
	 */
	private URI latestStateChangeFile = null;
	/** Latest file size. */
	private int latestStateChangeFileSize = 0;
	/** Class name to file URI map. */
	private Map<String, URI> class2FileMap = new HashMap<String, URI>();

	public void processOfflineRecovery() {
		try {
			this.sensorShellWrapper.getShell().recoverOfflineData();
		} catch (SensorShellException e) {
			Activator.getDefault().log("Error recovering offline data", e);
		}
	}
	
	/**
	 * Process the state change activity whose element consists of the
	 * (absolute) file name and its buffer size (or file size).
	 */
	public void processStateChangeActivity() {
		if (this.activeTextEditor == null) {
			return;
		}

		URI fileResource = this.getFileResource(this.activeTextEditor);
		URI projectURI = EclipseSensor.this
				.getProjectURI(this.activeTextEditor);
		if (fileResource != null) {
			int activeBufferSize = this.activeBufferSize;

			// Will not send out data if there is no state change at all.
			if (fileResource.equals(this.latestStateChangeFile)
					&& this.latestStateChangeFileSize == activeBufferSize) {
				return;
			}

			// Makes up state change data
			// StringBuffer statechangeData = new StringBuffer();
			// statechangeData.append(activeFileName);

			// Calculate test methods and assertions if it is a java file.
			IFileEditorInput fileEditorInput = (IFileEditorInput) this.activeTextEditor
					.getEditorInput();
			IFile file = fileEditorInput.getFile();
			if (file.exists()) {
				Map<String, String> statechangeMetricsMap = computeFileMetrics(file);

				// Status message for display
				StringBuffer msgBuf = new StringBuffer();
				msgBuf.append("statechange : ").append(file.getName());
				msgBuf.append(" [").append(
						statechangeMetricsMap.get(PROP_CURRENT_SIZE));

				String className = statechangeMetricsMap.get(PROP_CLASS_NAME);
				if (className != null) {
					msgBuf.append(", ").append(className);
				}

				String currentMethods = statechangeMetricsMap
						.get(PROP_CURRENT_METHODS);
				if (currentMethods != null) {
					msgBuf.append(", methods=").append(currentMethods);
				}

				String currentStatements = statechangeMetricsMap
						.get(PROP_CURRENT_STATEMENTS);
				if (currentStatements != null) {
					msgBuf.append(", stms=").append(currentStatements);
				}
				msgBuf.append(']');

				this.addDevEvent(EclipseSensorConstants.DEVEVENT_EDIT,
						projectURI, fileResource, statechangeMetricsMap,
						msgBuf.toString());
			}

			this.latestStateChangeFile = fileResource;
			this.latestStateChangeFileSize = activeBufferSize;
		}
	}

	/**
	 * Computes file metrics.
	 * 
	 * @param file
	 *            File.
	 * @return Map that contains key-value pairs if object metrics.
	 */
	private Map<String, String> computeFileMetrics(IFile file) {
		// Adds statechange DevEvent.
		Map<String, String> statechangeKeyValueMap = new HashMap<String, String>();
		statechangeKeyValueMap.put(EclipseSensorConstants.SUBTYPE,
				"StateChange");
		// Process file size
		String sizeString = String.valueOf(activeBufferSize);
		statechangeKeyValueMap.put(PROP_CURRENT_SIZE, sizeString);

		if (file.getName().endsWith(EclipseSensorConstants.JAVA_EXT)) {
			// Fully qualified class path
			String className = this.getFullyQualifedClassName(file);
			statechangeKeyValueMap.put(PROP_CLASS_NAME, className);

			// Measure java file.
			JavaStatementMeter testCounter = measureJavaFile(file);

			this.class2FileMap.put(className, file.getLocationURI());
			String methodCountString = String.valueOf(testCounter
					.getNumOfMethods());
			statechangeKeyValueMap.put(PROP_CURRENT_METHODS, methodCountString);

			String statementCountString = String.valueOf(testCounter
					.getNumOfStatements());
			statechangeKeyValueMap.put(PROP_CURRENT_STATEMENTS,
					statementCountString);

			// Number of test method and assertion statements.
			if (testCounter.hasTest()) {
				String testMethodCount = String.valueOf(testCounter
						.getNumOfTestMethods());
				statechangeKeyValueMap.put(PROP_CURRENT_TEST_METHODS,
						testMethodCount);

				String testAssertionCount = String.valueOf(testCounter
						.getNumOfTestAssertions());
				statechangeKeyValueMap.put(PROP_CURRENT_TEST_ASSERTIONS,
						testAssertionCount);
			}
		}

		return statechangeKeyValueMap;
	}

	/**
	 * Calculates java file unit test information.
	 * 
	 * @param file
	 *            IFile instance to a java file.
	 * 
	 * @return UnitTestCounter instance to this java file.
	 */
	private JavaStatementMeter measureJavaFile(IFile file) {
		// Compute number of tests and assertions to this file.
		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file);
		ASTParser parser = ASTParser.newParser(EclipseSensor.getJLSVersion());
		parser.setSource(cu);
		parser.setResolveBindings(true);

		ASTNode root = parser.createAST(null);
		JavaStatementMeter counter = new JavaStatementMeter();
		root.accept(counter);

		return counter;
	}

	private static int getJLSVersion() {
		if (EclipseSensor.JLSVersion == null) {
			try {
				EclipseSensor.JLSVersion = 2;
				Class<?> c = Class.forName("org.eclipse.jdt.core.dom.AST");
				int highestVersionNumber = 2;
				int highestVersionFieldValue = 2;
				String currentVersionNumberString;
				int currentVersionNumberInt;
				for (Field f : c.getFields()) {
					if (f.getName().startsWith("JLS")
							&& !f.getName().endsWith("_INTERNAL")) {
						currentVersionNumberString = f.getName()
								.replace("JLS", "").replace("_INTERNAL", "");
						currentVersionNumberInt = Integer
								.parseInt(currentVersionNumberString);
						if (currentVersionNumberInt > highestVersionNumber) {
							highestVersionNumber = currentVersionNumberInt;
							highestVersionFieldValue = f.getInt(null);
						}
					}
				}
				EclipseSensor.JLSVersion = highestVersionFieldValue;
			} catch (ClassNotFoundException e) {
				Activator.getDefault().log(e);
			} catch (IllegalArgumentException e) {
				Activator.getDefault().log(e);
			} catch (IllegalAccessException e) {
				Activator.getDefault().log(e);
			}
		}
		return EclipseSensor.JLSVersion;
	}

	/**
	 * Process the buffer transition to check if the current buffer is visiting
	 * a file and if that file is different from the file visited by the buffer
	 * during the last wakeup. Its element consists of the the (absolute) file
	 * name (or last-time-visited file name) from which an user is visiting, the
	 * (absolute) file name (or current-visiting file name) to which the user is
	 * visiting, and the modification status of the last-time-visited file.
	 */
	public void processBuffTrans() {
		// check if BufferTran property is enable
		if (this.activeTextEditor == null || (this.previousTextEditor == null)) {
			return;
		}
		URI toFile = this.getFileResource(this.activeTextEditor);
		URI projectURI = EclipseSensor.this
				.getProjectURI(this.activeTextEditor);
		URI fromFile = this.getFileResource(this.previousTextEditor);
		if (fromFile != null && toFile != null && !toFile.equals(fromFile)) {
			String buffTrans = fromFile.toString() + "->" + toFile.toString();
			// :RESOVED: 5/21/04 ISSUE:HACK109
			if (!latestBuffTrans.equals(buffTrans)) {
				HashMap<String, String> buffTranKeyValuePairs = new HashMap<String, String>();

				buffTranKeyValuePairs.put(EclipseSensorConstants.SUBTYPE,
						"BufferTransition");
				buffTranKeyValuePairs
						.put("From-Buff-Name", fromFile.toString());
				buffTranKeyValuePairs.put("To-Buff-Name", toFile.toString());
				buffTranKeyValuePairs.put("Modified",
						String.valueOf(this.isModifiedFromFile));

				String message = "BuffTrans : "
						+ this.extractFileName(fromFile) + " --> "
						+ this.extractFileName(toFile);

				this.addDevEvent(EclipseSensorConstants.DEVEVENT_EDIT,
						projectURI, toFile, buffTranKeyValuePairs, message);
				latestBuffTrans = buffTrans;
			}
		}
	}

	/**
	 * Gets the fully qualified class name for an active file. For example, its
	 * value is foo.bar.Baz.
	 * 
	 * @param file
	 *            Get fully qualified class file.
	 * @return The fully qualified class name. For example,foo.bar.Baz.
	 */
	private String getFullyQualifedClassName(IFile file) {
		String fullClassName = "";
		if (file.exists()
				&& file.getName().endsWith(EclipseSensorConstants.JAVA_EXT)) {
			ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore
					.create(file);
			String className = compilationUnit.getElementName();
			if (className.endsWith(EclipseSensorConstants.JAVA_EXT)) {
				className = className.substring(0, className.length() - 5);
			}

			try {
				IPackageDeclaration[] packageDeclarations = compilationUnit
						.getPackageDeclarations();
				// Should only have one package declaration
				if (packageDeclarations == null
						|| packageDeclarations.length == 0) {
					fullClassName = className;
				} else {
					fullClassName = packageDeclarations[0].getElementName()
							+ '.' + className;
				}
			} catch (JavaModelException e) {
				// This exception will be thrown if user is working on a Java
				// but did not open
				// it with "Java Perspective". Thus, the Java Model does not
				// exist to parse
				// Java files. So we only log our exception while Eclipse's Java
				// Perspective exists.
				if (!e.isDoesNotExist()) {
					Activator.getDefault().log(file.getName(), e);
				}
			}
		}

		return fullClassName;
	}

	private URI getProjectURI(ITextEditor textEditor) {
		if (textEditor != null) {
			IEditorInput editorInput = textEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput input = (IFileEditorInput) editorInput;
				IFile file = input.getFile();
				if (file != null) {
				    return file.getProject().getLocationURI();
				}
			}
		}

		// Return a dummy unknown resource if can not resolve the file being
		// edited.
		return URI.create("file:///Unknown");
	}

	/**
	 * Gets the URI of the associated file. It will return file URI in format
	 * such as file:/D:/cvs/foobarproject/src/foo/bar/Bar.java. When a
	 * developers is not editing an active file, this method will return a dummy
	 * file resource.
	 * 
	 * @param textEditor
	 *            A ITextEditor instance form which the file name is retrieved.
	 * @return The fully qualified file name. For example,
	 *         C:\cvs\foobarproject\src\foo\bar\Bar.java.
	 */
	private URI getFileResource(ITextEditor textEditor) {
		if (textEditor != null) {
			IEditorInput editorInput = textEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput input = (IFileEditorInput) editorInput;
				IFile file = input.getFile();
				if (file != null) {
					return file.getLocationURI();
				}
			}
		}

		// Return a dummy unknown resource if can not resolve the file being
		// edited.
		return URI.create("file:///Unknown");
	}

	/**
	 * Gets current active editor.
	 * 
	 * @return Current editor.
	 */
	public ITextEditor getActiveTextEditor() {
		return this.activeTextEditor;
	}

	/**
	 * Gets file being edited or was just edited.
	 * 
	 * @return File being edited.
	 */
	public URI getActiveFile() {
		return getFileResource(this.activeTextEditor);
	}

	/**
	 * Gets fully qualified path from object.
	 * 
	 * @param className
	 *            Class name.
	 * @return File resour URI.
	 */
	public URI getObjectFile(String className) {
		return this.class2FileMap.get(className);
	}

	/**
	 * Provides the IWindowListener-implemented class to catch the
	 * "Browser activated", "Browser closing" event. This inner class is
	 * designed to be used by the outer EclipseSensor class.
	 * 
	 * @author Takuya Yamashita
	 * @version $Id: EclipseSensor.java,v 1.1.1.1 2005/10/20 23:56:56 johnson
	 *          Exp $
	 */
	private class WindowListenerAdapter implements IWindowListener {
		/**
		 * Provides manipulation of browser open status due to implement
		 * <code>IWindowListener</code>. This method must not be called by
		 * client because it is called by platform. Do nothing for Eclipse
		 * sensor so far.
		 * 
		 * @param window
		 *            An IWorkbenchWindow instance to be triggered when a window
		 *            is activated.
		 */
		public void windowActivated(IWorkbenchWindow window) {
			IEditorPart activeEditorPart = window.getActivePage()
					.getActiveEditor();
			if (activeEditorPart instanceof ITextEditor) {
				EclipseSensor.this.activeTextEditor = (ITextEditor) activeEditorPart;
				ITextEditor editor = EclipseSensor.this.activeTextEditor;
				IDocumentProvider provider = editor.getDocumentProvider();
				IDocument document = provider.getDocument(editor
						.getEditorInput());
				document.addDocumentListener(new DocumentListenerAdapter());
				int activeBufferSize = provider.getDocument(
						editor.getEditorInput()).getLength();

				// BuffTrans: Copy the new active file size to the threshold
				// buffer size .
				EclipseSensor.this.thresholdBufferSize = activeBufferSize;
				EclipseSensor.this.activeBufferSize = activeBufferSize;
			}
		}

		/**
		 * Provides manipulation of browser close status due to implement
		 * <code>IWindowListener</code>. This method must not be called by
		 * client because it is called by platform. Whenever window is closing,
		 * set all the current active file to process file metrics, and then try
		 * to send them to server.
		 * 
		 * @param window
		 *            An IWorkbenchWindow instance to be triggered when a window
		 *            is closed.
		 */
		public void windowClosed(IWorkbenchWindow window) {
			EclipseSensor.this.sensorShellWrapper.send();
		}

		/**
		 * Provides manipulation of browser deactivation status due to implement
		 * <code>IWindowListener</code>. This method must not be called by
		 * client because it is called by platform. Do nothing for Eclipse
		 * sensor so far.
		 * 
		 * @param window
		 *            An IWorkbenchWindow instance to be triggered when a window
		 *            is deactivated.
		 */
		public void windowDeactivated(IWorkbenchWindow window) {
			EclipseSensor.this.isActivatedWindow = false;
			IEditorPart activeEditorPart = window.getActivePage()
					.getActiveEditor();
			if (activeEditorPart instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) activeEditorPart;
				IDocumentProvider provider = editor.getDocumentProvider();

				// provider could be null if the text editor is closed before
				// this method is called.
				EclipseSensor.this.previousTextEditor = editor;
				int fromFileBufferSize = provider.getDocument(
						editor.getEditorInput()).getLength();

				// Check if a threshold buffer is either dirty or
				// not the same as the current from file buffer size;
				EclipseSensor.this.isModifiedFromFile = (editor.isDirty() || (EclipseSensor.this.thresholdBufferSize != fromFileBufferSize));
			}
		}

		/**
		 * Provides manipulation of browser window open status due to implement
		 * <code>IWindowListener</code>. This method must not be called by
		 * client because it is called by platform. Do nothing for Eclipse
		 * sensor so far.
		 * 
		 * @param window
		 *            An IWorkbenchWindow instance to be triggered when a window
		 *            is opened.
		 */
		public void windowOpened(IWorkbenchWindow window) {
			EclipseSensor.this.registerListeners();
		}
	}

	/**
	 * Provides the IPartListener-implemented class to catch "part opened",
	 * "part closed" event as well as setting active editor part to the
	 * activeTextEditor instance and setting active buffer size of the
	 * activeBufferSize field of the EclipseSensor class. Note that methods are
	 * called by the following order:
	 * <ol>
	 * <li>partClosed() or partOpened()</li>
	 * <li>partDeactivated()</li>
	 * <li>partActivate() if any</li>
	 * </ol>
	 * 
	 * @author Takuya Yamashita
	 * @version $Id: EclipseSensor.java,v 1.1.1.1 2005/10/20 23:56:56 johnson
	 *          Exp $
	 */
	private class PartListenerAdapter implements IPartListener {
		/**
		 * Provides manipulation of browser part activation status due to
		 * implement <code>IPartListener</code>. This method must not be called
		 * by client because it is called by platform. Do nothing for Eclipse
		 * sensor so far.
		 * 
		 * @param part
		 *            An IWorkbenchPart instance to be triggered when a part is
		 *            activated.
		 */
		public void partActivated(IWorkbenchPart part) {

			if (part instanceof ITextEditor) {
				EclipseSensor.this.isActivatedWindow = true;
				EclipseSensor.this.activeTextEditor = (ITextEditor) part;
				ITextEditor editor = EclipseSensor.this.activeTextEditor;
				IDocumentProvider provider = editor.getDocumentProvider();
				IDocument document = provider.getDocument(editor
						.getEditorInput());
				document.addDocumentListener(new DocumentListenerAdapter());
				int activeBufferSize = provider.getDocument(
						editor.getEditorInput()).getLength();

				// BuffTrans: Copy the new active file size to the threshold
				// buffer size .
				EclipseSensor.this.thresholdBufferSize = activeBufferSize;
				EclipseSensor.this.activeBufferSize = activeBufferSize;
			}
		}

		/**
		 * Provides manipulation of browser part brought-to-top status due to
		 * implement <code>IPartListener</code>. This method must not be called
		 * by client because it is called by platform. Do nothing for Eclipse
		 * sensor so far.
		 * 
		 * @param part
		 *            An IWorkbenchPart instance to be triggered when a part is
		 *            brought to top.
		 */
		public void partBroughtToTop(IWorkbenchPart part) {
			// not supported in Eclipse Sensor.
		}

		/**
		 * Provides manipulation of browser part brought-to-top status due to
		 * implement <code>IPartListener</code>. This method must not be called
		 * by client because it is called by platform. Whenever part is closing,
		 * check whether or not part is the instance of <code>IEditorPart</code>
		 * , if so, set process activity as <code>ActivityType.CLOSE_FILE</code>
		 * with its absolute path.
		 * 
		 * @param part
		 *            An IWorkbenchPart instance to be triggered when a part is
		 *            closed.
		 */
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof ITextEditor) {
				URI fileResource = EclipseSensor.this
						.getFileResource((ITextEditor) part);
				URI projectURI = EclipseSensor.this
						.getProjectURI((ITextEditor) part);
				Map<String, String> keyValueMap = new HashMap<String, String>();
				keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Close");
				if (fileResource != null
						&& fileResource.toString().endsWith(
								EclipseSensorConstants.JAVA_EXT)) {
					keyValueMap.put("Language", "java");
				}
				if (fileResource != null) {
					keyValueMap.put(EclipseSensorConstants.UNIT_TYPE,
							EclipseSensorConstants.FILE);
					keyValueMap.put(EclipseSensorConstants.UNIT_NAME,
							EclipseSensor.this.extractFileName(fileResource));
					EclipseSensor.this.addDevEvent(
							EclipseSensorConstants.DEVEVENT_EDIT, projectURI,
							fileResource, keyValueMap, fileResource.toString());
				}
				IEditorPart activeEditorPart = part.getSite().getPage()
						.getActiveEditor();
				if (activeEditorPart == null) {
					EclipseSensor.this.activeTextEditor = null;
				}
			}
		}

		/**
		 * Provides manipulation of browser part deactivation status due to
		 * implement <code>IPartListener</code>. This method must not be called
		 * by client because it is called by platform. Sets active text editor
		 * to be null when the text editor part is deactivated.
		 * 
		 * @param part
		 *            An IWorkbenchPart instance to be triggered when a part is
		 *            deactivated.
		 */
		public void partDeactivated(IWorkbenchPart part) {
			if (part instanceof ITextEditor
					&& !part.equals(EclipseSensor.this.deactivatedTextEditor)) {
				EclipseSensor.this.deactivatedTextEditor = (ITextEditor) part;
				if (EclipseSensor.this.isActivatedWindow) {
					IEditorPart activeEditorPart = part.getSite().getPage()
							.getActiveEditor();

					// Sets activeTextEdtior to be null only when there is no
					// more active editor.
					// Otherwise the case that the non text editor part is
					// active causes the activeTextEditor
					// to be null so that sensor is not collected after that.
					if (activeEditorPart == null) {
						EclipseSensor.this.activeTextEditor = null;
					}

					// BuffTrans to get the toFrom buffer size.
					ITextEditor editor = (ITextEditor) part;
					IDocumentProvider provider = editor.getDocumentProvider();

					// provider could be null if the text editor is closed
					// before this method is called.
					EclipseSensor.this.isModifiedFromFile = false;
					EclipseSensor.this.previousTextEditor = null;

					if (provider != null) {
						EclipseSensor.this.previousTextEditor = editor;
						int fromFileBufferSize = provider.getDocument(
								editor.getEditorInput()).getLength();

						// Check if a threshold buffer is either dirty or
						// not the same as the current from file buffer size;
						EclipseSensor.this.isModifiedFromFile = (editor
								.isDirty() || (EclipseSensor.this.thresholdBufferSize != fromFileBufferSize));
					}
				} else {
					EclipseSensor.this.isActivatedWindow = true;
				}
			}
		}

		/**
		 * Provides manipulation of browser part brought-to-top status due to
		 * implement <code>IPartListener</code>. This method must not be called
		 * by client because it is called by platform. Whenever part is opened,
		 * check whether or not part is the instance of <code>IEditorPart</code>
		 * , if so, set process activity as <code>ActivityType.OPEN_FILE</code>
		 * with its absolute path.
		 * 
		 * @param part
		 *            An IWorkbenchPart instance to be triggered when part is
		 *            opened.
		 */
		public void partOpened(IWorkbenchPart part) {
			if (part instanceof ITextEditor
					&& !part.equals(EclipseSensor.this.activeTextEditor)) {
				EclipseSensor.this.activeTextEditor = (ITextEditor) part;
				URI fileResource = EclipseSensor.this
						.getFileResource((ITextEditor) part);
				URI projectURI = EclipseSensor.this
						.getProjectURI((ITextEditor) part);

				Map<String, String> keyValueMap = new HashMap<String, String>();
				keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Open");
				keyValueMap.put(EclipseSensorConstants.UNIT_TYPE,
						EclipseSensorConstants.FILE);
				keyValueMap.put(EclipseSensorConstants.UNIT_NAME,
						EclipseSensor.this.extractFileName(fileResource));
				EclipseSensor.this.addDevEvent(
						EclipseSensorConstants.DEVEVENT_EDIT, projectURI,
						fileResource, keyValueMap, fileResource.toString());
			}
		}
	}

	/**
	 * Provides IDocuementListener-implemented class to set an active buffer
	 * size when a document is being edited.
	 * 
	 * @author Takuya Yamashita
	 * @version $Id: EclipseSensor.java,v 1.1.1.1 2005/10/20 23:56:56 johnson
	 *          Exp $
	 */
	private class DocumentListenerAdapter implements IDocumentListener {
		/**
		 * Do nothing right now. Just leave it due to implementation of
		 * IDocumentationListener.
		 * 
		 * @param event
		 *            An event triggered when a document is about to be changed.
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			// not supported in Eclipse Sensor.
		}

		/**
		 * Provides the invocation of DeltaResource.setFileSize(long fileSize)
		 * method in order to get buffer size. This method is called every
		 * document change since this EclipseSensorPlugin instance was added to
		 * IDocumentLister. Since this method, the current buffer size of an
		 * active file could be grabbed.
		 * 
		 * @param event
		 *            An event triggered when a document is changed.
		 */
		public void documentChanged(DocumentEvent event) {
			EclipseSensor.this.activeBufferSize = event.getDocument()
					.getLength();
		}
	}

	/**
	 * Provides "Open Project, "Close Project", and "Save File" events. Note
	 * that this implementing class uses Visitor pattern so that key point to
	 * gather these event information is inside the visitor method which is
	 * implemented from <code>IResourceDeltaVisitor</code> class.
	 * 
	 * @author Takuya Yamashita
	 * @version $Id: EclipseSensor.java,v 1.1.1.1 2005/10/20 23:56:56 johnson
	 *          Exp $
	 */
	private class ResourceChangeAdapter implements IResourceChangeListener,
			IResourceDeltaVisitor {
		/**
		 * Provides manipulation of IResourceChangeEvent instance due to
		 * implement <code>IResourceChangeListener</code>. This method must not
		 * be called by client because it is called by platform when resources
		 * are changed.
		 * 
		 * @param event
		 *            A resource change event to describe changes to resources.
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			if (((event.getType() & IResourceChangeEvent.POST_CHANGE) != 0)) {
				// ||
				// ((event.getType() & IResourceChangeEvent.POST_AUTO_BUILD) !=
				// 0)) {
				try {
					IResourceDelta rootDelta = event.getDelta();

					// Accepts the class instance to let the instance be able to
					// visit resource delta.
					rootDelta.accept(this);
				} catch (CoreException e) {
					Activator.getDefault().log(e);
				}
			}
		}

		/**
		 * Provides visitor pattern due to implement
		 * <code>IResourceDeltaVisitor</code>. This method must not be called by
		 * client because it is called by EclipseSensorPlugin instance. Note
		 * that <code>true</code> is returned if the parameter of IResourceDelta
		 * instance has children. <code>false</code> is returned when either
		 * Project is opened, closed, or file is saved because no more traverse
		 * of children of the IResourceDelta instance is needed.
		 * 
		 * @param delta
		 *            IResourceDelta instance to contains delta resource.
		 * @return true if the resource delta's children should be visited;
		 *         false if they should be skipped.
		 * @throws CoreException
		 *             if the visit fails for some reason.
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			int flag = delta.getFlags();
			int kind = delta.getKind();

			// If there is compilation problem with the current java file then
			// send out the activity data.
			if ((flag & IResourceDelta.MARKERS) != 0
					&& EclipseSensor.this.buildErrorSensor != null) {
				EclipseSensor.this.buildErrorSensor.findBuildProblem(delta);
			}

			// :RESOLVED: 26 May 2003
			// Note that the 147456 enumeration type is not listed in the
			// IResourceDelta static filed.
			// However, its number is generated when Project is either opened or
			// closed so that
			// it is checked in the logical condition.
			if (resource instanceof IProject
					&& ((flag == IResourceDelta.OPEN) || (flag == 147456))) {
				IProject project = resource.getProject();
				String projectName = project.getName();
				URI projectResource = project.getFile(".project")
						.getLocationURI();
				URI projectUri = project.getLocationURI();

				Map<String, String> keyValueMap = new HashMap<String, String>();
				keyValueMap.put(EclipseSensorConstants.UNIT_TYPE, "project");
				keyValueMap.put(EclipseSensorConstants.UNIT_NAME, projectName);

				if (((IProject) resource).isOpen()) {
					if (Boolean.parseBoolean(EclipseSensor.IMPORT)) {
						keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Import");
						EclipseSensor.IMPORT = "false";
					} else {
						keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Open");
					}
					
					EclipseSensor.this.addDevEvent(
							EclipseSensorConstants.DEVEVENT_EDIT, projectUri,
							projectResource, keyValueMap,
							projectResource.toString());

				} else {
					keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Close");
					EclipseSensor.this.addDevEvent(
							EclipseSensorConstants.DEVEVENT_EDIT, projectUri,
							projectResource, keyValueMap,
							projectResource.toString());
				}
				return false;
			}
			if ((kind == IResourceDelta.CHANGED)
					&& (flag == IResourceDelta.CONTENT)
					&& resource instanceof IFile) {
				if (resource.getLocation().toString()
						.endsWith(EclipseSensorConstants.JAVA_EXT)) {
					IFile file = (IFile) resource;

					Map<String, String> keyValueMap = new HashMap<String, String>();

					keyValueMap.put("Language", "java");
					keyValueMap.put(EclipseSensorConstants.UNIT_TYPE,
							EclipseSensorConstants.FILE);

					// Fully qualified class path
					String className = EclipseSensor.this
							.getFullyQualifedClassName(file);
					keyValueMap.put("Class-Name", className);

					// Size of the file in buffer
					String bufferSize = String.valueOf(activeBufferSize);
					keyValueMap.put("Current-Size", bufferSize);

					// Measure java file.
					JavaStatementMeter testCounter = measureJavaFile(file);
					testCounter = measureJavaFile(file);
					String methodCount = String.valueOf(testCounter
							.getNumOfMethods());
					keyValueMap.put("Current-Methods", methodCount);

					String statementCount = String.valueOf(testCounter
							.getNumOfStatements());
					keyValueMap.put("Current-Statements", statementCount);

					// Number of test method and assertion statements.
					if (testCounter.hasTest()) {
						String testMethodCount = String.valueOf(testCounter
								.getNumOfTestMethods());
						keyValueMap
								.put("Current-Test-Methods", testMethodCount);

						String testAssertionCount = String.valueOf(testCounter
								.getNumOfTestAssertions());
						keyValueMap.put("Current-Test-Assertions",
								testAssertionCount);
					}

					URI fileResource = file.getLocationURI();
					URI projectURI = file.getProject().getLocationURI();
					
					if (file.getName().contains("test") || file.getName().contains("Test")) {
						keyValueMap.put("TestCodeEdit", "true");
					} else {
						keyValueMap.put("TestCodeEdit", "false");
					}

					StringBuffer message = new StringBuffer("Save File");
					message.append(" : ").append(
							EclipseSensor.this.extractFileName(fileResource));

					keyValueMap.put(EclipseSensorConstants.SUBTYPE, "Save");
					String commitMessage = "File " + fileResource.getPath()
							+ " changed at "
							+ new Date(System.currentTimeMillis());
					ObjectId hash = EclipseSensor.this.commitSnapshot(
							projectURI.getPath(), commitMessage);
					if (hash != null) {
						keyValueMap.put("CommitHash", hash.getName());
					}
					EclipseSensor.this.addDevEvent(
							EclipseSensorConstants.DEVEVENT_EDIT, projectURI,
							fileResource, keyValueMap, message.toString());

				}

				// Visit the children because it is not necessary for the saving
				// file to be only one file.
				return true;
			}
			return true; // visit the children
		}
	}

	/**
	 * Stops the Eclipse sensor and quits sensorshell.
	 * 
	 */
	public void stop() {
		this.sensorShellWrapper.quit();
		SensorBaseClient.getInstance().stopClient();
	}

	/**
	 * Commits a snapshot (via the SensorBaseClient) of the given project with
	 * the given message. This snapshot is pushed to the server if connection
	 * permits.
	 * 
	 * @param projectUri
	 *            The path of the project to commit.
	 * @param message
	 *            The commit message to use.
	 * @return The commitHash of the local commit.
	 */
	public ObjectId commitSnapshot(String projectUri, String message) {
		// Here, we check to see if we have a local repository already.
		// Otherwise, we need to create one.
		if (projectUri != null) {
			File localRepoDir = new File(projectUri);
			if (!localRepoDir.isDirectory()) {
				localRepoDir.mkdirs();
			}
			// If the directory exists, we need to pull form the server before
			// we push.
			boolean needsPull = !(new File(projectUri, "/.git").isDirectory());
			// If we have a .needspull file, also pass true for needsPull and
			// delete the file.
			if (!needsPull) {
				File needsPullFile = new File(projectUri, "/.needspull");
				if (needsPullFile.exists()) {
					needsPull = true;
					needsPullFile.delete();
				}
			}
			try {
				Git git = Git.init().setDirectory(localRepoDir).call();

				// Create default .gitignore file if it doesn't already exist
				GitIgnoreUtils.writeToGitIgnore(projectUri);

				// Add all files in the project directory
				git.add().addFilepattern(".").call();

				// Actual commit
				RevCommit commit = SensorBaseClient.getInstance()
						.commitSnapshot(projectUri, git, message, needsPull);
				git.close();
				if (commit != null) {
					return commit.getId();
				}
			} catch (NoFilepatternException e) {
				Activator.getDefault().log(e);
			} catch (GitAPIException e) {
				Activator.getDefault().log(e);
			}
		}
		return null;
	}
}