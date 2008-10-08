/*
 * SimplePathMatcher.java
 *
 * Created on Aug 13, 2007, 3:54:18 AM
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
 * This matcher checks paths against a simple expression with the following format:
 * <li>Any text will simply match that text in the path</li>
 * <li>A single asterisk (*) will match any file name</li>
 * <li>A double asterisk (**) will match a path with any number of elements</li>
 * This means that:
 * <li><b>/test/foo</b> only matches <b>/test/foo</b></li>
 * <li><b>/test/*</b> matches <b>/test/foo</b> and <b>/test/bar</b> but not
 * <b>/test/xxx</b> nor <b>/test/foo/bar</b></li>
 * <li><b>/test/**</b> matches <b>/test/foo</b> and <b>/test/bar</b> and
 * <b>/test/foo/bar</b> but not <b>/xxx/foo</b></li>
 * If a match was made normally the result of <code>matches()</code> is the path
 * that was passed to it, but it is possible to only return part of the path by
 * introducing parenthesis in the pattern around the part that we're interested in:
 * <li><b>/test(/**)</b> matches <b>/test/foo/bar</b> but will return <b>/foo/bar</b></li>
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class SimplePathMatcher extends PathMatcher {

	private transient Pattern compiledPattern;
	
	/**
	 * Creates a new SimplePathMatcher
	 */
	public SimplePathMatcher() {
	}

	/**
	 * Creates a new SimplePathMatcher
	 * @param pattern the pattern to use for path matching
	 */
	public SimplePathMatcher(String pattern) {
		super(pattern);
		String regexp = pattern.replace(".", "\\.");
		regexp = "^" + regexp.replace("**/", ".{0,}?/?").replace("**", ".{0,}?").replace("*", "[^/]{0,}") + "$";
		compiledPattern = Pattern.compile(regexp);
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