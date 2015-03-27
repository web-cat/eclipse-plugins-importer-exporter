package org.webcat.eclipse.deveventtracker.sensorshell;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;


/**
 * Provides Hackystat sensors with access to standard Hackystat sensorshell properties.  These 
 * properties are generally stored in ~/.hackystat/sensorshell/sensorshell.properties.
 * This class manages access to those files for sensors, 
 * performs type conversions on the String-based properties when appropriate, and sets default
 * values when the properties are missing or incorrectly specified.
 * <p>
 * See the public static final Strings for descriptions of the "standard" Hackystat sensorshell
 * properties.
 * 
 * @author Philip M. Johnson, Aaron A. Kagawa
 */
public class SensorShellProperties {
  
  /**
   * The property key retrieving an URL indicating the location of the SensorBase host. 
   * This a required property; if not supplied, instantiation will fail.
   * Example: "http://dasha.ics.hawaii.edu:9876/sensorbase".
   * No default value. 
   */
  public static final String SENSORSHELL_SENSORBASE_HOST_KEY = "sensorshell.sensorbase.host";
  
  /**
   * The property key retrieving the user account associated with the SensorBase.
   * This a required property; if not supplied, instantiation will fail.
   * Example: "johnson@hawaii.edu".
   * No default value.
   */
  public static final String SENSORSHELL_SENSORBASE_USER_KEY = "sensorshell.sensorbase.user";
  
  /**
   * The property key retrieving the timeout value (in seconds) for SensorShell HTTP requests.
   * Default: "10".
   */
  public static final String SENSORSHELL_TIMEOUT_KEY = "sensorshell.timeout";
  
  /**
   * The property key retrieving the timeout value (in seconds) for SensorShell 'PING' requests.
   * Default: "2".
   */
  public static final String SENSORSHELL_PING_TIMEOUT_KEY = "sensorshell.timeout.ping";
  
  /**
   * The property key retrieving a boolean indicating whether the MultiSensorShell is enabled.
   * If true, offline recovery and storage will be automatically disabled.
   * Default: "false".
   */
  public static final String SENSORSHELL_MULTISHELL_ENABLED_KEY = "sensorshell.multishell.enabled";
  
  
  /**
   * The property key retrieving an integer indicating the number of shells to instantiate when 
   * the MultiSensorShell is enabled.
   * Default: "10".
   */
  public static final String SENSORSHELL_MULTISHELL_NUMSHELLS_KEY = 
    "sensorshell.multishell.numshells";
  
  /**
   * The property key retrieving an integer indicating the number of instances to send to a single
   * shell in the MultiSensorShell at once. We make this one less than the default multishell 
   * maxbuffer value so that the non-blocking autosend has chance to run rather than the 
   * blocking send() resulting from hitting the maxbuffer size. 
   * Default: "499".
   */
  public static final String SENSORSHELL_MULTISHELL_BATCHSIZE_KEY = 
    "sensorshell.multishell.batchsize";
  
  /**
   * The property key retrieving an integer indicating the maximum number of instances to buffer
   * in each shell in the MultiSensorShell before a blocking send() is invoked.
   * Default: "500".
   */
  public static final String SENSORSHELL_MULTISHELL_MAXBUFFER_KEY = 
    "sensorshell.multishell.maxbuffer";
  
  /**
   * The property key retrieving a double indicating how many minutes between autosends of 
   * sensor data for each shell in a MultiShell.  If "0.0", then sensor data is not sent unless the
   * client invokes the send() method explicitly. This value is typically around 0.05 to 0.10 
   * (i.e. 3 to 6 seconds). 
   * Default: "0.05".
   */
  public static final String SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY = 
    "sensorshell.multishell.autosend.timeinterval";
  
  /**
   * The property key retrieving a boolean indicating if data will be cached locally if the 
   * SensorBase cannot be contacted. 
   * Default: "true" if multi-shell is disabled. If multishell is enabled, then set to false. 
   */
  public static final String SENSORSHELL_OFFLINE_CACHE_ENABLED_KEY = 
    "sensorshell.offline.cache.enabled";
  
  /**
   * The property key retrieving a boolean indicating if offline data will be recovered at startup
   * if the SensorBase can be contacted.
   * Default: "true" if multi-shell is disabled. If multishell is enabled, then set to false. 
   */
  public static final String SENSORSHELL_OFFLINE_RECOVERY_ENABLED_KEY = 
    "sensorshell.offline.recovery.enabled";
  
