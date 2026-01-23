package com.example.demo.mapper.task.actionContent;

import org.apache.ibatis.annotations.*;

import java.util.Map;
import java.util.List;

@Mapper
public interface ApiParamsMapper {

    /**
     * 插入参数
     * @param paramsList 参数列表
     */
    void insert(@Param("paramsList") List<Map<String, String>> paramsList, @Param("apiId") String apiId);

    /**
     * 删除参数
     * @param apiId apiId
     */
    @Delete("delete from api_params where api_id = #{apiId}")
    void delete(String apiId);

    /**
     * 查询参数
     * @param apiId apiId
     * @return 参数
     */
    @Select("select * from api_params where api_id = #{apiId}")
    @MapKey("param_key")
    Map<String, String> select(String apiId);
}
