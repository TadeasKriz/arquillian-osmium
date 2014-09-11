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
package arquillian.osmium.test;

import org.arquillian.osmium.util.IPABuilder;
import org.jboss.shrinkwrap.api.Archive;

import java.io.IOException;

public class Deployments {

    public static Archive<?> playgroundIpa() throws IOException {
        return IPABuilder.prepare()
                .sourceDirectory("../arquillian-osmium-playground")
                .projectName("arquillian-osmium-playground")
                .developerName("Fill your developer name (can be found in Xcode)")
                .provisioningProfile("Fill in path for your provisioning profile that can be used to deploy this archive")
                .buildArchive();
    }

}
