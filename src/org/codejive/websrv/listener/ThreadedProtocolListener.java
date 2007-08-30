/*
 * ThreadedProtocolListener.java
 *
 * Created on Aug 11, 2007, 6:55:43 PM
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

import org.codejive.websrv.protocol.ProtocolHandler;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is an implementation of a Listener which starts a thread
 * for each incoming connection and hands the communication socket of
 * to a protocol handler. The class itself is abstract and a sub class
 * will need to implement <code>getProtocolHandler()</code> which will
 * be used to obtain the protocol handler for each incoming conenction
 * @see org.codejive.websrv.protocol.ProtocolHandler
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public abstract class ThreadedProtocolListener extends Listener {

	/**
	 * The thread pool that will hold the threads for each of the
	 * protocol handlers
	 */
	private ExecutorService executor;
	
    /**
     * The number of connected clients
     */
    private int connectedCount;
	
    /**
     * The total number of handled connections
     */
    private int connectionCount;
	
	/**
	 * Creates a listener on the default address using any free port
	 */
	public ThreadedProtocolListener() {
		this(null, 0);
	}

	/**
	 * Creates a listener on the default address using the specified port
	 * @param port The port to listen on
	 */
	public ThreadedProtocolListener(int port) {
		this(null, port);
	}

	/**
	 * Creates a listener on the specified address and port
	 * @param address The local address to bind to
	 * @param port The port to listen on
	 */
	public ThreadedProtocolListener(InetAddress address, int port) {
		super(address, port);
		executor = Executors.newCachedThreadPool();
		connectedCount = 0;
		connectionCount = 0;
	}

	/**
	 * Returns the executor service that is/will be used to handle
	 * the threads for each of the protocol handlers
	 * @see java.util.concurrent.ExecutorService
	 * @return The executor service
	 */
	public ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * Returns the executor service that will be used to handle
	 * the threads for each of the protocol handlers.
	 * This can only be changed when the listener is not currently active
	 * (<code>isRunning()</code> returns <code>false</code>)
	 * @param executorPool The executor service to use
	 * @see java.util.concurrent.ExecutorService
	 */
	public void setExecutor(ExecutorService executorPool) {
		assert !isRunning() : "Listener must not be active!";
		this.executor = executorPool;
	}

	/**
     * Returns the number of connected clients (although literally it's
	 * the number of protocol handlers that are still active)
	 * @return The number of connected clients
	 */
	public int getConnectionCount() {
		return connectionCount;
	}

	/**
	 * Returns the total number of connections that have been handled so far
	 * @return The total number of handled connections
	 */
	public int getConnectedCount() {
		return connectedCount;
	}

	protected void onNewConnection(Socket socket) {
		ProtocolHandler handler = new ProtocolHandlerWrapper(getProtocolHandler());
		handler.setSocket(socket);
		executor.execute(handler);
	}

	/**
	 * Signal the listener to deactive. This will close socket the listener
	 * uses to accept connections and will stop the listener thread and will
	 * try to shut down any active protocol handlers
	 */
	@Override
	public void stop() {
		executor.shutdownNow();
		super.stop();
	}

	/**
	 * This abstract method will be called for each connection that gets
	 * accepted by this listener. It must return the protocol handler that
	 * will be used to handle the rest of the communication with the client
     * @see org.codejive.websrv.protocol.ProtocolHandler
	 * @return A protocol handler
	 */
	protected abstract ProtocolHandler getProtocolHandler();
	
	/**
	 * Wrapper for any protocol handlers that makes sure that the listener's
	 * <code>connectedCount</code> attribute is correct and up-to-date
	 */
	private class ProtocolHandlerWrapper implements ProtocolHandler {

		/**
		 * The wrapped protocol handler
		 */
		private ProtocolHandler handler;

		/**
		 * Creates a new wraper for the given handler
		 * @param handler The protocol handler to wrap
		 */
		public ProtocolHandlerWrapper(ProtocolHandler handler) {
			this.handler = handler;
		}
		
		/**
		 * Calls <code>setSocket()</code> on the wrapped handler
		 * @param socket The socket to pass to the wrapepd handler
		 */
		public void setSocket(Socket socket) {
			handler.setSocket(socket);
		}

		/**
		 * Besides calling <code>run()</code> on the wrapped handler
		 * it makes sure to update the listener's <code>connectedCount</code>
		 * and <code>connectionCount</code> attributes
		 */
		public void run() {
			synchronized (executor) {
    			connectionCount++;
    			connectedCount++;
			}
			try {
                handler.run();
			} finally {
                synchronized (executor) {
                    connectedCount--;
                }
			}
		}
		
	}
}
