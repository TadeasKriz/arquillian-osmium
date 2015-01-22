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
package org.arquillian.osmium.util;

import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.tool.basic.DownloadTool;
import org.arquillian.spacelift.tool.basic.UnzipTool;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OsmiumDeployWrapper {

    private static final Logger LOGGER = Logger.getLogger(OsmiumDeployWrapper.class.getName());

    public OsmiumDeployWrapper(WorkingDirectory workingDirectory) {

        File iosDeployZip = new File(workingDirectory.asFile(), "ios-deploy-master.zip");
        File iosDeployDirectory = new File(workingDirectory.asFile(), "ios-deploy-master");

        LOGGER.log(Level.INFO, "Downloading `ios-deploy.zip` to `{0}` and unzipping to {1}.",
                new String[] { iosDeployZip.getAbsolutePath(), iosDeployDirectory.getAbsolutePath() });

        Tasks.prepare(DownloadTool.class)
                .from("https://github.com/phonegap/ios-deploy/archive/master.zip")
                .to(iosDeployZip)
                .then(UnzipTool.class)
                .toDir(workingDirectory.asFile())
                .execute().await();

        LOGGER.log(Level.INFO, "Download and unzip successful.");

        LOGGER.log(Level.INFO, "Compiling `ios-deploy` from {0}.", iosDeployDirectory.getAbsolutePath());

        Tasks.prepare(CommandTool.class)
                .workingDir(iosDeployDirectory.getAbsolutePath())
                .programName("make")
                .parameters("ios-deploy")
                .interaction(new ProcessInteractionBuilder().when(".*").printToOut().build())
                .execute().await();

        LOGGER.log(Level.INFO, "Compilation successful.");
    }



}