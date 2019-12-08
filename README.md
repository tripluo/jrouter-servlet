
**jrouter-servlet** 是一个基于[jrouter](https://github.com/tripluo/jrouter)的Servlet组件；提供快速的路由定位、方法调用、参数绑定等功能。其核心设计目标即小而精、代码少、轻量级、易扩展、Restful。

● require [jdk 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

◇ [changelog](https://github.com/tripluo/jrouter-servlet/commits/master)

### Maven: ###

```xml
<dependency>
    <groupId>net.jrouter</groupId>
    <artifactId>jrouter-servlet</artifactId>
    <version>1.8.2</version>
</dependency>
```

###  JavaConfig: ###
```
import javax.servlet.DispatcherType;
import net.jrouter.ActionFactory;
import net.jrouter.http.servlet.ObjectHandlerActionFactory;
import net.jrouter.http.servlet.filter.SpringBeanJRouterFilter;
import net.jrouter.spring.SpringObjectFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
...
    @Bean
    ActionFactory<String> actionFactory(ApplicationContext applicationContext) {
        ObjectHandlerActionFactory.Properties properties = new ObjectHandlerActionFactory.Properties();
        properties.setActionPathCaseSensitive(false);
        properties.setExtension(".");
        properties.setDefaultResultType("fastjson");
        properties.setDefaultInterceptorStack(net.jrouter.home.interceptor.DefaultInterceptorStack.DEFAULT);
        properties.setObjectFactory(new SpringObjectFactory(applicationContext));
        ObjectHandlerActionFactory actionFactory = new ObjectHandlerActionFactory(properties);

        actionFactory.addInterceptors(net.jrouter.home.interceptor.TimerInterceptor.class);
        actionFactory.addInterceptors(net.jrouter.home.interceptor.ExceptionInterceptor.class);

        actionFactory.addInterceptorStacks(net.jrouter.home.interceptor.DefaultInterceptorStack.class);

        actionFactory.addResultTypes(net.jrouter.http.servlet.result.ServletResult.class);
        actionFactory.addResultTypes(...);

        actionFactory.addActions(net.jrouter.home.action.HomeAction.class);
        ...
        return actionFactory;
    }

    @Bean
    public FilterRegistrationBean actionFactoryFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        SpringBeanJRouterFilter actionFactoryFilter = new SpringBeanJRouterFilter();
        actionFactoryFilter.setEncoding("UTF-8");
        actionFactoryFilter.setFactoryName("jrouter_factory");
        actionFactoryFilter.setLogNotFoundException(false);
        actionFactoryFilter.setTrimRequestParameter(true);
        actionFactoryFilter.setUseThreadLocal(true);
        registration.addUrlPatterns("*.jj");
        registration.setFilter(actionFactoryFilter);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
        return registration;
    }
```

### Web Filter配置: ###

Sample [web.xml](https://github.com/tripluo/jrouter-home/blob/master/src/main/webapp/WEB-INF/web.xml) of project [jrouter-home](https://github.com/tripluo/jrouter-home)

```xml
<filter>
    <filter-name>JRouter-Filter</filter-name>
    <filter-class>net.jrouter.http.servlet.filter.SpringBeanJRouterFilter</filter-class>
    <init-param>
        <description>ActionFactory's bean name</description>
        <param-name>beanName</param-name>
        <param-value>servletActionFactory</param-value>
    </init-param>
    <init-param>
        <description>Character encoding (optional)</description>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
        <description>Trim HttpServletRequest#getParameter(String) (optional) (default:false)</description>
        <param-name>trimRequestParameter</param-name>
        <param-value>true</param-value>
    </init-param>
    <init-param>
        <description>ActionFactory's name in ServletContext (optional)</description>
        <param-name>factoryName</param-name>
        <param-value>servletActionFactory</param-value>
    </init-param>
    <init-param>
        <description>log NotFoundException (default:true)</description>
        <param-name>logNotFoundException</param-name>
        <param-value>false</param-value>
    </init-param>
</filter>
```
### Springframework Integration: ###

Sample [spring.xml](https://github.com/tripluo/jrouter-home/blob/master/src/main/resources/jrouter-home-spring.xml)

```xml
<!-- JRouter ActionFactory -->
<bean id="servletActionFactory" class="net.jrouter.http.servlet.spring.ObjectHandlerActionFactoryBean">
    <!-- deprecated since 1.7.5 <property name="defaultObjectResultType" value="freemarker" />-->
    <property name="actionFactoryProperties">
        <util:properties location="classpath:jrouterActionFactory.properties" />
    </property>
    <property name="interceptors">
        <list>
            <value>net.jrouter.home.interceptor.TimerInterceptor</value>
            <value>net.jrouter.home.interceptor.ExceptionInterceptor</value>
        </list>
    </property>
    <property name="interceptorStacks">
        <list>
            <value>net.jrouter.home.interceptor.DefaultInterceptorStack</value>
        </list>
    </property>
    <property name="resultTypes">
        <list>
            <value>net.jrouter.http.servlet.result.ServletResult</value>
            <ref bean="freemarkerResult" />
        </list>
    </property>
    <!-- scan classes properties -->
    <property name="componentClassScanProperties">
        <list>
            <value>
                package = net.jrouter
                includeExpression = net.jrouter.home.**.*Action
            </value>
        </list>
    </property>
</bean>
```