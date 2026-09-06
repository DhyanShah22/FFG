package com.antarang.cap.service;

/**
 * Internal keys stored in signup session JSON; stripped before account creation.
 */
final class SignupSessionMetadata {

    static final String VERIFIED_EMAIL = "_meta.verifiedEmail";
    static final String VERIFIED_MOBILE = "_meta.verifiedMobile";

    private SignupSessionMetadata() {
    }

    static boolean isMetadataKey(String key) {
        return key != null && key.startsWith("_meta.");
    }
}
