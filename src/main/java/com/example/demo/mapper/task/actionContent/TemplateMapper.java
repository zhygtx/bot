package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Template;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TemplateMapper {

    /**
     * 根据模板ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    @Select("SELECT * FROM template WHERE id = #{id}")
    Template getById(String id);

    /**
     * 获取所有模板
     * @return 模板列表
     */
    @Select("SELECT * FROM template")
    List<Template> getAllTemplates();

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    @Select("SELECT * FROM template WHERE id = #{id}")
    Template selectById(@Param("id") String id);

    /**
     * 根据用户ID获取模板列表
     * @param userId 用户ID
     * @return 模板列表
     */
    @Select("SELECT * FROM template WHERE user_id = #{userId}")
    List<Template> selectByUserId(@Param("userId") String userId);

    /**
     * 插入模板
     * @param template 模板
     * @return 插入数量
     */
    @Insert("INSERT INTO template (id, user_id, name, content, width, height, data_id, template_type) VALUES (#{id}, #{userId}, #{name}, #{content}, #{width}, #{height}, #{dataId}, #{templateType})")
    int insert(Template template);

    /**
     * 更新模板
     * @param template 模板
     * @return 更新数量
     */
    @Update("UPDATE template SET user_id = #{userId}, name = #{name}, content = #{content}, width = #{width}, height = #{height}, data_id = #{dataId}, template_type = #{templateType} WHERE id = #{id}")
    int update(Template template);

    /**
     * 根据ID删除模板
     * @param id 模板ID
     * @return 删除数量
     */
    @Delete("DELETE FROM template WHERE id = #{id}")
    int deleteById(@Param("id") String id);

}
