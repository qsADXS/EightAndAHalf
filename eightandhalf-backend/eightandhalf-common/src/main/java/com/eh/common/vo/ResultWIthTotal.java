package com.eh.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultWIthTotal<T> extends Result<T> {

    private int total;

    public <E> ResultWIthTotal(Base base,T data, int total) {
        super(base, data);
        this.total = total;
    }


    public static <E> ResultWIthTotal successWithTotal(E data, int total){
        return new ResultWIthTotal<>(new Base(10000,"操作成功"), data, total);
    }

    public static ResultWIthTotal error(String message){
        return new ResultWIthTotal(new Base(-1,message),null, 0);
    }

    public static ResultWIthTotal error(String message,Object data){
        return new ResultWIthTotal(new Base(-1,message),data, 0);
    }


}
