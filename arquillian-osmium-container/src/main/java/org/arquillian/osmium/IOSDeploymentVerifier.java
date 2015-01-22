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

import org.arquillian.osmium.util.NetworkHelper;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.FilterableResult;
import org.jboss.arquillian.drone.spi.filter.DeploymentFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class IOSDeploymentVerifier {

    private static final Logger LOGGER = Logger.getLogger(IOSDeploymentVerifier.class.getCanonicalName());

    @Inject
    @DeploymentScoped
    private Instance<ProtocolMetaData> protocolMetaDataInstance;

    @Inject
    private Instance<DroneContext> droneContextInstance;

    public void afterDeployment(@Observes AfterDeploy event) {
        if (event.getDeployableContainer() instanceof IOSDeployableContainer) {
            ProtocolMetaData metaData = protocolMetaDataInstance.get();

            Collection<IOSContainerContext> contexts = metaData.getContexts(IOSContainerContext.class);

            // In case we somehow deployed more than one?
            for (IOSContainerContext context : contexts) {
                ensureIOSArchiveDeployed(event.getDeployment(), context);
            }
        }
    }

    private void ensureIOSArchiveDeployed(DeploymentDescription deployment, IOSContainerContext context) {
        String bundleName = null;
        final int maxRetries = 100;
        final long waitTime = 100;

        for (int i = 0; i < maxRetries && bundleName == null; i++) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bundleName = getBundleName(context.getDeploymentTarget().getAbsolutePath());

            LOGGER.log(Level.INFO, "Bundle name: {0}", bundleName);
        }

        if (bundleName == null) {
            String message = String.format("Could not deploy %s or the package has no bundle name! IOS packages has " +
                    "to have bundle name you know, they deserve it!", deployment.getArchive().getName());

            throw new IllegalStateException(message);
        }

        DroneContext droneContext = droneContextInstance.get();

        FilterableResult<WebDriver> result = droneContext.find(WebDriver.class).filter(new DeploymentFilter
                (deployment.getName()));

        for (DronePoint<WebDriver> dronePoint : result) {
            droneContext.get(dronePoint).setMetadata(BundleNameKey.class, bundleName);
            droneContext.get(dronePoint).setMetadata(UseSimulatorKey.class, context.isSimulatorArchive());
        }
    }

    private String getBundleName(String packageLocation) {
        JSONObject responseJson;
        try {
            responseJson = NetworkHelper.getJSON("http://localhost:4444/wd/hub/status");
        } catch (IOException e) {
            throw new RuntimeException("Could not get response from /wd/hub/status!", e);
        } catch (JSONException e) {
            throw new RuntimeException("Response from /wd/hub/status was malformed!", e);
        }

        JSONObject value = responseJson.optJSONObject("value");

        JSONArray supportedApps = value.optJSONArray("supportedApps");

        for (int i = 0; i < supportedApps.length(); i++) {
            JSONObject supportedApp = supportedApps.optJSONObject(i);

            if (!supportedApp.has("applicationPath")) {
                continue;
            }

            String applicationPath = supportedApp.optString("applicationPath");
            if (applicationPath.startsWith(packageLocation)) {
                return supportedApp.optString("CFBundleName");
            }
        }

        return null;
    }

}
