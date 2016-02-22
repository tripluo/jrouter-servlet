package jrouter.servlet;

import java.util.Map;
import jrouter.ActionInvocation;
import jrouter.impl.ResultTypeProxy;
import jrouter.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供非{@code String}类型的结果类型处理，
 * 及可配置的类型{@code Class} -> 结果类型{@link ResultTypeProxy}映射。
 */
public class ObjectHandlerActionFactory extends ServletActionFactory.DefaultServletActionFactory {

    /** 日志 */
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHandlerActionFactory.class);

    /* default object handler */
    private ResultTypeProxy defaultObjectHandler;

    /**
     * 根据指定的键值映射构造初始化数据的ObjectHandlerActionFactory对象。
     *
     * @param properties 指定的初始化数据键值映射。
     */
    public ObjectHandlerActionFactory(Map<String, Object> properties) {
        super(properties);
    }

    /**
     * object class to ResultType mapping.
     *
     * 完全类型匹配，不考虑父子类继承等。
     */
    private Map<Class, ResultTypeProxy> objectResultTypes;

    @Override
    protected Object invokeObjectResult(ActionInvocation invocation, Object res) {
        ResultTypeProxy resultType = null;
        if (res != null && (resultType = objectResultTypes.get(res.getClass())) != null) {
            return MethodUtil.invoke(resultType, invocation);
        }
        return MethodUtil.invoke(defaultObjectHandler, invocation);
    }

    @Override
    protected Object invokeUndefinedResult(ActionInvocation<?> invocation, String resInfo) {
        LOG.debug("Invoking undefined String Result [{}] at {}, use defaultObjectHandler [{}] ",
                resInfo, invocation.getActionProxy().getMethodInfo(), defaultObjectHandler);
        return MethodUtil.invoke(defaultObjectHandler, invocation);
    }

    /**
     * 设置默认非{@code String}类型对象的结果类型处理对象。
     * 注意即使设置了{@code String}类型的对象也并不会调用invokeObjectResult方法。
     *
     * @see #invokeObjectResult(jrouter.ActionInvocation, java.lang.Object)
     *
     * @param defaultObjectHandler 非{@code String}对象的结果类型处理对象。
     */
    public void setDefaultObjectHandler(ResultTypeProxy defaultObjectHandler) {
        this.defaultObjectHandler = defaultObjectHandler;
    }

    /**
     * 设置(结果对象的类型:结果类型对象)的映射关系。
     *
     * @param objectResultTypes (结果对象的类型:结果类型对象)的映射关系。
     */
    public void setObjectResultTypes(Map<Class, ResultTypeProxy> objectResultTypes) {
        this.objectResultTypes = objectResultTypes;
    }

}
