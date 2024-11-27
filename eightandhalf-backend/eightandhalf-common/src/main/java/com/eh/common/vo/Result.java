package com.eh.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {

    private Base base;
    private T data;

    public static <E> Result<E> success(E data){
        return new Result<>(new Base(10000,"操作成功"),data);
    }



    public static <E> Result<E> success(){
        return new Result<>(new Base(10000,"操作成功"),null);
    }

    public static Result error(String message){
        return new Result(new Base(-1,message),null);
    }

    public static Result error(String message,Object data){return new Result(new Base(-1,message),data);}

}
