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
import org.arquillian.spacelift.process.ProcessInteraction;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class IPABuilder {

    private File sourceDirectory;
    private String projectName;
    private String targetSdkVersion = "8.0";
    private String targetSdkName = "iphoneos";
    private String configurationName = "Release";
    private String schemeName;
    private WorkingDirectory workingDirectory;
    private String developerName = Settings.DEVELOPER_NAME.getValue();
    private File provisioningProfile = Settings.PROVISIONING_PROFILE.getValue();

    private OnBeforeBuildListener onBeforeBuildListener;

    public static IPABuilder prepare() {
        return new IPABuilder();
    }

    public File sourceDirectory() {
        return sourceDirectory;
    }

    public IPABuilder sourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        return this;
    }

    public IPABuilder sourceDirectory(String sourceDirectoryPath) {
        return sourceDirectory(new File(sourceDirectoryPath));
    }

    public String projectName() {
        return projectName;
    }

    public IPABuilder projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String targetSdkVersion() {
        return targetSdkVersion;
    }

    public IPABuilder targetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
        return this;
    }

    public String targetSdkName() {
        return targetSdkName;
    }

    public IPABuilder targetSdkName(String targetSdkName) {
        this.targetSdkName = targetSdkName;
        return this;
    }

    public String configurationName() {
        return configurationName;
    }

    public IPABuilder configurationName(String configurationName) {
        this.configurationName = configurationName;
        return this;
    }

    public String schemeName() {
        return schemeName;
    }

    public IPABuilder schemeName(String schemeName) {
        this.schemeName = schemeName;
        return this;
    }

    public WorkingDirectory workingDirectory() {
        return workingDirectory;
    }

    public IPABuilder workingDirectory(WorkingDirectory workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public IPABuilder workingDirectory(String absolutePath) {
        this.workingDirectory = new WorkingDirectory(absolutePath);
        return this;
    }
    public String developerName() {
        return developerName;
    }

    public IPABuilder developerName(String developerName) {
        this.developerName = developerName;
        return this;
    }

    public File provisioningProfile() {
        return provisioningProfile;
    }

    public IPABuilder provisioningProfile(@Nonnull File provisioningProfile) {
        this.provisioningProfile = provisioningProfile;
        return this;
    }

    public IPABuilder provisioningProfile(@Nonnull String provisioningProfilePath) {
        return provisioningProfile(new File(provisioningProfilePath));
    }

    public OnBeforeBuildListener onBeforeBuildListener() {
        return onBeforeBuildListener;
    }

    public IPABuilder onBeforeBuildListener(OnBeforeBuildListener onBeforeBuildListener) {
        this.onBeforeBuildListener = onBeforeBuildListener;
        return this;
    }

    public File build() throws IOException {
        if(schemeName == null) {
            schemeName = projectName;
        }
        if(workingDirectory == null) {
            workingDirectory = new WorkingDirectory();
        }

        File sourceDirectory = workingDirectory.prepareSubdirectory("source");
        File buildDirectory = workingDirectory.prepareSubdirectory("build");

        String workingDirectoryPath = sourceDirectory.getAbsolutePath();
        String buildDirectoryPath = buildDirectory.getAbsolutePath();

        FileHelper.copy(this.sourceDirectory, sourceDirectory);

        /*List<File> mainFiles = FileHelper.findFiles(workingDirectory, "main.m");
        if(mainFiles.size() != 1) {
            throw new IllegalStateException("Project has to have exactly one main.m file!");
        }

        String accessibilityFixes = FileHelper.readResource(getClass(), "/IOSAccessibilityFixes.m");
        FileHelper.appendTextToFile(mainFiles.get(0), accessibilityFixes);
        */
        if(onBeforeBuildListener != null) {
            onBeforeBuildListener.onBeforeBuild(this, sourceDirectory, buildDirectory);
        }

        Tasks.prepare(CommandTool.class)
                .workingDir(workingDirectoryPath)
                .programName("xcodebuild")
                .parameter("clean")
                .execute().await();

        Tasks.prepare(CommandTool.class)
                .workingDir(workingDirectoryPath)
                .programName("xcodebuild")
                .addEnvironment()
                .parameters("-target", projectName)
                .parameters("-sdk", targetSdkName)
                .parameters("-configuration", configurationName)
                .parameters("-derivedDataPath", buildDirectoryPath)
                .parameters("-scheme", schemeName)
                .parameters("CODE_SIGN_RESOURCE_RULES_PATH=$(SDKROOT)/ResourceRules.plist")
                .interaction(new ProcessInteractionBuilder().when(".*").printToOut().outputPrefix("(xcodebuild) "))
                .execute().await();

        Tasks.prepare(CommandTool.class)
                .workingDir(workingDirectoryPath)
                .programName("xcrun")
                .parameters("-sdk", targetSdkName)
                .parameters("PackageApplication")
                .parameters("-v", buildDirectory + "/Build/Products/" + configurationName + "-" + targetSdkName + "/" + projectName + ".app")
                .parameters("-o", workingDirectory.asFile() + "/" + projectName + ".ipa")
                .parameters("--sign", developerName)
                .parameters("--embed", provisioningProfile.getAbsolutePath())
                .interaction(new ProcessInteractionBuilder().when(".*").printToOut().outputPrefix("(xcrun) "))
                .execute().await();

        return new File(workingDirectory.asFile(), projectName + ".ipa");
    }

    public JavaArchive buildArchive() throws IOException {
        return ShrinkWrap.create(ZipImporter.class, projectName + ".ipa")
                .importFrom(build())
                .as(JavaArchive.class);
    }

    public interface OnBeforeBuildListener {

        void onBeforeBuild(IPABuilder builder, File workingDirectory, File buildDirectory) throws IOException;

    }
}