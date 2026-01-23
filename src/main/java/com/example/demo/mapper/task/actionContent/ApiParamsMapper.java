package com.example.demo.mapper.task.actionContent;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Select("SELECT param_key, param_value FROM api_params WHERE api_id = #{apiId}")
    List<Map<String, Object>> selectByApiId(String apiId);

    default Map<String, String> select(String apiId){
        List<Map<String,Object>> rows = selectByApiId(apiId);
        return rows.stream()
                .collect(Collectors.toMap(
                        r -> (String) r.get("param_key"),
                        r -> (String) r.get("param_value")
                ));
    }
}
