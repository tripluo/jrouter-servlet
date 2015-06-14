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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jrouter.ActionInvocation;
import jrouter.ConverterFactory;
import jrouter.JRouterException;
import jrouter.ParameterConverter;
import jrouter.annotation.Dynamic;
import jrouter.util.CollectionUtil;

/**
 * 创建多参数自动映射转换器的工厂类。
 */
public class MultiParameterConverterFactory implements ConverterFactory {

    /** 缓存方法参数的匹配的位置 */
    private Map<Method, int[]> methodParametersCache;

    /**
     * 追加的参数类型是否固定顺序，默认固定参数。
     *
     * @see ActionInvocation#getParameters()
     * @see MultiParameterConverter#additionalParameters
     */
    private boolean fixedOrder = true;

    /**
     * 创建固定参数自动映射转换器的工厂类。
     */
    public MultiParameterConverterFactory() {
        this(true);
    }

    /**
     * 创建多参数自动映射转换器的工厂类。
     *
     * @param fixedOrder 参数类型是否固定顺序。
     */
    public MultiParameterConverterFactory(boolean fixedOrder) {
        this.fixedOrder = fixedOrder;
        if (fixedOrder) {
            methodParametersCache = new ConcurrentHashMap<Method, int[]>();
        }
    }

    /**
     * 此参数转换器需要ActionFactory支持，允许打乱原有参数进行无序的参数绑定。
     * 对ServletActionInvocation对象，追加注入的http参数。
     * 对ActionInvocation对象，追加注入其自身。
     *
     * @see ServletActionFactory.DefaultServletActionFactory#createActionInvocation
     */
    @Override
    public ParameterConverter getParameterConverter(ActionInvocation actionInvocation) {
        if (actionInvocation instanceof ServletActionInvocation) {
            ServletActionInvocation sa = (ServletActionInvocation) actionInvocation;
            return new MultiParameterConverter(sa.getRequest(), sa.getResponse(), sa.getServletContext(),
                    actionInvocation);
        }
        //TODO
        //Get parameters from ActionInvocation
        Object[] parameters = actionInvocation.getParameters();
        if (CollectionUtil.isEmpty(parameters))
            return new MultiParameterConverter(actionInvocation);
        Object[] allParameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, allParameters, 0, parameters.length);
        //add ActionInvocation as last parameter
        allParameters[parameters.length] = actionInvocation;
        return new MultiParameterConverter(allParameters);
    }

    /**
     * 提供多参数自动映射的转换器。
     * 如果原方法的调用参数数目大于或等于方法本身所需的参数个数，则返回未处理的原调用参数（调用时可能会抛出方法调用异常）。
     * 仅在原方法的调用参数数目小于方法本身所需的参数个数时，注入并自动映射追加的参数（无追加参数类型匹配映射<code>null</code>）。
     */
    @Dynamic
    public class MultiParameterConverter implements ParameterConverter {

        /** 追加注入的参数 */
        private final Object[] additionalParameters;

        /**
         * 追加注入的多个参数。
         *
         * @param additionalParameters 追加注入的多个参数。
         */
        public MultiParameterConverter(Object... additionalParameters) {
            this.additionalParameters = additionalParameters;
        }

        @Override
        public Object[] convert(Method method, Object obj, Object[] params) throws JRouterException {
            if (additionalParameters == null || additionalParameters.length == 0)
                return params;
            Class<?>[] parameterTypes = method.getParameterTypes();
            int originalSize = parameterTypes.length;
            //变长或原本无参数的方法
            if (method.isVarArgs() || originalSize == 0) {
                return params;
            }
            int pLen = (params == null ? 0 : params.length);
            //保留原参数，追加支持的绑定参数
            if (originalSize > pLen) {
                Object[] newArgs = new Object[originalSize];
                if (pLen > 0)
                    System.arraycopy(params, 0, newArgs, 0, pLen);
                int[] idx = match(method, parameterTypes, additionalParameters);
                for (int i = pLen; i < originalSize; i++) {
                    newArgs[i] = (idx[i] == -1 ? null : additionalParameters[idx[i]]);
                }
                return newArgs;
            }
            return params;
        }

        /**
         * 匹配追加注入的参数相对于方法参数类型中的映射；
         * 匹配顺序不考虑父子优先级，追加的参数按顺序优先匹配；
         * 如果追加注入的参数类型固定，则会缓存记录。
         *
         * @param method 指定的方法。
         * @param parameterTypes 方法的参数类型。
         * @param parameters 追加注入的参数。
         *
         * @return 追加注入的参数相对于方法参数类型中的映射。
         *
         * @see #methodParametersCache
         */
        private int[] match(Method method, Class[] parameterTypes, Object[] parameters) {
            int[] idx = null;
            if (fixedOrder) {
                //get from cache
                idx = methodParametersCache.get(method);
                if (idx != null)
                    return idx;
            }
            idx = new int[parameterTypes.length];
            for (int i = 0; i < idx.length; i++) {
                //初始值-1, 无匹配
                idx[i] = -1;
                if (parameters != null) {
                    Class parameterType = parameterTypes[i];
                    for (int j = 0; j < parameters.length; j++) {
                        //不考虑父子优先级，参数按顺序优先匹配。
                        if (parameterType.isInstance(parameters[j])) {
                            idx[i] = j;
                            break;
                        }
                    }
                }
            }
            if (fixedOrder) {
                //put in cache
                methodParametersCache.put(method, idx);
            }
            return idx;
        }
    }

    /**
     * 参数类型是否固定顺序。
     *
     * @return 参数类型是否固定顺序。
     */
    public boolean isFixedOrder() {
        return fixedOrder;
    }

}
