package ru.sberbank.synapse.backstage.serverapi.errors;

public class StorageException extends RuntimeException{

    public StorageException(String message){
        super(message);
    }

    public StorageException(String message, Throwable cause){
        super(message, cause);
    }
}
