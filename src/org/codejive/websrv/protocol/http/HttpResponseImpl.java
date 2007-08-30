/*
 * HttpResponseImpl.java
 *
 * Created on Aug 12, 2007, 9:51:46 PM
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpResponseImpl implements HttpResponse {

	private OutputStream outputStream;
	private BufferedOutputStream bufferedOutput;
	private PrintWriter writer;
	private HttpResponseCode responseCode;
	private HashMap<String, String> headers;
	private int bufferSize;
	
	private static final String CRLF = "\r\n";
	
	/**
	 * The name and version of this application
	 */
	private static final String SERVER_NAME = "websrv/0.1";
	
	private static final Logger logger = Logger.getLogger(HttpResponseImpl.class.getName());
	
	public HttpResponseImpl(OutputStream outputStream) {
		this.outputStream = outputStream;
		responseCode = HttpResponseCode.CODE_OK;
		bufferSize = 8192;
		headers = new HashMap<String, String>();
		setHeader("Content-Type", "text/plain; charset=UTF-8");
	}

	public HttpResponseCode getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(HttpResponseCode responseCode) {
		checkCommitted("Can't change response code");
		this.responseCode = responseCode;
		writeResultAndHeaders();
	}

	public String getContentType() {
		ContentType result = getContentTypeAndEncoding();
		return result.contentType;
	}

	public synchronized void setContentType(String contentType) {
		checkCommitted("Can't change content type");
		ContentType result = getContentTypeAndEncoding();
		result.contentType = contentType;
		updateContentType(result);
	}

	public String getCharacterEncoding() {
		ContentType result = getContentTypeAndEncoding();
		return result.characterEncoding;
	}

	public synchronized void setCharacterEncoding(String characterEncoding) {
		checkCommitted("Can't change character encoding");
		ContentType result = getContentTypeAndEncoding();
		result.characterEncoding = characterEncoding;
		updateContentType(result);
	}

	public String getHeader(String key) {
		return headers.get(key.toLowerCase());
	}

	public void setHeader(String key, String value) {
		headers.put(key.toLowerCase(), value);
		writeResultAndHeaders();
	}

	public void removeHeader(String key, String value) {
		headers.remove(key.toLowerCase());
		writeResultAndHeaders();
	}

	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public synchronized void setBufferSize(int bufferSize) {
		if (this.bufferedOutput != null) {
			throw new IllegalStateException("Output buffer already exists");
		}
		this.bufferSize = bufferSize;
	}

	public synchronized OutputStream getOutputStream() {
		initOuput();
		return bufferedOutput;
	}

	public synchronized PrintWriter getWriter() {
		initOuput();
		return writer;
	}

	public synchronized boolean isCommitted() {
		return (bufferedOutput != null) && (bufferedOutput.countBytesWritten() > 0);
	}

	public synchronized void reset() {
		if  (bufferedOutput != null) {
    		bufferedOutput.reset();
		}
	}

	private synchronized void initOuput() {
        if (bufferedOutput == null) {
            bufferedOutput = new BufferedOutputStream(outputStream, bufferSize);
			try {
                writer = new PrintWriter(new OutputStreamWriter(bufferedOutput, getCharacterEncoding()));
			} catch (UnsupportedEncodingException ex) {
                writer = new PrintWriter(new OutputStreamWriter(bufferedOutput));
			}
			writeResultAndHeaders();
		}
	}

	private void checkCommitted(String message) {
		if (isCommitted()) {
			throw new IllegalStateException(message + ": output has already been written");
		}
	}
	
	private class ContentType {
		public String contentType;
		public String characterEncoding;

		public ContentType(String contentType, String characterEncoding) {
			this.contentType = contentType;
			this.characterEncoding = characterEncoding;
		}
	}
	
	private ContentType getContentTypeAndEncoding() {
		String[] result = new String[2];
        result[0] = "";
        result[1] = "";
		
		String typeAndEncodingText = getHeader("Content-Type");
		if (typeAndEncodingText.length() > 0) {
            String[] typeAndEncoding = typeAndEncodingText.split(";", 2);
            result[0] = typeAndEncoding[0].trim();
            if (typeAndEncoding.length > 1) {
                String charset = result[1];
                String[] charsetParts = charset.split("=");
				if (charsetParts.length > 1 && charsetParts[0].trim().equalsIgnoreCase("charset")) {
                    result[1] = charsetParts[1].trim();
				}
            }
		}
		
		return new ContentType(result[0], result[1]);
	}

	private void updateContentType(ContentType contentType) {
		String typeAndEncoding = contentType.contentType;
		if (contentType.characterEncoding.length() > 0) {
			typeAndEncoding += "; charset=" + contentType.characterEncoding;
		}
		setHeader("Content-Type", typeAndEncoding);
	}
	
	private synchronized void writeResultAndHeaders() {
		if (writer != null) {
			bufferedOutput.reset();
			bufferedOutput.setPreventFlush(true);
            writer.print("HTTP/1.1 " + responseCode.getCode() + " " + responseCode.getMessage() + CRLF);
            writer.print("Date: " + dateString(new Date()) + CRLF);
    		writer.print("Server: " + SERVER_NAME);
			for (String name : getHeaderNames()) {
                writer.print(name + ": " + getHeader(name) + CRLF);
			}
            writer.print(CRLF);
			writer.flush();
			bufferedOutput.setPreventFlush(false);
		}
	}
	
	private String dateString(Date date) {
		SimpleDateFormat fmt = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		fmt.applyPattern("EEE, d MMM yyyy HH:mm:ss z");
		return fmt.format(date);
	}

	public void sendError(HttpResponseCode resultCode, String message) throws IOException {
		logger.info("SENDING ERROR #" + resultCode + " : " + message);
		try {
    		reset();
			setResponseCode(resultCode);
    		setContentType("text/plain");
			setCharacterEncoding("UTF-8");
		} catch (IllegalStateException ex) {
			// If we can't empty the buffer we'll just append it
		}
        setHeader("Connection", "close");
		
		message = resultCode.getCode() + " " + resultCode.getMessage() + "\n\n" + message;
		
		int len;
		try {
			len = message.getBytes(getCharacterEncoding()).length;
		} catch (UnsupportedEncodingException ex) {
			len = -1;
		}
		if (len >= 0) {
			setHeader("Content-Length", String.valueOf(len));
		}
		
		PrintWriter out = getWriter();
		out.print(message);
		out.flush();

		// Ugly way to interrupt to normal program flow but it works
		throw new PrematureEOFException();
	}
	
	private class BufferedOutputStream extends FilterOutputStream {

		protected byte[] buf;
		protected int count;
		protected long countWritten;
        protected boolean preventFlush;
		
		public BufferedOutputStream(OutputStream out, int size) {
			super(out);
			if (size <= 0) {
				throw new IllegalArgumentException("Buffer size <= 0");
			}
			buf = new byte[size];
			preventFlush = false;
		}

		private void flushBuffer() throws IOException {
			if (count > 0) {
				out.write(buf, 0, count);
				countWritten += count;
				count = 0;
			}
		}

		@Override
		public synchronized void write(int b) throws IOException {
			if (count >= buf.length) {
				flushBuffer();
			}
			buf[count++] = (byte) b;
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			if (len >= buf.length) {
				/* If the request length exceeds the size of the output buffer,
				flush the output buffer and then write the data directly.
				In this way buffered streams will cascade harmlessly. */
				flushBuffer();
				out.write(b, off, len);
				countWritten += len;
				return;
			}
			if (len > buf.length - count) {
				flushBuffer();
			}
			System.arraycopy(b, off, buf, count, len);
			count += len;
		}
		
		public boolean getPreventFlush() {
			return preventFlush;
		}

		public synchronized void setPreventFlush(boolean prevent) {
			preventFlush = prevent;
		}

		@Override
		public synchronized void flush() throws IOException {
			if (!preventFlush) {
                flushBuffer();
                out.flush();
			}
		}

		public synchronized long countBytesWritten() {
			return countWritten;
		}
		
		public synchronized void reset() {
			if (countWritten > 0) {
				throw new IllegalStateException("Output has already been written");
			}
            count = 0;
		}
	}
}
