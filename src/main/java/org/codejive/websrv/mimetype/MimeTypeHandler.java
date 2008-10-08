/*
 * MimeTypeHandler.java
 * 
 * Created on 23-oct-2007, 12:48:29
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

package org.codejive.websrv.mimetype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;

/**
 * This class is used to handle passing of certain types of data to the client.
 * Depending on the mime-type of the resource that has to be passed certain
 * implementations of this class will treat their resource data in a certain way.
 * A very simple implementation might just pass the resource data directly to
 * the client without affecting it in any way (this would be necessary for most
 * binary data types like images for example) while on the other extreme a handler
 * might generate output that doesn't at all look like the original resource data
 * (imagine an textual SVG resource that gets turned into a binary image file).
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface MimeTypeHandler {
	
	/**
	 * Passes the resource pointed to by the file to the client
	 * @param resourceFile The file resource to be passed
	 * @param request The originating request
	 * @param response The response to use for generating the output
	 * @throws java.io.IOException Will be thrown if the resource could not be
	 * found, could not be passed or could not be handled/generated
	 */
	void process(File resourceFile, HttpRequest request, HttpResponse response) throws IOException;
	
	/**
	 * Passes the resource pointed to by the url to the client
	 * @param resourceUrl The url to the resource to be passed
	 * @param request The originating request
	 * @param response The response to use for generating the output
	 * @throws java.io.IOException Will be thrown if the resource could not be
	 * found, could not be passed or could not be handled/generated
	 */
	void process(URL resourceUrl, HttpRequest request, HttpResponse response) throws IOException;
	
	/**
	 * Passes the resource contained in the input stream to the client
	 * @param resource The stream of resource data to be passed to the client
	 * @param request The originating request
	 * @param response The response to use for generating the output
	 * @throws java.io.IOException Will be thrown if the resource could not be
	 * found, could not be passed or could not be handled/generated
	 */
	void process(InputStream resource, HttpRequest request, HttpResponse response) throws IOException;
}
