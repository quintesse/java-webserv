/*
 * HttpProtocolHandler.java
 *
 * Created on Aug 11, 2007, 6:07:03 PM
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codejive.websrv.protocol.*;

/**
 * This is a handler that implements the HTTP protocol. While it handles all
 * communication and parsing of client requests it leaves the actual generation
 * of responses to a ResponseHandler
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpProtocolHandler implements ProtocolHandler {

	/**
	 * The reponse handler used to generate output
	 */
	private ResponseHandler responseHandler;
	
	/**
	 * The socket used for client-server communication
	 */
	private Socket socket;

	/**
	 * The time-out in milliseconds after which an idle connection
	 * will be closed. 0 means never (default = 10 seconds)
	 */
	private int keepAliveTimeout;
	
	/**
	 * The maximum number of requests that will be served before
	 * closing the connection. -1 means unlimited (default = -1)
	 */
	private int keepAliveMaxRequests;
	
	private static final String CRLF = "\r\n";
	
	/**
	 * Class private logger
	 */
	private static final Logger logger = Logger.getLogger(HttpProtocolHandler.class.getName());

	/**
	 * Creates a new protocol handler using the given response handler
	 * @param responseHandler THe response handler to use for generating output
	 */
	public HttpProtocolHandler(ResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
		keepAliveTimeout = 10000;
		keepAliveMaxRequests = -1;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Returns the time-out in milliseconds after which an idle connection
	 * will be closed. 0 means never
	 * @return The time-out in milliseconds
	 */
	public int getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	/**
	 * Sets the time-out in milliseconds after which an idle connection
	 * will be closed. 0 means never
	 * @param keepAliveTimeout The time-out in milliseconds
	 */
	public void setKeepAliveTimeout(int keepAliveTimeout) {
		this.keepAliveTimeout = keepAliveTimeout;
	}

	/**
	 * Returns the maximum number of requests that will be served before
	 * closing the connection. -1 means unlimited
	 * @return The maximum number of requests to serve
	 */
	public int getKeepAliveMaxRequests() {
		return keepAliveMaxRequests;
	}

	/**
	 * Sets the maximum number of requests that will be served before
	 * closing the connection. -1 means unlimited
	 * @param keepAliveMaxRequests The maximum number of requests to serve
	 */
	public void setKeepAliveMaxRequests(int keepAliveMaxRequests) {
		this.keepAliveMaxRequests = keepAliveMaxRequests;
	}

	/**
	 * In this method the actual steps of parsing a request ang generating
	 * a response are performed. Depending on Keep-Alive settings this
	 * method can handle one or many requests
	 */
	public void run() {
		String socketInfo = socket.toString();
		logger.info("Starting HTTP protocol handler for " + socketInfo);

		try {
			int requestCount = 0;
    		boolean keepAlive = false;
            int maxRequests = keepAliveMaxRequests;
			do {
                HttpResponseImpl response = new HttpResponseImpl(socket.getOutputStream());
                try {
					socket.setSoTimeout(keepAliveTimeout);

					LineNumberReader in = new LineNumberReader(new InputStreamReader(socket.getInputStream()));

					HttpRequestImpl request = parseRequest(in);
					
					boolean useHttp11 = "HTTP/1.1".equalsIgnoreCase(request.getRequestProtocol());
					if (useHttp11 && "100-continue".equalsIgnoreCase(request.getHeader("Expect"))) {
						// Generate the CONTINUE response before going on to generate
						// the actual response
						generateContinue(request, new HttpResponseImpl(socket.getOutputStream()));
					}
					
                    parseRequestHeaders(in, request);
					
					keepAlive = useHttp11
							|| "Keep-Alive".equalsIgnoreCase(request.getHeader("Connection"))
							|| (maxRequests > 0);
					if (keepAlive) {
                        if ((maxRequests >= 0) && (requestCount >= maxRequests)) {
                            logger.info("Connection keep-alive maximum requests reached");
                            keepAlive = false;
                        }
					}
					if (keepAlive) {
						response.setHeader("Connection", "Keep-Alive");
					} else {
						response.setHeader("Connection", "close");
					}
					
					// For HTTP/1.1 make sure the Host header is present (as required by the spec)
					if (useHttp11 && request.getHeader("Host") == null) {
						generateErrorResponse(HttpResponseCode.CODE_BAD_REQUEST, "No Host: header received", response);
					}
					
                    generateResponse(request, response);
					
					requestCount++;
                } catch (EOFException ex) {
                    logger.info("End of input was reached");
                    keepAlive = false;
                } catch (SocketTimeoutException ex) {
                    logger.info("Connection keep-alive timeout reached");
                    keepAlive = false;
                } catch (PrematureEOFException ex) {
                    logger.info("Processing of output was forcibly interrupted");
					requestCount++;
                } catch (MalformedRequestException ex) {
                    generateErrorResponse(HttpResponseCode.CODE_BAD_REQUEST, ex.getMessage(), response);
                    keepAlive = false;
                } catch (Exception ex) {
                    generateErrorResponse(HttpResponseCode.CODE_INTERNAL_SERVER_ERROR, ex, response);
                    logger.log(Level.SEVERE, "500 Internal Server Error", ex);
                    keepAlive = false;
                } finally {
					if (response.isCommitted()) {
						try {
							response.getOutputStream().close();
						} catch (IOException ex) {
		                    logger.log(Level.WARNING, "Could not properly close response output stream", ex);
						}
					}
				}

                // Maybe the response handler set the Connection to "close"?
                keepAlive = keepAlive && response.getHeader("Connection").equalsIgnoreCase("Keep-Alive");
			} while (keepAlive && !socket.isClosed());
            logger.info("Handled " + requestCount + " request(s) during this connection");
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		} finally {
			try {
				socket.close();
			} catch (IOException ex) {
				// Ignore
			}
		}

		logger.info("Exiting HTTP protocol handler for " + socketInfo);
	}

	/**
	 * Parses the incoming client request constructing an HttpRequest
	 * object with the information retrieved from the incoming data stream
	 * @param inStream The incoming data stream containing the client request
	 * @return A newly created HttpRequest object
	 * @throws java.io.IOException Will be trhown if the incoming data could
	 * not be read or parsed correctly
	 */
	private HttpRequestImpl parseRequest(LineNumberReader in) throws IOException {
		HttpRequestImpl request = new HttpRequestImpl();

		// Read a non-empty line
		// Skipping empty lines is not according to the spec
		// but it helps compatibilty with certain browsers
		String requestText;
		do {
			requestText = in.readLine();
		} while ((requestText != null) && (requestText.length() == 0));

		if (requestText == null) {
			throw new EOFException("End of input reached");
		}
		logger.info("REQUEST: " + requestText);

		String[] reqParts = requestText.split(" ");
		if (reqParts.length != 3) {
			throw new MalformedRequestException("Malformed request: " + requestText);
		}

		request.setRequestMethod(reqParts[0]);
		request.setRequestProtocol(reqParts[2]);
		try {
			request.parseUrl(reqParts[1]);
		} catch (URISyntaxException ex) {
			throw new MalformedRequestException("Malformed request URI: " + reqParts[1]);
		}

		return request;
	}

	/**
	 * Parses the incoming request headers storing them in the given request object
	 * @param inStream The incoming data stream containing the client request
	 * @param request The request object to store the headers in
	 * @return A newly created HttpRequest object
	 * @throws java.io.IOException Will be trhown if the incoming data could
	 * not be read or parsed correctly
	 */
	private HttpRequest parseRequestHeaders(LineNumberReader in, HttpRequestImpl request) throws IOException {
		String requestText;
		while (((requestText = in.readLine()) != null) && (requestText.length() > 0)) {
			String[] paramParts = requestText.split(":", 2);
			if (paramParts.length != 2) {
				throw new MalformedRequestException("Malformed request header: " + requestText);
			}
			request.setHeader(paramParts[0].trim(), paramParts[1].trim());
		}

		return request;
	}

	/**
	 * Generates a response for the client
	 * @param request A request object
	 * @param response A response object
	 * @throws java.io.IOException Will be thrown when the response could not be generated
	 */
	private void generateResponse(HttpRequest request, HttpResponse response) throws IOException {
		response.setResponseCode(HttpResponseCode.CODE_OK);
        responseHandler.handleResponse(request, response);
	}

	/**
	 * Generates a "100 CONTINUE" response for the client
	 * @param request A request object
	 * @param response A response object
	 * @throws java.io.IOException Will be thrown when the response could not be generated
	 */
	private void generateContinue(HttpRequest request, HttpResponse response) throws IOException {
		response.setResponseCode(HttpResponseCode.CODE_CONTINUE);
    	response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.print(CRLF);
		out.flush();
	}

	/**
	 * Generates a response for the client indicating that an error has occurred
	 * @param responseCode The actual HTTP response code to use
	 * @param message The message describing the error
	 * @param response A response object
	 * @throws java.io.IOException Will be thrown when the response could not be generated
	 */
	private void generateErrorResponse(HttpResponseCode responseCode, String message, HttpResponse response) throws IOException {
		response.sendError(responseCode, message);
	}

	/**
	 * Generates a response for the client indicating that an exception has been thrown
	 * @param responseCode The actual HTTP response code to use
	 * @param ex The exception that was thrown
	 * @param response A response object
	 * @throws java.io.IOException Will be thrown when the response could not be generated
	 */
	private void generateErrorResponse(HttpResponseCode responseCode, Exception ex, HttpResponse response) throws IOException {
		generateErrorResponse(responseCode, ex.toString(), response);
	}
}
