package com.litongjava.jfinal.dubbo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.litongjava.jfinal.aop.AopManager;

public class Dubbo {

  private static Map<Class<?>, Object> dubboCache = new ConcurrentHashMap<>();

  private static Map<Class<?>, ReferenceConfig<?>> referenceMap = new ConcurrentHashMap<>();

  private static ApplicationConfig applicationConfig = null;
  private static RegistryConfig registryConfig = null;

  public static void setApplication(ApplicationConfig applicationConfig) {
    Dubbo.applicationConfig = applicationConfig;
  }

  public static void setRegistry(RegistryConfig registryConfig) {
    Dubbo.registryConfig = registryConfig;
  }

  public static ApplicationConfig getApplicationConfig() {
    return applicationConfig;
  }

  public static RegistryConfig getRegistryConfig() {
    return registryConfig;
  }

  @SuppressWarnings("unchecked")
  public static <T> ReferenceConfig<T> getReference(Class<T> targetClass) {
    return (ReferenceConfig<T>) referenceMap.get(targetClass);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> targetClass) {
    Object ret = dubboCache.get(targetClass);
    if (ret != null) {
      return (T) ret;
    }

    ReferenceConfig<T> reference = new ReferenceConfig<>();
    reference.setInterface(targetClass);
    reference.setApplication(applicationConfig);
    reference.setRegistry(registryConfig);

    referenceMap.put(targetClass, reference);

    synchronized (Dubbo.class) {
      ret = dubboCache.get(targetClass);
      if (ret != null) {
        return (T) ret;
      }

      T result = reference.get();
      if (result != null) {
        // 添加到aop容器
        AopManager.me().addSingletonObject(targetClass, result);
        // 添加到cache
        dubboCache.put(targetClass, result);

      }
      return result;
    }

  }

  public static void clear() {
    referenceMap.clear();
    dubboCache.clear();
  }

}
