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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.submitter.targets.AssignmentTarget;
import org.webcat.submitter.targets.SubmissionTarget;

//--------------------------------------------------------------------------
/**
 * The label provider for the submission target tree in the wizard.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 */
public class SubmitterLabelProvider extends LabelProvider
{
	//~ Static/instance variables .............................................

	/* The image used for assignment groups and imported groups. */
	private Image folderImage;

	/* The image used for assignment targets. */
	private Image userImage;

	/* The image used for assignment targets. */
	private Image usersImage;

	
	//~ Constructors ..........................................................
	
	// ----------------------------------------------------------
	/**
	 * Creates a new instance of the label provider.
	 */
	public SubmitterLabelProvider()
	{
		folderImage = Activator
		        .getImageDescriptor("folder.png").createImage(); //$NON-NLS-1$
		userImage = Activator
		        .getImageDescriptor("user.png").createImage(); //$NON-NLS-1$
		usersImage = Activator
		        .getImageDescriptor("users.png").createImage(); //$NON-NLS-1$
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose()
	{
		folderImage.dispose();
		userImage.dispose();
		usersImage.dispose();

		super.dispose();
	}


	// ----------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element)
	{
		SubmissionTarget object = (SubmissionTarget) element;

		if (object.isContainer())
		{
			return folderImage;
		}
		else if (object instanceof AssignmentTarget
				&& ((AssignmentTarget) object).usesParameter("partners"))
		{
			return usersImage;
		}
		else
		{
			return userImage;
		}
	}


	// ----------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element)
	{
		SubmissionTarget object = (SubmissionTarget)element;

		if (object.getName() != null)
		{
			return object.getName();
		}
		else
		{
			return super.getText(element);
		}
	}
}
