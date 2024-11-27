package com.eh.user.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.eh.common.util.EmailUtil;
import com.eh.common.util.SnowFlakeUtil;
import com.eh.common.util.ThreadLocalUtil;
import com.eh.common.util.VerificationCodeUtil;
import com.eh.user.mapper.UserMapper;
import com.eh.user.model.dto.UserDTO;
import com.eh.user.model.entity.User;
import com.eh.user.model.request.BindCodeRequest;
import com.eh.user.model.request.EmailRequest;
import com.eh.user.model.request.RegisterRequest;
import com.eh.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final int EXPIRATION_TIME = 300;

    @Override
    public User findByUserName(String username) {
        return userMapper.getUser(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        User user = new User();
        user.setUserId(SnowFlakeUtil.getSnowFlakeId());
        user.setUserName(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(LocalDateTime.now());
        user.setIdentity(0);
        user.setIsSinger(0);
        //mq异步写入数据库
        rabbitTemplate.convertAndSend("user.topic","user.insert",user);
        redisTemplate.opsForValue().set("user:id:"+user.getUserId(),user,2, TimeUnit.HOURS);

    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = new User();
        try {
            user = (User) redisTemplate.opsForValue().get("user:id:" + id);
        } catch (Exception e) {
            log.info("redis中没有该用户");
        }
        if(user==null) {
            user = userMapper.getUserById(id);
            redisTemplate.opsForValue().set("user:id:" + user.getUserId(), user,2, TimeUnit.HOURS);
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setUserName(user.getUserName());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setDeletedAt(user.getDeletedAt());
        userDTO.setIsSinger(user.getIsSinger());
        return userDTO;
    }

    @Override
    public void updateAvatarUrl(Long id, String url) {
        User user = new User();
        user.setUserId(id);
        user.setAvatarUrl(url);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateUser(user);
        try {
            redisTemplate.delete("user:id:"+id);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("user.topic","user.delete",id);
        }
    }

    @Override
    public void bindCode(BindCodeRequest request) {
        User user = new User();
        Long id = ThreadLocalUtil.get();
        user.setUserId(id);
        user.setQrcode(request.getCode());
        user.setSecret(request.getSecret());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateUser(user);
        try {
            redisTemplate.delete("user:id:"+id);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("user.topic","user.delete",id);
        }
    }

    @Override
    public boolean sendEmailCode(EmailRequest request) {
        if(redisTemplate.getExpire("user:emailCode:" + request.getEmail(), TimeUnit.SECONDS) > 240){
            return false;
        }
        String code = VerificationCodeUtil.generateVerificationCode();
        request.setCode(code);
        redisTemplate.opsForValue().set("user:emailCode:"+request.getEmail(),request,EXPIRATION_TIME, TimeUnit.SECONDS);
        EmailUtil.sendCode(request.getEmail(),code);

        return true;
    }

    @Override
    public boolean proveEmailCode(EmailRequest request) {
        Object o = redisTemplate.opsForValue().get("user:emailCode:" + request.getEmail());
        if(o == null){
            return false;
        }
        EmailRequest r = (EmailRequest)o;
        if(Objects.equals(r.getCode(), request.getCode())){
            return redisTemplate.delete("user:emailCode:" + request.getEmail());
        }
        return false;
    }

    @Override
    public List<UserDTO> getUsers(List<Long> ids) {

        List<String> keys = new ArrayList<>();
        for (Long id : ids) {
            keys.add("user:id:"+id);
        }
        List<User> users = new ArrayList<>();
        try {
            log.info("www");
            users = (List<User>) redisTemplate.opsForValue().multiGet(keys).stream()
                    .filter(Objects::nonNull) // 过滤掉 null 元素
                    .collect(Collectors.toList());;
            log.info(users.toString());
        } catch (Exception e) {
            log.info("redis中获取不到用户");
        }

        if (users.size() < ids.size()) {
            log.info("qqq");
            users = userMapper.selectUsersByIds(ids);
            HashMap<String, User> hashMap = new HashMap<>();
            for (User user : users) {
                hashMap.put("user:id:"+user.getUserId(), user);
            }
            redisTemplate.opsForValue().multiSet(hashMap);
        }

        List<UserDTO> userDTOs = new ArrayList<UserDTO>();
        for (User user : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserId(user.getUserId());
            userDTO.setUserName(user.getUserName());
            userDTO.setAvatarUrl(user.getAvatarUrl());
            userDTO.setCreatedAt(user.getCreatedAt());
            userDTO.setUpdatedAt(user.getUpdatedAt());
            userDTO.setDeletedAt(user.getDeletedAt());
            userDTO.setIsSinger(user.getIsSinger());
            userDTO.setSingerCategory(user.getSingerCategory());
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }
}
