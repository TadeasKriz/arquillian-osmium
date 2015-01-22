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

import java.io.File;

/**
 * Utility class to have everything related to osmium inside one working directory.
 */
public class WorkingDirectory {

    private final File workingDirectory;

    public WorkingDirectory() {
        this(FileHelper.createTempDirectory());
    }

    public WorkingDirectory(String absolutePath) {
        this(new File(absolutePath));
    }

    public WorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public File asFile() {
        return workingDirectory;
    }

    public String asAbsolutePath() {
        return asFile().getAbsolutePath();
    }

    public WorkingDirectory getDeploymentDirectory() {
        return new WorkingDirectory(prepareSubdirectory("deployments"));
    }

    public File prepareSubdirectory(String subdirectoryName) {
        File subdirectory = new File(workingDirectory, subdirectoryName);

        if(subdirectory.exists()) {
            return subdirectory;
        }

        if (subdirectory.mkdirs()) {
            return subdirectory;
        } else {
            throw new IllegalStateException(
                    String.format("Could not create subdirectory `%s` in working directory `%s`",
                            subdirectoryName, workingDirectory.getAbsolutePath()));
        }
    }

}
