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

import javax.servlet.*;
import jrouter.ActionFactory;
import jrouter.config.Configuration;
import lombok.extern.slf4j.Slf4j;

/**
 * JRouter servlet filter.通过configLocation配置{@code Configuration}进而加载{@code ActionFactory}对象。
 *
 * @see jrouter.config.Configuration
 */
@lombok.Getter
@lombok.Setter
@Slf4j
public class JRouterFilter extends AbstractJRouterFilter {

    /**
     * Location of the jrouter ActionFactory's configuration file, default load resource file jrouter.xml.
     */
    private String configLocation = "jrouter.xml";

    @Override
    public void init(FilterConfig filterConfig) {
        String conf = filterConfig.getInitParameter("configLocation");
        if (conf != null) {
            this.configLocation = conf;
        }
        super.init(filterConfig);
    }

    @Override
    protected ActionFactory createActionFactory(FilterConfig filterConfig) {
        log.info("Load configuration location : {}", configLocation);
        Configuration configuration = new Configuration();
        configuration.load(configLocation);
        return configuration.buildActionFactory();
    }

}
