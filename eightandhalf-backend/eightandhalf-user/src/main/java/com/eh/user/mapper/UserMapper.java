package com.eh.user.mapper;

import com.eh.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface UserMapper {
    User getUser(String username);

    User getUserByEmail(String email);

    void register(User user);

    User getUserById(Long id);

    void updateUser(User user);

    List<User> selectUsersByIds(@Param("userIds") List<Long> userIds);

}
