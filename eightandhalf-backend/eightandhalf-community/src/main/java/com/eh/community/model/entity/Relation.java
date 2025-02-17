package com.eh.community.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Relation implements Serializable {
    private Long userId;
    private Long followerUserId;
}
