package com.generalbot.workflow.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generalbot.workflow.entity.execution.ExecutionTrace;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ExecutionTrace 与 LONGTEXT JSON 之间的类型转换。
 */
public class ExecutionTraceTypeHandler extends BaseTypeHandler<ExecutionTrace> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将执行轨迹序列化为 JSON 后写入 LONGTEXT 列。
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ExecutionTrace parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("序列化执行轨迹失败", e);
        }
    }

    /**
     * 按列名读取 JSON 并反序列化为执行轨迹。
     */
    @Override
    public ExecutionTrace getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    /**
     * 按列索引读取 JSON 并反序列化为执行轨迹。
     */
    @Override
    public ExecutionTrace getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    /**
     * 存储过程调用时按列索引读取 JSON。
     */
    @Override
    public ExecutionTrace getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    /**
     * JSON 字符串转执行轨迹，空值返回空轨迹。
     */
    private ExecutionTrace parse(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return new ExecutionTrace();
        }
        try {
            return MAPPER.readValue(json, ExecutionTrace.class);
        } catch (Exception e) {
            throw new SQLException("反序列化执行轨迹失败", e);
        }
    }
}