  /**
   * The property key retrieving an integer indicating the number of seconds between "wakeups" of
   * tool subprocesses that check for state changes. Typically used by editors such as Emacs or
   * Eclipse to provide a standard measure of developer activity. 
   * Default: "30".
   */
  public static final String SENSORSHELL_STATECHANGE_INTERVAL_KEY = 
    "sensorshell.statechange.interval";
  
  /**
   * The property key retrieving a double indicating how many minutes between autosends of 
   * sensor data when not in MultiShell mode.  If "0.0", then sensor data is not sent unless the 
   * client invokes the send() method explicitly. This value typically varies 
   * from 1.0 to 10.0 (i.e. 1 to 10 minutes).  
   * Default: "1.0".
   */
  public static final String SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY = 
    "sensorshell.autosend.timeinterval";
  
  /**
   * The property key retrieving an integer indicating the maximum number of sensor instances to 
   * buffer locally between autosends of sensor data.  If "0", then no maximum size is defined.
   * Default: "250". Note that this is the value used when multishell is not enabled, otherwise
   * the value associated with SENSORSHELL_MULTISHELL_MAXBUFFER_KEY is used.
   */
  public static final String SENSORSHELL_AUTOSEND_MAXBUFFER_KEY = 
    "sensorshell.autosend.maxbuffer";
  
  /**
   * The property key retrieving a string indicating the logging level for the SensorShell(s).
   * Default: "INFO".
   */
  public static final String SENSORSHELL_LOGGING_LEVEL_KEY = "sensorshell.logging.level";
 
  /** The internal properties object. */
  private Properties sensorProps = new Properties();
  
  private Logger logger = HackystatLogger.getLogger("org.hackystat.sensorshell.properties", 
      "sensorshell");

  /** The default timeout in seconds. */
  private int timeout = 10;
  /** The default ping timeout in seconds. */
  private int pingTimeout = 2;
  /** MultiShell processing is disabled by default. */
  private boolean multiShellEnabled = false;
  /** If MultiShell processing is enabled, then the default number of shells is 10. */
  private int multiShellNumShells = 10;
  /** If MultiShell processing is enabled, then the default num instances in a row is 499. */
  private int multiShellBatchSize = 499;
  /** If MultiShell processing is enabled, then the default max buffer is 500. */
  private int multiShellMaxBuffer = 500;
  /** If MultiShell processing is enabled, then the default autosend time interval is 0.10 . */
  private double multiShellAutoSendTimeInterval = 0.05;
  /** Offline caching of data is enabled by default. */
  private boolean offlineCacheEnabled = true;
  /** Recovery of offline data upon initialization is enabled by default. */
  private boolean offlineRecoveryEnabled = true;
  /** The default state change interval is 30 seconds. */
  private int statechangeInterval = 30;
  /** The default autosend time interval is 1.0 minutes. */
  private double autosendTimeInterval = 1.0;
  /** The default maximum number of buffered instances is 250. */
  private int autosendMaxBuffer = 250;
  /** Holds the required sensorbase host. */
  private String sensorBaseHost = null;
  /** Holds the required user. */
  private String user = null;
  /** Holds the required password. */
  private String password = null;
  /** Holds the default logging level. */
  private Level loggingLevel = Level.INFO;
  
  /**
   * Constructs a "basic" instance with the supplied three required properties.
   * All other properties are assigned values from sensorshell.properties, or the 
   * built-in defaults if not specified there.
   * <p>
   * Use SensorShellProperties.getTestInstance to create an instance for testing purposes, since
   * it will override certain properties that may be present in the sensorshell.properties file.
   * @param host The hackystat host.
   * @param email The user's email.   * @throws SensorShellException If the SensorProperties instance cannot be 
   * instantiated due to a missing host, user, and/or password properties.
   */
  public SensorShellProperties(String host, String email) 
  throws SensorShellException {
    setDefaultSensorShellProperties(true);
    this.sensorProps.setProperty(SENSORSHELL_SENSORBASE_HOST_KEY, host);
    this.sensorProps.setProperty(SENSORSHELL_SENSORBASE_USER_KEY, email);
    validateProperties();
  }
  
