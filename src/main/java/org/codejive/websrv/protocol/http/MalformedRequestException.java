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
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
class MalformedRequestException extends IOException {

	public MalformedRequestException() {
	}

	public MalformedRequestException(String message) {
		super(message);
	}

	public MalformedRequestException(Throwable cause) {
		super(cause);
	}

	public MalformedRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
