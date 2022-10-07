package ru.sberbank.synapse.backstage.serverapi.errors;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundInDirectoryException extends StorageException{

    public FileNotFoundInDirectoryException(String message) {
        super(message);
    }

    public FileNotFoundInDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
