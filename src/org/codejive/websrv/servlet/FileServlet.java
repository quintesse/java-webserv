/*
 * FileServlet.java
 *
 * Created on Aug 13, 2007, 12:18:53 AM
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

package org.codejive.websrv.servlet;

import org.codejive.websrv.mimetype.MimeTypes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;
import org.codejive.websrv.config.WelcomeFiles;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;
import org.codejive.websrv.protocol.http.HttpResponseCode;

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class FileServlet implements Servlet {

	private String rootPath;
	private MimeTypes mimeTypes;
    private WelcomeFiles welcomeFiles;
	
	private static final Logger logger = Logger.getLogger(FileServlet.class.getName());
	
	public FileServlet() {
	}

	public FileServlet(String rootPath, MimeTypes mimeTypes, WelcomeFiles welcomeFiles) {
		this.rootPath = rootPath;
		this.mimeTypes = mimeTypes;
		this.welcomeFiles = welcomeFiles;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public MimeTypes getMimeTypes() {
		return mimeTypes;
	}

	public WelcomeFiles getWelcomeFiles() {
		return welcomeFiles;
	}
	
	public void process(String requestPath, HttpRequest request, HttpResponse response) throws IOException {
        File file = new File(rootPath, requestPath);
		
		URL xxx = this.getClass().getResource("/" + file.getPath());
		logger.info("XXXXXXXXXXXXXXXXXXX " + xxx);
		
		logger.info("Serving file " + file.getAbsolutePath());
		
		// If the path exists but points to a directory we check
		// if any of the configured welcome files exists
		// TODO: move this code "higher up", it's too low level here
		if (file.exists() && file.isDirectory()) {
			for (String welcomeName : welcomeFiles.getFileNames()) {
    			File welcomeFile = new File(file, welcomeName);
				if (welcomeFile.exists() && welcomeFile.isFile()) {
					file = welcomeFile;
					break;
				}
			}
		}
		
		// If the path does not point to a file we send a NOT FOUND error
        if (!file.exists() || !file.isFile() || file.isHidden()) {
    		logger.info("File does not exist");
            response.sendError(HttpResponseCode.CODE_NOT_FOUND, request.getPath());
        }
		
		// Try to set the content mime type depending on the file's name
        String contentType = mimeTypes.findByPath(file.getName());
        if (contentType == null) {
            contentType = "text/plain";
        }
        response.setContentType(contentType);

		// Set the content length of the file we're about to send
        long fileSize = file.length();
        logger.info("Content length " + fileSize + " bytes");
        response.setHeader("Content-Length", String.valueOf(fileSize));

		// Write to file's content to the output
        byte[] buf = new byte[response.getBufferSize()];
        FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();
        try {
            int len;
            while ((len = in.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } finally {
            in.close();
        }
	}

}
