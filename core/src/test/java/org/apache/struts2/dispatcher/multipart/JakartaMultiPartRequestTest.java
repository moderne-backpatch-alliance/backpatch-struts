/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.struts2.dispatcher.multipart;

import org.apache.struts2.StrutsInternalTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;

/**
 * Test cases for {@link JakartaMultiPartRequest} that check what it leaves in the save directory.
 */
public class JakartaMultiPartRequestTest extends StrutsInternalTestCase {

    private static final String BOUNDARY = "----TestBoundary";

    private File tempDir;
    private JakartaMultiPartRequest multiPartRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = File.createTempFile("struts", "multipart");
        tempDir.delete();
        tempDir.mkdirs();

        multiPartRequest = new JakartaMultiPartRequest();
        multiPartRequest.setMaxSize("2048");
        multiPartRequest.setMaxStringLength("20");
    }

    @Override
    protected void tearDown() throws Exception {
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        tempDir.delete();
        super.tearDown();
    }

    public void testDefaultMaxStringLengthReachesTheParser() throws Exception {
        StringBuilder atLimit = new StringBuilder();
        for (int i = 0; i < 4096; i++) {
            atLimit.append('x');
        }

        MultiPartRequest accepted = container.getInstance(MultiPartRequest.class, "jakarta");
        ((JakartaMultiPartRequest) accepted).setMaxSize("2097152");
        accepted.parse(request(null, formField("field", atLimit.toString())), tempDir.getAbsolutePath());
        assertEquals(0, accepted.getErrors().size());
        assertEquals(atLimit.toString(), accepted.getParameter("field"));

        MultiPartRequest refused = container.getInstance(MultiPartRequest.class, "jakarta");
        ((JakartaMultiPartRequest) refused).setMaxSize("2097152");
        refused.parse(request(null, formField("field", atLimit + "x")), tempDir.getAbsolutePath());
        assertEquals(1, refused.getErrors().size());
        assertNull(refused.getParameter("field"));
    }

    private MockHttpServletRequest request(String encoding, String... parts) throws Exception {
        StringBuilder content = new StringBuilder();
        for (String part : parts) {
            content.append("--").append(BOUNDARY).append("\r\n").append(part).append("\r\n");
        }
        content.append("--").append(BOUNDARY).append("--\r\n");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=" + BOUNDARY);
        request.setCharacterEncoding(encoding);
        request.setContent(content.toString().getBytes("US-ASCII"));
        return request;
    }

    private String formField(String name, String value) {
        return "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value;
    }

}
