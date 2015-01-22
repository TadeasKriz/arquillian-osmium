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
package arquillian.osmium.test;

import org.arquillian.osmium.IOSDriver;
import org.arquillian.osmium.OsmiumResource;
import org.arquillian.osmium.util.OsmiumBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class IOSDeployableContainerTest {

    @OsmiumResource
    static OsmiumBuilder builder;

    @Drone
    @OperateOnDeployment("ios-app")
    IOSDriver driver;

    @Deployment(testable = false, name = "ios-app")
    public static Archive<?> createDeployment() throws IOException {
        return Deployments.playgroundIpa(builder);
    }

    @After
    public void tearDown() throws Exception {
        // end the test
        //driver.quit();
    }

    @Test
    public void testWebdriver() {
        assertNotNull(driver);

        // find textField
        WebElement textField = driver.findElement(By.id("textField"));
        assertThat(textField, is(not(nullValue())));
        textField.sendKeys("Hello Osmium");

        WebElement button = driver.findElement(By.id("button"));
        assertThat(button, is(not(nullValue())));
        button.click();

        textField.clear();

        WebElement switchElement = driver.findElement(By.id("switch"));
        switchElement.click();
        textField.sendKeys("Bye Osmium\n");

        File screenshot = driver.getScreenshotAs(OutputType.FILE);

    }

}
