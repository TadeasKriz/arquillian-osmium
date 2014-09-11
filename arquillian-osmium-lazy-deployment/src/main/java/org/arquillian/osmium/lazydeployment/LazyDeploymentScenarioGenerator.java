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

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LazyDeploymentScenarioGenerator extends AnnotationDeploymentScenarioGenerator {

    @Override
    public List<DeploymentDescription> generate(TestClass testClass) {
        List<DeploymentDescription> deployments = super.generate(testClass);

        Method[] lazyDeploymentMethods = testClass.getMethods(LazyDeployment.class);

        for (Method lazyDeploymentMethod : lazyDeploymentMethods) {
            validate(lazyDeploymentMethod);
            deployments.add(generateLazyDeployment(lazyDeploymentMethod));
        }

        sortByDeploymentOrder(deployments);

        return deployments;
    }

    private void validate(Method deploymentMethod) {
        if (!Modifier.isStatic(deploymentMethod.getModifiers())) {
            throw new IllegalArgumentException("Method annotated with " + LazyDeployment.class.getName() +
                    " is not static. " + deploymentMethod);
        }
        if (!Archive.class.isAssignableFrom(deploymentMethod.getReturnType())) {
            throw new IllegalArgumentException("Method annotated with " + LazyDeployment.class.getName() +
                    " must have return type " + Archive.class.getName() + ". " + deploymentMethod);
        }
        if (deploymentMethod.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Method annotated with " + LazyDeployment.class.getName() +
                    " can not accept parameters. " + deploymentMethod);
        }
    }

    private DeploymentDescription generateLazyDeployment(Method lazyDeploymentMethod) {
        TargetDescription target = generateTarget(lazyDeploymentMethod);
        ProtocolDescription protocol = generateProtocol(lazyDeploymentMethod);

        LazyDeployment lazyDeploymentAnnotation = lazyDeploymentMethod.getAnnotation(LazyDeployment.class);
        DeploymentDescription lazyDeployment = new DeploymentDescription(lazyDeploymentAnnotation.name(),
                LazyDeploymentProxy.createProxiedArchive(lazyDeploymentAnnotation.archiveName(), lazyDeploymentMethod));

        lazyDeployment.shouldBeManaged(lazyDeploymentAnnotation.managed());
        lazyDeployment.shouldBeTestable(lazyDeploymentAnnotation.testable());
        lazyDeployment.setOrder(lazyDeploymentAnnotation.order());
        if (target != null) {
            lazyDeployment.setTarget(target);
        }
        if (protocol != null) {
            lazyDeployment.setProtocol(protocol);
        }
        if (lazyDeploymentMethod.isAnnotationPresent(ShouldThrowException.class)) {
            lazyDeployment.setExpectedException(lazyDeploymentMethod.getAnnotation(ShouldThrowException.class).value());
            lazyDeployment.shouldBeTestable(false);
        }
        return lazyDeployment;
    }

    private TargetDescription generateTarget(Method lazyDeploymentMethod) {
        if (lazyDeploymentMethod.isAnnotationPresent(TargetsContainer.class)) {
            return new TargetDescription(lazyDeploymentMethod.getAnnotation(TargetsContainer.class).value());
        }
        return TargetDescription.DEFAULT;
    }

    private ProtocolDescription generateProtocol(Method deploymentMethod) {
        if (deploymentMethod.isAnnotationPresent(OverProtocol.class)) {
            return new ProtocolDescription(deploymentMethod.getAnnotation(OverProtocol.class).value());
        }
        return ProtocolDescription.DEFAULT;
    }

    private void sortByDeploymentOrder(List<DeploymentDescription> deploymentDescriptions) {
        // sort them by order
        Collections.sort(deploymentDescriptions, new Comparator<DeploymentDescription>() {
            public int compare(DeploymentDescription d1, DeploymentDescription d2) {
                return new Integer(d1.getOrder()).compareTo(d2.getOrder());
            }
        });
    }

}

