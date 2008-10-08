/*
 * PathMatcher.java
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

package org.codejive.websrv.util;

/**
 * A path matcher compares a given path against a predefined pattern or condition
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public abstract class PathMatcher {
    
	private String pattern;

	/**
	 * Creates a new PathMatcher
	 */
	public PathMatcher() {
	}

	/**
	 * Creates a new PathMatcher
	 * @param pattern The pattern to use for path matching
	 */
	public PathMatcher(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Returns the currently configured pattern
	 * @return the current pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets a new pattern to use to match paths
	 * @param pattern The new pattern to use for path matching
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Matches a path using the conditions defined by the class' implementation
	 * returning either <code>null</code> if no match could be made or a resulting
	 * path if the match was succesful. The resulting path is often the same as
	 * the path passed as an argument to the method but this is by no means a
	 * requirement.
	 * @param path The path to match
	 * @return the resulting path or <code>null</code> if no match could be made
	 */
	public abstract String matches(String path);
}
