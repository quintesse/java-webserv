/*
 * HttpRequestImpl.java
 *
 * Created on Aug 11, 2007, 6:22:07 PM
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Set;
import org.codejive.websrv.util.UriEncoder;

/**
 * This class implements HttpRequest used to hold all the relevant
 * contextual information of a HTTP request. Setters exist for all of
 * the fields and a utility method parseUrl() is provided to easily
 * extract most of the information from the url-part of a request.
 * But most of the time the rest of the code will only access this
 * class by means of the HttpRequest which only defines getters
 * efectively turning this into a read-only class after construction.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpRequestImpl implements HttpRequest {

	/**
	 * The HTTP request method
	 */
	private String requestMethod;
	/**
	 * The HTTP protocol version for the request
	 */
	private String requestProtocol;
	/**
	 * The full request URL
	 */
	private String url;
	/**
	 * The scheme-part of the request URL
	 */
	private String scheme;
	/**
	 * The user-part of the request URL
	 */
	private String userInfo;
	/**
	 * The host-part of the request URL
	 */
	private String host;
	/**
	 * The port-part of the request URL
	 */
	private int port;
	/**
	 * The path-part of the request URL
	 */
	private String path;
	/**
	 * The query-part of the request URL
	 */
	private String query;
	/**
	 * A map of all the available request parameters
	 */
	private HashMap<String, String> parameters;
	/**
	 * A map of all the available request headers
	 */
	private HashMap<String, String> headers;

	/**
	 * Creates a new HttpRequestImpl
	 */
	public HttpRequestImpl() {
		parameters = new HashMap<String, String>();
		headers = new HashMap<String, String>();
	}

	/**
	 * Creates a new HttpRequestImpl
	 * @param copy a HttpRequest to copy
	 */
	public HttpRequestImpl(HttpRequest copy) {
		parameters = new HashMap<String, String>();
		headers = new HashMap<String, String>();
		requestMethod = copy.getRequestMethod();
		requestProtocol = copy.getRequestProtocol();
		url = copy.getUrl();
		scheme = copy.getScheme();
		userInfo = copy.getUserInfo();
		host = copy.getHost();
		port = copy.getPort();
		path = copy.getPath();
		query = copy.getQuery();
		for (String name : copy.getParameterNames()) {
			parameters.put(name, copy.getParameter(name));
		}
		for (String name : copy.getHeaderNames()) {
			headers.put(name, copy.getHeader(name));
		}
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	/**
	 * Sets the HTTP request method (eg GET, POST, etc)
	 * @param method The request method
	 */
	public void setRequestMethod(String method) {
		this.requestMethod = method;
	}

	public String getRequestProtocol() {
		return requestProtocol;
	}

	/**
	 * Sets the HTTP protocol version for the request (eg HTTP/1.1)
	 * @param protocol The HTTP protocol version
	 */
	public void setRequestProtocol(String protocol) {
		this.requestProtocol = protocol;
	}

	public String getScheme() {
		return scheme;
	}

	/**
	 * Sets the scheme-part of the request URL (eg http://). Set to null
	 * if this information is not available
	 * @param scheme The request URL's scheme
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
		updateUrl();
	}

	public String getUserInfo() {
		return userInfo;
	}

	/**
	 * Sets the user-part of the request URL. Set to null if this
	 * information is not available
	 * @param userInfo The request URL's user information
	 */
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
		updateUrl();
	}

	public String getHost() {
		return host;
	}

	/**
	 * Sets the host-part of the request URL. Set to null if this
	 * information is not available
	 * @param host The request URL's host name
	 */
	public void setHost(String host) {
		this.host = host;
		updateUrl();
	}

	public int getPort() {
		return port;
	}

	/**
	 * Sets the port-part of the request URL. Set to 0 if this
	 * information is not available
	 * @param port The request URL's port number
	 */
	public void setPort(int port) {
		this.port = port;
		updateUrl();
	}

	public String getPath() {
		return path;
	}

	/**
	 * Sets the path-part of the request URL. Set to null if this
	 * information is not available
	 * @param path The path-part of the request URL
	 */
	public void setPath(String path) {
		this.path = path;
		updateUrl();
	}

	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query-part of the request URL. Set to null if this
	 * information is not available
	 * @param query The query-part of the request URL
	 */
	public void setQuery(String query) {
		this.query = query;
		updateUrl();
	}

	public String getUrl() {
		return url;
	}

	/**
	 * Sets the full request URL (Warning: setting this value will NOT
	 * affect any of the properties of this class even though most of
	 * them are directly related! In most cases it's best to use
	 * <code>parseUrl()</code> instead.)
	 * @param url The request URL
	 * @see org.codejive.websrv.protocol.http.HttpRequestImpl#parseUrl(java.lang.String)
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Parses a request URL and sets all the relevant properties to the
	 * values found in the URL. This affects the following properties:
	 * <code>url</code>, <code>scheme</code>, <code>userInfo</code>, 
	 * <code>host</code>, <code>port</code>, <code>path</code>, 
	 * <code>query</code> and <code>parameters</code>
	 * (NB: Afterwards <code>getUrl()</code> will return exactly the same string
	 * as the one passed to this method. On the other hand calling <code>setUrl()</code>
	 * will <b>not</b> call this method, in that case it's the responsibility of
	 * the calling code to make sure that the other properties are correctly filled)
	 * @param url The request URL
	 * @throws java.net.URISyntaxException Is thrown when the URL could
	 * not be correctly parsed
	 */
	public void parseUrl(String url) throws URISyntaxException {
		this.url = url;
		// Now we parse the url
		URI uri = new URI(url);
		scheme = uri.getScheme();
		userInfo = uri.getUserInfo();
		host = uri.getHost();
		port = (uri.getPort() > 0) ? uri.getPort() : 0;
		path = uri.getPath();
		query = uri.getRawQuery();
		// Unfortunately the URI class doesn't allow access to individual parameters
		// so now we parse them ourselves. For this we need the undecoded version
		// of the query string
		parameters.clear();
		if ((query != null) && (query.length() > 0)) {
            String[] queryParts = query.split("&");
            for (String item : queryParts) {
                String[] itemParts = item.split("=", 2);
                if (itemParts.length == 1) {
                    setParameter(UriEncoder.decode(itemParts[0]), null);
                } else {
                    setParameter(UriEncoder.decode(itemParts[0]), UriEncoder.decode(itemParts[1]));
				}
            }
		}
	}

	public String getParameter(String key) {
		return parameters.get(key.toLowerCase());
	}

	/**
	 * Sets the value of the request parameter specified by the given name.
	 * The value null is not allowed, use removeParameter() instead.
	 * @param key The name of the request parameter
	 * @param value The value of the request parameter
	 */
	public void setParameter(String key, String value) {
		parameters.put(key.toLowerCase(), value);
		updateQuery();
	}

	/**
	 * Removes the request parameter specified by the given name
	 * @param key The name of the request parameter
	 */
	public void removeParameter(String key) {
		parameters.remove(key.toLowerCase());
		updateQuery();
	}

	public Set<String> getParameterNames() {
		return parameters.keySet();
	}

	public String getHeader(String key) {
		return headers.get(key.toLowerCase());
	}

	/**
	 * Sets the value associated with a specific HTTP header
	 * @param key The name of the HTTP header to set
	 * @param value the new value for the HTTP header with the given name
	 */
	public void setHeader(String key, String value) {
		headers.put(key.toLowerCase(), value);
	}

	/**
	 * Removes the request header specified by the given name
	 * @param key The name of the request header
	 */
	public void removeHeader(String key) {
		headers.remove(key.toLowerCase());
	}

	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	private void updateUrl() {
		url = "";
		if (scheme != null) {
			url += scheme + "://";
		}
		if (userInfo != null) {
			url += userInfo + "@";
		}
		if (host != null) {
			url += host;
		}
		if ((port != 0) && (port != 80)) {
			url += ":" + port;
		}
		if (path != null) {
			url += path;
		}
		if (query != null) {
			url += "?" + query;
		}
	}

	private void updateQuery() {
		if (getParameterNames().size() > 0) {
			query = "";
			for (String name : getParameterNames()) {
				if (query.length() > 0) {
					query += "&";
				}
				query += UriEncoder.encode(name) + "=" + UriEncoder.encode(getParameter(name));
			}
		} else {
			query = null;
		}
		updateUrl();
	}
}
