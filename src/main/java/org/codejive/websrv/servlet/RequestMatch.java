/*
 * RequestMatch.java
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

package org.codejive.websrv.servlet;

import org.codejive.websrv.util.PathMatcher;

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class RequestMatch {
    
	private String method;
	private String hostName;
	private PathMatcher pathMatcher;
	private Servlet servlet;

	public RequestMatch() {
	}

	public RequestMatch(String method, String hostName, PathMatcher pathMatcher, Servlet servlet) {
		this.method = method;
		this.hostName = hostName;
		this.pathMatcher = pathMatcher;
		this.servlet = servlet;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public void setPathMatcher(PathMatcher matcher) {
		this.pathMatcher = matcher;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}
}
