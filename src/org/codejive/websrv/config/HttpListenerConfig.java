/*
 * HttpListenerConfig.java
 *
 * Created on Aug 11, 2007, 7:17:04 PM
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

package org.codejive.websrv.config;

import org.codejive.websrv.listener.HttpListener;
import org.codejive.websrv.servlet.Servlet;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Builder class for HttpListener objects
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpListenerConfig {

	/**
	 * The address to bind the listener to
	 */
	private String address;
	
	/**
	 * The port to bind the listener to
	 */
	private int port;
	
	/**
	 * The timeout in milliseconds after which inactive connections
	 * will be closed (default = 10 seconds)
	 */
	private int keepAliveTimeout;
	
	/**
	 * The maximum amount a requests that will be served over one
	 * connection before closing it. Setting it to -1 will allow
	 * an unlimited amount of requests (default = -1).
	 */
	private int keepAliveMaxRequests;
	
	/**
	 * The servlet that will handle client requests
	 */
	private Servlet defaultServlet;

	/**
	 * Constructs a new instance using "localhost" as the address
	 * and "0" as the port
	 */
	public HttpListenerConfig() {
		this("localhost", 0);
	}

	/**
	 * Constructs a new instance using "localhost" as the address
	 * and the given port
	 * @param port The port to use for this instance
     */
	public HttpListenerConfig(int port) {
		this("localhost", port);
	}

	/**
	 * Constructs a new instance using the given address and port
	 * @param address The address to use for this instance
	 * @param port The port to use for this instance
     */
	public HttpListenerConfig(String address, int port) {
		this.address = address;
		this.port = port;
		keepAliveTimeout = 10000;
		keepAliveMaxRequests = -1;
	}

	/**
	 * Returns the address that will be used to construct the listener
	 * @return The address to use for the listener
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets the address that will be used to construct the listener
	 * @param address The address to use for the listener
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Returns the configured address as an InetAddress object
	 * @return The address to use for the listener
	 * @throws java.net.UnknownHostException If the configured address
	 * cannot be resolved
	 */
	public InetAddress getInetAddress() throws UnknownHostException {
		return InetAddress.getByName(address);
	}

	/**
	 * Returns the port that will be used to construct the listener
	 * @return The port to use for the listener
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port that will be used to construct the listener
	 * @param port The port to use for the listener
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the default servlet that will be used to construct the listener
	 * @return The default servlet to use for the listener
	 */
	public Servlet getDefaultServlet() {
		return defaultServlet;
	}

	/**
	 * Sets the default servlet that will be used to construct the listener
	 * @param servlet The default servlet to use for the listener
	 */
	public void setDefaultServlet(Servlet servlet) {
		this.defaultServlet = servlet;
	}

	/**
	 * Returns the Keep-Alive time-out that will be used to construct the listener
	 * @return The Keep-Alive time-out to use for the listener
	 */
	public int getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	/**
	 * Sets the Keep-Alive time-out that will be used to construct the listener
	 * @param keepAliveTimeout The Keep-Alive time-out to use for the listener
	 */
	public void setKeepAliveTimeout(int keepAliveTimeout) {
		this.keepAliveTimeout = keepAliveTimeout;
	}

	/**
	 * Returns the maximum Keep-Alive requests that will be used to construct the listener
	 * @return The maximum number of requests to use for the listener
	 */
	public int getKeepAliveMaxRequests() {
		return keepAliveMaxRequests;
	}

	/**
	 * Sets the maximum Keep-Alive requests that will be used to construct the listener
	 * @param keepAliveMaxRequests The maximum number of requests to use for the listener
	 */
	public void setKeepAliveMaxRequests(int keepAliveMaxRequests) {
		this.keepAliveMaxRequests = keepAliveMaxRequests;
	}
	
	/**
	 * Constructs an HttpListener using the information previously stored
	 * in the object's attributes
	 * @return A properly configured HttpListener
	 * @throws org.codejive.websrv.config.ConfigurationException If the object
	 * could not be created
	 */
	public HttpListener buildListener() throws ConfigurationException {
		if (address == null || address.length() == 0) {
            throw new ConfigurationException("An address must be specified");
		}
		if (defaultServlet == null) {
            throw new ConfigurationException("A default servlet must be specified");
		}
		try {
			HttpListener listener = new HttpListener();
			listener.setAddress(getInetAddress());
			listener.setPort(port);
			listener.setDefaultServlet(defaultServlet);
			listener.setKeepAliveTimeout(keepAliveTimeout);
			listener.setKeepAliveMaxRequests(keepAliveMaxRequests);
			return listener;
		} catch (UnknownHostException ex) {
            throw new ConfigurationException(ex);
		}
	}
}
