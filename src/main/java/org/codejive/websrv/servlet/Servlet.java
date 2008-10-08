/*
 * Servlet.java
 *
 * Created on Aug 13, 2007, 12:15:52 AM
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
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;

/**
 * Classes that implement this interface are used for generating a response for
 * a client request
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface Servlet {
	
	/**
	 * Processes a client request and generates a response
	 * @param requestPath The path part of the originating request
	 * @param request The originating HTTP request
	 * @param response The HTTP response to send the result to
	 * @throws java.io.IOException will be thrown when data cannot be sent to the client
	 */
	void process(String requestPath, HttpRequest request, HttpResponse response) throws IOException;
}
