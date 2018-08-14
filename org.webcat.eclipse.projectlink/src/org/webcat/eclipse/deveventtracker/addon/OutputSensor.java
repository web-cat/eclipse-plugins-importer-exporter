package org.webcat.eclipse.deveventtracker.addon;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.webcat.eclipse.projectlink.Activator;

/**
 * Sensor for output written to stdout and stderr.
 * Behaves like a singleton.
 * 
 * @author Ayaan Kazerouni
 * @version 08/13/2018
 */
public class OutputSensor implements IConsoleLineTracker {
	private IConsole console;
	public static StringBuilder output = null;
	public static boolean launchFinished = false;
	private static int linesAdded;
	private static boolean stoppedAppending = false;
	
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
	
	/**
	 * Reset the output.
	 */
	public void dispose() {
		this.console = null;
		output.trimToSize();
	}

	/**
	 * Set the console to get lines from, and reset the output builder.
	 * 
	 * @param console
	 * 		The console where output is read from.
	 */
	public void init(IConsole console) {
		this.console = console;
		output = new StringBuilder();
		linesAdded = 0;
		stoppedAppending = false;
		launchFinished = false;
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
				if (linesAdded <= MAX_LINES) {
					linesAdded++;
					output.append(line + "\n");
				} else if (!stoppedAppending) { // add a final line saying the output was too long, then stop storing output
					output.append(TRUNC_MESSAGE);
					stoppedAppending = true;
				}
				
				if (this.console.getProcess().isTerminated()) {
					launchFinished = true;
				}
			}
		} catch (BadLocationException e) {
			Activator.getDefault().log(e);
		}
	}
	
	public static String getOutput() {
		return output.toString().trim();
	}
}