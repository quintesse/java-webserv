/*
 * ResponseHandler.java
 * 
 * Created on Aug 28, 2007, 1:45:32 PM
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

import java.io.IOException;

/**
 * This interface must be implemented by classes who know how to generate
 * output in response to a client request
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface ResponseHandler {
	/**
	 * Generates output in response to a client request
	 * @param request The client request
	 * @param response The response object to which the output will be written
	 * @throws java.io.IOException Will be thrown when the response cannot be written
	 */
	void handleResponse(HttpRequest request, HttpResponse response) throws IOException;
}
