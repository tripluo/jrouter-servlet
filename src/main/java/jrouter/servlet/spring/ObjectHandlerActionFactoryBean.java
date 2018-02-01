package jrouter.servlet.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jrouter.config.Configuration;
import jrouter.impl.ResultTypeProxy;
import jrouter.servlet.ObjectHandlerActionFactory;
import jrouter.spring.DefaultActionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供与springframework集成的ObjectHandlerActionFactory对象。
 */
public class ObjectHandlerActionFactoryBean extends DefaultActionFactoryBean<ObjectHandlerActionFactory> {

    /** 日志 */
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHandlerActionFactoryBean.class);

    /* object class to ResultType mapping */
    private Map<Class, String> objectResultTypes;

    @Override
    protected void setDefaultActionFactoryClass(Configuration config) {
        //default use ObjectHandlerActionFactory
        config.setActionFactoryClass(ObjectHandlerActionFactory.class);
    }

    @Override
    protected void afterActionFactoryCreation(ObjectHandlerActionFactory actionFactory) {
        super.afterActionFactoryCreation(actionFactory);
        Map<Class, ResultTypeProxy> _objectResultTypes = new HashMap<>(2);
        if (objectResultTypes != null && !objectResultTypes.isEmpty()) {
            for (Map.Entry<Class, String> e : objectResultTypes.entrySet()) {
                Class classType = e.getKey();
                String resultType = e.getValue();
                ResultTypeProxy type = actionFactory.getResultTypes().get(resultType);
                if (type == null) {
                    LOG.info("Can't find ResultType [{}] for [{}], use default [{}]",
                            resultType, classType, actionFactory.getDefaultResultType());
                } else {
                    if (String.class == classType) {
                        //String类型在PathActionFactory#invokeAction(...)中有做内置处理，除非有覆写此方法
                        LOG.warn("Set [java.lang.String] type is usually invalid when using PathActionFactory or it's subtypes");
                    }
                    LOG.info("Set ResultType [{}] for class [{}]", resultType, classType.getName());
                    _objectResultTypes.put(classType, type);
                }
            }
        }
        //use unmodifiable Map to avoid multi threading problem
        actionFactory.setObjectResultTypes(Collections.unmodifiableMap(_objectResultTypes));

    }

    /**
     * 设置(结果对象类型:结果类型对象)的映射关系。
     *
     * @param objectResultTypes (结果对象的类型:结果类型对象)的映射关系。
     */
    public void setObjectResultTypes(Map<Class, String> objectResultTypes) {
        this.objectResultTypes = objectResultTypes;
    }
}
