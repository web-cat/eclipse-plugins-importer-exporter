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

package org.webcat.eclipse.projectlink.importer;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;
import org.webcat.eclipse.projectlink.dialogs.ExceptionDialog;
import org.webcat.eclipse.projectlink.i18n.Messages;
import org.webcat.eclipse.projectlink.importer.model.ImportNode;
import org.webcat.eclipse.projectlink.importer.model.ImporterManifest;

//--------------------------------------------------------------------------
/**
 * A wizard that guides the user through the process of downloading an
 * assignment.
 * 
 * @author Ellen Boyd, Tony Allevato
 */
public class DownloadAssignmentWizard extends Wizard implements IImportWizard
{
	//~ Instance/static variables .............................................

	private Importer importer;
	private ImportNode selectedNode;

	private DownloadAssignmentPage page;


	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	public DownloadAssignmentWizard()
	{
		importer = new Importer();
		
		setNeedsProgressMonitor(true);
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		// Do nothing.
	}


	// ----------------------------------------------------------
	public void addPages()
	{
		page = new DownloadAssignmentPage(importer);
		addPage(page);
	}


	// ----------------------------------------------------------
	@Override
	public boolean performFinish()
	{
        selectedNode = page.getSelectedNode();

        if (selectedNode == null)
        {
        	return false;
        }

		try
		{
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException
				{
					try
					{
						downloadProjects(monitor);
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
			// TODO something
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			new ExceptionDialog(
					getContainer().getShell(), e.getCause()).open();
		}

		return true;
	}


	// ----------------------------------------------------------
	private void downloadProjects(IProgressMonitor monitor)
	{
		ImporterManifest manifest = new ImporterManifest();
        manifest.setImportNode(selectedNode);

        List<ImportError> errors = importer.importProjects(manifest, monitor);
        
        if (!errors.isEmpty())
        {
        	displayImportErrors(errors);
        }
	}
	
	
	// ----------------------------------------------------------
	private void displayImportErrors(List<ImportError> errors)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(
				Messages.DownloadAssignmentWizard_Download_Error_Description);
		
		for (ImportError error : errors)
		{
			buffer.append(MessageFormat.format(
					Messages.DownloadAssignmentWizard_Project_Error,
					error.getProject().getName(),
					error.getMessage()));
		}
		
		final String message = buffer.toString();

		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				MessageDialog.openWarning(getContainer().getShell(),
						Messages.DownloadAssignmentWizard_Download_Errors_Title,
						message);
			}
		});
	}
}
