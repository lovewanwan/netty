package com.th.basic.echo.shortconnect;

/**
 * @ClassName: Response
 * @Description:
 * @Author: 唐欢
 * @Date: 2023/6/2 16:15
 * @Version 1.0
 */
public class Response {
    private  long id;
    private  Object result;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}