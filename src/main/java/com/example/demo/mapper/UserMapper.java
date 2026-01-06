package com.example.demo.mapper;

import com.example.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户表映射
 */
@Mapper
public interface UserMapper {

    /**
     * 判断用户是否存在
     * @param userAccount 用户ID
     * @return 是否存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM user WHERE account = #{userAccount})")
    Boolean isExist(String userAccount);

    /**
     * 插入用户
     * @param user 用户信息
     */
    @Select("INSERT INTO user (id, name, account, pwd, email, create_time, update_time) " +
            "VALUES (#{id}, #{name}, #{account}, #{pwd},#{email} ,#{createTime}, #{updateTime})")
    void insertUser(User user);

    /**
     * 根据用户ID查询用户信息
     * @param account 用户ID
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE account = #{account}")
    User selectByAccount(String account);

    /**
     * 更新用户信息
     * @param user 用户信息
     */
    @Update("update user set name=#{name}, qq=#{QQ}, bot_id=#{botId}, update_time=#{updateTime} where account=#{account}")
    void updateUser(User user);

    /**
     * 更新用户密码
     * @param user 用户信息
     */
    @Update("update user set pwd= #{pwd} where account= #{account}")
    void updatePwd(User user);

    /**
     * 更新用户邮箱
     * @param user 用户信息
     */
    @Update("update user set email= #{email} where account= #{account}")
    void updateEmail(User user);
}
