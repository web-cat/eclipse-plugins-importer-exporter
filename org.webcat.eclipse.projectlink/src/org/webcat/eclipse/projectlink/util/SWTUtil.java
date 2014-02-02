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

package org.webcat.eclipse.projectlink.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;

//--------------------------------------------------------------------------
/**
 * A utility class used to simplify access to some SWT resources. This code is
 * adapted from code in the SWT source.
 *
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class SWTUtil
{
	// ----------------------------------------------------------
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated dispaly. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 * 
	 * @return the standard display
	 */
	public static Display getStandardDisplay()
	{
		Display display = Display.getCurrent();
		
		if (display == null)
		{
			display = Display.getDefault();
		}

		return display;
	}


	// ----------------------------------------------------------
	/**
	 * Returns the shell for the given widget. If the widget doesn't represent a
	 * SWT object that manage a shell, {@code null} is returned.
	 * 
	 * @param widget the widget
	 * @return the shell for the given widget
	 */
	public static Shell getShell(Widget widget)
	{
		if (widget instanceof Control)
		{
			return ((Control) widget).getShell();
		}
		else if (widget instanceof Caret)
		{
			return ((Caret) widget).getParent().getShell();
		}
		else if (widget instanceof DragSource)
		{
			return ((DragSource) widget).getControl().getShell();
		}
		else if (widget instanceof DropTarget)
		{
			return ((DropTarget) widget).getControl().getShell();
		}
		else if (widget instanceof Menu)
		{
			return ((Menu) widget).getParent().getShell();
		}
		else if (widget instanceof ScrollBar)
		{
			return ((ScrollBar) widget).getParent().getShell();
		}
		else
		{
			return null;
		}
	}


	// ----------------------------------------------------------
	/**
	 * Returns a width hint for a button control.
	 * 
	 * @param button the button
	 * @return a width hint
	 */
	public static int getButtonWidthHint(Button button)
	{
		button.setFont(JFaceResources.getDialogFont());

		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(
				IDialogConstants.BUTTON_WIDTH);

		return Math.max(widthHint, button.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x);
	}


	// ----------------------------------------------------------
	/**
	 * Sets width and height hint for the button control. <b>Note:</b> This is
	 * a NOP if the button's layout data is not an instance of
	 * {@code GridData}.
	 * 
	 * @param button the button for which to set the dimension hint
	 */
	public static void setButtonDimensionHint(Button button)
	{
		assert button != null;
		Object gd = button.getLayoutData();
		
		if (gd instanceof GridData)
		{
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}


	// ----------------------------------------------------------
	/**
	 * Returns an estimate of the height in pixels of a table with the
	 * specified number of rows.
	 * 
	 * @param table the Table whose height should be calculated
	 * @param rows the number of rows that should be used to estimate the
	 *     height of the table
	 * @return the estimated height of the table, in pixels
	 */
	public static int getTableHeightHint(Table table, int rows)
	{
		if (table.getFont().equals(JFaceResources.getDefaultFont()))
		{
			table.setFont(JFaceResources.getDialogFont());
		}

		int result = table.getItemHeight() * rows + table.getHeaderHeight();

		if (table.getLinesVisible())
		{
			result += table.getGridLineWidth() * (rows - 1);
		}

		return result;
	}


	// ----------------------------------------------------------
	/**
	 * Sets the value of a Text widget, substituting the empty string if the
	 * value is null.
	 * 
	 * @param text the Text widget
	 * @param value the value
	 */
	public static void setText(Text text, String value)
	{
		text.setText(value != null ? value : "");
	}


	// ----------------------------------------------------------
	/**
	 * Gets the value of a Text widget after trimming spaces.
	 * 
	 * @param text the Text widget
	 * @return the value of the text widget
	 */
	public static String getText(Text text)
	{
		return text.getText().trim();
	}
}
