package com.example.demo.mapper;

import com.example.demo.pojo.Docker;
import org.apache.ibatis.annotations.Mapper;

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
      * 添加docker
      * @param docker docker信息
     */
    void insertDockerInfo(Docker docker);
}