  public SensorShellProperties(Properties props, String host, String email) 
  throws SensorShellException {
    setDefaultSensorShellProperties(true);
    this.sensorProps.setProperty(SENSORSHELL_SENSORBASE_HOST_KEY, host);
    this.sensorProps.setProperty(SENSORSHELL_SENSORBASE_USER_KEY, email);
    this.sensorProps.putAll(props);
    validateProperties();
  }

  /**
   * Ensures that the SensorProperties instance has values defined for all standard 
   * properties with default values. Can optionally override existing values for these
   * standard properties.  Does not check for or provide values for the three required properties.
   * @param overridePreexisting If the default values should override the preexisting values. 
   */
  private void setDefaultSensorShellProperties (boolean overridePreexisting) {
    setDefaultProperty(SENSORSHELL_TIMEOUT_KEY, 
        String.valueOf(timeout), overridePreexisting);
    setDefaultProperty(SENSORSHELL_PING_TIMEOUT_KEY, 
        String.valueOf(pingTimeout), overridePreexisting);
    setDefaultProperty(SENSORSHELL_MULTISHELL_ENABLED_KEY, 
        String.valueOf(multiShellEnabled), overridePreexisting);
    setDefaultProperty(SENSORSHELL_MULTISHELL_NUMSHELLS_KEY, 
        String.valueOf(multiShellNumShells), overridePreexisting);
    setDefaultProperty(SENSORSHELL_MULTISHELL_BATCHSIZE_KEY, 
        String.valueOf(multiShellBatchSize), overridePreexisting);
    setDefaultProperty(SENSORSHELL_MULTISHELL_MAXBUFFER_KEY, 
        String.valueOf(multiShellMaxBuffer), overridePreexisting);
    setDefaultProperty(SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY, 
        String.valueOf(multiShellAutoSendTimeInterval), overridePreexisting);
    setDefaultProperty(SENSORSHELL_OFFLINE_CACHE_ENABLED_KEY, 
        String.valueOf(offlineCacheEnabled), overridePreexisting);
    setDefaultProperty(SENSORSHELL_OFFLINE_RECOVERY_ENABLED_KEY, 
        String.valueOf(offlineRecoveryEnabled), overridePreexisting);
    setDefaultProperty(SENSORSHELL_STATECHANGE_INTERVAL_KEY, 
        String.valueOf(statechangeInterval), overridePreexisting);
    setDefaultProperty(SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY, 
        String.valueOf(autosendTimeInterval), overridePreexisting);
    setDefaultProperty(SENSORSHELL_AUTOSEND_MAXBUFFER_KEY, 
        String.valueOf(autosendMaxBuffer), overridePreexisting);
    setDefaultProperty(SENSORSHELL_LOGGING_LEVEL_KEY, 
        String.valueOf(loggingLevel), overridePreexisting);
  }
  
