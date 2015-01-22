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

import org.openqa.selenium.Rotatable;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.uiautomation.ios.client.uiamodels.impl.augmenter.Configurable;
import org.uiautomation.ios.client.uiamodels.impl.augmenter.ElementTree;
import org.uiautomation.ios.client.uiamodels.impl.augmenter.IOSSearchContext;
import org.uiautomation.ios.client.uiamodels.impl.augmenter.IOSTouchScreen;

public interface IOSDriver extends WebDriver, TakesScreenshot, Rotatable, LocationContext, ElementTree,
        IOSSearchContext, Configurable, HasTouchScreen, IOSTouchScreen {
}
