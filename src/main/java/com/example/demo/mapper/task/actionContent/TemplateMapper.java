package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TemplateMapper {

    /**
     * 根据模板ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    @Select("SELECT * FROM template WHERE id = #{id}")
    Template getById(String id);

}
