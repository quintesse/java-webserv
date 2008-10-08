/*
 * PageMimeTypeHandler.java
 * 
 * Created on 26-oct-2007, 13:05:00
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

package org.codejive.websrv.mimetype;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codejive.websrv.protocol.http.HttpRequest;
import org.codejive.websrv.protocol.http.HttpRequestImpl;
import org.codejive.websrv.protocol.http.HttpResponse;
import org.codejive.websrv.protocol.http.HttpResponseCode;

/**
 * This handler takes a .page file, which is a kind of special .html file with
 * dynamic properties. The first line must contain the HTTP response code to use
 * (would normally be 200) while the next lines are HTTP headers that will be
 * added to the response (although not required, it's wise to always at least
 * add the Content-Type header). After the last header line must appear at least
 * one empty line, the rest of the file is the actual content to be sent to the
 * client. Using special constructs its possible to insert dynamic values in that
 * content using variables passed to it when the handler was constructed. There
 * are also constructs for repeating parts of the page:
 * <li><b>${variablename}</b> - will insert the value of the variable with the
 * specified name. The variable must be convertable to a String.
 * <li><b>${variablename.propertyname}</b> - will insert the value of the
 * property of the variable with the specified name. It is possible to "chain"
 * property names, separating them with periods, if the property itself is an
 * object with its own properties. The final property must be convertable to a String.
 * <li><b>%{loopname=variablename}</b>...<b>%{loopname}</b> - will loop over the
 * values found in the variable with the specified name. The variable must be
 * an array, an Iterator or implement Iterable.
 * <li><b>%{loopname=variablename.propertyname}</b>...<b>%{loopname}</b> - will
 * loop over the values found in the property of the variable with the specified
 * name. It is possible to "chain" property names, separating them with periods,
 * if the property itself is an object with its own properties. The final property
 * must be an array, an Iterator or implement Iterable.
 * @author Tako Schotanus &lt;tako AT codejive.org&gt;
 */
public class PageMimeTypeHandler implements MimeTypeHandler {

	private HashMap<String, Object> variables;

	private static final Logger logger = Logger.getLogger(PageMimeTypeHandler.class.getName());

	/**
	 * Creates a new PageMimeTypeHandler
	 */
	public PageMimeTypeHandler() {
        this.variables = new HashMap<String, Object>();
    }
	
	/**
	 * Returns the map of external object references that can be used by the
	 * scripts contained in the page file.
	 * @return map of external object references
	 */
	public HashMap<String, Object> getVariables() {
		return variables;
	}
	
    public void process(File resourceFile, HttpRequest request, HttpResponse response) throws IOException {
		// Read the entire file
		long fileSize = resourceFile.length();
		char[] buf = new char[(int)fileSize];
		InputStreamReader in = new InputStreamReader(new FileInputStream(resourceFile), "UTF-8");
		in.read(buf);
		in.close();
		StringBuilder script = new StringBuilder();
		script.append(buf);
		
		handleAction(request, response);
		
		parseScript(script, request, response);
    }

    public void process(URL resourceUrl, HttpRequest request, HttpResponse response) throws IOException {
		process(resourceUrl.openStream(), request, response);
    }

    public void process(InputStream resource, HttpRequest request, HttpResponse response) throws IOException {
		// Read the entire file
		StringBuilder script = new StringBuilder();
		int n;
		char[] buf = new char[8192];
		InputStreamReader in = new InputStreamReader(resource, "UTF-8");
		while ((n = in.read(buf)) > 0) {
			script.append(buf, 0, n);
		}
		
		handleAction(request, response);
		
		parseScript(script, request, response);
    }
	
