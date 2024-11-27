package com.eh.music.model.request;

import lombok.Data;

@Data
public class RequestParams {
    private String key;
    private String sortBy;
    private Integer pageNum;
    private Integer pageSize;
}
