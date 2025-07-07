package com.may.paperleap.common;

import lombok.Data;

/**
 * 通用返回对象
 *
 * @author May20242
 */
@Data
public class BaseResponse<T> {

    /**
     * 状态码
     */
    private int code;
    /**
     * 返回的数据
     */
    private T data;
    /**
     * 返回信息
     */
    private String message;

    /**
     * 具体的描述
     */
    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(CodeError codeError) {
        this(codeError.getCode(), null, codeError.getMessage(), codeError.getDescription());
    }

    public BaseResponse(CodeError codeError, T data) {
        this(codeError.getCode(), data, codeError.getMessage(), codeError.getDescription());
    }
}
