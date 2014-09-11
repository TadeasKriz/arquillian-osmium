/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.osmium.lazydeployment;

import org.jboss.shrinkwrap.api.Archive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LazyDeploymentProxy implements InvocationHandler {
    /**
     * This will be given to the {@link Proxy#newProxyInstance(ClassLoader, Class[],
     * InvocationHandler)} to create the archive proxy which will be used to redirect the {@link Archive#getName()}
     * call.
     */
    private static final Class<?>[] proxyInterfaces = { Archive.class };

    private final String archiveName;
    private final Method lazyDeploymentMethod;
    private Archive<?> cachedArchive;

    public LazyDeploymentProxy(String archiveName, Method lazyDeploymentMethod) {
        this.archiveName = archiveName;
        this.lazyDeploymentMethod = lazyDeploymentMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getName")) {
            return archiveName;
        }

        if (cachedArchive == null) {
            cachedArchive = Archive.class.cast(lazyDeploymentMethod.invoke(null));
        }

        return method.invoke(cachedArchive, args);
    }

    public static Archive<?> createProxiedArchive(String archiveName, Method lazyDeploymentMethod) {
        LazyDeploymentProxy handler = new LazyDeploymentProxy(archiveName, lazyDeploymentMethod);

        return (Archive<?>) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), proxyInterfaces, handler);
    }
}
