/*
 * MimeType.java
 *
 * Created on Aug 13, 2007, 3:47:20 AM
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

package org.codejive.websrv.mimetype;

/**
 * This class holds the information for one particular mime-type mapping.
 * Which is a list of file extensions and the mime-type that they relate to.
 * An example:
 * <pre>
 * MimeType mt = new MimeType("text/html", ".html", ".htm");
 * </pre>
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class MimeType {
    
	/**
	 * The name of the mime-type
	 */
	private String mimeType;
	
	/**
	 * A list of file extensions
	 */
	private String[] fileExtensions;

	/**
	 * Creates a new "empty" mime type
	 */
	public MimeType() {
	}

	/**
	 * Creates a new mime-type using the given name and extensions
	 * @param mimeType The name for the new mime-type
	 * @param fileExtensions The file extensions
	 */
	public MimeType(String mimeType, String... fileExtensions) {
		this.fileExtensions = fileExtensions;
		this.mimeType = mimeType;
	}

	/**
	 * Returns the file extensions mapped to this mime-type
	 * @return An array of strings containing file extensions
	 */
	public String[] getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * Sets the file extensions mapped to this mime-type
	 * @param fileExtensions An array of strings containing file extensions
	 */
	public void getFileExtensions(String... fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	/**
	 * Returns the name of the mime-type
	 * @return A mime-type name
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets the name of the mime-type
	 * @param mimeType A mime-type name
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
