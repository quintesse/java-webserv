/*
 * DefaultMimeTypeHandler.java
 * 
 * Created on 23-oct-2007, 12:49:41
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

package org.codejive.websrv.mimetype;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;

/**
 * This handler just passes the resource data directly to the browser which
 * makes it especially useful for binary data types like images, although text
 * documents can be handled as well if no transcoding is needed. If the source
 * allows it the handler will set the content-length HTTP header.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class DefaultMimeTypeHandler implements MimeTypeHandler {

	private static final Logger logger = Logger.getLogger(DefaultMimeTypeHandler.class.getName());

	public void process(File resourceFile, HttpRequest request, HttpResponse response) throws IOException {
		// Set the content length of the file we're about to send
        long fileSize = resourceFile.length();
        logger.info("Content length " + fileSize + " bytes");
        response.setHeader("Content-Length", String.valueOf(fileSize));
		
		process(new FileInputStream(resourceFile), request, response);
	}
	
    public void process(URL resourceUrl, HttpRequest request, HttpResponse response) throws IOException {
		// Set the content length of the resource we're about to send
        int resourceSize = resourceUrl.openConnection().getContentLength();
		if (resourceSize >= 0) {
			logger.info("Content length " + resourceSize + " bytes");
			response.setHeader("Content-Length", String.valueOf(resourceSize));
		} else {
			logger.info("Content length could not be determined");
		}
		
		process(resourceUrl.openStream(), request, response);
    }

    public void process(InputStream resource, HttpRequest request, HttpResponse response) throws IOException {
		// Write to stream's content to the output
        byte[] buf = new byte[response.getBufferSize()];
        OutputStream out = response.getOutputStream();
        try {
            int len;
            while ((len = resource.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } finally {
            resource.close();
        }
    }

}
