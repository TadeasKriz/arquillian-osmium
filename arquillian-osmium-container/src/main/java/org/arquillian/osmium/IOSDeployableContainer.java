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
package org.arquillian.osmium;

import org.arquillian.osmium.util.FileHelper;
import org.arquillian.osmium.util.IOSServerConfigurationWithMonitorDirectorySetter;
import org.arquillian.osmium.util.OsmiumDeployWrapper;
import org.arquillian.osmium.util.WorkingDirectory;
import org.arquillian.protocol.ios.impl.IOSProtocol;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.tool.basic.DownloadTool;
import org.arquillian.spacelift.tool.basic.UnzipTool;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.uiautomation.ios.IOSServer;
import org.uiautomation.ios.IOSServerConfiguration;

import java.io.File;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class IOSDeployableContainer implements DeployableContainer<IOSContainerConfiguration> {

    @Inject
    @SuiteScoped
    private InstanceProducer<IOSServer> server;

    @Inject
    @SuiteScoped
    private InstanceProducer<WorkingDirectory> workingDirectory;

    @Inject
    @SuiteScoped
    private InstanceProducer<IOSContainerConfiguration> configuration;

    @Inject
    private Instance<DroneContext> droneContextInstance;

    private IOSProtocol protocol;

    @Override
    public void setup(IOSContainerConfiguration configuration) {
        this.configuration.set(configuration);
        WorkingDirectory workingDirectory = new WorkingDirectory(configuration.getWorkingDirectory());
        this.workingDirectory.set(workingDirectory);

        File deploymentDirectory = workingDirectory.getDeploymentDirectory().asFile();
        protocol = new IOSProtocol();

        IOSServerConfigurationWithMonitorDirectorySetter serverConfiguration = new
                IOSServerConfigurationWithMonitorDirectorySetter();

        serverConfiguration.setAppFolderToMonitorFile(deploymentDirectory);
        serverConfiguration.setBeta(configuration.isBetaFeatures());
        serverConfiguration.setPort(configuration.getPort());

        IOSServer iosServer = new IOSServer(serverConfiguration);

        server.set(iosServer);

        OsmiumDeployWrapper wrapper = new OsmiumDeployWrapper(workingDirectory);

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
        WorkingDirectory workingDirectory = this.workingDirectory.get();
        ProtocolMetaData metadata = new ProtocolMetaData();
        boolean isSimulatorArchive = archive.getName().toLowerCase().endsWith(".app");
        File appOrIpaFile = new File(workingDirectory.getDeploymentDirectory().asFile(), archive.getName());

        if (!isSimulatorArchive) {


            archive.as(ZipExporter.class).exportTo(appOrIpaFile, true);

            File unzippedIPA = archive.as(ExplodedExporter.class).exportExploded(appOrIpaFile.getParentFile(),
                    appOrIpaFile.getName() + ".unzipped");

            File appFile = FileHelper.findSingleFile(unzippedIPA, "\\.app$");
            File iosDeployZip = new File(workingDirectory.asFile(), "ios-deploy.zip");
            File iosDeployDirectory = new File(workingDirectory.asFile(), "ios-deploy-master");


            Tasks.prepare(DownloadTool.class)
                    .from("https://github.com/phonegap/ios-deploy/archive/master.zip")
                    .to(iosDeployZip)
                    .then(UnzipTool.class)
                    .toDir(workingDirectory.asFile())
                    .execute().await();
            System.out.println("Downloaded and unzipped ios-deploy.");

            Tasks.prepare(CommandTool.class)
                    .workingDirectory(iosDeployDirectory)
                    .programName("make")
                    .parameters("ios-deploy")
                    .interaction(new ProcessInteractionBuilder().when(".*").printToOut().build())
                    .execute().await();
            Tasks.prepare(CommandTool.class)
                    .workingDirectory(iosDeployDirectory)
                    .programName("./ios-deploy")
                    .parameters("-r", "-b", appFile.getAbsolutePath())
                    .interaction(new ProcessInteractionBuilder().when(".*").printToOut().build())
                    .execute().await();
        } else {
            File unzippedIPA = archive.as(ExplodedExporter.class).exportExploded(appOrIpaFile.getParentFile());
        }

        metadata.addContext(new IOSContainerContext(appOrIpaFile, isSimulatorArchive));

        return metadata;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        // FIXME can we uninstall the app first?
        workingDirectory.get().getDeploymentDirectory().asFile().delete();
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Deployment of descriptors is not supported!");
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Deployment of descriptors is not supported!");
    }

    @Override
    public Class<IOSContainerConfiguration> getConfigurationClass() {
        return IOSContainerConfiguration.class;
    }

}
