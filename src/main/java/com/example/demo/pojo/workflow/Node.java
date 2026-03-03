package com.example.demo.pojo.workflow;

import com.example.demo.pojo.plugin.MethodClassInfo;
import com.example.demo.pojo.plugin.MethodInfo;
import com.example.demo.pojo.plugin.PluginInfo;
import com.example.demo.pojo.plugin.PluginVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工作流节点(一个节点对应一个方法)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node implements Comparable<Node>{

    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点位置（垂直位置）
     */
    private Integer y;

    /**
     * 节点位置（水平位置）
     */
    private Integer x;

    /**
     * 所属工作流ID
     */
    private String workflowId;

    /**
     * 使用的插件的ID
     */
    private String pluginId;

    /**
     * 使用的插件版本ID
     */
    private String pluginVersionId;

    /**
     * 使用的方法类ID
     */
    private String methodClassId;

    /**
     * 使用的方法的ID
     */
    private String methodId;

    /**
     * 入度
     */
    private Integer inDegree;

    /**
     * 数据映射
     */
    private List<DataMap> dataMaps;

    /**
     * 前置节点ID
     */
    private List<String> preNodeId;

    /**
     * 后置节点ID
     */
    private List<String> nextNodeId;

    /**
     * 节点默认值
     */
    private List<NodeDefaults> nodeDefaults;

    /**
     * 插件信息
     */
    private PluginInfo pluginInfo;

    /**
     * 插件版本信息
     */
    private PluginVersion pluginVersion;

    /**
     * 方法类信息
     */
    private MethodClassInfo methodClassInfo;

    /**
     * 方法信息
     */
    private MethodInfo methodInfo;

    @Override
    public int compareTo(Node other) {
        int degreeCompare = Integer.compare(this.inDegree, other.inDegree);
        return degreeCompare != 0 ? degreeCompare : this.id.compareTo(other.id);
    }
}