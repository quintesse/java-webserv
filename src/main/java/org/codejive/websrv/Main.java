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
			System.out.println("websrv by Tako Schotanus (version " + VersionInfo.VERSION + ")");

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
			
			String browserUrl = null;
			if (args.length == 0) {
				System.out.println("Starting demonstration, use 'java -jar websrv.jar help' for other options");
				setupDemo(serverConfig);
				browserUrl = "http://localhost:8090/index.html";
			} else if (args.length >= 1 && "demo".equalsIgnoreCase(args[0])) {
				System.out.println("Starting demonstration, use 'java -jar websrv.jar help' for other options");
				setupDemo(serverConfig);
				if (args.length == 2 && "browser".equalsIgnoreCase(args[1])) {
					browserUrl = "http://localhost:8090/index.html";
				}
			} else if (args.length >= 3 && "server".equalsIgnoreCase(args[0])) {
				int port = Integer.parseInt(args[1]);
				System.out.println("Starting...");
				setupServer(serverConfig, port, args[2]);
				if (args.length >= 4 && "browser".equalsIgnoreCase(args[3])) {
					browserUrl = "http://localhost:" + port;
					if (args.length == 5) {
						browserUrl += "/" + args[4];
					}
				}
			} else {
				showHelp();
				System.exit(0);
			}
			
			Server server = serverConfig.buildServer();
			
			// Bit of a hack this
            pageHandler.getVariables().put("server", server);

			server.startAll();
			
			if (browserUrl != null) {
				showDocument(browserUrl);
			}

			server.waitAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setupDemo(ServerConfig serverConfig) {
		MimeTypes mimeTypes = serverConfig.getMimeTypes();
		WelcomeFiles welcomeFiles = serverConfig.getWelcomeFiles();
		
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
	}

	private static void setupServer(ServerConfig serverConfig, int port, String path) {
		MimeTypes mimeTypes = serverConfig.getMimeTypes();
		WelcomeFiles welcomeFiles = serverConfig.getWelcomeFiles();
		
		HttpListenerConfig listenerConfig = new HttpListenerConfig(port);

		RequestMatcherServlet mainServlet = new RequestMatcherServlet();
		listenerConfig.setDefaultServlet(mainServlet);

		FileServlet defaultServlet = new FileServlet(path, mimeTypes, welcomeFiles);
		RequestMatch defaultMatcher = new RequestMatch("get,post", "*", new SimplePathMatcher("**"), defaultServlet);
		mainServlet.getRequestMatchers().add(defaultMatcher);

		serverConfig.getListeners().add(listenerConfig);
	}

	private static void showHelp() {
		System.out.println("Usage: java -jar websrv.jar [<action> [<args>...]]");
		System.out.println();
		System.out.println("Where <action> is one of the following:");
		System.out.println("   demo [browser]");
		System.out.println("      Starts a webserver listening on ports 8090 and 8091 for demonstration");
		System.out.println("      purposes. If the browser keyword is present a browser will be opened");
		System.out.println("      showing http://localhost:<port-number>");
		System.out.println();
		System.out.println("   server <port> <web-root-path> [browser [<url>]]");
		System.out.println("      Starts a webserver listening on the given port and serving the files");
		System.out.println("      out of the folder indicated by the given path. If the browser keyword");
		System.out.println("      is present a browser will be opened showing http://localhost:<port>");
		System.out.println("      or http://localhost:<port>/<url> if the <url> argument exists");
		System.out.println();
		System.out.println("   help");
		System.out.println("      The information you are currently reading");
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
