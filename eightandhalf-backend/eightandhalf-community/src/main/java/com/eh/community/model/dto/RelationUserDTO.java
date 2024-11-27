package com.eh.community.model.dto;

import com.eh.common.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelationUserDTO {
    private Long UserId;

    private String UserName;

    private String AvatarUrl;

    private int isSinger;

    private Long singerCategory;

    public void ConvertToRelationUserDTO(UserDTO userDTO) {
        this.UserId = userDTO.getUserId();
        this.UserName = userDTO.getUserName();
        this.AvatarUrl = userDTO.getAvatarUrl();
        this.isSinger = userDTO.getIsSinger();
        this.singerCategory = userDTO.getSingerCategory();
    }

}
