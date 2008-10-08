/*
 * ProtocolHandler.java
 *
 * Created on Aug 11, 2007, 4:35:42 PM
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

package org.codejive.websrv.protocol;

import java.net.Socket;

/**
 * Protocol handlers take care of all the semantics of a communication
 * between client and server. For many protocols this means that they
 * parse incoming requests, act on it and generate an appropriate response.
 * Any protocol handlers will need to implement this interface
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface ProtocolHandler extends Runnable {

	/**
	 * Sets the socket that will be used for client-server commmunication
	 * @param socket The socket to use for communication
	 */
	public void setSocket(Socket socket);
}
