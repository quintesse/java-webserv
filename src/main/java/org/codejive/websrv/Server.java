/*
 * Server.java
 *
 * Created on Aug 14, 2007, 1:09:35 AM
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

package org.codejive.websrv;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.codejive.websrv.listener.Listener;

/**
 * The Server class is a simple container for connection listeners.
 * @see org.codejive.websrv.listener.Listener
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class Server {
	
    /**
     * List of connection listeners
     */
    private ArrayList<Listener> listeners;
	
	/**
	 * A thread pool, holding one thread for each active listener
	 */
	private ExecutorService executorPool;

	/**
	 * The private logger for this class
	 */
	private static final Logger logger = Logger.getLogger(Server.class.getName());
	
	/**
	 * Constructs a new Server instance
	 */
	public Server() {
		listeners = new ArrayList<Listener>();
		executorPool = Executors.newCachedThreadPool();
	}

	/**
	 * Returns the version number of this web server's software
	 * @return Version of this server
	 */
	public String getVersion() {
        return VersionInfo.VERSION;
	}

	/**
	 * Returns the name of the local machine (which often is just
	 * "localhost" or "127.0.0.1")
	 * @return Name of the local machine
	 */
	public String getName() {
        String name;
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			name = "[unknown]";
		}
        return name;
	}
	
	/**
	 * Returns the list of listeners that are configured for this Server
	 * @return List of listeners
	 */
	public ArrayList<Listener> getListeners() {
		return listeners;
	}
	
	/**
	 * Adds the given listener to this Server. This is basically the same as
	 * doing <code>getListeners().add(listener)</code>.
	 * @param listener 
	 */
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes the given listener from this Server. This is basically the same as
	 * doing <code>getListeners().remove(listener)</code>.
	 * @param listener 
	 */
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Activates all the configured listeners
	 */
	public void startAll() {
        for (Listener listener : listeners) {
            executorPool.execute(listener);
        }
	}
	
	/**
	 * Waits until all listeners have stopped
	 */
	public void waitAll() {
		try {
			executorPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			logger.info("Main server thread interrupted");
		}
		logger.info("Server shutting down...");
	}
	
	/**
	 * Deactivates all the configured listeners
	 */
	public void stopAll() {
		for (Listener listener : listeners) {
            listener.stop();
		}
	}
	
	/**
	 * Shuts down the server
	 */
	public void shutdown() {
		for (Listener listener : listeners) {
            listener.shutdown();
		}
		executorPool.shutdownNow();
	}
}
