/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.jrouter.http.servlet;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.jrouter.annotation.Dynamic;

/**
 * A simple implementation of the {@link java.util.Map} interface to handle a collection of request
 * parameters.
 */
@Dynamic
public class RequestMap extends AbstractMap<String, String[]> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the http request parameters */
    private final Map<String, String[]> parameters;

    /**
     * Saves the request to use as the backing for getting and setting values
     *
     * @param request the http servlet request.
     */
    public RequestMap(final HttpServletRequest request) {
        super();
        parameters = new HashMap<>(request.getParameterMap());
    }

    /**
     * Removes all parameters from the request as well as clears entries in this map.
     */
    @Override
    public void clear() {
        if (parameters != null) {
            parameters.clear();
        }
    }

    /**
     * Returns a Set of parameters from the http request.
     *
     * @return a Set of parameters from the http request.
     */
    @Override
    public Set<Entry<String, String[]>> entrySet() {
        return parameters.entrySet();
    }

    /**
     * Returns the request parameter associated with the given key or <tt>null</tt> if it doesn't
     * exist.
     *
     * @param key the name of the request parameter.
     *
     * @return the request parameter or <tt>null</tt> if it doesn't exist.
     */
    @Override
    public String[] get(Object key) {
        return parameters.get(key);
    }

    /**
     * Saves an parameter in the request.
     *
     * @param key the name of the request parameter.
     * @param value the value to set.
     *
     * @return the object that was just set.
     */
    @Override
    public String[] put(String key, String[] value) {
        return parameters.put(key, value);
    }

    /**
     * Removes the specified request parameter.
     *
     * @param key the name of the parameter to remove.
     *
     * @return the value that was removed or <tt>null</tt> if the value was not found (and hence,
     * not removed).
     */
    @Override
    public String[] remove(Object key) {
        return parameters.remove(key);
    }
}
