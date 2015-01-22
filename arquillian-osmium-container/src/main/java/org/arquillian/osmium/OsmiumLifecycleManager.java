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
package org.arquillian.osmium;

import org.arquillian.osmium.util.OsmiumBuilder;
import org.arquillian.osmium.util.OsmiumDeployWrapper;
import org.arquillian.osmium.util.WorkingDirectory;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OsmiumLifecycleManager {

    @Inject
    private Instance<Injector> injector;

    @Inject
    @ApplicationScoped
    private InstanceProducer<OsmiumDeployWrapper> deployWrapper;

    @Inject
    @ApplicationScoped
    private InstanceProducer<WorkingDirectory> workingDirectory;

    private void prepareDependencies(@Observes ManagerStarted event) {
        WorkingDirectory workingDirectory = new WorkingDirectory();
        this.workingDirectory.set(workingDirectory);
    }

    private void injectBuilders(@Observes(precedence = 50) BeforeClass event) throws IllegalAccessException {
        TestClass testClass = event.getTestClass();
        Field[] fields = testClass.getJavaClass().getDeclaredFields();

        for (Field field : fields) {
            if(!Modifier.isStatic(field.getModifiers()) || field.getType() != OsmiumBuilder.class) {
                continue;
            }
            OsmiumResource resource = field.getAnnotation(OsmiumResource.class);
            if(resource == null) {
                continue;
            }

            field.setAccessible(true);
            field.set(null, new OsmiumBuilder());
        }
    }

}
