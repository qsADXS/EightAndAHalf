package com.eh.music.model.dto;

import com.eh.music.model.entity.MusicCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicCategoryDTO {
    private List<MusicCategory> musicCategoryList;
    private int total;
}
