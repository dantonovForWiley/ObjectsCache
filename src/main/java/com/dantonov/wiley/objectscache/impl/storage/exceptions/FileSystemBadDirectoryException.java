package com.dantonov.wiley.objectscache.impl.storage.exceptions;

import java.nio.file.Path;

/**
 * Exception for case when {@link com.dantonov.wiley.objectscache.impl.storage.FileSystemStorage}
 * has failed to init due to bad directory path
 */
public class FileSystemBadDirectoryException extends Exception {
    public FileSystemBadDirectoryException(String message) {
        super(message);
    }
}
