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
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

import java.util.logging.Logger;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class IOSContainerConfiguration implements ContainerConfiguration {
    private static final Logger logger = Logger.getLogger(IOSContainerConfiguration.class.getName());

    private boolean betaFeatures = true;

    private String serverHost = "localhost";

    private int port = 4444;

    private String workingDirectory;

    public boolean isBetaFeatures() {
        return betaFeatures;
    }

    public void setBetaFeatures(boolean betaFeatures) {
        this.betaFeatures = betaFeatures;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }


    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void validate() throws ConfigurationException {
        if (workingDirectory == null) {
            workingDirectory = FileHelper.createTempDirectory().getAbsolutePath();
        }


    }
}
