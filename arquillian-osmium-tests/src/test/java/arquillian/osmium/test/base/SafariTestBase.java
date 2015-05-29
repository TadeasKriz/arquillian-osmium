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
package arquillian.osmium.test.base;

import com.thoughtworks.selenium.webdriven.commands.WaitForCondition;
import org.arquillian.osmium.IOSDriver;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public abstract class SafariTestBase {

    @Drone
    IOSDriver driver;

    WebDriverWait wait;

    @Before
    public void setUp() {
        wait = new WebDriverWait(driver, 15);
    }

    @Test
    public void testWebdriver() {
        assertNotNull(driver);

        driver.get("http://arquillian.org/");

        wait.until(ExpectedConditions.titleIs("Arquillian · Write Real Tests"));

        driver.findElement(By.className("btn-navbar")).click();

        driver.findElement(By.linkText("Guides")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        WebElement h1_a = driver.findElement(By.tagName("h1")).findElement(By.tagName("a"));

        assertThat(h1_a.getText()).isEqualTo("Arquillian Guides");
        assertThat(h1_a.getAttribute("href")).isEqualTo("http://arquillian.org/guides/");

        File screenshot = driver.getScreenshotAs(OutputType.FILE);

    }

}
