package com.eh.community.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionCommentRequest {
    private String content;
    private String type;
    private String id;
}
