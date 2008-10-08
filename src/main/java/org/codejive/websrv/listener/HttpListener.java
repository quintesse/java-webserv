/*
 * HttpListener.java
 *
 * Created on Aug 11, 2007, 7:14:50 PM
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

package org.codejive.websrv.listener;

import org.codejive.websrv.servlet.Servlet;
import org.codejive.websrv.servlet.UnsupportedServlet;
import java.io.IOException;
import java.net.InetAddress;
import org.codejive.websrv.protocol.http.HttpProtocolHandler;
import org.codejive.websrv.protocol.ProtocolHandler;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;
import org.codejive.websrv.protocol.http.ResponseHandler;

/**
 * This class extends the ThreadedProtocolListener implementing
 * the HTTP protocol by using the HttpProtocolHandler
 * @see org.codejive.websrv.listener.ThreadedProtocolListener
 * @see org.codejive.websrv.protocol.http.HttpProtocolHandler
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpListener extends ThreadedProtocolListener implements ResponseHandler {

	/**
	 * The servlet that will be used to generate responses
	 */
	private Servlet defaultServlet;
	
	/**
	 * The time-out in milliseconds after which an idle connection
	 * will be closed. 0 means never (default = 10 seconds)
	 */
	private int keepAliveTimeout;
	
	/**
	 * The maximum number of requests that will be served before
	 * closing the connection. -1 means unlimited (default = -1)
	 */
	private int keepAliveMaxRequests;
	
	/**
	 * The total number of requests that have been handled
	 */
	private int requestCount;

	/**
	 * Creates a listener on the default address using port 80
	 */
	public HttpListener() {
		this(null, 80);
	}

	/**
	 * Creates a listener on the default address using the specified port
	 * @param port The port to listen on
	 */
	public HttpListener(int port) {
		this(null, port);
	}

	/**
	 * Creates a listener on the specified address and port
	 * @param address The local address to bind to
	 * @param port The port to listen on
	 */
	public HttpListener(InetAddress address, int port) {
		super(address, port);
		keepAliveTimeout = 10000;
		keepAliveMaxRequests = -1;
		requestCount = 0;
	}

	/**
	 * Returns the servlet that will be used to generate the responses
	 * for each client request
	 * @see org.codejive.websrv.servlet.Servlet
	 * @return A servlet
	 */
	public Servlet getDefaultServlet() {
		return defaultServlet;
	}

	/**
	 * Sets the servlet that will be used to generate the responses
	 * for each client request
	 * @see org.codejive.websrv.servlet.Servlet
	 * @param responseHandler A servlet
	 */
	public void setDefaultServlet(Servlet responseHandler) {
		this.defaultServlet = responseHandler;
	}

	/**
	 * Returns the time-out in milliseconds after which an idle connection
	 * will be closed. 0 means never
	 * @return The time-out in milliseconds
	 */
	public int getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	/**
	 * Sets the time-out in milliseconds after which an idle connection
	 * will be closed. 0 means never
	 * @param keepAliveTimeout The time-out in milliseconds
	 */
	public void setKeepAliveTimeout(int keepAliveTimeout) {
		this.keepAliveTimeout = keepAliveTimeout;
	}

	/**
	 * Returns the maximum number of requests that will be served before
	 * closing the connection. -1 means unlimited
	 * @return The maximum number of requests to serve
	 */
	public int getKeepAliveMaxRequests() {
		return keepAliveMaxRequests;
	}

	/**
	 * Sets the maximum number of requests that will be served before
	 * closing the connection. -1 means unlimited
	 * @param keepAliveMaxRequests The maximum number of requests to serve
	 */
	public void setKeepAliveMaxRequests(int keepAliveMaxRequests) {
		this.keepAliveMaxRequests = keepAliveMaxRequests;
	}

	/**
	 * Returns the total number of requests that have been served by
	 * this listener so far
	 * @return The total number of requests served
	 */
	public int getRequestCount() {
		return requestCount;
	}

	/**
	 * Increments the total number of requests that have been served
	 * so far by this listener by one
	 */
	private void incRequestCount() {
		synchronized (this) {
			requestCount++;
		}
	}
	
	protected ProtocolHandler getProtocolHandler() {
		HttpProtocolHandler handler = new HttpProtocolHandler(this);
		handler.setKeepAliveTimeout(keepAliveTimeout);
		handler.setKeepAliveMaxRequests(keepAliveMaxRequests);
		return handler;
	}

	public void handleResponse(HttpRequest request, HttpResponse response) throws IOException {
		incRequestCount();
		
		// Get the servlet for our request
		Servlet servlet = defaultServlet;
		
		// If no servlet was configured will use the unsupported servlet
		// which will always generate a 501 NOT IMPLEMENTED error
		if (servlet == null) {
			servlet = new UnsupportedServlet();
		}
		
		servlet.process(request.getPath(), request, response);
	}
}
