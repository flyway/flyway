/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal;

import org.flywaydb.core.internal.util.UrlUtils;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

public class UrlUtilsSmallTest {
    @Test
    public void toFilePath() throws MalformedURLException {
        File file = new File("/test dir/a+b");
        assertEquals(file.getAbsolutePath(), UrlUtils.toFilePath(file.toURI().toURL()));
    }
}
