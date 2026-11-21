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

package org.apache.struts2.dispatcher.mapper;

import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.struts2.RequestUtils;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.util.URLDecoderUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Simple Restfull Action Mapper to support REST application
 * See docs for more information
 * http://struts.apache.org/2.x/docs/restfulactionmapper.html
 */
public class RestfulActionMapper implements ActionMapper {
    protected static final Logger LOG = LoggerFactory.getLogger(RestfulActionMapper.class);

    /**
     * Matches action names allowed in the request URI, aligned with {@link DefaultActionMapper}.
     */
    private Pattern allowedActionNames = Pattern.compile("[a-zA-Z0-9._!/\\-]*");

    /**
     * Action name used when the name extracted from the URI is not allowed, aligned with {@link DefaultActionMapper}.
     */
    private String defaultActionName = "index";

    @Inject(value = StrutsConstants.STRUTS_ALLOWED_ACTION_NAMES, required = false)
    public void setAllowedActionNames(String allowedActionNames) {
        this.allowedActionNames = Pattern.compile(allowedActionNames);
    }

    @Inject(value = StrutsConstants.STRUTS_DEFAULT_ACTION_NAME, required = false)
    public void setDefaultActionName(String defaultActionName) {
        this.defaultActionName = defaultActionName;
    }

    /* (non-Javadoc)
     * @see org.apache.struts2.dispatcher.mapper.ActionMapper#getMapping(javax.servlet.http.HttpServletRequest)
     */
    public ActionMapping getMapping(HttpServletRequest request, ConfigurationManager configManager) {
        String uri = RequestUtils.getServletPath(request);

        int nextSlash = uri.indexOf('/', 1);
        if (nextSlash == -1) {
            return null;
        }

        String actionName = cleanupActionName(uri.substring(1, nextSlash));
        Map<String, Object> parameters = new HashMap<String, Object>();
        try {
            StringTokenizer st = new StringTokenizer(uri.substring(nextSlash), "/");
            boolean isNameTok = true;
            String paramName = null;
            String paramValue;

            // check if we have the first parameter name
            if ((st.countTokens() % 2) != 0) {
                isNameTok = false;
                paramName = actionName + "Id";
            }

            while (st.hasMoreTokens()) {
                if (isNameTok) {
                    paramName = URLDecoderUtil.decode(st.nextToken(), "UTF-8");
                    isNameTok = false;
                } else {
                    paramValue = URLDecoderUtil.decode(st.nextToken(), "UTF-8");

                    if ((paramName != null) && (paramName.length() > 0)) {
                        parameters.put(paramName, paramValue);
                    }

                    isNameTok = true;
                }
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
        	LOG.warn("Cannot determine url parameters", e);
            }
        }

        return new ActionMapping(actionName, "", "", parameters);
    }

    public ActionMapping getMappingFromActionName(String actionName) {
        return new ActionMapping(actionName, null, null, null);
    }

    /**
     * Checks action name against the allowed pattern; if it does not match, returns the default action name.
     * Mirrors {@link DefaultActionMapper#cleanupActionName(String)}.
     *
     * @param rawActionName action name extracted from the URI
     * @return safe action name
     */
    protected String cleanupActionName(final String rawActionName) {
        if (allowedActionNames.matcher(rawActionName).matches()) {
            return rawActionName;
        } else {
            LOG.warn("{} did not match allowed action names {} - default action {} will be used!", rawActionName, allowedActionNames, defaultActionName);
            return defaultActionName;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.struts2.dispatcher.mapper.ActionMapper#getUriFromActionMapping(org.apache.struts2.dispatcher.mapper.ActionMapping)
     */
    public String getUriFromActionMapping(ActionMapping mapping) {
        StringBuilder retVal = new StringBuilder();
        retVal.append(mapping.getNamespace());
        retVal.append(mapping.getName());
        Object value = mapping.getParams().get(mapping.getName() + "Id");
        if (value != null) {
            retVal.append("/");
            retVal.append(value);
        } 

        return retVal.toString();
    }
}
