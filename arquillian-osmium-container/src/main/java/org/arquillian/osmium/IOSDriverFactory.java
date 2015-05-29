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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.jboss.arquillian.drone.spi.DronePointFilter;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.uiautomation.ios.IOSCapabilities;

import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOSDriverFactory implements
        Configurator<IOSDriver, WebDriverConfiguration>,
        Instantiator<IOSDriver, WebDriverConfiguration>,
        Destructor<IOSDriver> {

    private static final Logger LOGGER = Logger.getLogger(IOSDriverFactory.class.getSimpleName());

    @Inject
    private Instance<DroneContext> droneContext;

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, DronePoint<IOSDriver> point) {
        WebDriverConfiguration configuration = new WebDriverConfiguration(new IOSBrowserCapabilities());
        configuration.configure(descriptor, point.getQualifier());
        return configuration;
    }

    @Override
    public void destroyInstance(IOSDriver iosDriver) {
        iosDriver.quit();
    }

    @Override
    public IOSDriver createInstance(final WebDriverConfiguration configuration) {
        URL remoteAddress = configuration.getRemoteAddress();

        if (remoteAddress == null || remoteAddress.toString().isEmpty()) {
            remoteAddress = WebDriverConfiguration.DEFAULT_REMOTE_URL;
        }

        DronePoint<WebDriver> dronePoint = droneContext.get().find(WebDriver.class).filter(
                new DronePointFilter<WebDriver>() {
                    @Override
                    public boolean accepts(DroneContext droneContext, DronePoint<? extends WebDriver> dronePoint) {
                        try {
                            DronePointContext<? extends WebDriver> pointContext = droneContext.get(dronePoint);
                            if (!pointContext.hasConfiguration()) {
                                return false;
                            }

                            return pointContext.getConfigurationAs(WebDriverConfiguration.class).equals(configuration);
                        } catch (ClassCastException e) {
                            return false;
                        }
                    }
                }).single();

        Capabilities capabilities = configuration.getCapabilities();
        Map<String, ?> capabilityMap = capabilities.asMap();

        DronePointContext<WebDriver> pointContext = droneContext.get().get(dronePoint);
        String bundleName = pointContext.getMetadata(BundleNameKey.class);
        if (bundleName == null) {
            LOGGER.log(Level.INFO, "There was no deployment bundle name specified, using `Safari` for safari tests.");
            bundleName = "Safari";
        }

        Boolean useSimulator = pointContext.getMetadata(UseSimulatorKey.class);
        if (useSimulator == null) {
            LOGGER.log(Level.INFO, "The deployment did not specify whether simulator or a device should be used. " +
                    "Using preferred value from webdriver configuration.");

            if (capabilityMap.containsKey(IOSCapabilities.SIMULATOR)) {
                useSimulator = (Boolean) capabilityMap.get(IOSCapabilities.SIMULATOR);
            } else {
                LOGGER.log(Level.INFO, "Using simulator because it was not overridden in configuration with `" +
                        IOSCapabilities.SIMULATOR + "`.");
                useSimulator = true;
            }
        }

        IOSCapabilities iosCapabilities = IOSCapabilities.iphone(bundleName);
        iosCapabilities.setCapability(IOSCapabilities.SIMULATOR, useSimulator);

        if (capabilityMap.containsKey(IOSCapabilities.UI_SDK_VERSION)) {
            iosCapabilities.setSDKVersion((String) capabilityMap.get(IOSCapabilities.UI_SDK_VERSION));
        }
        if (capabilityMap.containsKey(IOSCapabilities.UUID)) {
            iosCapabilities.setDeviceUUID((String) capabilityMap.get(IOSCapabilities.UUID));
        }
        return new IOSDriverImpl(remoteAddress, iosCapabilities);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
