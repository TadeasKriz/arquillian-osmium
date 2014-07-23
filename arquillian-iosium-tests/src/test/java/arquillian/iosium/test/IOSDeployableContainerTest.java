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
package arquillian.iosium.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.uiautomation.ios.IOSCapabilities;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class IOSDeployableContainerTest {


    //@Drone
    WebDriver driver;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap
                .create(ExplodedImporter.class, "ios_crypto.app")
                .importDirectory("ios_crypto.app")
                .as(JavaArchive.class);
    }

    @Before
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        //latch.await();

        DesiredCapabilities capabilities = IOSCapabilities.iphone("AeroGear-Crypto-Demo");

        driver = new RemoteWebDriver(new URL("http://localhost:5555/wd/hub"), capabilities);
    }

    @After
    public void tearDown() throws Exception {
        // end the test
        driver.quit();
    }

    @Test
    public void testWebdriver() {
        assertNotNull(driver);

        // find button "OK"
        List<WebElement> cells = driver.findElements(By.className("UIATableCell"));
        assertEquals(1, cells.size());

        // click to dismiss alert
        cells.get(0).click();

        // take a screenshot using the normal selenium api.
        TakesScreenshot screen =(TakesScreenshot)new Augmenter().augment(driver);
        File ss = new File("screenshot.png");
        screen.getScreenshotAs(OutputType.FILE).renameTo(ss);
        System.out.println("screenshot take :"+ss.getAbsolutePath());

        // access the content
        By selector = By.xpath("//UIAStaticText[contains(@name,'climbed')]");
        WebElement text = driver.findElement(selector);
        System.out.println(text.getAttribute("name"));

    }

}
