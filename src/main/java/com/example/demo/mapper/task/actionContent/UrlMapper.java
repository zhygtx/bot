package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Url;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UrlMapper {

    /**
     * 根据id获取url
     * @param id url id
     * @return url
     */
    @Select("select * from url where id = #{id}")
    Url getUrlById(String id);

    /**
     * 根据url id获取url参数
     * @param id url id
     * @return url参数
     */
    @Select("select params_key, params_value from url_params where url_id = #{id}")
    @MapKey("params_key")
    List<Map<String, String>> getParamsById(String id);
}
