package com.news.web.mapper;

import com.news.web.pojo.UserTpl;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserTplMapper {

    @Insert("insert into user_tpl(group_id,user_id, template_name) values (#{groupId},#{userId}, #{templateName})")
    int insert(UserTpl userTpl);

    @Select("select * from user_tpl where user_id=#{userId}")
    UserTpl selectByUserId(Long userId);

    @Update("update user_tpl set template_name=#{templateName} where user_id=#{userId} and group_id=#{groupId}")
    void update(UserTpl userTpl);

    @Delete("delete from user_tpl where user_id= #{userId} and template_name=#{templateName}")
    void delete(UserTpl userTpl);
}
