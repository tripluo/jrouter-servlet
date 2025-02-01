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

package net.jrouter.http.servlet.result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jrouter.annotation.ResultType;
import net.jrouter.http.servlet.ServletActionInvocation;

import java.io.IOException;

/**
 * Result for http servlet, include "forward" and "redirect".
 */
public class ServletResult {

    /**
     * "action_forward" symbol.
     */
    public static final String ACTION_FORWARD = "action_forward";

    /**
     * "forward" symbol.
     */
    public static final String FORWARD = "forward";

    /**
     * "redirect" symbol.
     */
    public static final String REDIRECT = "redirect";

    /**
     * Action结果直接调用映射的Action，类似forward结果类型。 forward可多次关联调用，需自行判断循环调用。
     * @param invocation Action运行时上下文。
     * @return 返回forward后的调用结果。
     *
     * @see net.jrouter.result.DefaultResult#actionForward
     */
    @ResultType(type = ACTION_FORWARD)
    public static Object actionForward(ServletActionInvocation invocation) {
        return invocation.getActionFactory()
            .invokeAction(invocation.getResult().location(), invocation.getParameters());
    }

    /**
     * @see jakarta.servlet.RequestDispatcher#forward
     */
    @ResultType(type = FORWARD)
    public static void forward(ServletActionInvocation invocation) throws IOException, ServletException {
        HttpServletResponse response = invocation.getResponse();
        if (response.isCommitted()) {
            return;
        }
        String location = invocation.getResult().location();
        if (location.charAt(0) != '/') {
            location = '/' + location; // NOPMD
        }

        HttpServletRequest request = invocation.getRequest();
        if (request.getContextPath() != null && !request.getContextPath().isEmpty()) {
            location = request.getContextPath() + location; // NOPMD
        }
        request.getRequestDispatcher(location).forward(request, response);
    }

    /**
     * @see HttpServletResponse#sendRedirect
     */
    @ResultType(type = REDIRECT)
    public static void redirect(ServletActionInvocation invocation) throws IOException {
        HttpServletResponse response = invocation.getResponse();
        if (response.isCommitted()) {
            return;
        }
        String location = invocation.getResult().location();
        if (location.charAt(0) != '/') {
            location = '/' + location; // NOPMD
        }

        HttpServletRequest request = invocation.getRequest();
        if (request.getContextPath() != null && !request.getContextPath().isEmpty()) {
            location = request.getContextPath() + location; // NOPMD
        }
        response.sendRedirect(location);
    }

}
