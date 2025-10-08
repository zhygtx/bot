package com.news.bot.mapper;

import com.news.bot.pojo.GroupPermission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GroupPermissionMapper {

    @Insert("insert into group_permission(group_id, permission, create_time) VALUES (#{groupPermission.groupId},#{groupPermission.permission},#{groupPermission.createTime})")
    void insert(@Param("groupPermission") GroupPermission groupPermission);

    void delete(@Param("groupPermission")GroupPermission groupPermission);

    @Select("select exists(select 1 from group_permission where group_id=#{groupId} and permission=#{permission})")
    boolean hasPermission(@Param("groupId") long groupId,@Param("permission") String permission);
}
