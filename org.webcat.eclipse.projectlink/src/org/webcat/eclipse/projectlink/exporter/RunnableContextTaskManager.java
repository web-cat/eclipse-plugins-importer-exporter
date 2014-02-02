/*==========================================================================*\
 |  Copyright (C) 2012 Virginia Tech
 |
 |  This file is part of Web-CAT Eclipse Plugins.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License along
 |  with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package org.webcat.eclipse.projectlink.exporter;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.webcat.submitter.ILongRunningTask;
import org.webcat.submitter.ILongRunningTaskManager;
import org.webcat.submitter.IProgressChangeListener;

//-------------------------------------------------------------------------
/**
 * A long-running task manager for the submitter that uses an Eclipse
 * {@link IRunnableContext} to execute its tasks with progress notifications.
 * 
 * @author Tony Allevato
 */
public class RunnableContextTaskManager implements ILongRunningTaskManager
{
	//~ Instance/static variables .............................................

	private IRunnableContext context;


	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	public RunnableContextTaskManager(IRunnableContext context)
	{
		this.context = context;
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	public void run(final ILongRunningTask task)
			throws InvocationTargetException
	{
		try
		{
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException
				{
					monitor.beginTask(task.getDescription(), 100);
					task.addProgressChangeListener(
							new ProgressListener(monitor));

					try
					{
						task.run();
						monitor.done();
					}
					catch (Exception e)
					{
						throw new InvocationTargetException(e);
					}
				}			
			});
		}
		catch (InterruptedException e)
		{
			// Do nothing.
		}
	}


	//~ Inner classes .........................................................

	// ----------------------------------------------------------
	/**
	 * A progress change listener that uses an Eclipse
	 * {@link IProgressMonitor}.
	 */
	private class ProgressListener implements IProgressChangeListener
	{
		private int lastProgress = 0;
		private IProgressMonitor monitor;


		// ----------------------------------------------------------
		public ProgressListener(IProgressMonitor monitor)
		{
			this.monitor = monitor;
		}


		// ----------------------------------------------------------
		public void progressChanged(int progress)
		{
			int delta = progress - lastProgress;
			monitor.worked(delta);
			
			lastProgress = progress;
		}
	}
}
