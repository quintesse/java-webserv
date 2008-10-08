/*
 * MimeTypes.java
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

import java.util.ArrayList;
import org.codejive.websrv.util.FileExtensionPathMatcher;

/**
 * This is a simple container class for mime-types. It is a Java Bean
 * so it and its contents can be easily stored and retrieved.
 * @see org.codejive.websrv.mimetype.MimeType
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class MimeTypes {
    
	/**
	 * A list of mime-types
	 */
	private ArrayList<MimeType> mimeTypes;

	/**
	 * Creates a new empty mime-types container
	 */
	public MimeTypes() {
		mimeTypes = new ArrayList<MimeType>();
	}

	/**
	 * Returns the list of mime-types
	 * @see org.codejive.websrv.mimetype.MimeType
	 * @return A list of mime-types
	 */
	public ArrayList<MimeType> getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * Finds a mime-type by comparing the file extensions to the given path
	 * @param path A path or url pointing to a file
	 * @return The mime-type or null if no match was found
	 */
	public MimeType findByPath(String path) {
		FileExtensionPathMatcher matcher = new FileExtensionPathMatcher();
		for (MimeType def : mimeTypes) {
			matcher.setFileExtensions(def.getFileExtensions());
            if (matcher.matches(path) != null) {
				return def;
			}
		}
		return null;
	}

	/**
	 * Finds a mime-type by comparing the name of the mime-type
	 * @param name A mime-type name (eg "text/plain")
	 * @return The mime-type or null if no match was found
	 */
    public MimeType findByName(String name) {
		for (MimeType def : mimeTypes) {
            if (def.getMimeType().equalsIgnoreCase(name)) {
				return def;
			}
		}
		return null;
    }
}
