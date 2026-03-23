package com.example.demo.mapper.plugin;

import com.example.demo.pojo.plugin.PluginInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

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

    @Update("update plugin_info set is_public = #{isPublic} where id = #{id}")
    int updatePublic(@Param("id") String id, @Param("isPublic") boolean isPublic);

    @Select("select exists(select * from plugin_info where author_id = #{authorId} and name = #{name})")
    Boolean existsByAuthorIdAndName(String authorId, String name);

    /**
     * 根据ID查询插件信息
     * @param id 插件ID
     * @return 插件信息
     */
    PluginInfo selectById(String id);

    /**
     * 根据用户ID查询插件信息
     * @param authorId 作者ID
     * @return 插件信息列表
     */
    List<PluginInfo> selectByAuthorId(String authorId);

    /**
     * 根据作者ID计算插件总数
     * @param authorId 作者ID
     * @return 插件总数
     */
    int countByAuthorId(String authorId);
}
