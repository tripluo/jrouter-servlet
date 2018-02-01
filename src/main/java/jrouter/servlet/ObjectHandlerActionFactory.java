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

    /**
     * Object class to ResultType mapping.
     *
     * 完全类型匹配，不考虑父子类继承等。
     */
    private Map<Class, ResultTypeProxy> objectResultTypes;

    /**
     * 根据指定的键值映射构造初始化数据的ObjectHandlerActionFactory对象。
     *
     * @param properties 指定的初始化数据键值映射。
     */
    public ObjectHandlerActionFactory(Map<String, Object> properties) {
        super(properties);
    }

    @Override
    protected Object invokeResult(ActionInvocation invocation, Object res) {
        ResultTypeProxy resultType = null;
        //优先根据结果对象的类型获取处理类型
        if (res != null && (resultType = objectResultTypes.get(res.getClass())) != null) {
            return MethodUtil.invoke(resultType, invocation);
        }
        return super.invokeResult(invocation, res);
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
