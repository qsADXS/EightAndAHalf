package com.eh.community.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Like implements Serializable {
    private Long userId;
    private Long commentId;
    private Long blogId;
}
