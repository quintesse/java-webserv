/*
 * FileExtensionPathMatcher.java
 *
 * Created on Aug 13, 2007, 5:24:47 AM
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

package org.codejive.websrv.util;

/**
 * This path matcher only checks if the given path ends in a period followed by
 * any of the configured file extensions
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class FileExtensionPathMatcher extends PathMatcher {

	private String[] fileExtensions;
	
	/**
	 * Creates a FileExtensionPathMatcher
	 */
	public FileExtensionPathMatcher() {
	}

	/**
	 * Creates a FileExtensionPathMatcher
	 * @param fileExtensions A list of file extensions (without any leading dots)
	 */
	public FileExtensionPathMatcher(String... fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	/**
	 * Returns the list of file extensions configured for this matcher
	 * @return a list of file extensions
	 */
	public String[] getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * Sets the list of file extensions for this matcher
	 * @param fileExtension a list of file extensions
	 */
	public void setFileExtensions(String[] fileExtension) {
		this.fileExtensions = fileExtension;
	}

	public String matches(String path) {
		for (String ext : fileExtensions) {
            if (path.endsWith("." + ext)) {
				return path;
			}
		}
		return null;
	}

}
