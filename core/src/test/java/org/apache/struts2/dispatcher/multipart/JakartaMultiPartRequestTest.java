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

import org.apache.commons.fileupload.FileItem;
import org.apache.struts2.StrutsInternalTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.util.List;

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

        multiPartRequest = new TestableJakartaMultiPartRequest();
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

    public void testTooLongFormFieldLeavesNoTempFile() throws Exception {
        multiPartRequest.parse(request(null, formField("field", "this value is longer than the limit")), tempDir.getAbsolutePath());

        assertEquals(1, multiPartRequest.getErrors().size());
        assertNull(multiPartRequest.getParameter("field"));
        assertEquals("the refused field has to be deleted without a cleanUp", 0, tempDir.listFiles().length);
    }

    public void testFormFieldWithUnknownCharsetLeavesNoTempFile() throws Exception {
        multiPartRequest.parse(request("no-such-charset", formField("field", "short")), tempDir.getAbsolutePath());

        assertEquals(1, multiPartRequest.getErrors().size());
        assertEquals("the field has to be deleted without a cleanUp", 0, tempDir.listFiles().length);
    }

    public void testAcceptedFormFieldLeavesNoTempFile() throws Exception {
        multiPartRequest.parse(request(null, formField("field", "short")), tempDir.getAbsolutePath());

        assertEquals(0, multiPartRequest.getErrors().size());
        assertEquals("short", multiPartRequest.getParameter("field"));
        assertEquals(0, tempDir.listFiles().length);
    }

    /**
     * Test that comprehensive cleanup removes all temporary files created during multipart processing.
     * This addresses the security vulnerability where temporary files could be leaked.
     */
    public void testComprehensiveCleanupRemovesAllTempFiles() throws Exception {
        // Create a mock multipart request with both file upload and form field
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        request.setMethod("POST");

        String multipartContent = createMultipartContent();
        request.setContent(multipartContent.getBytes("UTF-8"));

        // Count files before processing
        int filesBefore = countTempFiles();

        // Process the multipart request
        multiPartRequest.parse(request, tempDir.getAbsolutePath());

        // Count files after processing (should be more due to temp files)
        int filesAfterProcessing = countTempFiles();

        // Verify that temp files were created during processing
        assertTrue("Temporary files should be created during multipart processing",
                   filesAfterProcessing > filesBefore);

        // Perform cleanup
        multiPartRequest.cleanUp();

        // Count files after cleanup
        int filesAfterCleanup = countTempFiles();

        // Verify comprehensive cleanup removed all temporary files
        assertEquals("All temporary files should be cleaned up", filesBefore, filesAfterCleanup);
    }

    /**
     * Test that all FileItem instances are tracked for cleanup, including both file uploads and form fields.
     */
    public void testAllFileItemsAreTrackedForCleanup() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        request.setMethod("POST");

        String multipartContent = createMultipartContentWithMultipleFields();
        request.setContent(multipartContent.getBytes("UTF-8"));

        // Process the multipart request
        multiPartRequest.parse(request, tempDir.getAbsolutePath());

        // Access the tracked items through our testable implementation
        TestableJakartaMultiPartRequest testable = (TestableJakartaMultiPartRequest) multiPartRequest;
        List<FileItem> trackedItems = testable.getAllFileItems();

        // Verify that all items (both files and form fields) are tracked
        assertTrue("Should track multiple FileItem instances", trackedItems.size() >= 3);

        // Verify tracking includes both form fields and file uploads
        boolean hasFormField = false;
        boolean hasFileUpload = false;

        for (FileItem item : trackedItems) {
            if (item.isFormField()) {
                hasFormField = true;
            } else {
                hasFileUpload = true;
            }
        }

        assertTrue("Should track form field items", hasFormField);
        assertTrue("Should track file upload items", hasFileUpload);
    }

    public void testFilePartWithoutAFileNameIsCleanedUp() throws Exception {
        multiPartRequest.parse(request(null, filePart("file", "", "file content")), tempDir.getAbsolutePath());

        assertEquals(1, tempDir.listFiles().length);
        assertFalse(multiPartRequest.getFileParameterNames().hasMoreElements());

        multiPartRequest.cleanUp();

        assertEquals(0, tempDir.listFiles().length);
    }

    public void testItemsAfterAFailingFieldAreCleanedUp() throws Exception {
        multiPartRequest.parse(request("no-such-charset", formField("field", "value"), filePart("file", "file.txt", "file content")),
                tempDir.getAbsolutePath());

        assertEquals(1, tempDir.listFiles().length);
        assertEquals(1, multiPartRequest.getErrors().size());

        multiPartRequest.cleanUp();

        assertEquals(0, tempDir.listFiles().length);
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

    private String filePart(String name, String fileName, String content) {
        return "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: text/plain\r\n\r\n" + content;
    }

    private String createMultipartContent() {
        return "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
               "Content-Disposition: form-data; name=\"textField\"\r\n\r\n" +
               "test value\r\n" +
               "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
               "Content-Disposition: form-data; name=\"fileField\"; filename=\"test.txt\"\r\n" +
               "Content-Type: text/plain\r\n\r\n" +
               "file content\r\n" +
               "------WebKitFormBoundary7MA4YWxkTrZu0gW--\r\n";
    }

    private String createMultipartContentWithMultipleFields() {
        return "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
               "Content-Disposition: form-data; name=\"textField1\"\r\n\r\n" +
               "value1\r\n" +
               "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
               "Content-Disposition: form-data; name=\"textField2\"\r\n\r\n" +
               "value2\r\n" +
               "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n" +
               "Content-Disposition: form-data; name=\"fileField\"; filename=\"test.txt\"\r\n" +
               "Content-Type: text/plain\r\n\r\n" +
               "file content\r\n" +
               "------WebKitFormBoundary7MA4YWxkTrZu0gW--\r\n";
    }

    private int countTempFiles() {
        if (tempDir == null || !tempDir.exists()) {
            return 0;
        }
        File[] files = tempDir.listFiles();
        return files != null ? files.length : 0;
    }

    /**
     * Testable subclass that exposes internal state for verification
     */
    private static class TestableJakartaMultiPartRequest extends JakartaMultiPartRequest {
        public List<FileItem> getAllFileItems() {
            return allFileItems;
        }
    }

}
