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
package jrouter.servlet.filter;

import javax.servlet.FilterConfig;
import jrouter.ActionFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * ActionFactory对象引自springframework中的bean实例对象。
 * 由ActionFactory类型或已知的bean名称指定已存在于spring容器中的ActionFactory对象，因而必须首先加载spring容器。
 */
public class SpringBeanJRouterFilter extends SpringJRouterFilter {

    /**
     * ActionFactory于springframework中bean的名称。
     */
    private String beanName;

    @Override
    public void init(FilterConfig filterConfig) {
        beanName = filterConfig.getInitParameter("beanName");
        if (beanName != null)
            log.info("Set bean's name of springframework : " + beanName);
        super.init(filterConfig);
    }

    /**
     * 设置ActionFactory为spring中指定的bean。
     *
     * @param filterConfig 过滤器配置。
     */
    @Override
    protected ActionFactory createActionFactory(FilterConfig filterConfig) {
        //set ActionFactory with spring bean
        return beanName == null
                ? WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext()).getBean(ActionFactory.class)
                : WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext()).getBean(beanName, ActionFactory.class);
    }
}
