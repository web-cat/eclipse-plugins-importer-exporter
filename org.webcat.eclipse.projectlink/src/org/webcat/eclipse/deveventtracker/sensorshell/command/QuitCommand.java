package org.webcat.eclipse.deveventtracker.sensorshell.command;

import java.util.logging.Handler;

import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellException;
import org.webcat.eclipse.deveventtracker.sensorshell.SensorShellProperties;
import org.webcat.eclipse.deveventtracker.sensorshell.SingleSensorShell;


/**
 * Implements the Quit command, which sends any buffered data and closes the loggers. 
 * @author Philip Johnson
 */
public class QuitCommand extends Command {
  
  /** Holds the sensor data command for sending data. */
  private SensorDataCommand sensorDataCommand;
  
  /** Holds the autosend instance. */
  private AutoSendCommand autoSendCommand;
  
  /**
   * Creates the QuitCommand. 
   * @param shell The sensorshell. 
   * @param properties The sensorproperties.
   * @param sensorDataCommand The SensorDataCommand. 
   * @param autoSendCommand The AutoSendCommand.
   */
  public QuitCommand(SingleSensorShell shell, SensorShellProperties properties, 
      SensorDataCommand sensorDataCommand, AutoSendCommand autoSendCommand) {
    super(shell, properties);
    this.sensorDataCommand = sensorDataCommand;
    this.autoSendCommand = autoSendCommand;
  }
  
  /** 
   * Quits the shell. Sends all data and closes the loggers.
   * @throws SensorShellException if an exception occurred during an autosend event.  
   */
  public void quit() throws SensorShellException {
    SensorShellException exception = null;
    
    // Log this command if not running interactively.
    if (!this.shell.isInteractive()) {
      this.shell.getLogger().info("#> quit" + cr);
    }
    // Try to send any remaining data. 
    try {
      this.sensorDataCommand.send();
    }
    catch (SensorShellException e) {
      // Note that we had an exception thrown, now proceed to finally block.
      exception = e; 
    }
    finally {
      // Shut down all timers. 
      this.autoSendCommand.quit();
      // Log quit() information.
      if (exception != null) {
        this.shell.println("Error sending data during final quit: " + exception);
      }
      this.shell.println("Quitting SensorShell started at: " + this.shell.getStartTime());
      this.shell.println("Total sensor data instances sent: " + this.shell.getTotalSent());
      // Close all loggers. 
      if (this.shell.getLogger() != null) {
        // Close all the open handler.
        Handler[] handlers = this.shell.getLogger().getHandlers();
        for (int i = 0; i < handlers.length; i++) {
          handlers[i].close();
        }
      }
      // If we had an autosend exception, and there's data left, we throw it again. 
      if ((this.autoSendCommand.getException() != null) && 
          (this.sensorDataCommand.hasUnsentData())) {
        throw this.autoSendCommand.getException();
      }
      // If we had a quit() exception, and there's data left, we throw it again. 
      if ((exception != null) && (this.sensorDataCommand.hasUnsentData())) {
        throw exception;
      }
      
    }
  }
}
