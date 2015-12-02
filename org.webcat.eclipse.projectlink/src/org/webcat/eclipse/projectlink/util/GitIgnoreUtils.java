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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.webcat.eclipse.projectlink.Activator;

/**
 * Utility class for setting up and modifying
 * the .gitignore file
 * 
 * @author Ayaan Kazerouni
 * @version September 5, 2015
 */
public class GitIgnoreUtils {

	private GitIgnoreUtils() {
		//do nothing
	}
	
	/**
	 * Checks if the project at the specified path has
	 * a .gitignore file, and if it doesn't, creates one.
	 * Creates the file using Java and C++ .gitignore
	 * templates from Github.
	 * 
	 * https://github.com/github/gitignore
	 * 
	 * @param projectUri
	 * 				the specified project path
	 */
	public static void writeToGitIgnore(String projectUri) {
		File gitignore = new File(projectUri, "/.gitignore");
		if (!gitignore.exists()) {
			try {
				gitignore.createNewFile();
				FileWriter fw = new FileWriter(gitignore, true);
				BufferedWriter out = new BufferedWriter(fw);
				out.write("~*"); out.newLine();
				out.write("._*"); out.newLine();
				out.write(".TemporaryItems"); out.newLine();
				out.write(".DS_Store"); out.newLine();
				out.write("Thumbs.db"); out.newLine();
				out.write("/bin/"); out.newLine();
				out.write("*.class"); out.newLine();
//				out.write("*.classpath"); out.newLine();
				out.write("*.jar"); out.newLine();
				out.write("*.war"); out.newLine();
				out.write("*.ear"); out.newLine();
				out.write("hs_error_pid*"); out.newLine();
				out.write("*.slo"); out.newLine();
				out.write("*.lo"); out.newLine();
				out.write("*.o"); out.newLine();
				out.write("*.obj"); out.newLine();
				out.write("*.gch"); out.newLine();
				out.write("*.pch"); out.newLine();
				out.write("*.so"); out.newLine();
				out.write("*.dylib"); out.newLine();
				out.write("*.mod"); out.newLine();
				out.write("*.lai"); out.newLine();
				out.write("*.la"); out.newLine();
				out.write("*.a"); out.newLine();
				out.write("*.dll"); out.newLine();
				out.write("*.lib"); out.newLine();
				out.write("*.exe"); out.newLine();
//				out.write("*.out"); out.newLine();
				out.write("*.app"); out.newLine();
				out.flush();
				out.close();
				fw.close();
			} catch (IOException e) {
				Activator.getDefault().log(e);
			}
		}
	}
}
