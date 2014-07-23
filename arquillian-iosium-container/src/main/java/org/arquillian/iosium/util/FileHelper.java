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
package org.arquillian.iosium.util;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.FileAsset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class FileHelper {
    private static final Logger LOGGER = Logger.getLogger(FileHelper.class.getName());
    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static File prepareWorkingDirectory() {
        return prepareWorkingDirectory(TEMP_DIRECTORY);
    }

    public static File prepareWorkingDirectory(String parent) {
        return prepareWorkingDirectory(new File(parent));
    }

    public static File prepareWorkingDirectory(File parent) {
        String randomDirectoryName = UUID.randomUUID().toString();

        File workingDirectory = new File(parent, randomDirectoryName);

        if (workingDirectory.exists()) {
            if (!workingDirectory.delete()) {
                throw new IllegalStateException(
                        "Couldn't delete existing working directory: " + workingDirectory.getAbsolutePath());
            }
        }

        if (!workingDirectory.mkdirs()) {
            throw new IllegalStateException("Couldn't create working directory: " + workingDirectory.getAbsolutePath());
        }

        LOGGER.info("Created working directory " + workingDirectory.getAbsolutePath());
        return workingDirectory;
    }
}