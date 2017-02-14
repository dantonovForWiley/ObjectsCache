package com.dantonov.wiley.objectscache.exceptions;

/**
 * Exception for case when cache builder can not process unacceptable parameter
 */
public class UnacceptableCacheBuildParameter extends Exception {
    public UnacceptableCacheBuildParameter(String message) {
        super(message);
    }
}
