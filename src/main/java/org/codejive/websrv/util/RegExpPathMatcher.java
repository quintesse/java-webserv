/*
 * RegExpPathMatcher.java
 *
 * Created on Aug 13, 2007, 3:52:24 AM
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This path matcher checks the given path against a configured regular expression
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class RegExpPathMatcher extends PathMatcher {

	private transient Pattern compiledPattern;
	
	/**
	 * Creates a new RegExpPathMatcher
	 */
	public RegExpPathMatcher() {
	}

	/**
	 * Creates a new RegExpPathMatcher
	 * @param pattern the regular expression to use for matching paths
	 */
	public RegExpPathMatcher(String pattern) {
		super(pattern);
		compiledPattern = Pattern.compile(pattern);
	}

	@Override
	public String matches(String path) {
		String result = null;
		Matcher m = compiledPattern.matcher(path);
		if (m.matches()) {
			if (m.groupCount() > 0) {
				result = m.group(1);
			} else {
				result = m.group(0);
			}
			if (result == null) {
				result = "";
			}
		}
		return result;
	}
	
}
