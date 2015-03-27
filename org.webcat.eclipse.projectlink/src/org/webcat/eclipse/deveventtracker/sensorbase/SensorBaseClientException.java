package org.webcat.eclipse.deveventtracker.sensorbase;

import org.restlet.data.Status;

/**
 * An exception that is thrown when the SensorBase server does not a success code. 
 * @author Philip Johnson
 */
public class SensorBaseClientException extends Exception {

  /** The default serial version UID. */
  private static final long serialVersionUID = 1L;
  
  /**
   * Thrown when an unsuccessful status code is returned from the Server.
   * @param status The Status instance indicating the problem.
   */
  public SensorBaseClientException(Status status) {
    super(status.getCode() + ": " + status.getDescription());
  }

  /**
   * Thrown when an unsuccessful status code is returned from the Server.
   * @param status The status instance indicating the problem. 
   * @param error The previous error.
   */
  public SensorBaseClientException(Status status, Throwable error) {
    super(status.getCode() + ": " + status.getDescription(), error);
  }
  
  /**
   * Thrown when some problem occurs with Client not involving the server. 
   * @param description The problem description.
   * @param error The previous error.
   */
  public SensorBaseClientException(String description, Throwable error) {
    super(description, error);
  }
  
  /**
   * Thrown when some problem occurs with Client not involving the server. 
   * @param description The problem description.
   */
  public SensorBaseClientException(String description) {
    super(description);
  }

}
