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
package org.arquillian.osmium.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class FileHelper {
    private static final Logger LOGGER = Logger.getLogger(FileHelper.class.getName());
    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    private static final int TEMP_CREATION_TRIES = 1000;


    public static File createTempDirectory() throws IllegalStateException {
        for (int i = 0; i < TEMP_CREATION_TRIES; i++) {
            File workingDirectory = new File(TEMP_DIRECTORY, "osmium-" + UUID.randomUUID().toString());

            if (workingDirectory.mkdir()) {
                LOGGER.info("Created temp directory at " + workingDirectory.getAbsolutePath());
                return workingDirectory;
            }
        }

        throw new IllegalStateException("Could not create temp directory. No of retries: " + TEMP_CREATION_TRIES);
    }

    public static void copy(File source, File target) throws IOException {
        if (!source.exists()) {
            throw new IllegalArgumentException("Source file does not exist! " + source.getAbsolutePath());
        }
        if (source.isDirectory()) {
            copyDirectory(source, target);
        } else {
            copyFile(source, target);
        }
    }

    private static void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            if (!target.mkdir()) {
                throw new IllegalStateException("Could not create target directory at " + target.getAbsolutePath());
            }
            LOGGER.log(Level.FINE, "Created directory {0}", target.getAbsolutePath());
        }

        File[] files = source.listFiles();
        if (files == null) {
            throw new IllegalStateException("Specified File was not a directory, even though File#isDirectory was " +
                    "true! " + source.getAbsolutePath());
        }
        for (File file : files) {
            File targetFile = new File(target, file.getName());
            copy(file, targetFile);
        }
    }

    private static void copyFile(File source, File target) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            LOGGER.log(Level.FINE, "Copied file {0}", target.getAbsolutePath());
        } finally {
            // We do not care about the possible exception while closing
            safeClose(inputStream);
            safeClose(outputStream);
        }
    }

    public static IOException safeClose(Closeable closeable) {
        if (closeable == null) {
            return null;
        }

        try {
            closeable.close();
            return null;
        } catch (IOException e) {
            return e;
        }
    }

    public static void replaceAllInFile(File target, String regex, String replacement) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(target));
        List<String> originalText = new ArrayList<String>();
        String line;
        while((line = reader.readLine()) != null) {
            originalText.add(line);
        }
        reader.close();

        BufferedWriter writer =  new BufferedWriter(new FileWriter(target));
        for (int i = 0; i < originalText.size(); i++) {
            String originalLine = originalText.get(i);
            String replacedLine = originalLine.replaceAll(regex, replacement);
            if(i > 0) {
                writer.newLine();
            }
            writer.write(replacedLine);
        }
        writer.close();
    }

    public static void exportResource(Class<?> cls, String resource, File target) throws IOException {
        InputStream inputStream = cls.getResourceAsStream(resource);
        OutputStream outputStream = null;
        byte[] buffer = new byte[4096];
        int read;
        try {
            outputStream = new FileOutputStream(target);
            while((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } finally {
            safeClose(inputStream);
            safeClose(outputStream);
        }
    }

    public static List<File> findFiles(File directory, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return findFiles(directory, pattern);
    }

    public static List<File> findFiles(File directory, Pattern pattern) {
        List<File> result = new ArrayList<File>();
        if(!directory.isDirectory()) {
            throw new IllegalStateException("Target was not a directory! " + directory.getAbsolutePath());
        }

        File[] files = directory.listFiles();
        if(files != null) {
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if(matcher.find()) {
                    result.add(file);
                }

                if(file.isDirectory()) {
                    result.addAll(findFiles(file, pattern));
                }
            }
        }
        return result;
    }

    public static File findSingleFile(File directory, String regex) {
        List<File> result = findFiles(directory, regex);
        if(result.size() != 1) {
            throw new IllegalStateException("Number of matched files should be 1, was " + result.size());
        }
        return result.iterator().next();
    }

    public static void appendTextToFile(File target, String text) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(target, true);
            writer.append(text);
        } finally {
            safeClose(writer);
        }

    }

    public static String readResource(Class<?> cls, String resource) throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(cls.getResourceAsStream(resource), "UTF-8");
            char[] buffer = new char[1024];
            int read;
            while((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            safeClose(reader);
        }
    }
}