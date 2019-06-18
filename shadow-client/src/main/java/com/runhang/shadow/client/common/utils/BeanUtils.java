package com.runhang.shadow.client.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * ClassName BeanUtils
 * Description spring bean管理工具集合
 * Author szh
 * Date 2018-11-03 22:05
 **/
@Slf4j
@Component
public class BeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeanUtils.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取bean
     *
     * @param name bean name
     * @return bean
     */
    public static Object getBean(String name) {
        try {
            return getApplicationContext().getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("容器中不存在bean：" + name);
            return null;
        }
    }

    /**
     * 通过class获取bean
     *
     * @param clazz bean类
     * @return bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name和class获取bean
     * @param name bean name
     * @param clazz bean类
     * @return bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 根据obj的类型，创建一个新的bean，添加到Spring容器中
     *
     * @param bean 注入bean类型
     * @param beanName bean name
     */
    public static void injectNewBean(Object bean, String beanName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
        BeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(bean.getClass().getName());
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 注入已有的bean到spring容器
     *
     * @param bean 注入的bean
     * @param beanName bean name
     */
    public static void injectExistBean(Object bean, String beanName) {
        try {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
            beanFactory.applyBeanPostProcessorsAfterInitialization(bean, beanName);
            beanFactory.registerSingleton(beanName, bean);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * @Description 删除容器中的bean
     * @param beanName bean name
     * @author szh
     * @Date 2019/6/18 0:06
     */
    public static void destroyBean(String beanName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
        beanFactory.destroySingleton(beanName);
    }

}
