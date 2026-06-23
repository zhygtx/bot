package com.example.demo.mapper;

import com.example.demo.pojo.entity.Docker;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DockerMapper {

    /**
      * 获取docker信息
      * @param userId 用户id
      * @return docker信息
     */
    @Select("SELECT * FROM docker WHERE user_id = #{userId}")
    Docker selectByUserId(String userId);

    /**
      * 判断docker数量是否超过限制
      * @return true:超过限制 false:未超过限制
     */
    @Select("SELECT COUNT(*) > 3 FROM docker")
    Boolean isOverLimit();

    /**
      * 根据botQQ获取docker信息
      * @param botQQ botQQ
      * @return docker信息
     */
    @Select("SELECT * FROM docker WHERE bot_qq = #{botQQ}")
    Docker selectByBotQQ(Long botQQ);

    /**
      * 添加docker
      * @param docker docker信息
     */
    @Insert("INSERT INTO docker (container_id, name, user_id, bot_qq, port, token, create_time, update_time) " +
            "VALUES (#{containerId}, #{name}, #{userId}, #{botQQ}, #{port}, #{token}, #{createTime}, #{updateTime})")
    void insert(Docker docker);

    /**
      * 获取需要更新的docker
      * @return docker列表
     */
    @Select("SELECT * FROM docker WHERE update_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE)")
    List<Docker> selectNeedUpdate();

    /**
      * 更新docker信息
      * @param docker docker信息
     */
    @Update("UPDATE docker SET update_time = #{updateTime} WHERE container_id = #{containerId}")
    void update(Docker docker);

    /**
      * 删除docker
      * @param containerId docker容器id
     */
    @Delete("DELETE FROM docker WHERE container_id = #{containerId}")
    void deleteByContainerId(String containerId);
}
