
**jrouter-servlet** 是一个基于[jrouter](https://github.com/innjj/jrouter)的Servlet组件；提供快速的路由定位、方法调用、参数绑定等功能。其核心设计目标即使代码少、小而精、轻量级、易扩展、Restful。


### Maven: ###

```xml
<dependency>
    <groupId>net.jrouter</groupId>
    <artifactId>jrouter-servlet</artifactId>
    <version>1.7.1</version>
</dependency>
```
### Web Filter配置: ###

Sample [web.xml](https://github.com/innjj/jrouter-home/blob/master/src/main/webapp/WEB-INF/web.xml) of project [jrouter-home](https://github.com/innjj/jrouter-home)

```xml
<filter>
    <filter-name>JRouter-Filter</filter-name>
    <filter-class>jrouter.servlet.filter.SpringBeanJRouterFilter</filter-class>
    <init-param>
        <description>Character encoding (optional) (default:UTF-8)</description>
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
        <param-value>jrouter_factory</param-value>
    </init-param>
    <init-param>
        <description>log NotFoundException (default:true)</description>
        <param-name>logNotFoundException</param-name>
        <param-value>false</param-value>
    </init-param>
</filter>
```
### Springframework Integration: ###

Sample [spring.xml](https://github.com/innjj/jrouter-home/blob/master/src/main/resources/jrouter-home-spring.xml)

```xml
<!-- JRouter ActionFactory -->
<bean id="actionFactory" class="jrouter.servlet.spring.ObjectHandlerActionFactoryBean">
    <property name="defaultObjectResultType" value="freemarker" />
    <property name="actionFactoryProperties">
        <util:properties location="classpath:jrouterActionFactory.properties" />
    </property>
    <property name="interceptors">
        <list>
            <value>jrouter.home.interceptor.TimerInterceptor</value>
            <value>jrouter.home.interceptor.ExceptionInterceptor</value>
        </list>
    </property>
    <property name="interceptorStacks">
        <list>
            <value>jrouter.home.interceptor.DefaultInterceptorStack</value>
        </list>
    </property>
    <property name="resultTypes">
        <list>
            <value>jrouter.servlet.result.ServletResult</value>
            <ref bean="freemarkerResult" />
        </list>
    </property>
    <!-- scan classes properties -->
    <property name="componentClassScanProperties">
        <list>
            <value>
                package = jrouter
                includeExpression = jrouter.home.**.*Action
            </value>
        </list>
    </property>
</bean>
```