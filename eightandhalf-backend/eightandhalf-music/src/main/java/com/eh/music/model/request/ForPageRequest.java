package com.eh.music.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForPageRequest {
    Integer pageNo;
    Integer pageSize;
}
