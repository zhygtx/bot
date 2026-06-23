package com.example.demo.mapper;

import com.example.demo.pojo.entity.User;
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
    Boolean isExistByAccount(String userAccount);

    /**
     * 判断用户是否存在
     * @param email 邮箱
     * @return 存在返回true
     */
    @Select("SELECT EXISTS(SELECT 1 FROM user WHERE email = #{email})")
    Boolean isExistByEmail(String email);

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
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(String id);

    /**
     * 更新用户信息
     * @param user 用户信息
     */
    Integer updateUser(User user);

    /**
     * 更新用户密码
     * @param user 用户信息
     */
    @Update("update user set pwd= #{pwd} where id= #{id}")
    void updatePwd(User user);

    /**
     * 根据用户ID查询用户邮箱
     * @param id 用户ID
     * @return 用户邮箱
     */
    @Select("SELECT email FROM user WHERE id = #{id}")
    String selectEmailById(String id);

    /**
     * 更新用户邮箱
     * @param user 用户信息
     */
    @Update("update user set email= #{email} where account= #{account}")
    void updateEmail(User user);
}