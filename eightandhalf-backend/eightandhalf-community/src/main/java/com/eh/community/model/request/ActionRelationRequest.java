package com.eh.community.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionRelationRequest {

    private String target_user_id;

    private String action_type;
}
