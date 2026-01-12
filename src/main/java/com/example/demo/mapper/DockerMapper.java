package com.example.demo.mapper;

import com.example.demo.pojo.Docker;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DockerMapper {
    //todo:数据库实现

    /**
      * 获取docker信息
      * @param userId 用户id
      * @return docker信息
     */
    Docker selectByUserId(String userId);

    /**
      * 判断docker数量是否超过限制
      * @return true:超过限制 false:未超过限制
     */
    Boolean isOverLimit();

    /**
      * 根据botQQ获取docker信息
      * @param botQQ botQQ
      * @return docker信息
     */
    Docker selectByBotQQ(Long botQQ);

    /**
      * 添加docker
      * @param docker docker信息
     */
    void insert(Docker docker);

    /**
      * 获取需要更新的docker
      * @return docker列表
     */
    List<Docker> selectNeedUpdate();

    /**
      * 更新docker信息
      * @param docker docker信息
     */
    void update(Docker docker);

    /**
      * 删除docker
      * @param containerId docker容器id
     */
    void deleteByContainerId(String containerId);
}
