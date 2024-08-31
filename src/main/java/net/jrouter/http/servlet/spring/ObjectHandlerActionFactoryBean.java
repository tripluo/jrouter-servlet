package net.jrouter.http.servlet.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.jrouter.http.servlet.ObjectHandlerActionFactory;
import net.jrouter.http.servlet.ServletActionFactory;
import net.jrouter.impl.ResultTypeProxy;
import net.jrouter.spring.SpringObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 提供与springframework集成的{@code ObjectHandlerActionFactory}对象。
 */
public class ObjectHandlerActionFactoryBean implements FactoryBean<ObjectHandlerActionFactory>, InitializingBean,
        DisposableBean, ApplicationContextAware {

    /**
     * LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHandlerActionFactoryBean.class);

    /**
     * ApplicationContext.
     */
    private ApplicationContext applicationContext;

    /**
     * Object class to ResultType mapping.
     */
    @lombok.Setter
    private Map<Class, String> objectResultTypes = Collections.emptyMap();

    /**
     * Properties.
     */
    @lombok.Setter
    private ServletActionFactory.DefaultServletActionFactory.Properties properties = null;

    /**
     * ActionFactory.
     */
    private ObjectHandlerActionFactory actionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (properties == null) {
            properties = new ServletActionFactory.DefaultServletActionFactory.Properties();
            properties.setObjectFactory(new SpringObjectFactory(applicationContext));
        }
        actionFactory = new ObjectHandlerActionFactory(properties);
        Map<Class, ResultTypeProxy> tmpObjectResultTypes = new HashMap<>(2);
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
                        // String类型在PathActionFactory#invokeAction(...)中有做内置处理，除非有覆写此方法
                        LOG.warn("Set [java.lang.String] type is usually invalid when using PathActionFactory or it's subtypes");
                    }
                    LOG.info("Set ResultType [{}] for class [{}]", resultType, classType.getName());
                    tmpObjectResultTypes.put(classType, type);
                }
            }
        }
        // use unmodifiable Map to avoid multi threading problem
        actionFactory.setObjectResultTypes(Collections.unmodifiableMap(tmpObjectResultTypes));

    }

    @Override
    public ObjectHandlerActionFactory getObject() throws Exception {
        return actionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return actionFactory.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        if (actionFactory != null) {
            this.actionFactory.clear();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
