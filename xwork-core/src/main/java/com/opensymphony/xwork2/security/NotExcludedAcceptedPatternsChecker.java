package com.opensymphony.xwork2.security;

/**
 * Used across different places to check if given string is not excluded and is accepted
 * @see <a href="https://securitylab.github.com/research/apache-struts-double-evaluation/">here</a>
 */
public interface NotExcludedAcceptedPatternsChecker extends ExcludedPatternsChecker, AcceptedPatternsChecker {

    /**
     * Checks if value doesn't match excluded pattern and matches accepted pattern
     *
     * @param value to check
     * @return object containing result of matched pattern and pattern itself
     */
    IsAllowed isAllowed(String value);

    final class IsAllowed {

        private final boolean allowed;
        private final String allowedPattern;

        public static IsAllowed yes(String allowedPattern) {
            return new IsAllowed(true, allowedPattern);
        }

        public static IsAllowed no(String allowedPattern) {
            return new IsAllowed(false, allowedPattern);
        }

        private IsAllowed(boolean allowed, String allowedPattern) {
            this.allowed = allowed;
            this.allowedPattern = allowedPattern;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getAllowedPattern() {
            return allowedPattern;
        }

        @Override
        public String toString() {
            return "IsAllowed { " +
                    "allowed=" + allowed +
                    ", allowedPattern=" + allowedPattern +
                    " }";
        }
    }
}
