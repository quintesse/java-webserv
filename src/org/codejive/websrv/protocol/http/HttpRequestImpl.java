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
 * This class is used to hold all the relevant contextual information
 * of a HTTP request. A utility method parseUrl() is provided to easily
 * extract most of the information from the url-part of a request.
 * 
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpRequestImpl implements HttpRequest {

	private String requestMethod;
	private String requestProtocol;
	private String url;
	private String scheme;
	private String userInfo;
	private String host;
	private int port;
	private String path;
	private String query;
	private HashMap<String, String> parameters;
	private HashMap<String, String> headers;

	public HttpRequestImpl() {
		parameters = new HashMap<String, String>();
		headers = new HashMap<String, String>();
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String method) {
		this.requestMethod = method;
	}

	public String getRequestProtocol() {
		return requestProtocol;
	}

	public void setRequestProtocol(String protocol) {
		this.requestProtocol = protocol;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void parseUrl(String url) throws URISyntaxException {
		this.url = url;
		// Now we parse the url
		URI uri = new URI(url);
		scheme = uri.getScheme();
		userInfo = uri.getUserInfo();
		host = uri.getHost();
		port = uri.getPort();
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

	public void setParameter(String key, String value) {
		parameters.put(key.toLowerCase(), value);
	}

	public Set<String> getParameterNames() {
		return parameters.keySet();
	}

	public String getHeader(String key) {
		return headers.get(key.toLowerCase());
	}

	public void setHeader(String key, String value) {
		headers.put(key.toLowerCase(), value);
	}

	public Set<String> getHeaderNames() {
		return headers.keySet();
	}
}
