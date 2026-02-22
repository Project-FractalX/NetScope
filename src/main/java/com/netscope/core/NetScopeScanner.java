package com.netscope.core;

import com.netscope.annotation.AuthType;
import com.netscope.annotation.NetworkPublic;
import com.netscope.annotation.NetworkSecured;
import com.netscope.config.NetScopeConfig;
import com.netscope.model.NetworkMethodDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetScopeScanner {

    private static final Logger logger = LoggerFactory.getLogger(NetScopeScanner.class);

    private final ApplicationContext context;
    private final NetScopeConfig config;

    // Canonical cache: "ConcreteClassName.memberName" → definition  (used by GetDocs)
    private final Map<String, NetworkMethodDefinition> cache = new ConcurrentHashMap<>();
    // Alias cache: "InterfaceName.memberName" → same definition  (lookup-only, not in GetDocs)
    private final Map<String, NetworkMethodDefinition> aliasCache = new ConcurrentHashMap<>();
    private volatile boolean scanned = false;

    public NetScopeScanner(ApplicationContext context, NetScopeConfig config) {
        this.context = context;
        this.config  = config;
    }

    public List<NetworkMethodDefinition> scan() {
        if (!scanned) {
            doScan();
        }
        return new ArrayList<>(cache.values());
    }

    public Optional<NetworkMethodDefinition> findMethod(String beanName, String memberName) {
        if (!scanned) doScan();
        String key = beanName + "." + memberName;
        NetworkMethodDefinition def = cache.get(key);
        if (def == null) def = aliasCache.get(key);
        return Optional.ofNullable(def);
    }

    private synchronized void doScan() {
        if (scanned) return;

        logger.info("NetScope: scanning for @NetworkPublic and @NetworkSecured members...");
        int count = 0;

        for (String beanName : context.getBeanDefinitionNames()) {
            Object bean;
            try {
                bean = context.getBean(beanName);
            } catch (Exception e) {
                continue;
            }

            Class<?> clazz = getTargetClass(bean);

            // ── Scan METHODS (including inherited) ───────────────────────────
            for (Method method : getAllMethods(clazz)) {
                NetworkMethodDefinition def = null;

                NetworkPublic pub = method.getAnnotation(NetworkPublic.class);
                if (pub != null) {
                    def = new NetworkMethodDefinition(bean, method, false, null,
                            pub.description());
                }

                NetworkSecured sec = method.getAnnotation(NetworkSecured.class);
                if (sec != null) {
                    def = new NetworkMethodDefinition(bean, method, true, sec.auth(),
                            sec.description());
                }

                if (def != null) {
                    String key = def.getBeanName() + "." + def.getMethodName();
                    // putIfAbsent: subclass member (processed first) always wins over superclass
                    if (cache.putIfAbsent(key, def) == null) {
                        logger.info("  [method] {}.{} → {} | auth={} | static={} | final={}",
                                def.getBeanName(), def.getMethodName(),
                                def.isSecured() ? "SECURED" : "PUBLIC",
                                def.getAuthType(),
                                def.isStatic(), def.isFinal());
                        count++;
                    }
                }
            }

            // ── Scan FIELDS (including inherited) ────────────────────────────
            for (Field field : getAllFields(clazz)) {
                NetworkMethodDefinition def = null;

                NetworkPublic pub = field.getAnnotation(NetworkPublic.class);
                if (pub != null) {
                    field.setAccessible(true);
                    def = new NetworkMethodDefinition(bean, field, false, null,
                            pub.description());
                }

                NetworkSecured sec = field.getAnnotation(NetworkSecured.class);
                if (sec != null) {
                    field.setAccessible(true);
                    def = new NetworkMethodDefinition(bean, field, true, sec.auth(),
                            sec.description());
                }

                if (def != null) {
                    String key = def.getBeanName() + "." + def.getMethodName();
                    // putIfAbsent: subclass member (processed first) always wins over superclass
                    if (cache.putIfAbsent(key, def) == null) {
                        logger.info("  [field]  {}.{} → {} | auth={} | static={} | final={} | writeable={}",
                                def.getBeanName(), def.getMethodName(),
                                def.isSecured() ? "SECURED" : "PUBLIC",
                                def.getAuthType(),
                                def.isStatic(), def.isFinal(), def.isWriteable());
                        count++;
                    }
                }
            }
            // ── Register interface aliases (lookup-only, not in GetDocs) ─────
            String concreteName = clazz.getSimpleName();
            for (Class<?> iface : collectInterfaces(clazz)) {
                if (!isUserInterface(iface)) continue;
                String ifaceName = iface.getSimpleName();
                if (ifaceName.equals(concreteName)) continue;

                int aliasCount = 0;
                for (Map.Entry<String, NetworkMethodDefinition> entry : cache.entrySet()) {
                    String cacheKey = entry.getKey();
                    if (cacheKey.startsWith(concreteName + ".")) {
                        String memberName = cacheKey.substring(concreteName.length() + 1);
                        if (aliasCache.putIfAbsent(ifaceName + "." + memberName, entry.getValue()) == null) {
                            aliasCount++;
                        }
                    }
                }
                if (aliasCount > 0) {
                    logger.info("  [alias]  {} → {} ({} member(s))", ifaceName, concreteName, aliasCount);
                }
            }
        }

        scanned = true;
        logger.info("NetScope: scan complete — {} member(s) registered", count);
    }

    /**
     * Collects all methods from:
     *   1. The class hierarchy (subclass → superclass), then
     *   2. All reachable interfaces (depth-first, deduplicated).
     * Order ensures putIfAbsent lets the most-specific declaration win:
     *   concrete class > abstract superclass > interface default.
     */
    private List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();

        // 1. Class hierarchy
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }

        // 2. Interfaces (all reachable, deduplicated)
        for (Class<?> iface : collectInterfaces(clazz)) {
            methods.addAll(Arrays.asList(iface.getDeclaredMethods()));
        }

        return methods;
    }

    /**
     * Returns all interfaces reachable from clazz (via implements and extends),
     * deduplicated and in depth-first order.
     */
    private Set<Class<?>> collectInterfaces(Class<?> clazz) {
        Set<Class<?>> visited = new LinkedHashSet<>();
        collectInterfaces(clazz, visited);
        return visited;
    }

    private void collectInterfaces(Class<?> clazz, Set<Class<?>> visited) {
        if (clazz == null || clazz == Object.class) return;
        for (Class<?> iface : clazz.getInterfaces()) {
            if (visited.add(iface)) {
                collectInterfaces(iface, visited);  // interface can extend interfaces
            }
        }
        collectInterfaces(clazz.getSuperclass(), visited);
    }

    /**
     * Collects all fields declared on clazz and its superclasses,
     * stopping at Object. Subclass fields come first so that putIfAbsent
     * lets the subclass shadow take precedence over the superclass declaration.
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Returns true for user-defined interfaces — excludes java.*, javax.*,
     * jakarta.*, org.springframework.*, and other JVM/framework internals.
     */
    private boolean isUserInterface(Class<?> iface) {
        String pkg = iface.getPackageName();
        return !pkg.startsWith("java.")
            && !pkg.startsWith("javax.")
            && !pkg.startsWith("jakarta.")
            && !pkg.startsWith("org.springframework.")
            && !pkg.startsWith("com.sun.")
            && !pkg.startsWith("sun.");
    }

    /** Unwrap Spring proxies (both CGLIB and JDK dynamic) to get the real class */
    private Class<?> getTargetClass(Object bean) {
        return AopUtils.getTargetClass(bean);
    }
}
