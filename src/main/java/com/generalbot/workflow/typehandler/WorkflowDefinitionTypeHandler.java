package com.generalbot.workflow.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generalbot.workflow.entity.definition.WorkflowDefinition;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * WorkflowDefinition 与 LONGTEXT JSON 之间的类型转换。
 */
public class WorkflowDefinitionTypeHandler extends BaseTypeHandler<WorkflowDefinition> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将工作流定义序列化为 JSON 后写入 LONGTEXT 列。
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, WorkflowDefinition parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("序列化工作流定义失败", e);
        }
    }

    /**
     * 按列名读取 JSON 并反序列化为工作流定义。
     */
    @Override
    public WorkflowDefinition getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    /**
     * 按列索引读取 JSON 并反序列化为工作流定义。
     */
    @Override
    public WorkflowDefinition getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    /**
     * 存储过程调用时按列索引读取 JSON。
     */
    @Override
    public WorkflowDefinition getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    /**
     * JSON 字符串转工作流定义，空值返回 null。
     */
    private WorkflowDefinition parse(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, WorkflowDefinition.class);
        } catch (Exception e) {
            throw new SQLException("反序列化工作流定义失败", e);
        }
    }
}
