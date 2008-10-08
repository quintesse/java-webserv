/*
 * MalformedRequestException.java
 *
 * Created on Aug 11, 2007, 7:53:56 PM
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
 * This exception will be thrown when an incoming client request can
 * not be parsed and converted into a HttpRequest object.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class MalformedRequestException extends IOException {

	/**
	 * Creates a new instance
	 */
	public MalformedRequestException() {
	}

	/**
	 * Creates a new instance using the given message
	 * @param message The exception message
	 */
	public MalformedRequestException(String message) {
		super(message);
	}
}
