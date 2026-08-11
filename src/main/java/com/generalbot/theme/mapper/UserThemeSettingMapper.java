package com.generalbot.theme.mapper;

import com.generalbot.theme.entity.UserThemeSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户主题选择表映射。
 */
@Mapper
public interface UserThemeSettingMapper {

    /**
     * 按用户和主题模式查询当前主题选择。
     */
    @Select("select * from user_theme_setting where user_id = #{userId} and mode = #{mode}")
    UserThemeSetting selectByUserIdAndMode(@Param("userId") String userId, @Param("mode") String mode);

    /**
     * 新增或更新用户当前主题选择。
     */
    int upsert(UserThemeSetting setting);
}
