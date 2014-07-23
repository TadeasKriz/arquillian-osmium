/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.arquillian.iosium;

import org.arquillian.iosium.util.IOSServerConfigurationWithMonitorDirectorySetter;
import org.arquillian.protocol.ios.impl.IOSProtocol;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.uiautomation.ios.IOSServer;

import java.io.File;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class IOSDeployableContainer implements DeployableContainer<IOSContainerConfiguration> {

    @Inject
    @SuiteScoped
    private InstanceProducer<IOSServer> server;

    private IOSProtocol protocol;
    private IOSContainerConfiguration configuration;
    private File deploymentDirectory;

    @Override
    public void setup(IOSContainerConfiguration iosContainerConfiguration) {
        configuration = iosContainerConfiguration;
        deploymentDirectory = new File(configuration.getWorkingDirectory() + "/deployments");
        deploymentDirectory.mkdirs();
        protocol = new IOSProtocol();

        IOSServerConfigurationWithMonitorDirectorySetter serverConfiguration = new
                IOSServerConfigurationWithMonitorDirectorySetter();

        serverConfiguration.setAppFolderToMonitorFile(deploymentDirectory);

        IOSServer iosServer = new IOSServer(serverConfiguration);

        server.set(iosServer);
    }

    @Override
    public void start() throws LifecycleException {
        try {
            server.get().start();
        } catch (Exception e) {
            throw new LifecycleException("Couldn't start IOSServer!", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            server.get().stop();
        } catch (Exception e) {
            throw new LifecycleException("Couldn't stop IOSServer!", e);
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return protocol.getDescription();
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        File tempAppFile = new File(deploymentDirectory, archive.getName());
        boolean isApp = archive.getName().toLowerCase().endsWith(".app");

        if (isApp) {
            archive.as(ExplodedExporter.class).exportExploded(tempAppFile.getParentFile());
        } else {
            archive.as(ZipExporter.class).exportTo(tempAppFile, true);
        }

        //APPIOSApplication application = APPIOSApplication.createFrom(tempAppFile);

        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        File tempAppFile = new File(deploymentDirectory, archive.getName());

        //ApplicationStore applicationStore = server.get().getDriver().getApplicationStore();

        //APPIOSApplication application = applicationStore.getApplication(tempAppFile.getAbsolutePath());

        //applicationStore.getApplications().remove(application);

        tempAppFile.delete();
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {

    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {

    }

    @Override
    public Class<IOSContainerConfiguration> getConfigurationClass() {
        return IOSContainerConfiguration.class;
    }
}
