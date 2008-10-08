/*
 * RequestMatcherServlet.java
 *
 * Created on Aug 13, 2007, 4:50:41 PM
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

import java.io.IOException;
import java.util.ArrayList;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;

/**
 * This servlet basically serves as a "switch board", distributing requests to
 * other servlets depending on certain conditions and patterns found in the
 * client request. The servlet manages a list of request matchers and their
 * corresponding servlet. Each time a request matches a certain request matcher
 * the request will be passed on to the corresponding servlet. Only the first
 * servlet who's matcher matches the request will be executed.
 * @see org.codejive.websrv.servlet.RequestMatch
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class RequestMatcherServlet implements Servlet {

    private ArrayList<RequestMatch> requestMatchers;

	/**
	 * Creates a new RequestMatcherServlet
	 */
	public RequestMatcherServlet() {
		requestMatchers = new ArrayList<RequestMatch>();
	}

	/**
	 * Returns the list of matchers configured for this servlet
	 * @return a list of request matchers
	 */
	public ArrayList<RequestMatch> getRequestMatchers() {
		return requestMatchers;
	}

	private String matches(RequestMatch match, String requestMethod, String requestHost, String requestPath) {
		String result = null;
		boolean matching = false;
		if ((match.getMethod() == null) || match.getMethod().equals("*")) {
			matching = true;
		} else {
			// Silly way to check if the requst method appears in the list of supported methods
			String list = ("," + match.getMethod() + ",").toLowerCase();
			String key = ("," + requestMethod + ",").toLowerCase();
			if (list.contains(key)) {
				matching = true;
			}
		}
		if (matching) {
			String hostName = match.getHostName();
            if ((hostName == null) || hostName.equals("*")) {
				matching = true;
			} else if (hostName.startsWith("*.")) {
                String regex = "^" + hostName.replace("*.", ".{0,}?\\.?") + "$";
				matching = requestHost.matches(regex);
            } else if (hostName.endsWith(".*")) {
                String regex = "^" + hostName.replace(".*", "\\..{0,}?") + "$";
				matching = requestHost.matches(regex);
            } else {
                matching = hostName.equalsIgnoreCase(requestHost);
            }
		}
		if (matching) {
			if (match.getPathMatcher() != null) {
				result = match.getPathMatcher().matches(requestPath);
			} else {
				result = requestPath;
			}
		}
		return result;
	}
	
	public void process(String requestPath, HttpRequest request, HttpResponse response) throws IOException {
		// Find a servlet for our request
		Servlet servlet = null;
		String result = null;
		for (RequestMatch match : requestMatchers) {
			result = matches(match, request.getRequestMethod(), request.getHost(), requestPath);
			if (result != null) {
				servlet = match.getServlet();
				break;
			}
		}
		
		// If no servlet was configured will use the unsupported servlet
		// which will always generate a 501 NOT IMPLEMENTED error
		if (servlet == null) {
			servlet = new UnsupportedServlet();
		}
		
		servlet.process(result, request, response);
	}

}
