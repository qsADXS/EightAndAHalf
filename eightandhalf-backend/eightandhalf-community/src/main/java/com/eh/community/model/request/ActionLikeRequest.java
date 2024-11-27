package com.eh.community.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionLikeRequest {
    private String action_type;
    private String target_type;
    private String id;
}
