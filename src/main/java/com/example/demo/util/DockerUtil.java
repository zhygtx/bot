package com.example.demo.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DockerUtil {

    private static final String IMAGE_NAME = "mlikiowa/napcat-docker:latest";

    @Value("${server.port}")
    private int serverPort;

    @Value("${napcat.ws.server.url}")
    private String wsUrl;

    // 创建 Docker 客户端实例
    private final DockerClient dockerClient;

    public DockerUtil() {
        // 配置 Docker 客户端 - 使用 TCP 连接
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://127.0.0.1:2375")
                .withDockerCertPath("")
                .withDockerTlsVerify(false)
                .build();

        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(httpClient)
                .build();

        log.info("Docker客户端已初始化，Docker主机: {}", config.getDockerHost());
    }

    /**
     * 创建容器并自动处理配置文件
     * @param hostPort 主机端口
     * @param containerName 容器名
     * @param token 环境变量中的WEBUI_TOKEN值
     * @param botToken Bot鉴权token（用于onebot11.json中的token字段）
     * @param pathSuffix Bot连接路径后缀（用于构造WS URL）
     * @param botQQ 机器人QQ号（用于命名配置文件）
     * @return 创建的容器ID
     */
    public String createContainerWithAutoConfig(int hostPort, String containerName, String token, String botToken, String pathSuffix, Long botQQ) {
        log.info("开始创建容器，主机端口: {}, 容器名称: {}, token: {}", hostPort, containerName, token != null ? "***" : null);

        try {
            // 检查镜像是否存在（不存在则尝试拉取）
            final String imageName = IMAGE_NAME;
            try {
                log.info("检查镜像是否存在: {}", imageName);
                dockerClient.inspectImageCmd(imageName).exec();
                log.info("镜像已存在: {}", imageName);
            } catch (NotFoundException nf) {
                log.info("镜像不存在，开始拉取: {}", imageName);
                dockerClient.pullImageCmd(imageName).start().awaitCompletion();
                log.info("镜像拉取完成: {}", imageName);
            }

            // 自动获取程序所在目录
            String programDir = System.getProperty("user.dir");
            log.info("获取程序运行目录: {}", programDir);
            String configDir = Paths.get(programDir, "config").toString();
            File configFile = Paths.get(configDir, "onebot11_" + botQQ + ".json").toFile();
            log.info("配置文件路径: {}", configFile.getAbsolutePath());

            // 获取宿主IP
            String hostIp = getHostIpAddress();
            log.info("获取到宿主IP地址: {}", hostIp);

            // 始终覆盖写入配置文件（确保使用最新的Bot配置）
            log.info("写入配置文件: {}", configFile.getAbsolutePath());
            createOneBotConfigFile(configFile, hostIp, botToken, pathSuffix);
            log.info("配置文件写入完成: {}", configFile.getAbsolutePath());

            // 创建端口绑定 (宿主 hostPort -> 容器 6099)
            ExposedPort internalPort = ExposedPort.tcp(6099);
            Ports.Binding binding = new Ports.Binding("0.0.0.0", String.valueOf(hostPort));
            PortBinding portBinding = new PortBinding(binding, internalPort);
            log.info("创建端口绑定: 宿主端口 {} -> 容器端口 {}", hostPort, 6099);

            // 创建容器命令，只挂载 onebot11.json 文件
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withPortBindings(portBinding)
                    .withBinds(new Bind(configFile.getAbsolutePath(), new Volume("/app/napcat/config/onebot11.json"))) // 只挂载单个配置文件
                    .withNetworkMode("bridge") // 使用bridge网络
                    .withExtraHosts("host.docker.internal:host-gateway"); // 添加extra_hosts以支持Linux Docker Engine

            log.info("设置容器配置 - 端口绑定: {}, 配置文件挂载: {} -> /app/napcat/config/onebot11.json, 网络模式: bridge, extra_hosts: host.docker.internal:host-gateway",
                    portBinding, configFile.getAbsolutePath());

            CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(imageName)
                    .withName(containerName)
                    .withHostConfig(hostConfig)
                    .withExposedPorts(internalPort)
                    .withEnv("WEBUI_TOKEN=" + (token == null ? "" : token)); // 设置环境变量

            log.info("准备创建容器，镜像: {}, 容器名称: {}, 环境变量: WEBUI_TOKEN={}",
                    imageName, containerName, token != null ? "***" : "");

            // 执行创建容器
            String containerId = createContainerCmd.exec().getId();
            log.info("容器创建成功，容器ID: {}", containerId);

            // 启动容器
            log.info("启动容器: {}", containerId);
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动完成: {}", containerId);

            log.info("容器{}已创建并启动，容器端口 6099 映射到宿主端口 {}", containerName, hostPort);
            return containerId;

        } catch (Exception e) {
            log.error("创建容器时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("创建容器失败: " + e.getMessage(), e);
        }
    }


    /**
     * 获取宿主IP地址（跨平台兼容）
     * @return 宿主IP地址
     */
    public String getHostIpAddress() {
        // 优先使用 host.docker.internal（Docker Desktop 官方推荐）
        // Windows Docker Desktop、Mac Docker Desktop、Linux Docker Desktop 4.28+ 都支持
        log.info("使用 host.docker.internal 作为容器访问宿主机的地址（跨平台兼容）");
        return "host.docker.internal";
    }


    /**
     * 查找可用的宿主端口：从 6099 开始，向两边扩展查找（6099, 6098, 6100, 6097, 6101 ...）
     * 首个可用端口立即返回；如果全部范围内都不可用则抛出 IOException
     * @return 第一个可用端口
     * @throws IOException 端口检查失败或未找到可用端口时抛出
     */
    public int findAvailableHostPort() throws IOException {
        final int basePort = 6099;
        final int minPort = 1024;   // 避开特权端口
        final int maxPort = 65535;

        log.debug("开始查找可用端口，范围: {}-{}, 基准端口: {}", minPort, maxPort, basePort);

        // 先尝试基准端口
        if (isTcpPortAvailable(basePort)) {
            log.debug("基准端口 {} 可用", basePort);
            return basePort;
        } else {
            log.debug("基准端口 {} 不可用", basePort);
        }

        // 两边交替查找
        int maxOffset = Math.max(basePort - minPort, maxPort - basePort);
        log.debug("最大偏移量: {}", maxOffset);

        for (int offset = 1; offset <= maxOffset; offset++) {
            int lower = basePort - offset;
            if (lower >= minPort) {
                log.debug("检查端口: {} minPort: {}", lower,minPort);
                if (isTcpPortAvailable(lower)) {
                    log.debug("端口 {} 可用 minPort: {}", lower,minPort);
                    return lower;
                }
            }

            int upper = basePort + offset;
            if (upper < maxPort) {
                log.debug("检查端口: {} maxPort: {}", upper, maxPort);
                if (isTcpPortAvailable(upper)) {
                    log.debug("端口 {} 可用 maxPort: {}", upper,maxPort);
                    return upper;
                }
            }
        }
        log.warn("未找到可访问的端口，请检查端口占用情况");
        throw new IOException("未能找到可用的宿主端口（在 1024-65535 范围内）");
    }

    /**
     * 检查指定 TCP 端口是否可用（能在本机绑定）
     * @param port 要检查的端口
     * @return 可用返回 true，否则 false
     */
    private boolean isTcpPortAvailable(int port) {
        log.debug("检查端口 {} 的可用性", port);
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            log.debug("端口 {} 不可用: {}", port, e.getMessage());
            return false;
        }
    }

    /**
     * 创建 onebot11.json 配置文件
     * @param configFile 配置文件对象
     * @param hostIp 宿主IP地址
     * @param botToken Bot鉴权token
     * @param pathSuffix Bot连接路径后缀
     * @throws IOException 文件操作异常
     */
    private void createOneBotConfigFile(File configFile, String hostIp, String botToken, String pathSuffix) throws IOException {
        log.info("开始创建配置文件: {}", configFile.getAbsolutePath());

        // 创建父目录
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            log.info("创建配置目录: {}", parentDir.getAbsolutePath());
            boolean created = parentDir.mkdirs();
            if (!created) {
                log.warn("无法创建配置目录: {}", parentDir.getAbsolutePath());
                throw new IOException("无法创建配置目录: " + parentDir.getAbsolutePath());
            }
            log.info("配置目录创建成功: {}", parentDir.getAbsolutePath());
        }

        // 准备JSON内容，使用动态端口、动态WS路径、动态token
        String jsonContent = "{\n" +
                "  \"network\": {\n" +
                "    \"websocketClients\": [\n" +
                "      {\n" +
                "        \"enable\": true,\n" +
                "        \"name\": \"rws\",\n" +
                "        \"url\": \"ws://" + hostIp + ":" + serverPort + wsUrl + "/" + pathSuffix + "\",\n" +
                "        \"reportSelfMessage\": false,\n" +
                "        \"messagePostFormat\": \"array\",\n" +
                "        \"token\": \"" + (botToken == null ? "" : botToken) + "\",\n" +
                "        \"info\": false,\n" +
                "        \"heartInterval\": 5000,\n" +
                "        \"reconnectInterval\": 5000\n" +
                "      }\n" +
                "    ],\n" +
                "    \"plugins\": []\n" +
                "  },\n" +
                "  \"musicSignUrl\": \"\",\n" +
                "  \"enableLocalFile2Url\": false,\n" +
                "  \"parseMultMsg\": false\n" +
                "}";

        log.info("准备写入配置文件内容到: {}", configFile.getAbsolutePath());

        // 写入文件
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(jsonContent);
            log.info("配置文件写入完成: {}", configFile.getAbsolutePath());
        }
    }

    /**
     * 删除容器
     * @param containerId 容器ID
     */
    public void deleteContainer(String containerId) {
        try {
            log.info("开始删除容器: {}", containerId);

            try {
                dockerClient.stopContainerCmd(containerId).exec();
                log.info("容器已停止: {}", containerId);
            } catch (Exception e) {
                log.warn("容器可能已经停止或不存在: {} - 错误: {}", containerId, e.getMessage());
            }

            // 删除容器
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)  // 强制删除（即使容器正在运行）
                    .exec();

            log.info("容器删除成功: {}", containerId);

        } catch (Exception e) {
            log.error("删除容器时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 列出容器中的文件
     * @param containerId 容器ID
     * @return 文件列表
     */
    public List<String> listFilesInContainer(String containerId) {
        String path = "/app/napcat/config/";
        try {
            ExecCreateCmdResponse execCreate = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "ls -p " + path + " | grep -v /")
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();

            // 创建回调
            ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame frame) {
                    try {
                        // 将输出写入stdout
                        stdout.write(frame.getPayload());
                    } catch (IOException e) {
                        // 记录但不阻塞回调
                    }
                }
            };
            //
            dockerClient.execStartCmd(execCreate.getId()).exec(callback);
            callback.awaitCompletion(); // 等待命令执行完
            String output = stdout.toString(StandardCharsets.UTF_8);
            // 将输出转换为列表
            return Arrays.stream(output.split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("列出容器 {} 中的文件时出错: {}", containerId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查容器是否正在运行
     * @param containerId 容器ID
     * @return 运行状态
     */
    public Boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
            return inspectContainerResponse.getState().getRunning();
        } catch (Exception e) {
            log.error("检查容器 {} 运行状态时出错: {}", containerId, e.getMessage(), e);
            return false;
        }
    }

}
