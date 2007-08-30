/*
 * WelcomeFiles.java
 *
 * Created on Aug 14, 2007, 3:23:00 AM
 * Copyright Tako Schotanus
 *
 * This file is part of websrv.
 *
 * websrv is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * websrv is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.codejive.websrv.config;

import java.util.ArrayList;

/**
 * This class holds a list of file/document names that a request handler
 * might look for if an incoming request didn't directly specify a document
 * name but rather a folder/container. Typical values for a web server are
 * names like "index.html", "index.htm", "default.html", etc.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class WelcomeFiles {

	/**
	 * List of file/document names
	 */
	private ArrayList<String> fileNames;

	/**
	 * Constructs a new instance
	 */
	public WelcomeFiles() {
		fileNames = new ArrayList<String>();
	}

	/**
	 * Returns a list of file/document names
	 * @return A list of file/document names
	 */
	public ArrayList<String> getFileNames() {
		return fileNames;
	}
	
}
