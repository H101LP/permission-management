package com.fast.system.mapper;

import com.fast.system.domain.User;
import org.apache.ibatis.annotations.Mapper;
/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     * @param userName 用户名
     * @return 用户
     */
    public User selectByUserName(String userName);


    User selectByUserUserId(long userId);
}
