package com.news.bot.wf.mapper;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.wf.pojo.FissureTask;
import org.apache.ibatis.annotations.*;
import com.news.bot.wf.pojo.Fissure;

import java.util.List;

@Mapper
public interface FissureMapper {

    void batchInsert(@Param("list") List<Fissure> fissures);

    @Delete("TRUNCATE TABLE fissure")
    void clearFissures();

    @Select("SELECT id FROM fissure")
    List<String> getActiveIds();

    List<Fissure> selectByKey(@Param("key") String key);

    void fissureStatistic(@Param("list") List<Fissure> fissures);

    @Insert("INSERT INTO fissure_task(id, group_id, user_id, node,tier, mission_type, is_hard, is_persist,is_storm) " +
            "VALUES (#{fissureTask.id}, #{fissureTask.groupId}, #{fissureTask.userId}, #{fissureTask.node},#{fissureTask.tier}, #{fissureTask.missionType}, #{fissureTask.isHard}, #{fissureTask.isPersist},#{fissureTask.isStorm})")
    void insertFissureTask(@Param("fissureTask") FissureTask fissureTask);

    int deleteFissureTask(@Param("fissureTask") FissureTask fissureTask);

    @Select("SELECT * FROM fissure_task")
    List<FissureTask> selectAllFissureTasks();

    List<FissureTask> selectFissureTaskByUserOrGroup(@Param("event") GroupMessageEvent event);

    @Select("SELECT st.template_name FROM user_tpl st, user u " +
            "WHERE st.user_id = u.user_id AND st.group_id = #{groupId} AND u.qq_id = #{userId}")
    String selectTemplateName(Long groupId, Long userId);
}