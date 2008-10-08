/*
 * HttpResponse.java
 *
 * Created on Aug 12, 2007, 9:51:34 PM
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 * This interface provides the support and methods for writing HTTP output to
 * a client connected to the server. The output will automatically be prepended
 * with the necessary HTTP response code and response headers all according to
 * the official HTTP specifications. Output will be buffered up to a certain
 * amount before being sent to the client. Before the first data has been sent
 * (ie before the first buffer has been flushed) it is still possible to change
 * the object's properties, add, change or remove response headers or clear the
 * entire buffer by calling <code>reset()</code>
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface HttpResponse {

	/**
	 * Returns the response code that will be sent/has been sent to the client
	 * @return An HTTP response code
	 */
	public HttpResponseCode getResponseCode();

	/**
	 * Sets the response code that will be sent to the client
	 * @param responseCode The HTTP response code to send
	 * @throws IllegalStateException Is thrown when output has already been
	 * written to the client
	 */
	public void setResponseCode(HttpResponseCode responseCode);

	/**
	 * Returns the character encoding (eg UTF-8) that will be used/has been
	 * used for sending (textual) ouput to the client.
	 * By default the character encoding is set to UTF-8
	 * @return The character encoding
	 */
	public String getCharacterEncoding();

	/**
	 * Sets the character encoding (eg UTF-8) that will be used for sending
	 * (textual) ouput to the client
	 * @param characterEncoding The character encoding
	 * @throws IllegalStateException Is thrown when output has already been
	 * written to the client
	 */
	public void setCharacterEncoding(String characterEncoding);

	/**
	 * Returns the mime type (eg text/html) that will be used/has been used
	 * to define the type of output that will be/has been generated
	 * By default the content type is set to text/plain
	 * @return The mime type
	 */
	public String getContentType();

	/**
	 * Sets the mime type (eg text/html) that will be used to define the type
	 * of output that will be/has been generated
	 * @param contentType The mime type
	 * @throws IllegalStateException Is thrown when output has already been
	 * written to the client
	 */
	public void setContentType(String contentType);

	/**
	 * Returns the size in bytes of the internal buffer that will be used
	 * to gather output before sending it to the client
	 * @return The size of the buffer in bytes
	 */
	public int getBufferSize();

	/**
	 * Sets the size in bytes of the internal buffer that will be used
	 * to gather output before sending it to the client
	 * @param bufferSize The size of the buffer in bytes
	 * @throws IllegalStateException Is thrown when the output buffer has
	 * already been created
	 */
	public void setBufferSize(int bufferSize);

	/**
	 * Retrieves the stream that can be used to write output to the client.
	 * Calling this method for the first  will create the internal buffer
	 * after which it will be impossible to change its size
	 * @return An output stream
	 * @see java.io.OutputStream
	 */
	public OutputStream getOutputStream();

	/**
	 * Retrieves the writer that can be used to write output to the client.
	 * Calling this method for the first  will create the internal buffer
	 * after which it will be impossible to change its size
	 * @return A print writer
	 * @see java.io.PrintWriter
	 */
	public PrintWriter getWriter();

	/**
	 * Indicates if output has already been written to the client (making
	 * it impossible to call <code>reset()</code> or change any of the
	 * object's properties)
	 * @return A boolean indicating if output has already been written
	 */
	public boolean isCommitted();

	/**
	 * Returns the value of the response header specified by the given name or
	 * null if the given name was not found
	 * @param key The name of the response header value to retrieve
	 * @return The value of the response header or null
	 */
	public String getHeader(String key);

	/**
	 * Sets the value of the response header specified by the given name.
	 * The value null is not allowed, use removeHeader() instead.
	 * @param key The name of the response header
	 * @param value The value of the response header
	 */
	public void setHeader(String key, String value);

	/**
	 * Returns a set of all the available response header names
	 * @return A set of available header names
	 */
	public Set<String> getHeaderNames();

	/**
	 * Removes the response header specified by the given name
	 * @param key The name of the response header
	 */
	public void removeHeader(String key);

	/**
	 * Tries to clear all output currently waiting to be sent to the client
	 * leaving the output buffer empty and ready to receive new information.
	 * This only works if NO data has been written yet to the client
	 * @throws IllegalStateException Is thrown when the ouput buffer could
	 * not be reset because output has already been written to the client
	 */
	public void reset();
	
	/**
	 * Generates a HTTP error response page as output to the client.
	 * The result code will be used to determine the type of error while
	 * the message will be added for additional information. This method
	 * will always (try to) reset the currently buffered output and will
	 * abort further processing of the output. Attention: this method will
	 * always throw an exception! Either a PrematureEOFException when the
	 * error page could be written correctly or any other IOException if
	 * something went wrong
	 * @param resultCode The result code to send to the client
	 * @param message Any additional information
	 * @throws java.io.IOException A PrematureEOFException will be thrown
	 * when the error page could be written correctly or any other IOException
	 * if something went wrong
	 */
	public void sendError(HttpResponseCode resultCode, String message) throws IOException;
	
	/**
	 * Redirects the client to a different URL
	 * @param url Any additional information
	 * @throws java.io.IOException A PrematureEOFException will be thrown
	 * when the error page could be written correctly or any other IOException
	 * if something went wrong
	 */
	public void sendRedirect(String url) throws IOException;
}
