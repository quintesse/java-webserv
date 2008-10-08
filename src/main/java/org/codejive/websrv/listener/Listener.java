/*
 * Listener.java
 *
 * Created on Aug 10, 2007, 8:54:26 PM
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

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When activated by using the <code>start()</code> this class listens
 * on a given port for connections and after accepting them it passes
 * the resulting socket on to the <code>onNewConnection()</code> method.
 * Being an abstract method the actual control of the connection must
 * be handled by a subclass that implements the method.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public abstract class Listener implements Runnable {

	/**
	 * The local address that the listener is bound to
	 */
	private InetAddress address;
	
	/**
	 * The port that the listener is bound to
	 */
	private int port;
	
	/**
	 * The socket to use for communications
	 */
	private ServerSocketChannel channel;
	
	/**
	 * The selector used for accepting connections
	 */
	private Selector acceptSelector;
	
	/**
	 * Indicator if the listener has stopped listening or not
	 */
	private volatile boolean stopped;
	
	/**
	 * The private logger for this class
	 */
	private static final Logger logger = Logger.getLogger(Listener.class.getName());

	/**
	 * Creates a listener on the default address using any free port
	 */
	public Listener() {
		this(null, 0);
	}

	/**
	 * Creates a listener on the default address using the specified port
	 * @param port The port to listen on
	 */
	public Listener(int port) {
		this(null, port);
	}

	/**
	 * Creates a listener on the specified address and port
	 * @param address The local address to bind to
	 * @param port The port to listen on
	 */
	public Listener(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.stopped = true;
	}

	/**
	 * The local address that the listener is/will be bound to
	 * @return The local address
	 */
	public InetAddress getAddress() {
		return channel.socket().getInetAddress();
	}

	/**
	 * The local address that the listener will be bound to.
	 * This can only be changed when the listener is not currently active
	 * (<code>isRunning()</code> returns <code>false</code>)
	 * @param address The local address to bind to
	 */
	public void setAddress(InetAddress address) {
		assert stopped : "Listener must not be active!";
		this.address = address;
	}

	/**
	 * The port that the listener is/will be receiving connections on
	 * @return The port number
	 */
	public int getPort() {
		return channel.socket().getLocalPort();
	}

	/**
	 * The port that the listener will be receiving connections on.
	 * This can only be changed when the listener is not currently active
	 * (<code>isRunning()</code> returns <code>false</code>)
	 * @param port The port to bind to
	 */
	public void setPort(int port) {
		assert stopped : "Listener must not be active!";
		this.port = port;
	}

	/**
	 * Returns the current active state for this listener. <code>true</code>
	 * is active/running while <code>false</code> is inactive/stopped.
	 * Only in an inactive state can changes be made to the <code>address</code>
	 * and <code>port</code> attributes.
	 * @return The current active state
	 */
	public boolean isRunning() {
		return !stopped;
	}
	
	/**
	 * Starts listening for connections on the specified address and port.
	 * For each new connection that gets accepted the method
	 * <code>onNewConnection()</code> will be called with the newly created
	 * socket as its parameter
	 * @throws java.lang.Exception Gets throws when the socket could not be
	 * set up properly
	 */
	public void start() throws Exception {
		try {
            // Create a new server socket and set to non blocking mode
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);

            // Bind the server socket to the local host and port
            if (address == null) {
                address = InetAddress.getLocalHost();
            }
            InetSocketAddress isa = new InetSocketAddress(address, port);
            channel.socket().bind(isa);

            // Register accepts on the server socket
            acceptSelector = SelectorProvider.provider().openSelector();
            channel.register(acceptSelector, SelectionKey.OP_ACCEPT);

            // Start listening
            logger.info("Started listening for connections on " + getAddress() + ":" + getPort());
            stopped = false;
            while (!stopped && acceptSelector.select() > 0) {
                Set<SelectionKey> selectedKeys = acceptSelector.selectedKeys();
                for (SelectionKey sk : selectedKeys) {
                    // Get the socket that connects us to the client
                    ServerSocketChannel clientChannel = (ServerSocketChannel) sk.channel();
                    Socket s = clientChannel.accept().socket();
                    // Create and execute the protocol handler that will manage the connection
                    onNewConnection(s);
                }

                selectedKeys.clear();
            }
            logger.info("Stopped listening for connections on " + getAddress() + ":" + getPort());
		} catch (Exception ex) {
			if (stopped) {
    			logger.log(Level.SEVERE, "Unable to start listener", ex);
			} else {
    			logger.log(Level.SEVERE, "Listener process aborted", ex);
			}
			throw ex;
		} finally {
			stopped = true;
			channel.close();
		}
	}

	/**
	 * Signal the listener to deactive. This will close socket the listener
	 * uses to accept connections and will stop the listener thread
	 */
	public void stop() {
		stopped = true;
		acceptSelector.wakeup();
	}

	/**
	 * Signal the listener to shut down releasing all resources. The listener
	 * can NOT be restarted anymore and may not be used afterwards.
	 */
	public void shutdown() {
		stop();
	}

	/**
	 * This utility method exists to make it easy to use the listener
	 * directly as the target of a Thread or Executor service and it simply
	 * calls <code>start()</code>
	 */
	public void run() {
		try {
			start();
		} catch (Exception ex) {
			// Do nothing
		}
	}
	
	/**
	 * This abstract method will be called for each connection that gets
	 * accepted by this listener. It gets passed the socket that can be
	 * used to communicate with the connected client.
	 * @param socket The accepted client connection
	 */
	protected abstract void onNewConnection(Socket socket);
}
