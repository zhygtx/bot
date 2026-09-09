package com.generalbot.workflow.mapper;

import com.generalbot.workflow.entity.execution.BigText;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 大数据存储 Mapper。
 */
@Mapper
public interface BigTextMapper {

    /**
     * 批量插入大数据记录
     * @param bigTextList 大数据列表
     */
    int insertBatch(@Param("bigTextList") List<BigText> bigTextList);

    /**
     * 按引用键查询大数据内容
     * @param key 引用键
     * @return 大数据内容
     */
    @Select("SELECT value FROM big_text WHERE `key` = #{key}")
    String selectByKey(String key);
}