	private void handleAction(HttpRequest request, HttpResponse response) throws IOException {
		// Check if we're supposed to invoke a bean action
		String beanName = request.getParameter("bean");
		String propertyName = request.getParameter("property");
		String actionName = request.getParameter("action");
		if (beanName != null && actionName != null) {
            // Retrieve the bean
            Object bean = getVariable(beanName, propertyName);
			
            try {
                // Invoke the action on the bean
                logger.info("Invoking action '" + actionName + "' on bean '" + beanName + "." + propertyName + "'");
                Method m = getBeanMethod(bean, actionName);
                m.invoke(bean, (Object[])null);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Could not invoke action '" + actionName + "' on bean '" + beanName + "." + propertyName + "'", ex);
	            throw new IllegalArgumentException("Could not invoke action", ex);
            }

            HttpRequestImpl tmp = new HttpRequestImpl(request);
            tmp.removeParameter("bean");
            tmp.removeParameter("property");
            tmp.removeParameter("action");
            response.sendRedirect(tmp.getUrl());
		}
	}
	
	private void parseScript(StringBuilder script, HttpRequest request, HttpResponse response) throws IOException {
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
			response.setHeader(paramParts[0].trim(), paramParts[1].trim());
            line = readLine(script);
		}
		
		if ("HTTP/1.1".equalsIgnoreCase(request.getRequestProtocol())) {
			// For Page files we don't know the final output size
			// so for HTTP version 1.1 we switch to chunked transfer
			response.setHeader("Transfer-Encoding", "chunked");
		}
		
		OutputStreamWriter w = new OutputStreamWriter(response.getOutputStream());
		processScript(script, w);
		w.flush();
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

	private void processScript(StringBuilder script, Writer result) throws IOException {
		// Matches things like ${ident} , ${foo.bar[2].baz} and %{ident=foo.x} etc
		Pattern p = Pattern.compile("(\\$\\{|\\%\\{(\\p{Alpha}\\w*)=)(\\p{Alpha}\\w*(?:\\[\\d+\\])?)(\\.(\\p{Alpha}\\w*(?:\\[\\d+\\])?))*\\}");
		Matcher m = p.matcher(script);
		
		// We replace the matches with the actual values
		int lastIndex = 0;
		while (m.find(lastIndex)) {
    		logger.fine("MATCH:" + m.group(0));
			
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
				doLoop(loopName, varName, propName, subScript, result);
    			lastIndex = m2.end(0);
			}
		}
        result.append(script.subSequence(lastIndex, script.length()));
	}

	private void doLoop(String loopName, String varName, String propName, StringBuilder script, Writer result) throws IOException {
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
                processScript(script, result);
                idx++;
            }
		} finally {
            variables.remove(loopName);
            variables.remove(loopName + "_INDEX");
		}
	}
	
	private String getVariableValue(String varName, String propName) {
		Object var = getVariable(varName, propName);
		return (var != null) ? var.toString() : "";
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
		// Check if the property name has an array index part
		int index = -1;
		Pattern p = Pattern.compile("(\\p{Alpha}\\w*)\\[(\\d+)\\]");
		Matcher m = p.matcher(propertyName);
		if (m.matches()) {
			propertyName = m.group(1);
			index = Integer.parseInt(m.group(2));
		}
		
		try {
            PropertyDescriptor[] props = Introspector.getBeanInfo(obj.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                if (prop.getName().equals(propertyName)) {
                    Method method = prop.getReadMethod();
                    Object var = method.invoke(obj);
					
					// If an array index was specified we return the specified child item instead
					Object result;
					if (index >= 0) {
						Iterator i;
						if (var instanceof Iterable) {
							i = ((Iterable) var).iterator();
						} else if (var.getClass().isArray()) {
							List items = Arrays.asList(var);
							i = items.iterator();
						} else {
							i = (Iterator) var;
						}
						while (index > 0) {
							i.next();
							index--;
						}
						result = i.next();
					} else {
						result  = var;
					}
					
					return result;
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

	private Method getBeanMethod(Object obj, String methodName) {
		try {
            MethodDescriptor[] methods = Introspector.getBeanInfo(obj.getClass()).getMethodDescriptors();
            for (MethodDescriptor m : methods) {
                if (m.getName().equals(methodName)) {
                    Method method = m.getMethod();
                    return method;
                }
            }
		} catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Access denied to method: " + methodName, ex);
		} catch (IntrospectionException ex) {
            throw new IllegalArgumentException("Access denied to method: " + methodName, ex);
        }
        throw new IllegalArgumentException("Unknown method: " + methodName);
	}
}
