/*
 * ConfigurationException.java
 *
 * Created on Aug 14, 2007, 1:45:10 AM
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

/**
 * The exception that will be thrown when errors occur during
 * the reading, writing or application of configuration information
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class ConfigurationException extends Exception {

	/**
	 * Constructs a new instance
	 */
	public ConfigurationException() {
	}

	/**
	 * Constructs a new instance with the given message
	 * @param message The message to use for this exception
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance with the given message and cause
	 * @param message The message to use for this exception
	 * @param cause The underlying cause for this exception
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new instance with the given cause
	 * @param cause The underlying cause for this exception
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

}
