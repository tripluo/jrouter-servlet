package net.jrouter.http.servlet;

import java.util.Collections;
import java.util.Map;
import net.jrouter.ActionInvocation;
import net.jrouter.impl.ResultTypeProxy;
import net.jrouter.util.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供非{@code String}类型的结果类型处理，
 * 及可配置的类型{@code Class} - 结果类型{@link ResultTypeProxy}映射。
 */
public class ObjectHandlerActionFactory extends ServletActionFactory.DefaultServletActionFactory {

    /** 日志 */
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHandlerActionFactory.class);

    /**
     * Object class to ResultType mapping.
     * <p>
     * 完全类型匹配，不考虑父子类继承等。
     */
    @lombok.Setter
    private Map<Class, ResultTypeProxy> objectResultTypes = Collections.EMPTY_MAP;

    /**
     * Constructor.
     *
     * @param properties Properties
     */
    public ObjectHandlerActionFactory(Properties properties) {
        super(properties);
    }

    @Override
    protected Object invokeResult(ActionInvocation invocation, Object res) {
        ResultTypeProxy resultType = null;
        //优先根据结果对象的类型获取处理类型
        if (res != null && (resultType = objectResultTypes.get(res.getClass())) != null) {
            return MethodUtil.invokeConvertParameters(resultType, invocation);
        }
        return super.invokeResult(invocation, res);
    }

}
