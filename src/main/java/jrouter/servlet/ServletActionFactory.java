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
import jrouter.ActionInvocation;
import jrouter.impl.DefaultActionFactory;

/**
 * ServletActionFactory to store the ActionInvocation in thread local variable.
 */
public class ServletActionFactory extends DefaultActionFactory {

    /**
     * 构造ServletActionFactory并初始化数据。
     *
     * @see DefaultActionFactory
     */
    public ServletActionFactory() {
        super();
    }

    /**
     * 根据指定的键值映射构造初始化数据的ServletActionFactory对象。
     *
     * @param properties 指定的初始化数据键值映射。
     *
     * @see DefaultActionFactory
     */
    public ServletActionFactory(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    protected ActionInvocation<?> createActionInvocation(String path, Object... params) {
        ActionInvocation<?> ai = super.createActionInvocation(path, params);
        if (ServletThreadContext.get() != null) {
            //put the ActionInvocation to thread local variable
            ServletThreadContext.setActionInvocation(ai);
        }
        return ai;
    }
}
