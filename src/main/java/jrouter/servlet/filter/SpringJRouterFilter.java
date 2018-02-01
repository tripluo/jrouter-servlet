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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterConfig;
import jrouter.ActionFactory;
import jrouter.config.Configuration;
import jrouter.spring.SpringObjectFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 通过configLocation配置Configuration进而加载ActionFactory对象。
 * ActionFactory生成新对象实例依托于springframework工厂创建新的对象实例，因而必须首先加载spring容器。
 *
 * @see jrouter.ObjectFactory
 * @see jrouter.spring.SpringObjectFactory
 * @see jrouter.config.Configuration
 */
public class SpringJRouterFilter extends JRouterFilter {

    /**
     * 默认使用springframework工厂创建新的对象实例。
     */
    private boolean useSpringObjectFactory = true;

    @Override
    public void init(FilterConfig filterConfig) {
        String useSpring = filterConfig.getInitParameter("useSpringObjectFactory");
        //default true if not set
        if (useSpring != null)
            useSpringObjectFactory = Boolean.parseBoolean(useSpring);
        super.init(filterConfig);
    }

    /**
     * A hook to give subclass another way to create ActionFactory。
     *
     * @param filterConfig 过滤器配置。
     *
     * @return ActionFactory bean.
     */
    @Override
    protected ActionFactory createActionFactory(final FilterConfig filterConfig) {
        log.info("Load configuration location : {}", configLocation);
        Configuration configuration = new Configuration();
        configuration.load(configLocation);
        if (useSpringObjectFactory) {
            Map<String, Object> actionFactoryProperties = new HashMap<>(2);
            actionFactoryProperties.put("objectFactory",
                    new SpringObjectFactory(WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext())));
            configuration.addActionFactoryProperties(actionFactoryProperties);
        }
        return configuration.buildActionFactory();
    }
}
