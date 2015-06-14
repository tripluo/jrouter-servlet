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
package jrouter.servlet.result;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jrouter.annotation.ResultType;
import jrouter.servlet.ServletActionInvocation;

/**
 * Result for http servlet, include "forward" and "redirect".
 */
public class ServletResult {

    /**
     * "forward" symbol.
     */
    public static final String FORWARD = "forward";

    /**
     * "redirect" symbol.
     */
    public static final String REDIRECT = "redirect";

    /**
     * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @ResultType(type = FORWARD)
    public static void forward(ServletActionInvocation invocation) throws IOException,
            ServletException {
        HttpServletResponse response = invocation.getResponse();
        if (response.isCommitted())
            return;

        String location = invocation.getResult().location();
        if (location.charAt(0) != '/') {
            location = '/' + location;
        }

        HttpServletRequest request = invocation.getRequest();
        if (request.getContextPath() != null && request.getContextPath().length() > 0) {
            location = request.getContextPath() + location;
        }
        request.getRequestDispatcher(location).forward(request, response);
    }

    /**
     * @see HttpServletResponse#sendRedirect(java.lang.String)
     */
    @ResultType(type = REDIRECT)
    public static void redirect(ServletActionInvocation invocation) throws IOException {
        HttpServletResponse response = invocation.getResponse();
        if (response.isCommitted())
            return;

        String location = invocation.getResult().location();
        if (location.charAt(0) != '/') {
            location = '/' + location;
        }

        HttpServletRequest request = invocation.getRequest();
        if (request.getContextPath() != null && request.getContextPath().length() > 0) {
            location = request.getContextPath() + location;
        }
        response.sendRedirect(location);
    }
}
