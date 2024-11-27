package com.eh.api.client;

import com.eh.common.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("music-service")
public interface MusicClient {
    @PostMapping("/music/comment/update")
    Result updateCommentCount(@RequestParam Long musicId);

    @PostMapping("/music/comment/decr")
    Result decrCommentCount(@RequestParam Long musicId);
}
