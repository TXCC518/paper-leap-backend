package com.may.paperleap.exception;

import com.may.paperleap.common.CodeError;

/**
 * @author May20242
 */
public class BusinessException extends RuntimeException{

    private final int code;
    private final String description;

    public BusinessException(int code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(CodeError codeError) {
        super(codeError.getMessage());
        this.code = codeError.getCode();
        this.description = codeError.getDescription();
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
