/*
 * Main.java
 *
 * Created on Aug 11, 2007, 6:51:55 PM
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

package org.codejive.websrv;

import java.io.IOException;
import org.codejive.websrv.mimetype.MimeType;
import org.codejive.websrv.config.HttpListenerConfig;
import org.codejive.websrv.config.ServerConfig;
import org.codejive.websrv.config.WelcomeFiles;
import org.codejive.websrv.mimetype.DefaultMimeTypeHandler;
import org.codejive.websrv.mimetype.MimeTypeHandler;
import org.codejive.websrv.util.SimplePathMatcher;
import org.codejive.websrv.servlet.RequestMatcherServlet;
import org.codejive.websrv.servlet.FileServlet;
import org.codejive.websrv.mimetype.MimeTypes;
import org.codejive.websrv.mimetype.PageMimeTypeHandler;
import org.codejive.websrv.servlet.RequestMatch;

/**
 * This main class is only used for demonstration purposes.
 * It will start two HTTP listeners on ports 8090 and 8091
 * and will try to open a browser pointing to the 8090 site.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class Main {

	/**
	 * Main entry point for the application
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Starting...");

			ServerConfig serverConfig = new ServerConfig();
			
			MimeTypeHandler defaultHandler = new DefaultMimeTypeHandler();
			PageMimeTypeHandler pageHandler = new PageMimeTypeHandler();
			
			MimeTypes mimeTypes = new MimeTypes();
			mimeTypes.getMimeTypes().add(new MimeType("text/plain", defaultHandler, "txt"));
			mimeTypes.getMimeTypes().add(new MimeType("text/html", defaultHandler, "html", "htm"));
			mimeTypes.getMimeTypes().add(new MimeType("text/xml", defaultHandler, "xml"));
			mimeTypes.getMimeTypes().add(new MimeType("image/gif", defaultHandler, "gif"));
			mimeTypes.getMimeTypes().add(new MimeType("image/jpg", defaultHandler, "jpg"));
			mimeTypes.getMimeTypes().add(new MimeType("image/png", defaultHandler, "png"));
			mimeTypes.getMimeTypes().add(new MimeType("x-application/x-websrv-page", pageHandler, "page"));
			serverConfig.setMimeTypes(mimeTypes);
			
			WelcomeFiles welcomeFiles = new WelcomeFiles();
			welcomeFiles.getFileNames().add("index.page");
			welcomeFiles.getFileNames().add("index.html");
			welcomeFiles.getFileNames().add("index.htm");
			serverConfig.setWelcomeFiles(welcomeFiles);
			
			{
                HttpListenerConfig listenerConfig = new HttpListenerConfig(8090);

                RequestMatcherServlet mainServlet = new RequestMatcherServlet();
                listenerConfig.setDefaultServlet(mainServlet);

                FileServlet docServlet = new FileServlet("dist/javadoc", mimeTypes, welcomeFiles);
                RequestMatch docMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("/javadoc(/**)?"), docServlet);
                mainServlet.getRequestMatchers().add(docMatcher);

                FileServlet defaultServlet = new FileServlet("ROOT_8090", mimeTypes, welcomeFiles);
                RequestMatch defaultMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**"), defaultServlet);
                mainServlet.getRequestMatchers().add(defaultMatcher);

                serverConfig.getListeners().add(listenerConfig);
			}
			
			{
                HttpListenerConfig listenerConfig = new HttpListenerConfig(8091);

                RequestMatcherServlet mainServlet = new RequestMatcherServlet();
                listenerConfig.setDefaultServlet(mainServlet);

                FileServlet defaultServlet = new FileServlet("ROOT_8091", mimeTypes, welcomeFiles);
                RequestMatch defaultMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**"), defaultServlet);
                mainServlet.getRequestMatchers().add(defaultMatcher);

                serverConfig.getListeners().add(listenerConfig);
			}
			
			Server server = serverConfig.buildServer();
			
			// Bit of a hack this
            pageHandler.getVariables().put("server", server);

			server.startAll();
			
			// Would be nice to be able to open a browser here
			showDocument("http://localhost:8090/index.html");

			server.waitAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showDocument(String url) {
		boolean dummy = tryBrowser("firefox", url)
                     || tryBrowser("mozilla", url)
                     || tryBrowser("konqueror", url)
                     || tryBrowser("explorer", url);
	}
	
	private static boolean tryBrowser(String cmd, String url) {
        try {
            Runtime.getRuntime().exec(cmd + " " + url);
            return true;
        } catch (IOException ex) {
            return false;
        }
	}
}
