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
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.codejive.websrv.config.WelcomeFiles;
import org.codejive.websrv.mimetype.MimeType;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpRequestImpl;
import org.codejive.websrv.protocol.http.HttpResponse;
import org.codejive.websrv.protocol.http.HttpResponseCode;

/**
 * This class serves files found in a given directory. The servlet will look for
 * resources requested by the client in that directory and if encountered will
 * look up the corresponding mime-type handler, it will then pas on the actual
 * content generation to that handler. The servlet can also be configured with
 * a list of "welcome files" which will be used when the resource that the client
 * asks for happens to be a directory. In that case the servlet will search the
 * directory to see if it can find any of those "welcome files" and serve the
 * first one it encounters.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class FileServlet implements Servlet {

	private String rootPath;
	private MimeTypes mimeTypes;
    private WelcomeFiles welcomeFiles;
	
	private static final Logger logger = Logger.getLogger(FileServlet.class.getName());
	
	/**
	 * Creates a new FileServlet
	 * @param rootPath The path where the files that this servlet is allowed to
	 * serve can be found
	 * @param mimeTypes The mime-types handled by this servlet
	 * @param welcomeFiles The welcome files to be used
	 */
	public FileServlet(String rootPath, MimeTypes mimeTypes, WelcomeFiles welcomeFiles) {
		this.rootPath = rootPath;
		this.mimeTypes = mimeTypes;
		this.welcomeFiles = welcomeFiles;
	}

	/**
	 * Returns the path where the files that this servlet is allowed to serve
	 * can be found
	 * @return path to the files that can be served
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * Sets the path where the files that this servlet is allowed to serve
	 * can be found
	 * @param rootPath path to the files that can be served
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * Returns the mime-types that are handled by this servlet
	 * @return the mime-types that are handled by this servlet
	 */
	public MimeTypes getMimeTypes() {
		return mimeTypes;
	}

	/**
	 * Returns the welcome files that the servlet will consider if the request
	 * path points to a folder
	 * @return the welcome files that the servlet will consider for requests
	 */
	public WelcomeFiles getWelcomeFiles() {
		return welcomeFiles;
	}
	
	public void process(String requestPath, HttpRequest request, HttpResponse response) throws IOException {
        File file = new File(rootPath, requestPath);
		URL url = null;
		
		// If the path exists but points to a directory we check
		// if any of the configured welcome files exists
		// TODO: move this code "higher up", it's too low level here
		if (file.exists() && file.isDirectory()) {
			if (!requestPath.endsWith("/")) {
				redirect(request, response);
			}
			for (String welcomeName : welcomeFiles.getFileNames()) {
    			File welcomeFile = new File(file, welcomeName);
				if (welcomeFile.exists() && welcomeFile.isFile()) {
					file = welcomeFile;
					break;
				}
			}
		}
		
		// If the path does not point to a file we try the class-path resources
        if (!file.exists() || !file.isFile() || file.isHidden()) {
    		logger.info("Trying resource /" + file.getPath().replace('\\', '/'));
			url = this.getClass().getResource("/" + file.getPath().replace('\\', '/'));
			if ((url == null) || (url.openConnection().getContentLength() == 0)) {
				if ((url != null) && !file.getPath().replace('\\', '/').endsWith("/")) {
					redirect(request, response);
				}
				for (String welcomeName : welcomeFiles.getFileNames()) {
					String path = "/" + file.getPath().replace('\\', '/');
					if (!path.endsWith("/")) {
						path += "/";
					}
					path += welcomeName;
		    		logger.info("Trying resource " + path);
					url = this.getClass().getResource(path);
					if ((url != null) && (url.openConnection().getContentLength() > 0)) {
						break;
					}
				}
				// If we didn't find a resource either we send a NOT FOUND error
				if ((url == null) || (url.openConnection().getContentLength() == 0)) {
					logger.info("Resource does not exist");
					response.sendError(HttpResponseCode.CODE_NOT_FOUND, request.getPath());
				}
			}
			file = null;
			logger.info("Serving resource " + url);
        } else {
			url = file.toURI().toURL();
			logger.info("Serving file " + file.getAbsolutePath());
		}
		
		// Try to obtain the content mime type depending on the file's name
        MimeType contentType = mimeTypes.findByPath(url.getPath());
        if (contentType == null) {
            contentType = mimeTypes.findByName("text/plain");
        }
		
		// Set the content mime type (which might be overridden by the handler below)
        response.setContentType(contentType.getMimeType());

		if (file != null) {
			contentType.getHandler().process(file, request, response);
		} else {
			contentType.getHandler().process(url, request, response);
		}
	}

	private void redirect(HttpRequest request, HttpResponse response) throws IOException {
		HttpRequestImpl tmp = new HttpRequestImpl(request);
		tmp.setPath(tmp.getPath() + "/");
		response.sendRedirect(tmp.getUrl());
	}

}
