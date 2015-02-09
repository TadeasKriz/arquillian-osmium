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
package arquillian.osmium.test.device;

import arquillian.osmium.test.Deployments;
import arquillian.osmium.test.base.IOSDeployableContainerTestBase;
import arquillian.osmium.test.util.TestUtils;
import org.arquillian.osmium.OsmiumResource;
import org.arquillian.osmium.util.OsmiumBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(Arquillian.class)
@RunAsClient
public class DeviceContainerTest extends IOSDeployableContainerTestBase {

    @OsmiumResource
    static OsmiumBuilder builder;

    @Deployment(testable = false, name = Deployments.IOS_APP)
    public static Archive<?> createDeployment() throws IOException {
        return Deployments.playgroundIpa(builder);
    }

    @BeforeClass
    public static void assumption() {
        Assume.assumeTrue(TestUtils.nativeTestsEnabled());
    }

}