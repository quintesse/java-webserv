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
 * This class is used to hold information used to match requests to servlets.
 * It stores information about the request method and host name it matches as
 * well as a reference to a <code>PathMatcher</code> that can be used to match
 * certain (parts of the) request paths. And finally it holds a reference to
 * the <code>Servlet</code> that will handle the request if all of the previous
 * items match up.
 * @see org.codejive.websrv.util.PathMatcher
 * @see org.codejive.websrv.servlet.Servlet
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class RequestMatch {
    
	private String method;
	private String hostName;
	private PathMatcher pathMatcher;
	private Servlet servlet;

	/**
	 * Creates a new RequestMatch
	 */
	public RequestMatch() {
	}

	/**
	 * Creates a new RequestMatch
	 * @param method
	 * @param hostName
	 * @param pathMatcher
	 * @param servlet
	 */
	public RequestMatch(String method, String hostName, PathMatcher pathMatcher, Servlet servlet) {
		assert(servlet != null);
		this.method = method;
		this.hostName = hostName;
		this.pathMatcher = pathMatcher;
		this.servlet = servlet;
	}

	/**
	 * Returns the request method(s) that this matcher will match
	 * @return the request method(s) that this matcher will match or null
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets the request method(s) that this matcher will match
	 * @param method The new request method(s) or null
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Returns the hostname(s) that this matcher will match
	 * @return the hostname(s) that this matcher will match or null
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Sets the hostname(s) that this matcher will match
	 * @param hostName The new hostname(s) or null
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Returns the <code>PathMatcher</code> that will be used to match against
	 * the path part of the request
	 * @return the <code>PathMatcher</code> defined for this matcher or null
	 */
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	/**
	 * Sets the <code>PathMatcher</code> that will be used to match against
	 * the path part of the request
	 * @param matcher the new <code>PathMatcher</code> for this matcher or null
	 */
	public void setPathMatcher(PathMatcher matcher) {
		this.pathMatcher = matcher;
	}

	/**
	 * Returns the <code>Servlet</code> that will be used to handle those requests
	 * that this matcher matches
	 * @return a <code>Servlet</code>
	 * @see org.codejive.websrv.servlet.Servlet
	 */
	public Servlet getServlet() {
		return servlet;
	}

	/**
	 * Sets the <code>Servlet</code> that will be used to handle those requests
	 * that this matcher matches
	 * @param servlet the new <code>Servlet</code>
	 * @see org.codejive.websrv.servlet.Servlet
	 */
	public void setServlet(Servlet servlet) {
		assert(servlet != null);
		this.servlet = servlet;
	}
}
