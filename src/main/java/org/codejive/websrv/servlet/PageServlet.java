/*
 * PageServlet.java
 *
 * Created on Aug 14, 2007, 3:46:40 AM
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

import org.codejive.websrv.listener.HttpListener;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codejive.websrv.config.WelcomeFiles;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpResponse;
import org.codejive.websrv.protocol.http.HttpResponseCode;

/**
 *
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class PageServlet implements Servlet {

	private String rootPath;
	private WelcomeFiles welcomeFiles;
	private HashMap<String, Object> variables;
	
	private static final Logger logger = Logger.getLogger(PageServlet.class.getName());

	public PageServlet() {
	}

	public PageServlet(String rootPath, WelcomeFiles welcomeFiles) {
		this.rootPath = rootPath;
		this.welcomeFiles = welcomeFiles;
		this.variables = new HashMap<String, Object>();
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public WelcomeFiles getWelcomeFiles() {
		return welcomeFiles;
	}

	public HashMap<String, Object> getVariables() {
		return variables;
	}

	public void process(String requestPath, HttpRequest request, HttpResponse response) throws IOException {
		File file = new File(rootPath, requestPath);
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

		// Set the content length of the file we're about to send
		long fileSize = file.length();

		// Read the entire file
		char[] buf = new char[(int)fileSize];
		FileReader in = new FileReader(file);
		in.read(buf);
		in.close();
		StringBuilder script = new StringBuilder();
		script.append(buf);
		
		// The first line of a page file should always contain the response code
		int responseCode = Integer.parseInt(readLine(script));
		HttpResponseCode code = HttpResponseCode.getByCode(responseCode);
		if (code == null) {
            throw new IllegalArgumentException("Illegal response code: " + responseCode);
		}
		response.setResponseCode(code);
		
		// All the folowing lines are expected to be header key-value pairs
		// until the first empty line is encountered
		String line = readLine(script);
		while (line.length() > 0) {
			String[] paramParts = line.split(":", 2);
			if (paramParts.length != 2) {
				throw new IllegalArgumentException("Malformed response header: " + line);
			}
			response.setHeader(paramParts[0].trim(), paramParts[0].trim());
            line = readLine(script);
		}
		
		StringBuilder result = processScript(script);
		
		// Get the bytes to write using the encoding specified in the response object
        byte bytes[];
        String encoding = response.getCharacterEncoding();
        if (encoding != null && encoding.length() > 0) {
            bytes = result.toString().getBytes(response.getCharacterEncoding());
        } else {
            bytes = result.toString().getBytes();
        }
		
		// Now that we know the final output size we set the content-length header
		logger.info("Content length " + bytes.length + " bytes");
		response.setHeader("Content-Length", String.valueOf(bytes.length));
		
		// Write the bytes to the output
		OutputStream out = response.getOutputStream();
		try {
            out.write(bytes, 0, bytes.length);
			out.flush();
		} finally {
			in.close();
		}
	}
	
	// Removes the first LF or CR-LF terminated line from the text
	// and returns it (or all of the text of no end of line was found)
	private String readLine(StringBuilder text) {
		String result;
		
		// Determine the end of line by looking for
		// either a single LF or a CR-LF pair
		int eolPos = text.indexOf("\n");
		int eolPos2 = text.indexOf("\r\n");
		if (eolPos == -1 || (eolPos2 != -1 && eolPos2 < eolPos)) {
			eolPos = eolPos2;
		}
		
		if (eolPos >= 0) {
			result = text.substring(0, eolPos);
			if (text.charAt(eolPos) == '\n') {
    			text.delete(0, eolPos + 1);
			} else {
    			text.delete(0, eolPos + 2);
			}
		} else {
			result = text.toString();
			text.delete(0, text.length());
		}
		
		return result;
	}

	private StringBuilder processScript(StringBuilder script) {
		StringBuilder result = new StringBuilder();
		
		// Matches things like ${ident} , ${foo.bar[2].baz} and %{ident=foo.x} etc
		Pattern p = Pattern.compile("(\\$\\{|\\%\\{(\\p{Alpha}\\w*)=)(\\p{Alpha}\\w*(?:\\[\\d+\\])?)(\\.(\\p{Alpha}\\w*(?:\\[\\d+\\])?))*\\}");
		Matcher m = p.matcher(script);
		
		// We replace the matches with the actual values
		int lastIndex = 0;
		while (m.find(lastIndex)) {
    		logger.info("MATCH:" + m.group(0));
			
			result.append(script.subSequence(lastIndex, m.start(0)));
			
			if (m.group(0).startsWith("$")) {
				// Variable substitution
                String varName = m.group(3);
                String propName = m.group(5);
				String res = getVariableValue(varName, propName);
                result.append(res);
				lastIndex = m.end(0);
			} else {
				// Loop construct
				String loopName = m.group(2);
                String varName = m.group(3);
                String propName = m.group(5);
				
				// Find the matching end of the loop, eg %{ident}
                Pattern p2 = Pattern.compile("\\%\\{" + loopName + "\\}");
                Matcher m2 = p2.matcher(script);
				if (!m2.find(m.end(0))) {
    				throw new IllegalArgumentException("Matching close statement not found for loop: " + loopName);
				}
				
				StringBuilder subScript = new StringBuilder(script.subSequence(m.end(0), m2.start(0)));
				StringBuilder res = doLoop(loopName, varName, propName, subScript);
                result.append(res);
    			lastIndex = m2.end(0);
			}
		}
        result.append(script.subSequence(lastIndex, script.length()));
		
		return result;
	}

	private Object getVariable(String varName, String propName) {
		if (!variables.containsKey(varName)) {
            throw new IllegalArgumentException("Unknown variable: " + varName);
		}
		Object var = variables.get(varName);

		if (propName != null) {
            String[] propPartNames = propName.split("\\.");
            for (String propPartName : propPartNames) {
                var = getPropertyValue(var, propPartName);
            }
		}
		
		return var;
	}

	private Object getPropertyValue(Object obj, String propertyName) {
		try {
            PropertyDescriptor[] props = Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if (prop.getName().equals(propertyName)) {
                    Method method = prop.getReadMethod();
                    return method.invoke(obj);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Access denied to property: " + propertyName, ex);
		} catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Access denied to property: " + propertyName, ex);
		} catch (InvocationTargetException ex) {
            throw new IllegalArgumentException("Access denied to property: " + propertyName, ex);
		} catch (IntrospectionException ex) {
            throw new IllegalArgumentException("Access denied to property: " + propertyName, ex);
        }
        throw new IllegalArgumentException("Unknown property: " + propertyName);
	}
	
	private String getVariableValue(String varName, String propName) {
		Object var = getVariable(varName, propName);
		return (var != null) ? var.toString() : "";
	}

	private StringBuilder doLoop(String loopName, String varName, String propName, StringBuilder script) {
		StringBuilder result = new StringBuilder();

		Object var = getVariable(varName, propName);
		if (!(var instanceof Iterable) && !(var instanceof Iterator) && !var.getClass().isArray()) {
            throw new IllegalArgumentException("Variable is not iterable: " + varName + "." + propName);
		}
		
        if (variables.containsKey(loopName)) {
            throw new IllegalArgumentException("Variable already exists in context: " + loopName);
        }
		
		Iterator i;
		if (var instanceof Iterable) {
			i = ((Iterable) var).iterator();
		} else if (var.getClass().isArray()) {
			List items = Arrays.asList(var);
			i = items.iterator();
		} else {
			i = (Iterator) var;
		}
		
		try {
            int idx = 0;
            while (i.hasNext()) {
                Object loopVar = i.next();
                variables.put(loopName, loopVar);
                variables.put(loopName + "_INDEX", idx);
                StringBuilder res = processScript(script);
                result.append(res);
                idx++;
            }
		} finally {
            variables.remove(loopName);
            variables.remove(loopName + "_INDEX");
		}
			
		return result;
	}
}
