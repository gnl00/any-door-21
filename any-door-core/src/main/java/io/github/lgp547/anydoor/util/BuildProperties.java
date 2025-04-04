/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.github.lgp547.anydoor.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties {
    
    private static final BuildProperties INSTANCE = load();
    
    public static BuildProperties getInstance() {
        return INSTANCE;
    }
    
    public static BuildProperties load() {
        final Properties buildProperties = new Properties();
        try (InputStream is = BuildProperties.class.getResourceAsStream("build.properties")) {
            buildProperties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Could not read build.properties");
        }
        return new BuildProperties(
                buildProperties.getProperty("version"),
                buildProperties.getProperty("os.detected.name"),
                buildProperties.getProperty("os.detected.arch"));
    }
    
    private final String version;
    private final String osName;
    private final String osArch;
    
    public BuildProperties(String version, String os, String arch) {
        this.version = version;
        this.osName = os;
        this.osArch = arch;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getOsName() {
        return osName;
    }
    
    public String getOsArch() {
        return osArch;
    }
}