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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.webcat.eclipse.deveventtracker.sensorbase.SensorBaseClient;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.ProjectLinkException;
import org.webcat.eclipse.projectlink.importer.model.ImportNode;
import org.webcat.eclipse.projectlink.importer.model.ImporterManifest;
import org.webcat.eclipse.projectlink.importer.model.PreferencesNode;
import org.webcat.eclipse.projectlink.importer.model.ProjectGroupNode;
import org.webcat.eclipse.projectlink.importer.model.ProjectNode;
import org.webcat.eclipse.projectlink.importer.model.RootImportNode;
import org.webcat.eclipse.projectlink.util.ZipUtils;
import org.webcat.submitter.SubmissionTargetException;
import org.webcat.submitter.TargetParseError;
import org.webcat.submitter.TargetParseException;
import org.webcat.submitter.internal.SubmissionParserErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// -------------------------------------------------------------------------
/**
 * The primary class providing functionality for the electronic importer.
 *
 * @author Ellen Boyd
 * @version Feb 5, 2012
 */
public class Importer
{
	//~ Instance/static variables .............................................

    private RootImportNode root;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the root object of the import target tree.
     *
     * @return an ITargetRoot represented the root of the import target
     */
    public RootImportNode getRoot()
    {
        return root;
    }


    // ----------------------------------------------------------
    /**
     * Reads the submission target definitions from the specified URL.
     *
     * @param definitionsUrl a URL that points to the submission target
     *     definitions
     * @throws IOException if an I/O exception occurred
     * @throws SubmissionTargetException if some other exception occurred
     */
    public void readSchema(URL definitionsUrl, IProgressMonitor monitor)
    		throws IOException
    {
        InputStream stream = null;

        try
        {
            stream = definitionsUrl.openStream();
            readSchema(new InputStreamReader(stream), monitor);
        }
        finally
        {
            try
            {
                if (stream != null)
                {
                    stream.close();
                }
            }
            catch (IOException e)
            {
            	Activator.getDefault().log(e);
                // Do nothing.
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Reads the import target definitions from the specified reader.
     *
     * @param reader
     *            the Reader
     * @throws IOException
     *             if an I/O exception occurred.
     */
    public void readSchema(Reader reader, IProgressMonitor monitor)
    		throws IOException
    {
        try
        {
			monitor.beginTask(
					"Getting the list of assignments available for download...", 1);
        	
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

            factory.setIgnoringComments(true);
            factory.setCoalescing(false);
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            SubmissionParserErrorHandler errorHandler =
                new SubmissionParserErrorHandler();

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);

            Document document = builder.parse(new InputSource(reader));
            TargetParseError[] errors = errorHandler.getErrors();

            if (errors != null)
            {
                throw new TargetParseException(errors);
            }
            else
            {
                root = new RootImportNode();
                root.parse(document.getDocumentElement());
            }

        }
        catch (ParserConfigurationException e)
        {
        	Activator.getDefault().log(e);
            throw new ProjectLinkException(e);
        }
        catch (SAXException e)
        {
        	Activator.getDefault().log(e);
            throw new ProjectLinkException(e);
        }
        finally
        {
        	monitor.done();
        }
    }


    // ----------------------------------------------------------
    public List<ImportError> importProjects(ImporterManifest manifest,
    		IProgressMonitor monitor)
    {
    	HashSet<ProjectNode> projects = new HashSet<ProjectNode>();
    	gatherProjects(projects, manifest.getImportNode());
    	gatherDependencies(projects, manifest.getImportNode(), true);

    	HashSet<PreferencesNode> preferences = new HashSet<PreferencesNode>();
    	gatherPreferences(preferences, projects);

		monitor.beginTask("Downloading assignments...", projects.size());

		ArrayList<ImportError> errors = new ArrayList<ImportError>();

		for (PreferencesNode prefs : preferences)
		{
			importPreferences(prefs, monitor, errors);
		}

		for (ProjectNode project : projects)
		{
			importProject(manifest, project, monitor, errors);
		}
    	
    	monitor.done();
    	
    	return errors;
    }
    
    
    // ----------------------------------------------------------
    private void gatherPreferences(Set<PreferencesNode> preferences,
    		Set<ProjectNode> projects)
    {
    	for (ProjectNode project : projects)
    	{
    		preferences.addAll(project.getPreferences());
    	}
    }


    // ----------------------------------------------------------
    public void gatherProjects(Set<ProjectNode> projects, ImportNode node)
    {
    	if (node instanceof ProjectGroupNode)
    	{
    		for (ImportNode child : ((ProjectGroupNode) node).getChildren())
    		{
    			gatherProjects(projects, child);
    		}
    	}
    	else if (node instanceof ProjectNode)
    	{
    		ProjectNode project = (ProjectNode) node;
    		
    		if (project.getAvailability().isDownloadable())
    		{
    			projects.add(project);
    		}
    	}
    }


    // ----------------------------------------------------------
    public void gatherDependencies(
    		Set<ProjectNode> depends, ImportNode node,
    		boolean includeSelf)
    {
    	if (node instanceof ProjectGroupNode)
    	{
    		for (ImportNode child : ((ProjectGroupNode) node).getChildren())
    		{
    			gatherDependencies(depends, child, true);
    		}
    	}
    	else if (node instanceof ProjectNode)
    	{
    		ProjectNode project = (ProjectNode) node;

    		if (project.getAvailability().isDownloadable())
    		{
	    		if (includeSelf)
	    		{
	    			depends.add(project);
	    		}
	    		
	    		for (ProjectNode depend : project.getNodeDependencies())
	    		{
	    			gatherDependencies(depends, depend, true);
	    		}
    		}
    	}
    }


    // ----------------------------------------------------------
    private void importProjects(
    		ImporterManifest manifest, ImportNode node,
    		IProgressMonitor monitor,
    		List<ImportError> errors)
    {
    	if (node instanceof ProjectGroupNode)
    	{
    		for (ImportNode child : ((ProjectGroupNode) node).getChildren())
    		{
    			importProjects(manifest, child, monitor, errors);
    		}
    	}
    	else if (node instanceof ProjectNode)
    	{
    		ProjectNode project = (ProjectNode) node;
    		
    		importProject(manifest, project, monitor, errors);
    	}
    }


    // ----------------------------------------------------------
    private void importPreferences(
    		final PreferencesNode prefs,
    		IProgressMonitor monitor,
    		final List<ImportError> errors)
    {
    	//monitor.subTask("Workspace preferences");

    	final ImportedPreferences importedPrefs =
    			ImportedPreferences.getInstance();

    	Date importedAt = importedPrefs.uriLastImported(prefs.getURI());
    	if (importedAt != null)
    	{
    		return;
    	}

    	try
    	{
	        URL url = new URL(prefs.getURI());
	        final InputStream stream = url.openStream();

	        Display.getDefault().syncExec(new Runnable() {
				public void run()
				{
					try
					{
						IPreferencesService service =
								Platform.getPreferencesService();
						IExportedPreferences preferences =
								service.readPreferences(stream);

						IPreferenceFilter[] filters = new IPreferenceFilter[] {
							new InstancePreferenceFilter()
						};

						service.applyPreferences(preferences, filters);
						
						importedPrefs.trackUri(prefs.getURI());
					}
					catch (CoreException e)
					{
						Activator.getDefault().log(e);
			    		//FIXME
			    		//errors.add(new ImportError(prefs, e.getMessage()));
					}
				}
	        });

	        stream.close();
    	}
    	catch (Exception e)
    	{
    		Activator.getDefault().log(e);
    		//FIXME
    		//errors.add(new ImportError(prefs, e.getMessage()));
    	}

    	//monitor.worked(1);
    }


    // ----------------------------------------------------------
    private void importProject(
    		ImporterManifest manifest,
    		ProjectNode project,
    		IProgressMonitor monitor,
    		List<ImportError> errors)
    {
    	ProjectTracker tracker = ProjectTracker.getInstance();
    	String trackedName = tracker.projectNameForUri(project.getURI());
    	
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();

    	if (trackedName != null)
    	{
    		IProject trackedProject =
    				workspace.getRoot().getProject(trackedName);
    		
    		if (trackedProject.exists())
    		{
    			return;
    		}
    	}

    	ProjectNode[] depends = project.getNodeDependencies();
    	
        for (ProjectNode depend : depends)
        {
        	importProject(manifest, depend, monitor, errors);
        }

    	monitor.subTask(project.getName());

    	try
    	{	
	        IPath workspacePath = workspace.getRoot().getLocation();
	        File workspaceDir = workspacePath.toFile();
	
	        URL url = new URL(project.getURI());
	        InputStream stream = url.openStream();
	
	        File tempFile = File.createTempFile("downloadedproject", ".zip");
	        ZipUtils.copyStreamToFile(stream, tempFile, System.currentTimeMillis());
	        stream.close();
	
	        String rootDir = ZipUtils.directoryAtArchiveRoot(tempFile);
	
	        ZipFile zipFile = new ZipFile(tempFile);
	        InputStream descriptionStream;
	        
	        if (rootDir == null)
	        {
	        	ZipEntry entry = zipFile.getEntry(".project");
	        	descriptionStream = zipFile.getInputStream(entry);
	        }
	        else
	        {
	        	ZipEntry entry = zipFile.getEntry(rootDir + "/.project");
	        	descriptionStream = zipFile.getInputStream(entry);
	        }
	
	        IProjectDescription description =
	        		workspace.loadProjectDescription(descriptionStream);
	
	        IProject workspaceProject =
	                workspace.getRoot().getProject(description.getName());
	
	        if (!workspaceProject.exists())
	        {
	            File projectDir = new File(workspaceDir, description.getName());
	
	        	if (rootDir == null)
		        {
		        	ZipUtils.unpack(projectDir, tempFile);
		        }
		        else
		        {
		        	ZipUtils.unpack(workspaceDir, tempFile);
		        	File extractedDir = new File(workspaceDir, rootDir);
	
		        	extractedDir.renameTo(projectDir);
		        }
	
		        workspaceProject.create(description, null);
		        workspaceProject.open(null);
		        
		        tracker.setProjectNameForUri(project.getURI(),
		        		description.getName());
		        
	        }
	        // Send an event to the server indicating that a starter project has been downloaded.
			SensorBaseClient.getInstance().downloadStarterProjectHappened(workspaceProject.getDescription().getLocationURI().getPath(), description.getName());
			
	        tempFile.delete();
    	}
    	catch (Exception e)
    	{
    		errors.add(new ImportError(project, e.getMessage()));
    		Activator.getDefault().log(e);
    	}

        monitor.worked(1);
    }
}
