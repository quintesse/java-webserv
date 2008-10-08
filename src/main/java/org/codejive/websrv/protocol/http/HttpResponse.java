/*
 * HttpResponse.java
 *
 * Created on Aug 12, 2007, 9:51:34 PM
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public interface HttpResponse {

	public HttpResponseCode getResponseCode();

	public void setResponseCode(HttpResponseCode responseCode);

	public String getCharacterEncoding();

	public void setCharacterEncoding(String characterEncoding);

	public String getContentType();

	public void setContentType(String contentType);

	public String getHeader(String key);

	public void setHeader(String key, String value);

	public int getBufferSize();

	public void setBufferSize(int bufferSize);

	public Set<String> getHeaderNames();

	public OutputStream getOutputStream();

	public PrintWriter getWriter();

	public boolean isCommitted();

	public void removeHeader(String key, String value);

	public void reset();
	
	public void sendError(HttpResponseCode resultCode, String message) throws IOException;
}
