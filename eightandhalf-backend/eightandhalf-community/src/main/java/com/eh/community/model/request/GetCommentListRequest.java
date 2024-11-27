package com.eh.community.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCommentListRequest {
    private String type;
    private String id;
    private int page_num;
    private int page_size;
}
