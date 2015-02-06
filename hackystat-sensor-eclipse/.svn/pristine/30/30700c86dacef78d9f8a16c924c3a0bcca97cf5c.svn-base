package org.hackystat.sensor.eclipse.addon;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.hackystat.sensor.eclipse.EclipseSensor;

/**
 * Implements debug listener to intercept debug activity.
 *  
 * @author Hongbing Kou
 * @version $Id: DebugSensor.java,v 1.1.1.1 2005/10/20 23:56:56 johnson Exp $
 */
public class DebugSensor implements IDebugEventSetListener {
  /** The file being debugged. */
  private URI debuggedFile;
  /** Eclipse sensor. */
  private EclipseSensor eclipseSensor;
  
  /**
   * Instantiates an Eclipse sensor.
   * 
   * @param sensor Eclipse sensor.
   */
  public DebugSensor(EclipseSensor sensor) {
    this.eclipseSensor = sensor;
  }
  
  /**
   * Handle debug event to get execution information of the program being developed.
   * 
   * @param events Debug events being fired.
   */
  public void handleDebugEvents(DebugEvent[] events) {
    if (events != null && events.length > 0) {
      for (int i = 0; i < events.length; i++) {
        DebugEvent event = events[i];
        String debugActivity = translateEventKind(event);
        if (debugActivity != null) {
          StringBuffer displayMessage = new StringBuffer();
          
          Map<String, String> keyValueMap = new HashMap<String, String>();
          keyValueMap.put("Subtype", debugActivity);
          
          displayMessage.append("Debug : ").append(
            this.eclipseSensor.extractFileName(this.debuggedFile));
          displayMessage.append(" [").append(debugActivity).append(']');
          
          this.eclipseSensor.addDevEvent("Debug", this.debuggedFile, 
            keyValueMap, displayMessage.toString());
        }
      }
    }
  }
  
  /**
   * Translates debug event from event kind and detail to meaningful 
   * string representation.
   * 
   * @param event Debug event.
   * @return String representation.
   */
  private String translateEventKind(DebugEvent event) {
    String debugType = null;
    if (event.getKind() == DebugEvent.CREATE) {
      if (event.getSource() instanceof IDebugTarget) {
        debugType = "Start";
        
        // Update the file being edited
        ITextEditor activeEditor = this.eclipseSensor.getActiveTextEditor();
        IFileEditorInput fileEditorInput = (IFileEditorInput) activeEditor.getEditorInput();
        IFile file = fileEditorInput.getFile();
        this.debuggedFile = file.getLocationURI();
      }
    }
    else if (event.getKind() == DebugEvent.TERMINATE) {
      if (event.getSource() instanceof IDebugTarget) {
        debugType = "Terminate";
      }
    }
    else if (event.getKind() == DebugEvent.SUSPEND || 
             event.getKind() == DebugEvent.RESUME) {
      switch (event.getDetail()) {
        case DebugEvent.BREAKPOINT:
          debugType = "Breakpoint";
          break;
        case DebugEvent.STEP_INTO:
          debugType = "Step into";
          break;
        case DebugEvent.STEP_OVER:
          debugType = "Step over";
          break;
        default:
          debugType = "Unknown";
      }
    }
    
    return debugType;      
  }  
}