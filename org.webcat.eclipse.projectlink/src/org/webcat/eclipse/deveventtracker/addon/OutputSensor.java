package org.webcat.eclipse.deveventtracker.addon;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.webcat.eclipse.projectlink.Activator;

/**
 * Sensor for output written to stdout and stderr.
 * Wraps a singleton ConsoleOutput object, which stores
 * data that is communicated to other parts of the plugin.
 * 
 * @author Ayaan Kazerouni
 * @version 08/13/2018
 */
public class OutputSensor implements IConsoleLineTracker {
	private IConsole console;
	
	/**
	 * Singleton instance.
	 */
	private static ConsoleOutput outputInstance;
	
	public OutputSensor() {
		outputInstance = getOutputInstance();
	}
	
	/**
	 * Gets the singleton instance of the output object.
	 * 
	 * @return the ConsoleOutput object
	 */
	public static ConsoleOutput getOutputInstance() {
		if (outputInstance == null) {
			outputInstance = new ConsoleOutput();
			return outputInstance;
		} else {
			return outputInstance;
		}
	}
	
	/**
	 * Reset the console. DOES NOT reset the output,
	 * since the stored output data is usually consumed
	 * after the OutputSensor is disposed of.
	 */
	public void dispose() {
		this.console = null;
	}

	/**
	 * Set the console to get lines from, and reset the output.
	 * 
	 * @param console
	 * 		The console where output is read from.
	 */
	public void init(IConsole console) {
		this.console = console;
		outputInstance.reset();
	}

	/**
	 * Store the output to be reported after the launch is finished.
	 * 
	 * @param region
	 * 		A region (offset, length) telling us where the next line
	 * 		may be read from.
	 */
	public void lineAppended(IRegion region) {
		try {
			if (this.console != null) {
				String line = this.console.getDocument().get(region.getOffset(), region.getLength());
				outputInstance.addLine(line);
			}
		} catch (BadLocationException e) {
			Activator.getDefault().log(e);
		}
	}
	
	/**
	 * Inner singleton class that stores output captured from
	 * the OutputSensor.
	 * 
	 * @author Ayaan Kazerouni
	 * @version 08/14/2018
	 */
	public static class ConsoleOutput {
		private StringBuilder output;
		private int linesAdded;
		private boolean stoppedAppending;
		
		/**
		 * Stop appending output if output exceeds these many lines.
		 * If the launch goes into an infinite loop, sending that much data
		 * over the network would get very slow and is probably not that useful.
		 */
		private static final int MAX_LINES = 1000;
		
		/**
		 * A message to be written to the output indicating that we truncated it because it was too long.
		 */
		private static final String TRUNC_MESSAGE = "org.webcat.deveventtracker.OutputTruncated";
		
		private ConsoleOutput() {
			this.reset();
		}
		
		/**
		 * Get whatever has been captured so far.
		 * @return A string containing the lines seen in
		 * 		the console so far.
		 */
		public String getOutput() {
			return this.output.toString().trim();
		}
		
		/**
		 * Increase the number of lines appended by 1.
		 */
		public void incrementLines() {
			this.linesAdded++;
		}
		
		/**
		 * Reset the singleton instance. Sets the line count to 0 and
		 * flushes the output.
		 */
		public void reset() {
			this.linesAdded = 0;
			this.output = new StringBuilder();
			this.stoppedAppending = false;
		}
		
		/**
		 * Checks if this ConsoleOutput is full yet.
		 * 
		 * @return false if more than MAX_LINES lines have been read,
		 * 		true otherwise
		 */
		public boolean underCapacity() {
			return this.linesAdded <= MAX_LINES;
		}
		
		/**
		 * Appends the given line to the output, if it is under capacity.
		 * 
		 * @param line
		 * 		The line just read by the OutputSensor
		 */
		public void addLine(String line) {
			if (this.underCapacity()) {
				this.output.append(line + "\n");
				this.linesAdded++;
			} else if (!this.stoppedAppending) { // add a final line saying the output was too long, then stop storing output
				this.output.append(TRUNC_MESSAGE);
				this.stoppedAppending = true;
			}
		}
	}
}