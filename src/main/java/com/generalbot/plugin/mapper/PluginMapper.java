package com.generalbot.plugin.mapper;

import com.generalbot.plugin.dto.PluginInfoDto;
import com.generalbot.plugin.entity.PluginInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 插件信息映射接口
 */
@Mapper
public interface PluginMapper {

    /**
     * 插入插件信息
     * @param pluginInfo 插件信息
     */
    int insert(PluginInfo pluginInfo);

    /**
     * 删除插件信息
     * @param id 插件ID
     */
    @Delete("delete from plugin_info where id = #{id}")
    int delete(String id);

    /**
     * 更新插件信息
     * @param pluginInfo 插件信息
     */
    int update(PluginInfo pluginInfo);

    /**
     * 更新插件信息
     * @param id 插件ID
     * @param isPublic 是否公开
     */
    @Update("update plugin_info set is_public = #{isPublic} where id = #{id}")
    int updatePublic(@Param("id") String id, @Param("isPublic") boolean isPublic);

    /**
     * 根据作者ID和插件名称查询插件信息是否存在
     * @param authorId 作者ID
     * @param name 插件名称
     * @return 存在返回true，否则返回false
     */
    @Select("select exists(select * from plugin_info where author_id = #{authorId} and name = #{name})")
    Boolean existsByAuthorIdAndName(String authorId, String name);

    /**
     * 根据ID查询插件信息
     * @param id 插件ID
     * @return 插件信息
     */
    PluginInfo selectById(String id);

    /**
     * 根据ID和版本ID查询插件信息
     * @param id 插件ID
     * @param versionId 插件版本ID
     * @return 插件信息
     */
    PluginInfo selectByPluginIdAndVersionId(String id, String versionId);

    /**
     * 根据内容、作者ID和是否公开查询插件信息
     * @param content 内容
     * @param authorId 作者ID
     * @param isPublic 是否公开
     * @return 插件信息列表
     */
    List<PluginInfoDto> selectPlugins(@Param("content") String content, @Param("authorId") String authorId, @Param("isPublic") Boolean isPublic);

    /**
     * 根据插件ID查询插件信息
     * @param pluginId 插件ID
     * @return 插件信息
     */
    @MapKey("version")
    List<Map<String, Object>> selectPluginVersionByPluginId(String pluginId);
}
