package com.may.paperleap.common;

/**
 * 自动创建通用返回对象
 *
 * @author May20242
 */
public class ResultUtils {

    /**
     * 成功返回响应对象
     * @param codeError 状态码信息
     * @param data  传递给前端的数据
     * @return  通用返回对象
     * @param <T>
     */
    public static <T> BaseResponse<T> success(CodeError codeError, T data) {
        return new BaseResponse<>(codeError, data);
    }

    /**
     * 返回发生错误的响应对象
     * @param codeError 状态码信息
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(CodeError codeError) {
        return new BaseResponse<>(codeError);
    }

    public static <T> BaseResponse<T> error(CodeError codeError, String message, String description) {
        return new BaseResponse<>(codeError.getCode(), message, description);
    }

    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, message, description);
    }

}
