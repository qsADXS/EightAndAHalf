package com.eh.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@AllArgsConstructor
//@NoArgsConstructor
//@Data
public class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final int repeatedOperationsCode = 10001;

    public static final String targetNotFound = "target not found";
    public static final String repeatedOperations = "repeated operations";
}

