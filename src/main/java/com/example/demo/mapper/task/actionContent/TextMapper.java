package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Text;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TextMapper {

    /**
     * 根据ID获取文本动作细节
     * @param id 动作ID
     * @return 文本动作细节
     */
    @Select("SELECT * FROM text WHERE id = #{id}")
    Text getText(String id);

}
