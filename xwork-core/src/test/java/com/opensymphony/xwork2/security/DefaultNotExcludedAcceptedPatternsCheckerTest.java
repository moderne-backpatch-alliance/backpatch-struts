package com.opensymphony.xwork2.security;

import com.opensymphony.xwork2.XWorkTestCase;

import java.util.Set;
import java.util.regex.Pattern;

public class DefaultNotExcludedAcceptedPatternsCheckerTest extends XWorkTestCase {

    public void testNoExclusionAcceptAllPatternsChecker() {
        assertTrue(NO_EXCLUSION_ACCEPT_ALL_PATTERNS_CHECKER.isAllowed("%{1+1}").isAllowed());
    }

    public static final NotExcludedAcceptedPatternsChecker NO_EXCLUSION_ACCEPT_ALL_PATTERNS_CHECKER
            = new NotExcludedAcceptedPatternsChecker() {
        @Override
        public IsAllowed isAllowed(String value) {
            return IsAllowed.yes("*");
        }

        @Override
        public IsAccepted isAccepted(String value) {
            return null;
        }

        @Override
        public void setAcceptedPatterns(String commaDelimitedPatterns) {

        }

        @Override
        public void setAcceptedPatterns(String[] patterns) {

        }

        @Override
        public void setAcceptedPatterns(Set<String> patterns) {

        }

        @Override
        public Set<Pattern> getAcceptedPatterns() {
            return null;
        }

        @Override
        public IsExcluded isExcluded(String value) {
            return null;
        }

        @Override
        public void setExcludedPatterns(String commaDelimitedPatterns) {

        }

        @Override
        public void setExcludedPatterns(String[] patterns) {

        }

        @Override
        public void setExcludedPatterns(Set<String> patterns) {

        }

        @Override
        public Set<Pattern> getExcludedPatterns() {
            return null;
        }
    };
}