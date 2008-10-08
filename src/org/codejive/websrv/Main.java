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
import org.codejive.websrv.util.SimplePathMatcher;
import org.codejive.websrv.servlet.RequestMatcherServlet;
import org.codejive.websrv.servlet.FileServlet;
import org.codejive.websrv.servlet.PageServlet;
import org.codejive.websrv.mimetype.MimeTypes;
import org.codejive.websrv.servlet.RequestMatch;

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class Main {

	// Entry point.
	public static void main(String[] args) {
		try {
			System.out.println("Starting...");

			ServerConfig serverConfig = new ServerConfig();
			
			MimeTypes mimeTypes = new MimeTypes();
			mimeTypes.getMimeTypes().add(new MimeType("text/html", "html", "htm"));
			mimeTypes.getMimeTypes().add(new MimeType("image/gif", "gif"));
			mimeTypes.getMimeTypes().add(new MimeType("image/jpg", "jpg"));
			mimeTypes.getMimeTypes().add(new MimeType("image/png", "png"));
			serverConfig.setMimeTypes(mimeTypes);
			
			WelcomeFiles welcomeFiles = new WelcomeFiles();
			welcomeFiles.getFileNames().add("index.page");
			welcomeFiles.getFileNames().add("index.html");
			welcomeFiles.getFileNames().add("index.htm");
			serverConfig.setWelcomeFiles(welcomeFiles);
			
			PageServlet pageHandler1, pageHandler2;
			
			{
                HttpListenerConfig listenerConfig = new HttpListenerConfig(8090);

                RequestMatcherServlet handler = new RequestMatcherServlet();
                listenerConfig.setDefaultServlet(handler);

                FileServlet docHandler = new FileServlet("dist/javadoc", mimeTypes, welcomeFiles);
                RequestMatch docMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("/docs(/**)"), docHandler);
                handler.getRequestMatchers().add(docMatcher);

                pageHandler1 = new PageServlet("ROOT_8090", welcomeFiles);
                RequestMatch pageMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**/*.page"), pageHandler1);
                handler.getRequestMatchers().add(pageMatcher);

                FileServlet defaultHandler = new FileServlet("ROOT_8090", mimeTypes, welcomeFiles);
                RequestMatch defaultMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**"), defaultHandler);
                handler.getRequestMatchers().add(defaultMatcher);

                serverConfig.getListeners().add(listenerConfig);
			}
			
			{
                HttpListenerConfig listenerConfig = new HttpListenerConfig(8091);

                RequestMatcherServlet handler = new RequestMatcherServlet();
                listenerConfig.setDefaultServlet(handler);

                FileServlet docHandler = new FileServlet("dist/javadoc", mimeTypes, welcomeFiles);
                RequestMatch docMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("/docs(/**)"), docHandler);
                handler.getRequestMatchers().add(docMatcher);

                pageHandler2 = new PageServlet("ROOT_8091", welcomeFiles);
                RequestMatch pageMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**/*.page"), pageHandler2);
                handler.getRequestMatchers().add(pageMatcher);

                FileServlet defaultHandler = new FileServlet("ROOT_8091", mimeTypes, welcomeFiles);
                RequestMatch defaultMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**"), defaultHandler);
                handler.getRequestMatchers().add(defaultMatcher);

                serverConfig.getListeners().add(listenerConfig);
			}
			
			Server server = serverConfig.buildServer();
			
			// Bit of a hack this
            pageHandler1.getVariables().put("server", server);
            pageHandler2.getVariables().put("server", server);

			server.startAll();
			
			// Would be nice to be able to open a browser here
			showDocument("http://localhost:8090");

			server.waitAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showDocument(String url) {
		boolean dummy = tryBrowser("firefox", url)
                     || tryBrowser("mozilla", url)
                     || tryBrowser("konqueror", url)
                     || tryBrowser("iexplore", url);
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
