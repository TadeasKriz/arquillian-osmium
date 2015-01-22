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

public class Settings {
    public static final StringProperty DEVELOPER_NAME = new StringProperty("osmium.developerName");
    public static final FileProperty PROVISIONING_PROFILE = new FileProperty("osmium.provisioningProfile");

    public abstract static class Property<T> {
        private final String name;
        private final T defaultValue;

        private Property(String name) {
            this(name, null);
        }

        private Property(String name, T defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public T getValue() {
            return getValue(defaultValue);
        }

        public abstract T getValue(T defaultValue);

    }

    public static class StringProperty extends Property<String> {

        private StringProperty(String name) {
            this(name, "");
        }

        private StringProperty(String name, String defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public String getValue(String defaultValue) {
            return System.getProperty(getName(), defaultValue);
        }
    }

    public static class FileProperty extends Property<File> {

        private FileProperty(String name) {
            super(name);
        }

        private FileProperty(String name, File defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public File getValue(File defaultValue) {
            String pathname = System.getProperty(getName());
            if (pathname == null || pathname.equals("")) {
                return defaultValue;
            } else {
                return new File(pathname);
            }
        }
    }

}
