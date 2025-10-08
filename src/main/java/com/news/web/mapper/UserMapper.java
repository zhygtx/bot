package com.news.web.mapper;

import com.news.web.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Insert("insert into user(username,password,create_time,update_time) values (#{username},#{password},#{createTime},#{updateTime})")
    int insert(User user);

    @Select("select * from user where username=#{username}")
    User selectByName(String username);

    @Update("update user set password=#{password} where user_id =#{id}")
    int updatePwd(User user);
}