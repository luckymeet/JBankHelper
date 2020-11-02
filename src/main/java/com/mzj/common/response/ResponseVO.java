package com.mzj.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yuminjun
 * @version 1.00
 * @date 2020/10/29 11:39
 * @record <pre>
 * version  author      date      desc
 * -------------------------------------------------
 * 1.00     yuminjun    2020/10/29     新建
 * -------------------------------------------------
 * </pre>
 */
@Data
public class ResponseVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = 200;

    private Integer code;

    private String message;

    private T data;

    private Long timestamp = System.currentTimeMillis();

    private ResponseVO() {
    }

    public static <D> ResponseVO<D> success(D data) {
        return success(data, (String) null);
    }

    public static <D> ResponseVO<D> success(D data, String message) {
        ResponseVO<D> responseVO = new ResponseVO<>();
        responseVO.setCode(SUCCESS);
        responseVO.setMessage(message);
        responseVO.setData(data);
        return responseVO;
    }

    public static <D> ResponseVO<D> fail(int code, String message) {
        ResponseVO<D> responseVO = new ResponseVO<>();
        responseVO.setCode(code);
        responseVO.setMessage(message);
        return responseVO;
    }

    public boolean isSuccess() {
        return this.code == SUCCESS;
    }

}
