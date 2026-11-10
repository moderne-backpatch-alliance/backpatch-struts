/*
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
package org.apache.struts2.util;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ognl.OgnlUtil;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.StrutsInternalTestCase;
import org.apache.velocity.VelocityContext;

import java.net.URI;
import java.net.URISyntaxException;

public class StrutsDefaultExclusionsTest extends StrutsInternalTestCase {

    private OgnlUtil ognlUtil;
    private ValueStack stack;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ognlUtil = container.getInstance(OgnlUtil.class);
        stack = ActionContext.getContext().getValueStack();
    }

    public void testS2059PackagesAreExcluded() {
        String[] packages = {"java.net.", "freemarker.ext.jsp.", "sun.misc.", "org.apache.velocity.",
                "org.objectweb.asm.", "org.springframework.context.", "com.opensymphony.xwork2.inject."};
        for (String name : packages) {
            assertTrue(name, ognlUtil.getExcludedPackageNames().contains(name));
        }
    }

    public void testS2059UnsafeClassIsExcluded() throws Exception {
        assertTrue(ognlUtil.getExcludedClasses().contains(Class.forName("sun.misc.Unsafe")));
    }

    public void testS2059JavaNetMemberIsRefused() throws URISyntaxException {
        final URI uri = new URI("http://example.org/");
        stack.push(new Object() {
            public URI getUri() {
                return uri;
            }
        });

        assertNull(stack.findValue("uri.host"));
    }

    public void testS2059VelocityMemberIsRefused() {
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("key", "value");
        stack.push(new Object() {
            public VelocityContext getVelocityContext() {
                return velocityContext;
            }
        });

        assertNull(stack.findValue("velocityContext.keys"));
    }
}
