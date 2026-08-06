package com.generalbot.workflow.mapper;

import com.generalbot.workflow.entity.WorkflowCanvasView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowCanvasViewMapper {

    @Insert("insert into workflow_canvas_view (workflow_id, user_id,offset_x, offset_y, scale) " +
            "values (#{workflowId}, #{userId},  #{offsetX}, #{offsetY}, #{scale})")
    int insert(WorkflowCanvasView record);

}