  /**
   * Checks that the standard sensor properties have valid values.
   * <p>
   * The approach is that the instance variables (timeout, multishellEnabled, etc.) always start off
   * holding the default values, while the properties start off with potentially new and 
   * potentially invalid values.  We attempt to set the instance variables to the new values
   * in the properties.  If this fails, we use the instance variables to reset the properties
   * to the defaults.
   * <p>
   * The approach for the required variables is that if they do not have values, we throw
   * an error.   
   * @throws SensorShellException if problems occur.
   */
  private final void validateProperties() throws SensorShellException {
    String newValue = null;
    String origValue = null;
    String errMsg = "SensorProperties instantiation error: "; 
    // TIMEOUT
    try {
      origValue = String.valueOf(this.timeout);
      newValue = this.getProperty(SENSORSHELL_TIMEOUT_KEY);
      this.timeout = Integer.parseInt(newValue);
      if (this.timeout < 1) {
        this.logger.warning(errMsg + SENSORSHELL_TIMEOUT_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_TIMEOUT_KEY, origValue);
        this.timeout = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_TIMEOUT_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_TIMEOUT_KEY, origValue);
    }
    // Now set the SensorBaseClient default timeout.
    System.setProperty(SensorBaseClient.SENSORBASECLIENT_TIMEOUT_KEY, 
        String.valueOf(this.timeout * 1000));
    // PING TIMEOUT
    try {
      origValue = String.valueOf(this.pingTimeout);
      newValue = this.getProperty(SENSORSHELL_PING_TIMEOUT_KEY);
      this.pingTimeout = Integer.parseInt(newValue);
      if (this.pingTimeout < 1) {
        this.logger.warning(errMsg + SENSORSHELL_PING_TIMEOUT_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_PING_TIMEOUT_KEY, origValue);
        this.pingTimeout = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_PING_TIMEOUT_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_PING_TIMEOUT_KEY, origValue);
    }
    // MULTISHELL_ENABLED
    try {
      origValue = String.valueOf(multiShellEnabled);
      newValue = this.getProperty(SENSORSHELL_MULTISHELL_ENABLED_KEY);
      this.multiShellEnabled = Boolean.parseBoolean(newValue);
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_ENABLED_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_ENABLED_KEY, origValue); 
    }
    // MULTISHELL_NUMSHELLS
    try {
      origValue = String.valueOf(multiShellNumShells);
      newValue = this.getProperty(SENSORSHELL_MULTISHELL_NUMSHELLS_KEY);
      this.multiShellNumShells = Integer.parseInt(newValue);
      if (this.multiShellNumShells < 1) {
        this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_NUMSHELLS_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_NUMSHELLS_KEY, origValue);
        this.multiShellNumShells = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_NUMSHELLS_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_NUMSHELLS_KEY, origValue); 
    }
    // MULTISHELL_BATCHSIZE
    try {
      origValue = String.valueOf(multiShellBatchSize);
      newValue = this.getProperty(SENSORSHELL_MULTISHELL_BATCHSIZE_KEY);
      this.multiShellBatchSize = Integer.parseInt(newValue);
      if (this.multiShellBatchSize < 1) {
        this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_BATCHSIZE_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_BATCHSIZE_KEY, origValue);
        this.multiShellBatchSize = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_BATCHSIZE_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_BATCHSIZE_KEY, origValue);
    }
    // MULTISHELL_MAXBUFFER
    try {
      origValue = String.valueOf(multiShellMaxBuffer);
      newValue = this.getProperty(SENSORSHELL_MULTISHELL_MAXBUFFER_KEY);
      this.multiShellMaxBuffer = Integer.parseInt(newValue);
      if (this.multiShellMaxBuffer < 1) {
        this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_MAXBUFFER_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_MAXBUFFER_KEY, origValue);
        this.multiShellMaxBuffer = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_MAXBUFFER_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_MAXBUFFER_KEY, origValue);
    }    
    // MULTISHELL_AUTOSEND_TIMEINTERVAL
    try {
      origValue = String.valueOf(multiShellAutoSendTimeInterval);
      newValue = this.getProperty(SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY);
      this.multiShellAutoSendTimeInterval = Double.parseDouble(newValue);
      if (this.multiShellAutoSendTimeInterval < 0.0) {
        this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY, origValue);
        this.multiShellAutoSendTimeInterval = Double.parseDouble(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_MULTISHELL_AUTOSEND_TIMEINTERVAL_KEY, origValue);
    }
    // OFFLINE_CACHE_ENABLED
    try {
      origValue = String.valueOf(offlineCacheEnabled);
      newValue = this.getProperty(SENSORSHELL_OFFLINE_CACHE_ENABLED_KEY);
      this.offlineCacheEnabled = Boolean.parseBoolean(newValue);
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_OFFLINE_CACHE_ENABLED_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_OFFLINE_CACHE_ENABLED_KEY, origValue);
    }
    if (this.multiShellEnabled && this.offlineCacheEnabled) {
      this.logger.warning("Warning: Offline cache disabled since multishell enabled.");
      this.offlineCacheEnabled = false;
    }

    // OFFLINE_RECOVERY_ENABLED
    try {
      origValue = String.valueOf(offlineRecoveryEnabled);
      newValue = this.getProperty(SENSORSHELL_OFFLINE_RECOVERY_ENABLED_KEY);
      this.offlineRecoveryEnabled = Boolean.parseBoolean(newValue);
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_OFFLINE_RECOVERY_ENABLED_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_OFFLINE_RECOVERY_ENABLED_KEY, origValue);
    }
    if (this.multiShellEnabled && this.offlineRecoveryEnabled) {
      this.logger.warning("Warning: Offline recovery disabled since multishell enabled.");
      this.offlineRecoveryEnabled = false;
    }
    // STATECHANGE_INTERVAL
    try {
      origValue = String.valueOf(statechangeInterval);
      newValue = this.getProperty(SENSORSHELL_STATECHANGE_INTERVAL_KEY);
      this.statechangeInterval = Integer.parseInt(newValue);
      if (this.statechangeInterval < 1) {
        this.logger.warning(errMsg + SENSORSHELL_STATECHANGE_INTERVAL_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_STATECHANGE_INTERVAL_KEY, origValue);
        this.statechangeInterval = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_STATECHANGE_INTERVAL_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_STATECHANGE_INTERVAL_KEY, origValue);
    }
    // AUTOSEND_TIMEINTERVAL (Single Shell)
    try {
      origValue = String.valueOf(autosendTimeInterval);
      newValue = this.getProperty(SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY);
      this.autosendTimeInterval = Double.parseDouble(newValue);
      if (this.autosendTimeInterval < 0.0) {
        this.logger.warning(errMsg + SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY, origValue);
        this.autosendTimeInterval = Double.parseDouble(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY, origValue);
    }
    // AUTOSEND_MAXBUFFER
    try {
      origValue = String.valueOf(autosendMaxBuffer);
      newValue = this.getProperty(SENSORSHELL_AUTOSEND_MAXBUFFER_KEY);
      this.autosendMaxBuffer = Integer.parseInt(newValue);
      if (this.autosendMaxBuffer < 1) {
        this.logger.warning(errMsg + SENSORSHELL_AUTOSEND_MAXBUFFER_KEY + " " + newValue); 
        this.sensorProps.setProperty(SENSORSHELL_AUTOSEND_MAXBUFFER_KEY, origValue);
        this.autosendMaxBuffer = Integer.parseInt(origValue);
      }
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_AUTOSEND_MAXBUFFER_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_AUTOSEND_MAXBUFFER_KEY, origValue);
    }
    // LOGGING_LEVEL
    try {
      origValue = String.valueOf(loggingLevel);
      newValue = this.getProperty(SENSORSHELL_LOGGING_LEVEL_KEY);
      this.loggingLevel = Level.parse(newValue);
    }
    catch (Exception e) {
      this.logger.warning(errMsg + SENSORSHELL_LOGGING_LEVEL_KEY + " " + newValue); 
      this.sensorProps.setProperty(SENSORSHELL_LOGGING_LEVEL_KEY, origValue);
    }
  }
  
  /**
   * Ensures that the specified property has a value in this sensorshell properties.
   * If overridePreexisting is true, then property will be set to value regardless of whether
   * it had a previously existing value. 
   * If overridePreexisting is false, then property will be set to value only if there is no
   * currently defined property.    
   * @param property The property to set, if not yet defined.
   * @param value The value to set the property to, if not yet defined.
   * @param overridePreexisting True if property will be set to value even if it currently exists. 
   */
  private void setDefaultProperty(String property, String value, boolean overridePreexisting) {
    if (overridePreexisting) {
      this.sensorProps.setProperty(property, value);
    }
    else if (!this.sensorProps.containsKey(property)) {
      this.sensorProps.setProperty(property, value);
    }
  }
  
  /**
   * Returns the trimmed property value associated with the property key. This is useful for
   * clients who want to look for "non-standard" properties supplied in sensorshell.properties.
   * For standard properties, clients should use the accessor methods, since these will perform
   * type conversion.
   * @param key The parameter key.
   * @return The trimmed property value associated with this property key, or null if not found.
   */
  public final String getProperty(String key) {
    String value = this.sensorProps.getProperty(key);
    if (value != null) {
      value = value.trim();
    }
    return value;
  }

  /**
   * Returns the sensorbase host, such as "http://dasha.ics.hawaii.edu:9876/sensorbase".
   * @return The sensorbase host.
   */
  public String getSensorBaseHost() {
    return this.sensorBaseHost;
  }


  /**
   * Returns the password for this user, such as "xu876csld".
   * @return The user password.
   */
  public String getSensorBasePassword() {
    return this.password;
  }

  /**
   * Returns the account for this user, such as "johnson@hawaii.edu".
   * @return The user account.
   */
  public String getSensorBaseUser() {
    return this.user;
  }
  
  /**
   * Returns the AutoSend time interval, such as 1.0.
   * @return The autosend interval.
   */
  public double getAutoSendTimeInterval() {
    return this.autosendTimeInterval;
  }
  
  /**
   * Returns the AutoSend batch size, such as 250.
   * @return The autosend batch size.
   */
  public int getAutoSendMaxBuffer() {
    return this.autosendMaxBuffer;
  }

  /**
   * Returns the StateChange interval.
   * @return The state change interval in seconds.
   */
  public int getStateChangeInterval() {
    return this.statechangeInterval;
  }
  
  /**
   * Returns the current timeout setting for non-Ping requests.
   * @return The timeout in seconds. 
   */
  public int getTimeout() {
    return this.timeout;
  }
  
  /**
   * Returns the current timeout setting for Ping requests.
   * @return The ping timeout in seconds. 
   */
  public int getPingTimeout() {
    return this.pingTimeout;
  }
  
  /**
   * Returns the logging level specified for SensorShells.
   * @return The logging level.
   */
  public Level getLoggingLevel() {
    return this.loggingLevel;
  }
  
  /**
   * Returns true if multishell processing is enabled.
   * @return True if multishell processing.
   */
  public boolean isMultiShellEnabled () {
    return this.multiShellEnabled;
  }

  /**
   * Returns the number of shells to instantiate if multishell processing is enabled.
   * @return The number of shells to instantiate.
   */
  public int getMultiShellNumShells() {
    return this.multiShellNumShells;
  }
  
  /**
   * Returns the number of instances to send to one shell in a row if multishell processing.
   * @return The number of instances to send to one shell in a row.
   */
  public int getMultiShellBatchSize() {
    return this.multiShellBatchSize;
  }
  
  /**
   * Returns the maximum number of instances to buffer before a blocking send is invoked in
   * multishell mode.
   * @return The maximum number of instances to buffer.
   */
  public int getMultiShellMaxBuffer() {
    return this.multiShellMaxBuffer;
  }
  
  /**
   * Returns the MultiShell AutoSend time interval, such as 0.10.
   * @return The multishell autosend interval.
   */
  public double getMultiShellAutoSendTimeInterval() {
    return this.multiShellAutoSendTimeInterval;
  }


  /**
   * Returns true if offline cache data saving is enabled.
   * @return True if offline caching enabled.
   */
  public boolean isOfflineCacheEnabled() {
    return this.offlineCacheEnabled;
  }
  
  
  /**
   * Returns true if offline data recovery is enabled.
   * @return True if offline data recovery enabled.
   */
  public boolean isOfflineRecoveryEnabled () {
    return this.offlineRecoveryEnabled;
  }
  
  /**
   * Returns the set of SensorProperties as a multi-line string.
   * Does not print out the value associated with SENSORSHELL_PASSWORD_KEY. 
   * @return The Sensor property keys and values. 
   */
  @Override
  public String toString() {
    String cr = System.getProperty("line.separator");
    StringBuffer buff = new StringBuffer(100);
    // It turns out to be much, much more usable to get these properties alphabetized.
    TreeMap<String, String> map = new TreeMap<String, String>();
    for (Object key : this.sensorProps.keySet()) {
      map.put(key.toString(), this.sensorProps.get(key).toString());
    }
    // Now print them out in alphabetical order.
    buff.append("SensorProperties");
    for (Entry<String, String> entry : map.entrySet()) {
      buff.append(cr);
      buff.append("  ");
      buff.append(entry.getKey());
    }
    return buff.toString();
  }
 
  /**
   * Sets the autosend time interval and autosend max buffer size to the multishell versions.
   * Invoked by the multishell just before instantiating its child SingleSensorShells so that
   * they are set up with the appropriate multishell time interval. 
   */
  public void switchToMultiShellMode() {
    this.sensorProps.setProperty(SENSORSHELL_AUTOSEND_TIMEINTERVAL_KEY, 
        String.valueOf(this.getMultiShellAutoSendTimeInterval()));
    this.autosendTimeInterval = this.multiShellAutoSendTimeInterval;
    this.sensorProps.setProperty(SENSORSHELL_AUTOSEND_MAXBUFFER_KEY, 
        String.valueOf(this.getMultiShellMaxBuffer()));
    this.autosendMaxBuffer = this.getMultiShellMaxBuffer();
  }

}

