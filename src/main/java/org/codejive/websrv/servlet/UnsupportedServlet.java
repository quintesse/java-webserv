/*
 * UnsupportedServlet.java
 *
 * Created on Aug 13, 2007, 4:19:59 AM
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
import org.codejive.websrv.protocol.http.HttpResponseCode;

/**
 * This is a servlet that will simply send a 501 NOT IMPLEMENTED response to the
 * client. It is used when no other servlet has been found to handle the client
 * request
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class UnsupportedServlet implements Servlet {

	/**
	 * Creates a new UnsupportedServlet
	 */
	public UnsupportedServlet() {
	}

	public void process(String requestPath, HttpRequest request, HttpResponse response) throws IOException {
        response.sendError(HttpResponseCode.CODE_NOT_IMPLEMENTED, request.getRequestMethod());
	}

}
