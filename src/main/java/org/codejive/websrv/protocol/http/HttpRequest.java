/*
 * HttpRequest.java
 *
 * Created on Aug 12, 2007, 9:24:28 PM
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

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface HttpRequest {

	public java.lang.String getRequestMethod();

	public java.lang.String getRequestProtocol();

	public java.lang.String getUrl();

	public java.lang.String getScheme();

	public java.lang.String getUserInfo();

	public java.lang.String getHost();

	public int getPort();

	public java.lang.String getPath();

	public java.lang.String getQuery();

	public java.lang.String getHeader(java.lang.String key);

	public java.util.Set<java.lang.String> getHeaderNames();

	public java.lang.String getParameter(java.lang.String key);

	public java.util.Set<java.lang.String> getParameterNames();

}
