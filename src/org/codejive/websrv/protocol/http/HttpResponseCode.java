/*
 * HttpResponseCode.java
 *
 * Created on Aug 13, 2007, 12:37:19 AM
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
public enum HttpResponseCode {
	CODE_CONTINUE(100, "Continue"),
	CODE_SWITCHING_PROTOCOLS(101, "Switching Protocols"),
	CODE_OK(200, "OK"),
	CODE_CREATED(201, "Created"),
	CODE_ACCEPTED(202, "Accepted"),
	CODE_NONAUTHORATIVE_INFORMATION(203, "Non-Authoritative Information"),
	CODE_NO_CONTENT(204, "No Content"),
	CODE_RESET_CONTENT(205, "Reset Content"),
	CODE_PARTIAL_CONTENT(206, "Partial Content"),
	CODE_MULTIPLE_CHOICES(300, "Multiple Choices"),
	CODE_MOVED_PERMANENTLY(301, "Moved Permanently"),
	CODE_FOUND(302, "Found"),
	CODE_SEE_OTHER(303, "See Other"),
	CODE_NOT_MODIFIED(304, "Not Modified"),
	CODE_USE_PROXY(305, "Use Proxy"),
	CODE_TEMPORARY_REDIRECT(307, "Temporary Redirect"),
	CODE_BAD_REQUEST(400, "Bad Request"),
	CODE_UNAUTHORIZED(401, "Unauthorized"),
	CODE_PAYMENT_REQUIRED(402, "Payment Required"),
	CODE_FORBIDDEN(403, "Forbidden"),
	CODE_NOT_FOUND(404, "Not Found"),
	CODE_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	CODE_NOT_ACCEPTABLE(406, "Not Acceptable"),
	CODE_PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
	CODE_REQUEST_TIMEOUT(408, "Request Time-out"),
	CODE_CONFLICT(409, "Conflict"),
	CODE_GONE(410, "Gone"),
	CODE_LENGTH_REQUIRED(411, "Length Required"),
	CODE_PRECONDITION_FAILED(412, "Precondition Failed"),
	CODE_REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
	CODE_REQUEST_URI_TOO_LARGE(414, "Request-URI Too Large"),
	CODE_UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	CODE_REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable"),
	CODE_EXPECTATION_FAILED(417, "Expectation Failed"),
	CODE_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	CODE_NOT_IMPLEMENTED(501, "Not Implemented"),
	CODE_BAD_GATEWAY(502, "Bad Gateway"),
	CODE_SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	CODE_GATEWAY_TIMEOUT(504, "Gateway Time-out"),
	CODE_HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version not supported");

    private final int code;
    private final String message;
	
	private HttpResponseCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	public static HttpResponseCode getByCode(int responseCode) {
		for (HttpResponseCode code : values()) {
			if (code.getCode() == responseCode) {
				return code;
			}
		}
		return null;
	}
	
	public static String getTextForCode(int responseCode) {
		for (HttpResponseCode code : values()) {
			if (code.getCode() == responseCode) {
				return code.getMessage();
			}
		}
		return "Unknown Response Code #" + responseCode;
	}

}
