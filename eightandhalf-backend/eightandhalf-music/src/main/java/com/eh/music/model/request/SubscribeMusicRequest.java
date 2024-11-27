package com.eh.music.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscribeMusicRequest {
    private Long musicId;
    private Long playlistId;
    private LocalDateTime createdAt;
}
