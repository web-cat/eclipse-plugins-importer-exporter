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

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.webcat.eclipse.projectlink.Activator;
import org.webcat.eclipse.projectlink.importer.model.ImportNode;

// -------------------------------------------------------------------------
/**
 *  The label provider for the import target tree in the wizard.
 *
 *  @author  Ellen Boyd (Ellen Boyd)
 *  @version Feb 4, 2012
 */
public class ImporterLabelProvider
	extends LabelProvider implements IColorProvider
{
	//~ Instance/static variables .............................................

    private Image folderImage;
    private Image fileImage;

    
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new instance of the label provider.
     */
    public ImporterLabelProvider()
    {
        folderImage = Activator.getImageDescriptor("folder.png").createImage();
        fileImage = Activator.getImageDescriptor("box.png").createImage();
    }


    // ----------------------------------------------------------
    public void dispose()
    {
        folderImage.dispose();
        fileImage.dispose();

        super.dispose();
    }


    // ----------------------------------------------------------
    public Image getImage(Object element)
    {
        ImportNode object = (ImportNode)element;

        if (object.isContainer())
        {
            return folderImage;
        }
        else
        {
            return fileImage;
        }
    }


    // ----------------------------------------------------------
    public String getText(Object element)
    {
        ImportNode object = (ImportNode)element;

        if (object.getName() != null)
        {
            return object.getName();
        }
        else if (object.isContainer())
        {
            return "";
        }
        else
        {
            return super.getText(element);
        }
    }


    // ----------------------------------------------------------
	public Color getForeground(Object element)
	{
		return null;
	}


    // ----------------------------------------------------------
	public Color getBackground(Object element)
	{
		return null;
	}
}
