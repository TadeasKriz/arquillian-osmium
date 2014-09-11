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

import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.uiautomation.ios.IOSCapabilities;

import java.util.Map;

public class IOSBrowserCapabilities implements BrowserCapabilities {
    @Override
    public String getImplementationClassName() {
        return IOSDriverImpl.class.getCanonicalName();
    }

    @Override
    public Map<String, ?> getRawCapabilities() {
        return new IOSCapabilities().getRawCapabilities();
    }

    @Override
    public String getReadableName() {
        return "ios";
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
