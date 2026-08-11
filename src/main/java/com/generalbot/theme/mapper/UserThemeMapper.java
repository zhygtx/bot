package com.generalbot.theme.mapper;

import com.generalbot.theme.entity.UserTheme;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户自定义主题表映射。
 */
@Mapper
public interface UserThemeMapper {

    /**
     * 插入用户自定义主题。
     */
    int insert(UserTheme theme);

    /**
     * 更新用户自定义主题。
     */
    int update(UserTheme theme);

    /**
     * 查询当前用户的全部自定义主题。
     */
    @Select("select * from user_theme where user_id = #{userId} order by update_time desc")
    List<UserTheme> selectByUserId(String userId);

    /**
     * 查询当前用户的一条自定义主题。
     */
    @Select("select * from user_theme where id = #{id} and user_id = #{userId}")
    UserTheme selectByIdAndUserId(@Param("id") String id, @Param("userId") String userId);

    /**
     * 删除当前用户的一条自定义主题。
     */
    @Delete("delete from user_theme where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") String id, @Param("userId") String userId);
}
