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
import org.codejive.websrv.VersionInfo;

/**
 * This is the default implementation of HttpResponse
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class HttpResponseImpl implements HttpResponse {

	/**
	 * The original stream that this class will use to write data to the client
	 */
	private OutputStream outputStream;
	/**
	 * The stream wrapper that users of this object can use to write data to
	 */
	private BufferedOutputStream bufferedOutput;
	/**
	 * The print writer (wrapper) that users of this object can use to write data to
	 */
	private PrintWriter writer;
	/**
	 * The response code that will be sent to the client
	 */
	private HttpResponseCode responseCode;
	/**
	 * A map of all the available response headers
	 */
	private HashMap<String, String> headers;
	/**
	 * The size in bytes of the internal buffer
	 */
	private int bufferSize;
	
	/**
	 * Carriage return & line feed
	 */
	private static final String CRLF = "\r\n";
	
	/**
	 * The name and version of this application
	 */
	private static final String SERVER_NAME = "websrv/" + VersionInfo.VERSION;
	// TODO: ^^^ this is not something that should be here ^^^
	
	/**
	 * Class private logger
	 */
	private static final Logger logger = Logger.getLogger(HttpResponseImpl.class.getName());
	
	/**
	 * Creates a new instance using the given output stream to write
	 * data to the client
	 * @param outputStream The output stream for writing data to the client
	 */
	public HttpResponseImpl(OutputStream outputStream) {
		this.outputStream = outputStream;
		responseCode = HttpResponseCode.CODE_OK;
		bufferSize = 8192;
		headers = new HashMap<String, String>();
		setHeader("Content-Type", "text/plain; charset=UTF-8");
	}

	@Override
	public HttpResponseCode getResponseCode() {
		return responseCode;
	}

	@Override
	public void setResponseCode(HttpResponseCode responseCode) {
		checkCommitted("Can't change response code");
		this.responseCode = responseCode;
	}

	@Override
	public String getContentType() {
		ContentType result = getContentTypeAndEncoding();
		return result.contentType;
	}

	@Override
	public synchronized void setContentType(String contentType) {
		checkCommitted("Can't change content type");
		ContentType result = getContentTypeAndEncoding();
		result.contentType = contentType;
		updateContentType(result);
	}

	@Override
	public String getCharacterEncoding() {
		ContentType result = getContentTypeAndEncoding();
		return result.characterEncoding;
	}

	@Override
	public synchronized void setCharacterEncoding(String characterEncoding) {
		checkCommitted("Can't change character encoding");
		ContentType result = getContentTypeAndEncoding();
		result.characterEncoding = characterEncoding;
		updateContentType(result);
	}

	@Override
	public String getHeader(String key) {
		return headers.get(key.toLowerCase());
	}

	@Override
	public void setHeader(String key, String value) {
		headers.put(key.toLowerCase(), value);
	}

	@Override
	public void removeHeader(String key) {
		headers.remove(key.toLowerCase());
	}

	@Override
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public synchronized void setBufferSize(int bufferSize) {
		if (this.bufferedOutput != null) {
			throw new IllegalStateException("Output buffer already exists");
		}
		this.bufferSize = bufferSize;
	}

	@Override
	public synchronized OutputStream getOutputStream() {
		initOuput();
		return bufferedOutput;
	}

	@Override
	public synchronized PrintWriter getWriter() {
		initOuput();
		return writer;
	}

	@Override
	public synchronized boolean isCommitted() {
		return (bufferedOutput != null) && (bufferedOutput.countBytesWritten() > 0);
	}

	@Override
	public synchronized void reset() {
		if  (bufferedOutput != null) {
    		bufferedOutput.reset();
		}
	}

	/**
	 * Initialize the output stream and stream writer and the underlying
	 * buffered output stream
	 */
	private synchronized void initOuput() {
        if (bufferedOutput == null) {
            bufferedOutput = new BufferedOutputStream(outputStream, bufferSize);
			try {
                writer = new PrintWriter(new OutputStreamWriter(bufferedOutput, getCharacterEncoding()));
			} catch (UnsupportedEncodingException ex) {
                writer = new PrintWriter(new OutputStreamWriter(bufferedOutput));
			}
		}
	}

	/**
	 * Check if data has already been written to the client, if not just
	 * return without doing anything but throw an exception otherwise
	 * @param message The message to use for the exception if necessary
	 */
	private void checkCommitted(String message) {
		if (isCommitted()) {
			throw new IllegalStateException(message + ": output has already been written");
		}
	}
	
	/**
	 * This class holds the two most important bits of information about an
	 * ouput stream: the mime type and character encoding of its content
	 */
	private class ContentType {
		/**
		 * Mime type of the content
		 */
		public String contentType;
		/**
		 * Character encoding of the content
		 */
		public String characterEncoding;

		/**
		 * Creates a new instance using the given mime type and encoding
		 * @param contentType The content mime type
		 * @param characterEncoding The content character encoding
		 */
		public ContentType(String contentType, String characterEncoding) {
			this.contentType = contentType;
			this.characterEncoding = characterEncoding;
		}
	}
	
	/**
	 * Determine the content type and character encoding using the information
	 * encountered in the response header "Content-Type"
	 * @return A ContentType object
	 */
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

	/**
	 * Update the value of the response header "Content-Type" using the
	 * information in the given ContentType object
	 * @param contentType A ContentType object
	 */
	private void updateContentType(ContentType contentType) {
		String typeAndEncoding = contentType.contentType;
		if (contentType.characterEncoding.length() > 0) {
			typeAndEncoding += "; charset=" + contentType.characterEncoding;
		}
		setHeader("Content-Type", typeAndEncoding);
	}
	
	/**
	 * Returns the given date as a string using the official format defined
	 * by the HTTP specification
	 * @param date A date object
	 * @return A date string using the oficial HTTP format
	 */
	private String dateString(Date date) {
		SimpleDateFormat fmt = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		fmt.applyPattern("EEE, d MMM yyyy HH:mm:ss z");
		return fmt.format(date);
	}

	@Override
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
	
	@Override
	public void sendRedirect(String url) throws IOException {
		HttpResponseCode resultCode = HttpResponseCode.CODE_TEMPORARY_REDIRECT;
		logger.info("SENDING REDIRECT #" + resultCode + " : " + url);
		try {
    		reset();
			setResponseCode(resultCode);
			setCharacterEncoding("UTF-8");
		} catch (IllegalStateException ex) {
			// If we can't empty the buffer we'll just append it
		}
		
		setHeader("Location", url);
		
		PrintWriter out = getWriter();
		out.print(CRLF);
		out.flush();

		// Ugly way to interrupt to normal program flow but it works
		throw new PrematureEOFException();
	}
	
	/**
	 * Write all the necessary response codes and headers to the given output
	 * stream. This method is used by our BufferedOutputStream to insert this
	 * information into the stream just ahead of the actual data.
	 */
	private synchronized void writeResultAndHeaders(OutputStream out) {
		PrintWriter w;
		try {
			w = new PrintWriter(new OutputStreamWriter(out, getCharacterEncoding()));
		} catch (UnsupportedEncodingException ex) {
			w = new PrintWriter(new OutputStreamWriter(out));
		}
		w.print("HTTP/1.1 " + responseCode.getCode() + " " + responseCode.getMessage() + CRLF);
		w.print("Date: " + dateString(new Date()) + CRLF);
		w.print("Server: " + SERVER_NAME + CRLF);
		for (String name : getHeaderNames()) {
			w.print(name + ": " + getHeader(name) + CRLF);
		}
		w.print(CRLF);
		w.flush();
	}
	
	/**
	 * This class is very much like the official Java BufferedOutputStream
	 * except for the fact that we allow the internal buffer to be reset
	 * which in effect clears its contents and allows us to start over.
	 * This is only allowed when no output has been written yet to the
	 * wrapped output stream.
	 */
	private class BufferedOutputStream extends FilterOutputStream {

		/**
		 * Our internal buffer
		 */
		protected byte[] buf;
		/**
		 * The number of bytes of data currently in the buffer
		 */
		protected int count;
		/**
		 * The number of bytes of data actually written to the client
		 */
		protected long countWritten;
		
		private boolean chunked;
		
		/**
		 * Creates a new instance using the given output stream and buffer size
		 * @param out The buffer stream to be wrapped
		 * @param size The size in bytes of our internal buffer
		 */
		public BufferedOutputStream(OutputStream out, int size) {
			super(out);
			if (size <= 0) {
				throw new IllegalArgumentException("Buffer size <= 0");
			}
			buf = new byte[size];
			chunked = false;
		}

		/**
		 * Perform a flsuh of our internal buffer writing its data to the
		 * output stream that we wrap
		 * @throws java.io.IOException Is thrown when the data could not be written
		 */
		private void flushBuffer() throws IOException {
			if (count > 0) {
				writeBytes(buf, 0, count);
				count = 0;
			}
		}

		/**
		 * Writing the data in the buffer to the output stream that we wrap.
		 * If this is the first time that anything gets written to the output
		 * we first write the proper result and response headers. If chunked
		 * transfers are enabled the proper information will be inserted
		 * into the output stream
		 * @throws java.io.IOException Is thrown when the data could not be written
		 */
		private void writeBytes(byte[] b, int off, int len) throws IOException {
			if (countWritten == 0) {
				// The very first time
				writeResultAndHeaders(out);
				// Shall we use chunks?
				chunked = "chunked".equalsIgnoreCase(getHeader("Transfer-Encoding"));
			}
			if (chunked) {
				writeChunkSize(len);
			}
			out.write(b, off, len);
			countWritten += len;
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
				writeBytes(b, off, len);
				return;
			}
			if (len > buf.length - count) {
				flushBuffer();
			}
			System.arraycopy(b, off, buf, count, len);
			count += len;
		}
		
		@Override
		public synchronized void flush() throws IOException {
			flushBuffer();
			out.flush();
		}

		@Override
		public void close() throws IOException {
			if (chunked) {
				// Write final empty chunk
				// (NB: The extra leading CRLF is necessary for IE)
				String finale = CRLF + "0" + CRLF + CRLF;
				byte[] crlfBuf = finale.getBytes("UTF-8");
				out.write(crlfBuf, 0, crlfBuf.length);
			}
			// We don't call super.close() here because that would close the socket!!!
		}

		/**
		 * Returns the total number of bytes of data already written to the client
		 * @return The number of bytes written
		 */
		public synchronized long countBytesWritten() {
			return countWritten;
		}
		
		/**
		 * Resets the internal counter that holds the number of bytes in the
		 * buffer to 0. This only works of no bytes were written yet to the client
		 */
		public synchronized void reset() {
			if (countWritten > 0) {
				throw new IllegalStateException("Output has already been written");
			}
            count = 0;
		}
		
		private void writeChunkSize(int size) throws IOException {
			String hexSize = Integer.toHexString(size) + CRLF;
			byte[] hexBuf = hexSize.getBytes("UTF-8");
			out.write(hexBuf, 0, hexBuf.length);
		}
	}
}
