package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Url;
import org.apache.ibatis.annotations.*;

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

    /**
     * 获取所有URL
     * @return URL列表
     */
    @Select("SELECT * FROM url")
    List<Url> getAllUrls();

    /**
     * 根据用户ID获取URL列表
     * @param userId 用户ID
     * @return URL列表
     */
    @Select("SELECT * FROM url WHERE user_id = #{userId}")
    List<Url> selectByUserId(@Param("userId") String userId);

    /**
     * 插入URL
     * @param url URL
     * @return 插入数量
     */
    @Insert("INSERT INTO url (id, name ,user_id, url) VALUES (#{id}, #{name},#{userId}, #{url})")
    int insert(Url url);

    /**
     * 更新URL
     * @param url URL
     * @return 更新数量
     */
    @Update("UPDATE url SET name =#{name},url = #{url} WHERE id = #{id}")
    int update(Url url);

    /**
     * 根据ID删除URL
     * @param id URL ID
     * @return 删除数量
     */
    @Delete("DELETE FROM url WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    /**
     * 插入URL参数
     * @param urlId URL ID
     * @param key 参数键
     * @param value 参数值
     */
    @Insert("INSERT INTO url_params (url_id, params_key, params_value) VALUES (#{urlId}, #{key}, #{value})")
    void insertUrlParam(@Param("urlId") String urlId, @Param("key") String key, @Param("value") String value);

    /**
     * 更新URL参数
     * @param urlId URL ID
     * @param key 参数键
     * @param value 参数值
     * @return 更新数量
     */
    @Update("UPDATE url_params SET params_value = #{value} WHERE url_id = #{urlId} AND params_key = #{key}")
    int updateUrlParam(@Param("urlId") String urlId, @Param("key") String key, @Param("value") String value);

    /**
     * 根据URL ID删除所有参数
     * @param urlId URL ID
     */
    @Delete("DELETE FROM url_params WHERE url_id = #{urlId}")
    void deleteUrlParamsByUrlId(@Param("urlId") String urlId);
}
