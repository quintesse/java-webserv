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
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class FileExtensionPathMatcher extends PathMatcher {

	private String[] fileExtensions;
	
	public FileExtensionPathMatcher() {
	}

	public FileExtensionPathMatcher(String... fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public String[] getFileExtension() {
		return fileExtensions;
	}

	public void setFileExtension(String[] fileExtension) {
		this.fileExtensions = fileExtension;
	}

	public String[] matches(String path) {
		for (String ext : fileExtensions) {
            if (path.endsWith("." + ext)) {
				String[] result = { path };
				return result;
			}
		}
		return null;
	}

}
