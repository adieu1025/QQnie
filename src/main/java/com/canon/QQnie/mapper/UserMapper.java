package com.canon.QQnie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canon.QQnie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

//    @Select("SELECT * FROM User where phone = #{phone}")
//    User getUserByPhone(@Param("phone") String phone);
}
