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
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class RegExpPathMatcher extends PathMatcher {

	private transient Pattern compiledPattern;
	
	public RegExpPathMatcher() {
	}

	public RegExpPathMatcher(String method, String pattern) {
		super(pattern);
		compiledPattern = Pattern.compile(pattern);
	}

	@Override
	public String[] matches(String path) {
		String[] result = null;
		Matcher m = compiledPattern.matcher(path);
		if (m.matches()) {
			result = new String[m.groupCount() + 1];
			for (int i = 0; i <= m.groupCount(); i++) {
				result[i] = m.group(i);
			}
		}
		return result;
	}
	
}
