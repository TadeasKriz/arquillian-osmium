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

import org.arquillian.protocol.ios.impl.IOSProtocol;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class OsmiumExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        // IOS Protocol
        extensionBuilder.service(Protocol.class, IOSProtocol.class);

        extensionBuilder.service(Configurator.class, IOSDriverFactory.class);
        extensionBuilder.service(Instantiator.class, IOSDriverFactory.class);
        extensionBuilder.service(Destructor.class, IOSDriverFactory.class);

        // IOS Container
        extensionBuilder.service(DeployableContainer.class, IOSDeployableContainer.class);

        extensionBuilder.observer(OsmiumLifecycleManager.class);
        extensionBuilder.observer(IOSDeploymentVerifier.class);
    }
}
