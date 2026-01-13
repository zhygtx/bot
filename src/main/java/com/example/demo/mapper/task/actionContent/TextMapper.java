package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Text;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TextMapper {

    /**
     * 根据ID获取文本动作细节
     * @param id 动作ID
     * @return 文本动作细节
     */
    @Select("SELECT * FROM text WHERE id = #{id}")
    Text getText(String id);

    /**
     * 获取所有文本内容
     * @return 文本内容列表
     */
    @Select("SELECT * FROM text")
    List<Text> getAllTexts();

    /**
     * 根据ID获取文本内容
     * @param id 文本内容ID
     * @return 文本内容
     */
    @Select("SELECT * FROM text WHERE id = #{id}")
    Text selectById(@Param("id") String id);

    /**
     * 根据用户ID获取文本内容列表
     * @param userId 用户ID
     * @return 文本内容列表
     */
    @Select("SELECT * FROM text WHERE user_id = #{userId}")
    List<Text> selectByUserId(@Param("userId") String userId);

    /**
     * 插入文本内容
     * @param text 文本内容
     * @return 插入数量
     */
    @Insert("INSERT INTO text (id, user_id, text) VALUES (#{id}, #{userId}, #{text})")
    int insert(Text text);

    /**
     * 更新文本内容
     * @param text 文本内容
     * @return 更新数量
     */
    @Update("UPDATE text SET user_id = #{userId}, text = #{text} WHERE id = #{id}")
    int update(Text text);

    /**
     * 根据ID删除文本内容
     * @param id 文本内容ID
     * @return 删除数量
     */
    @Delete("DELETE FROM text WHERE id = #{id}")
    int deleteById(@Param("id") String id);

}
