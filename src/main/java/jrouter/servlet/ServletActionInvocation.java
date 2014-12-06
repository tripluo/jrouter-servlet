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
package jrouter.servlet;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jrouter.ActionInvocation;

/**
 * 扩展ActionInvocation，返回HTTP Servlet常用参数的接口。
 */
public interface ServletActionInvocation extends ActionInvocation {

    /**
     * Gets the HTTP servlet request object.
     *
     * @return the HTTP servlet request object.
     */
    HttpServletRequest getRequest();

    /**
     * Gets the HTTP servlet session object.
     *
     * @return the HTTP servlet session object.
     */
    HttpSession getSession();

    /**
     * Gets the HTTP servlet request parameters.
     *
     * @return the HTTP servlet request parameters.
     */
    Map<String, String[]> getRequestParameters();

    /**
     * Gets the HTTP servlet response object.
     *
     * @return the HTTP servlet response object.
     */
    HttpServletResponse getResponse();

    /**
     * Gets the servlet context.
     *
     * @return the servlet context.
     */
    ServletContext getServletContext();
}
