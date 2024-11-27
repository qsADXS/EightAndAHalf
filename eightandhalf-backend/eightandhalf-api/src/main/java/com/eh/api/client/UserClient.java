package com.eh.api.client;

import com.eh.common.dto.UserDTO;
import com.eh.common.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping("/user/listAll")
    List<UserDTO> getUsers(@RequestParam List<Long> ids);

    @GetMapping("/user/{username}")
    User getUser(@PathVariable("username") String username);

}
