/*
 * HttpRequest.java
 *
 * Created on Aug 12, 2007, 9:24:28 PM
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

package org.codejive.websrv.protocol.http;

/**
 * Classes implementing this interface are used to hold all the relevant
 * contextual information of a HTTP request. The basic format of a GET
 * or POST request looks like this:
 * <pre>
 * METHOD SCHEME://USER:PASSWORD@HOST:PORT/PATH?QUERY PROTOCOL
 * HEADER-KEY : HEADER-VALUE
 * HEADER-KEY : HEADER-VALUE
 * ... etc ...
 * </pre>
 * and a more realistic example:
 * <pre>
 * GET /index.jsp?search=websrv HTTP/1.1
 * host : www.example.com
 * </pre>
 * All of the seperate bits of information that can be encountered in a HTTP
 * request can be retrieved by using the methods defined in this interface.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface HttpRequest {

	/**
	 * Returns the HTTP request method (eg GET, POST, etc)
	 * @return Returns the HTTP request method
	 */
	public java.lang.String getRequestMethod();

	/**
	 * Returns the HTTP protocol version for the request (eg HTTP/1.1)
	 * @return Returns the HTTP protocol version
	 */
	public java.lang.String getRequestProtocol();

	/**
	 * Returns the full request URL (including path and parameters, eg
	 * http://user:password@test.com:8000/path/to/document.html?a=1&amp;b=2)
	 * @return the full request URL
	 */
	public java.lang.String getUrl();

	/**
	 * Returns the scheme part of the request URL
	 * (eg <b>http:</b>//user:password@test.com:8000/path/to/document.html?a=1&amp;b=2)
	 * or null if this information is absent
	 * @return the scheme part of the request URL
	 */
	public java.lang.String getScheme();

	/**
	 * Returns the user information part of the request URL
	 * (eg http://<b>user:password</b>@test.com:8000/path/to/document.html?a=1&amp;b=2)
	 * or null if this information is absent
	 * @return the user information part of the request URL
	 */
	public java.lang.String getUserInfo();

	/**
	 * Returns the host part of the request URL
	 * (eg http://user:password@<b>test.com:8000</b>/path/to/document.html?a=1&amp;b=2)
	 * or null if this information is absent
	 * @return the scheme part of the request URL
	 */
	public java.lang.String getHost();

	/**
	 * Returns the port part of the request URL
	 * (eg http://user:password@test.com:<b>8000</b>/path/to/document.html?a=1&amp;b=2)
	 * or null if this information is absent
	 * @return the port part of the request URL
	 */
	public int getPort();

	/**
	 * Returns the path part of the request URL
	 * (eg http://user:password@test.com<b>/path/to/document.html</b>?a=1&amp;b=2)
	 * or null if this information is absent
	 * @return the path part of the request URL
	 */
	public java.lang.String getPath();

	/**
	 * Returns the query part of the request URL
	 * (eg http://user:password@test.com/path/to/document.html?<b>a=1&amp;b=2</b>)
	 * or null if this information is absent
	 * @return the query part of the request URL
	 */
	public java.lang.String getQuery();

	/**
	 * Returns the value of the request header specified by the given name or
	 * null if the given name was not found
	 * @param key The name of the request header value to retrieve
	 * @return The value of the requested header or null
	 */
	public java.lang.String getHeader(java.lang.String key);

	/**
	 * Returns a set of all the available request header names
	 * @return A set of available header names
	 */
	public java.util.Set<java.lang.String> getHeaderNames();

	/**
	 * Returns the value of the request parameter specified by the given
	 * name or null if the given name was not found
	 * @param key The name of the request parameter value to retrieve
	 * @return The value of the requested parameter or null
	 */
	public java.lang.String getParameter(java.lang.String key);

	/**
	 * Returns a set of all the available request parameter names
	 * @return A set of available request parameter names
	 */
	public java.util.Set<java.lang.String> getParameterNames();

}
