package com.huatu.springboot.dubbo.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.huatu.springboot.dubbo.annotation.InterfaceMethods;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AnnotationBean
 *
 * @author william.liangf
 * @export >>>修复@com.alibaba.dubbo.config.annotation.Service
 * 与Spring @Transactional 冲突
 */
public class AnnotationBean extends AbstractConfig
        implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor, ApplicationContextAware {

    private static final long serialVersionUID = -7582802454287589552L;

    private static final Logger logger = LoggerFactory.getLogger(Logger.class);

    private ConfigurableListableBeanFactory beanFactory;

    private boolean buildFactory;

    private String annotationPackage;

    private boolean enabled = false;

    private String[] annotationPackages;

    private final Set<ServiceConfig<?>> serviceConfigs = new ConcurrentHashSet<ServiceConfig<?>>();

    private final ConcurrentMap<String, ReferenceBean<?>> referenceConfigs = new ConcurrentHashMap<String, ReferenceBean<?>>();

    public String getPackage() {
        return annotationPackage;
    }

    public void setPackage(String annotationPackage) {
        this.annotationPackage = annotationPackage;
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
                : Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
    }

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, Object> beans = beanFactory.getBeansWithAnnotation(EnableDubboApplication.class);
        if(beans != null && beans.size() != 0){
            for (String key : beans.keySet()) {
                Class<?> clazz = beans.get(key).getClass();
                EnableDubboApplication config = clazz.getAnnotation(EnableDubboApplication.class);
                this.annotationPackage = config.value();
                this.buildFactory = config.buildFactory();
                this.enabled = true;
                break;
            }
        }

        if (annotationPackage == null || annotationPackage.length() == 0) {
            logger.debug("未定义注解扫描包");
            return;
        }
        logger.info("扫描：" + annotationPackage);
        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = ReflectUtils.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
                Object scanner = scannerClass.getConstructor(new Class<?>[]{BeanDefinitionRegistry.class, boolean.class}).newInstance(new Object[]{(BeanDefinitionRegistry) beanFactory, true});
                // add filter
                Class<?> filterClass = ReflectUtils.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
                Object filter = filterClass.getConstructor(Class.class).newInstance(Service.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter",ReflectUtils.forName("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                String[] packages = Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
                Method scan = scannerClass.getMethod("scan", new Class<?>[]{String[].class});
                scan.invoke(scanner, new Object[]{packages});
            } catch (Throwable e) {
                // spring 2.0
            }
        }
        this.beanFactory = beanFactory;
    }

    public void destroy() throws Exception {
        for (ServiceConfig<?> serviceConfig : serviceConfigs) {
            try {
                serviceConfig.unexport();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
        for (ReferenceConfig<?> referenceConfig : referenceConfigs.values()) {
            try {
                referenceConfig.destroy();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        Class<?> clazz = getBeanClass(bean);
        Service service = clazz.getAnnotation(Service.class);
        // >>>>>>>>>>>>>>>>>>
        if (service != null) {
            ServiceBean<Object> serviceConfig = new ServiceBean<Object>(service);
            if (void.class.equals(service.interfaceClass()) && "".equals(service.interfaceName())) {
                if (bean.getClass().getInterfaces().length > 0) {
                    serviceConfig.setInterface(bean.getClass().getInterfaces()[0]);
                } else {
                    throw new IllegalStateException("Failed to export remote service class " + bean.getClass().getName()
                            + ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
                }
            }
            if (applicationContext != null) {
                serviceConfig.setApplicationContext(applicationContext);
                if (service.registry() != null && service.registry().length > 0) {
                    List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
                    for (String registryId : service.registry()) {
                        if (registryId != null && registryId.length() > 0) {
                            registryConfigs
                                    .add((RegistryConfig) applicationContext.getBean(registryId, RegistryConfig.class));
                        }
                    }
                    serviceConfig.setRegistries(registryConfigs);
                }
                if (service.provider() != null && service.provider().length() > 0) {
                    serviceConfig.setProvider(
                            (ProviderConfig) applicationContext.getBean(service.provider(), ProviderConfig.class));
                }
                if (service.monitor() != null && service.monitor().length() > 0) {
                    serviceConfig.setMonitor(
                            (MonitorConfig) applicationContext.getBean(service.monitor(), MonitorConfig.class));
                }
                if (service.application() != null && service.application().length() > 0) {
                    serviceConfig.setApplication((ApplicationConfig) applicationContext.getBean(service.application(),
                            ApplicationConfig.class));
                }
                if (service.module() != null && service.module().length() > 0) {
                    serviceConfig
                            .setModule((ModuleConfig) applicationContext.getBean(service.module(), ModuleConfig.class));
                }
                if (service.provider() != null && service.provider().length() > 0) {
                    serviceConfig.setProvider(
                            (ProviderConfig) applicationContext.getBean(service.provider(), ProviderConfig.class));
                } else {

                }
                if (service.protocol() != null && service.protocol().length > 0) {
                    List<ProtocolConfig> protocolConfigs = new ArrayList<ProtocolConfig>();
                    for (String protocolId : service.registry()) {
                        if (protocolId != null && protocolId.length() > 0) {
                            protocolConfigs
                                    .add((ProtocolConfig) applicationContext.getBean(protocolId, ProtocolConfig.class));
                        }
                    }
                    serviceConfig.setProtocols(protocolConfigs);
                }
                try {
                    serviceConfig.afterPropertiesSet();
                } catch (RuntimeException e) {
                    throw (RuntimeException) e;
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            serviceConfig.setRef(bean);
            serviceConfigs.add(serviceConfig);
            serviceConfig.export();
        }
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // logger.debug("扫描bean:"+bean);
        // System.out.println("扫描bean:"+bean);
        if (!isMatchPackage(bean)) {
            return bean;
        }
        Class<?> clazz = getBeanClass(bean);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("set") && method.getParameterTypes().length == 1
                    && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                try {
                    Reference reference = method.getAnnotation(Reference.class);
                    if (reference != null) {
                        Object value = refer(reference, method.getParameterTypes()[0],getMethods(method),buildFactory);
                        if (value != null) {
                            method.invoke(bean, new Object[]{});
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to init remote service reference at method " + name + " in class "
                            + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
                }
            }
        }
        // >>>>>>>>>>>>>>>>>>>>>>>>
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Reference reference = field.getAnnotation(Reference.class);
                if (reference != null) {
                    Object value = refer(reference, field.getType(),getMethods(field),buildFactory);
                    if (value != null) {
                        field.set(bean, value);
                    }
                }
            } catch (Throwable e) {
                logger.error("Failed to init remote service reference at filed " + field.getName() + " in class "
                        + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
                throw new BeanCreationException("Failed to init remote service reference...",e);//增加抛出异常;check=true时，正常启动的bug
            }
        }
        return bean;
    }

    private Object refer(Reference reference, Class<?> referenceClass,com.huatu.springboot.dubbo.annotation.Method[] methodArray,boolean buildFactory) { // method.getParameterTypes()[0]
        String interfaceName;
        if (!"".equals(reference.interfaceName())) {
            interfaceName = reference.interfaceName();
        } else if (!void.class.equals(reference.interfaceClass())) {
            interfaceName = reference.interfaceClass().getName();
        } else if (referenceClass.isInterface()) {
            interfaceName = referenceClass.getName();
        } else {
            throw new IllegalStateException(
                    "The @Reference undefined interfaceClass or interfaceName, and the property type "
                            + referenceClass.getName() + " is not a interface.");
        }

        //注解method配置开始


        String key = reference.group() + "/" + interfaceName + ":" + reference.version();
        ReferenceBean<?> referenceConfig = referenceConfigs.get(key);
        if (referenceConfig == null) {
            referenceConfig = new ReferenceBean<Object>(reference);
            if (void.class.equals(reference.interfaceClass()) && "".equals(reference.interfaceName())
                    && referenceClass.isInterface()) {
                referenceConfig.setInterface(referenceClass);
            }


            if (methodArray != null && methodArray.length > 0) {
                List<MethodConfig> methodConfigList = new ArrayList<>();
                for (int i = 0; i < methodArray.length; i++) {
                    com.huatu.springboot.dubbo.annotation.Method m = methodArray[i];
                    MethodConfig mc = new MethodConfig();
                    mc.setName(m.name());
                    mc.setDeprecated(m.deprecated());
                    mc.setExecutes(m.executes());
                    if (m.retries() >= 0) {
                        mc.setRetries(m.retries());
                    }
                    if (StringUtils.isNotEmpty(m.loadbalance())) {
                        mc.setLoadbalance(m.loadbalance());
                    }
                    if(m.timeout() >= 0){
                        mc.setTimeout(m.timeout());
                    }
                    if(m.async() != reference.async()){
                        mc.setAsync(m.async());
                        mc.setSent(m.sent());
                    }
                    if(StringUtils.isNotEmpty(m.cache())){
                        mc.setCache(m.cache());
                    }
                    mc.setValidation(String.valueOf(m.validation()));
                    mc.setActives(m.actives());
                    mc.setSticky(m.sticky());
                    mc.setReturn(m.isReturn());
                    methodConfigList.add(mc);

                }
                referenceConfig.setMethods(methodConfigList);
            }

            if (applicationContext != null) {
                referenceConfig.setApplicationContext(applicationContext);
                if (reference.registry() != null && reference.registry().length > 0) {
                    List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
                    for (String registryId : reference.registry()) {
                        if (registryId != null && registryId.length() > 0) {
                            registryConfigs.add((RegistryConfig) applicationContext.getBean(registryId, RegistryConfig.class));
                        }
                    }
                    referenceConfig.setRegistries(registryConfigs);
                }
                if (reference.consumer() != null && reference.consumer().length() > 0) {
                    referenceConfig.setConsumer(
                            (ConsumerConfig) applicationContext.getBean(reference.consumer(), ConsumerConfig.class));
                }
                if (reference.monitor() != null && reference.monitor().length() > 0) {
                    referenceConfig.setMonitor(
                            (MonitorConfig) applicationContext.getBean(reference.monitor(), MonitorConfig.class));
                }
                if (reference.application() != null && reference.application().length() > 0) {
                    referenceConfig.setApplication((ApplicationConfig) applicationContext
                            .getBean(reference.application(), ApplicationConfig.class));
                }
                if (reference.module() != null && reference.module().length() > 0) {
                    referenceConfig.setModule(
                            (ModuleConfig) applicationContext.getBean(reference.module(), ModuleConfig.class));
                }
                if (reference.consumer() != null && reference.consumer().length() > 0) {
                    referenceConfig.setConsumer(
                            (ConsumerConfig) applicationContext.getBean(reference.consumer(), ConsumerConfig.class));
                }
                try {
                    referenceConfig.afterPropertiesSet();
                } catch (RuntimeException e) {
                    throw (RuntimeException) e;
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }

            referenceConfigs.putIfAbsent(key, referenceConfig);
            referenceConfig = referenceConfigs.get(key);
            if( buildFactory){
                beanFactory.registerSingleton(key,referenceConfig);
            }
        }
        return referenceConfig.get();
    }


    /**
     * 获取method注解
     * @param accessibleObject
     * @return
     */
    private com.huatu.springboot.dubbo.annotation.Method[] getMethods(AccessibleObject accessibleObject) {
        InterfaceMethods interfaceMethods = accessibleObject.getAnnotation(InterfaceMethods.class);
        com.huatu.springboot.dubbo.annotation.Method[] methodArray = null;
        if (interfaceMethods != null) {
            methodArray = interfaceMethods.methods();
        }
        return methodArray;
    }

    private boolean isMatchPackage(Object bean) {
        if(! enabled){
            return false;
        }
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        Class<?> clazz = getBeanClass(bean);
        String beanClassName = clazz.getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private Class<?> getBeanClass(Object bean) {
        Class<?> clazz = bean.getClass();
        if (AopUtils.isAopProxy(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        }
        return clazz;
    }


    public boolean isEnabled(){
        return this.enabled;
    }


    Map<String,ReferenceBean> getAllReferences(){
        return Collections.unmodifiableMap(referenceConfigs);
    }
}