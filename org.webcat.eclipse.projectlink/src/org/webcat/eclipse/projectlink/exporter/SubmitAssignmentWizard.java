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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.TimerTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.webcat.eclipse.deveventtracker.EclipseSensor;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClientException;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.dialogs.AuthenticationDialog;
import org.webcat.eclipse.projectlink.dialogs.ExceptionDialog;
import org.webcat.submitter.ISubmittableItem;
import org.webcat.submitter.RequiredItemsMissingException;
import org.webcat.submitter.SubmissionManifest;
import org.webcat.submitter.SubmittableFile;
import org.webcat.submitter.Submitter;
import org.webcat.submitter.targets.AssignmentTarget;

//--------------------------------------------------------------------------
/**
 * A wizard that guides the user through the process or submitting an
 * assignment.
 * 
 * @author Tony Allevato
 */
public class SubmitAssignmentWizard extends Wizard implements IExportWizard
{
	private Submitter submitter;

	private IProject selectedProject;
	private SubmitAssignmentPage submitPage;


	// ----------------------------------------------------------
	public SubmitAssignmentWizard()
	{
		submitter = new Submitter();

		setNeedsProgressMonitor(true);
	}


	// ----------------------------------------------------------
	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection)selection;
			Object obj = ss.getFirstElement();

			if (obj instanceof IAdaptable)
			{
				IAdaptable adapt = (IAdaptable) obj;
				selectedProject = (IProject) adapt.getAdapter(IProject.class);
			}
		}
	}


	// ----------------------------------------------------------
	public void setSelectedProject(IProject project)
	{
		this.selectedProject = project;
	}


	// ----------------------------------------------------------
	public void addPages()
	{
		submitter.setLongRunningTaskManager(
				new RunnableContextTaskManager(getContainer()));

		submitPage = new SubmitAssignmentPage(submitter, selectedProject);

		addPage(submitPage);
	}


	// ----------------------------------------------------------
	@Override
	public boolean performFinish()
	{
		submitPage.saveState();

		AssignmentTarget target = submitPage.getSelectedAssignment();
		boolean rememberPassword = true;

		if (target.usesParameter("user")
				&& !Activator.getDefault().hasFullCredentials())
		{
			AuthenticationDialog dialog =
					new AuthenticationDialog(getContainer().getShell());

			if (dialog.open() != Window.OK)
			{
				return false;
			}
			
			rememberPassword = dialog.remembersPassword();
		}

		SubmissionManifest manifest = new SubmissionManifest();
		manifest.setAssignment(target);
		manifest.setSubmittableItems(getFilesToSubmit());
		manifest.setUsername(Activator.getDefault().getStoredUsername());
		manifest.setPassword(Activator.getDefault().getLastEnteredPassword());
		manifest.setParameter("partners", submitPage.getPartners()); //$NON-NLS-1$

		try
		{
			submitter.submit(manifest);
			
			TimerTask submissionHappenedTask = new TimerTask() {
				
				@Override
				public void run() {
					// Send an event to the server summarizing this submission.
					try {
						SensorBaseClient.getInstance().submissionHappened(
						    submitPage.getProject());
					} catch (SensorBaseClientException e) {
						new  ExceptionDialog(getContainer().getShell(), e, true).open();
					}
				}
			};
			
			EclipseSensor.getInstance().scheduleOneTimeTask(submissionHappenedTask);
			
			if (submitter.hasResponse())
			{
				openResponseInBrowser(submitter.getResponse());
			}
			else
			{
				MessageDialog.openInformation(getContainer().getShell(),
						Messages.SubmitAssignmentWizard_No_Response_Dialog_Title,
						Messages.SubmitAssignmentWizard_No_Response_Message);
			}

			return true;
		}
		catch (RequiredItemsMissingException e)
		{
/*			StringBuffer buffer = new StringBuffer();
			buffer.append(Messages.STARTPAGE_ERROR_REQUIRED_FILES_MISSING);

			for (int i = 0; i < e.getMissingFiles().length; i++)
			{
				buffer.append('\u2022');
				buffer.append(' ');
				buffer.append(e.getMissingFiles()[i]);
				buffer.append('\n');
			}

			//nextPage.setResultCode(SubmitterSummaryPage.RESULT_INCOMPLETE,
			//        buffer.toString());*/
			return false;
		}
		catch (Exception e)
		{
			new ExceptionDialog(getContainer().getShell(), e, true).open();
			return false;
		}
		finally
		{
			if (!rememberPassword)
			{
				Activator.getDefault().setLastEnteredPassword(null);
			}
		}
	}
	
	
	// ----------------------------------------------------------
	private void openResponseInBrowser(String response)
	{
		try
		{
			File tempFile = File.createTempFile("submissionresult", ".html");

			FileWriter writer = new FileWriter(tempFile);
			writer.write(response);
			writer.close();

			openBrowser(tempFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	// ----------------------------------------------------------
	private void openBrowser(File file)
	{
		try
		{
			URL url = file.toURI().toURL();
			PlatformUI.getWorkbench().getBrowserSupport()
				.getExternalBrowser().openURL(url);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	// ----------------------------------------------------------
	private ISubmittableItem[] getFilesToSubmit()
	{
		IProject project = submitPage.getProject();
		File[] children = project.getLocation().toFile().listFiles();
		ISubmittableItem[] submittables = new SubmittableFile[children.length - 1];
		int childrenIterator = 0;
		int submittablesIterator = 0;
		while (submittablesIterator < submittables.length) {
			if (children[childrenIterator].isFile() || !children[childrenIterator].getName().contains(".git")) {
				submittables[submittablesIterator] = new SubmittableFile(children[childrenIterator]);
				submittablesIterator++;
			}
			
			childrenIterator++;
		}
		
//		return new ISubmittableItem[] {
//				new SubmittableFile(project.getLocation().toFile())
//		};
		
		return submittables;
	}
}
