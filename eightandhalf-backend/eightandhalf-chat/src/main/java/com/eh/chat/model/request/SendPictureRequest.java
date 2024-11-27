package com.eh.chat.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendPictureRequest {
    @NotNull
    private MultipartFile picture;
    @NotNull
    private String to;
}
