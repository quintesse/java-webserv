/*
 * ServerConfig.java
 *
 * Created on Aug 14, 2007, 1:29:34 AM
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

import java.util.ArrayList;
import org.codejive.websrv.Server;
import org.codejive.websrv.mimetype.MimeTypes;

/**
 * Builder class for Server objects
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class ServerConfig {

	/**
	 * The mime-type mappings to use for the server
	 */
	private MimeTypes mimeTypes;
	
	/**
	 * The welcome-files to use for the server
	 */
	private WelcomeFiles welcomeFiles;
	
    /**
     * The list of listener builders to use for the server
     */
    private ArrayList<HttpListenerConfig> listeners;

	/**
	 * Constructs a new instance
	 */
	public ServerConfig() {
		listeners = new ArrayList<HttpListenerConfig>();
	}

	/**
	 * Returns the mime-types that will be used to construct the server
	 * @return The mime-types to use for the server
	 */
	public MimeTypes getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * Sets the mime-types that will be used to construct the server
	 * @param mimeTypes The address to use for the listener
	 */
	public void setMimeTypes(MimeTypes mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	/**
	 * Returns the welcome-files that will be used to construct the server
	 * @return The welcome-files to use for the server
	 */
	public WelcomeFiles getWelcomeFiles() {
		return welcomeFiles;
	}

	/**
	 * Sets the welcome-files that will be used to construct the server
	 * @param welcomeFiles The welcome-files to use for the server
	 */
	public void setWelcomeFiles(WelcomeFiles welcomeFiles) {
		this.welcomeFiles = welcomeFiles;
	}

	/**
	 * Returns the list of listener builders that will be used to
	 * construct the listeners for the server
	 * @return A list of listener builders
	 */
	public ArrayList<HttpListenerConfig> getListeners() {
		return listeners;
	}

	/**
	 * Constructs a Server using the information previously stored
	 * in the object's attributes first and then building and adding
	 * a listener for each of the configured listener builders
	 * @return A properly configured Server object
	 * @throws org.codejive.websrv.config.ConfigurationException If the object
	 * could not be created
	 */
	public Server buildServer() throws ConfigurationException {
		Server server = new Server();
		for (HttpListenerConfig listener : listeners) {
            server.addListener(listener.buildListener());
		}
		return server;
	}
}
