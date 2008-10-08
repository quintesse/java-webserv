/*
 * PrematureEOFException.java
 *
 * Created on Aug 13, 2007, 11:15:45 PM
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
 * This exception, although in this case exception is not really the
 * proper word, will be thrown when the generation of the server response
 * has to be aborted prematurely. The reason that this is not really an
 * exception is that it will always be silently ignored as if nothing
 * happened. Is is used as a way to easily "exit" from deep-lying pieces
 * of code directly to outermost server component that handles response
 * generation. This component will treat this exception as a sign that the
 * output has been generated correctly.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class PrematureEOFException extends IOException {

	/**
	 * Creates a new instance
	 */
	public PrematureEOFException() {
	}

	/**
	 * Creates a new instance using the given message
	 * @param message The exception message
	 */
	public PrematureEOFException(String message) {
		super(message);
	}
}
