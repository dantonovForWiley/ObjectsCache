package com.dantonov.wiley.objectscache.impl.storage;

import com.dantonov.wiley.objectscache.impl.storage.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * {@link SerializableStorage} implementation that stores data on file system
 */
public class FileSystemStorage implements SerializableStorage {

    public FileSystemStorage(String baseDirectory) throws FileSystemBadDirectoryException {
        this.baseDirectory = baseDirectory;
        checkDirectory();
    }

    @Override
    public void store(UUID uuid, byte[] context) throws FailedToStoreDataInStorage {
        try {
            Files.write(Paths.get(baseDirectory, uuid.toString()), context);
        } catch (IOException e) {
            throw new FailedToStoreDataInStorage(uuid);
        }
    }

    @Override
    public byte[] retrieve(UUID uuid) throws FailedToRetrieveStorageData, DataNotFoundInStorage {
        try {
            return Files.readAllBytes(Paths.get(baseDirectory, uuid.toString()));
        } catch (IOException e) {
            throw new FailedToRetrieveStorageData(uuid);
        }
    }

    @Override
    public void delete(UUID uuid) throws DataNotFoundInStorage, FailedToDeleteDataInStorage {
        try {
            Files.delete(Paths.get(baseDirectory, uuid.toString()));
        } catch (IOException e) {
            throw new FailedToDeleteDataInStorage(uuid);
        }
    }

    @Override
    public long getOccupiedSize() {
        try {
            return Files.list(Paths.get(baseDirectory)).mapToLong((path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0;
                }
            })).sum();
        } catch (Exception e) {
            return 0;
        }
    }

    private void checkDirectory() throws FileSystemBadDirectoryException {
        String errorMessage = null;
        if (baseDirectory == null) {
            throw new FileSystemBadDirectoryException("Directory path is null: ");
        } else {
            Path basePath = Paths.get(baseDirectory);
            LOGGER.info(String.format("Checking directory: %s", baseDirectory));
            if (!Files.exists(basePath)) {
                try {
                    Files.createDirectories(basePath);
                } catch (IOException e) {
                    errorMessage = "Directory has not been created due to exception <<<< " + e
                            .getMessage() + ". Directory: ";
                }
            }
            if (errorMessage != null) {
                throw new FileSystemBadDirectoryException(errorMessage + baseDirectory);
            }
        }
    }

    private String baseDirectory;
    private final static Logger LOGGER = LoggerFactory.getLogger(FileSystemStorage.class);
}
