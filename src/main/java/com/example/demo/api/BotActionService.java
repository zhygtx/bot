package com.example.demo.api;

import com.example.demo.annotation.ActionParam;
import com.example.demo.annotation.BotAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.zhygtx.napcat.api.response.extra.*;
import com.github.zhygtx.napcat.api.response.file.*;
import com.github.zhygtx.napcat.api.response.friend.*;
import com.github.zhygtx.napcat.api.response.group.*;
import com.github.zhygtx.napcat.api.response.message.*;
import com.github.zhygtx.napcat.api.response.system.*;
import java.util.List;

@SuppressWarnings("unused")
public interface BotActionService {

    /**
     * 检查URL安全性。
     * <p>
     * 检查指定URL的安全等级
     * <p>
     * 对应 NapCat API: {@code check_url_safely}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param url 【必填】要检查的 URL
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "检查URL安全性",
        description = "检查指定URL的安全等级",
        categories = {"Go-CQHTTP"}
    )
    UrlSafelyData checkUrlSafely(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                 @ActionParam(description = "要检查的 URL", order = 1) String url);

    /**
     * 清理流式传输临时文件。
     * <p>
     * 分类：流式传输扩展
     * <p>
     * 对应 NapCat API: {@code clean_stream_temp_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "清理流式传输临时文件",
        description = "分类：流式传输扩展",
        categories = {"流式传输扩展"}
    )
    void cleanStreamTempFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 创建收藏。
     * <p>
     * 分类：扩展接口
     * <p>
     * 对应 NapCat API: {@code create_collection}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param rawData 【必填】原始数据
     *
     * @param brief 【必填】简要描述
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "创建收藏",
        description = "分类：扩展接口",
        categories = {"扩展接口"}
    )
    CollectionData createCollection(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                    @ActionParam(description = "原始数据", order = 1) String rawData,
                                    @ActionParam(description = "简要描述", order = 2) String brief);

    /**
     * 创建群文件目录。
     * <p>
     * 在群文件系统中创建新的文件夹
     * <p>
     * 对应 NapCat API: {@code create_group_file_folder}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param folderName 【可选】文件夹名称
     *
     * @param name 【可选】文件夹名称
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "创建群文件目录",
        description = "在群文件系统中创建新的文件夹",
        categories = {"Go-CQHTTP"}
    )
    GroupFileFolderData createGroupFileFolder(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                              @ActionParam(description = "群号", order = 1) Long groupId,
                                              @ActionParam(description = "文件夹名称", order = 2, nullable = true) String folderName,
                                              @ActionParam(description = "文件夹名称", order = 3, nullable = true) String name);

    /**
     * 删除好友。
     * <p>
     * 从好友列表中删除指定用户
     * <p>
     * 对应 NapCat API: {@code delete_friend}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param friendId 【可选】好友 QQ 号
     *
     * @param userId 【可选】用户 QQ 号
     *
     * @param tempBlock 【可选】是否加入黑名单
     *
     * @param tempBothDel 【可选】是否双向删除
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "删除好友",
        description = "从好友列表中删除指定用户",
        categories = {"Go-CQHTTP"}
    )
    String deleteFriend(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "好友 QQ 号", order = 1, nullable = true) String friendId,
                        @ActionParam(description = "用户 QQ 号", order = 2, nullable = true) Long userId,
                        @ActionParam(description = "是否加入黑名单", order = 3, nullable = true) Boolean tempBlock,
                        @ActionParam(description = "是否双向删除", order = 4, nullable = true) Boolean tempBothDel);

    /**
     * 删除群文件。
     * <p>
     * 在群文件系统中删除指定的文件
     * <p>
     * 对应 NapCat API: {@code delete_group_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param fileId 【必填】文件ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "删除群文件",
        description = "在群文件系统中删除指定的文件",
        categories = {"Go-CQHTTP"}
    )
    void deleteGroupFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "群号", order = 1) Long groupId,
                         @ActionParam(description = "文件ID", order = 2) String fileId);

    /**
     * 删除群文件目录。
     * <p>
     * 在群文件系统中删除指定的文件夹
     * <p>
     * 对应 NapCat API: {@code delete_group_folder}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param folderId 【可选】文件夹ID
     *
     * @param folder 【可选】文件夹ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "删除群文件目录",
        description = "在群文件系统中删除指定的文件夹",
        categories = {"Go-CQHTTP"}
    )
    void deleteGroupFolder(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                           @ActionParam(description = "群号", order = 1) Long groupId,
                           @ActionParam(description = "文件夹ID", order = 2, nullable = true) String folderId,
                           @ActionParam(description = "文件夹ID", order = 3, nullable = true) String folder);

    /**
     * 下载文件。
     * <p>
     * 下载网络文件到本地临时目录
     * <p>
     * 对应 NapCat API: {@code download_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param url 【可选】下载链接
     *
     * @param base64 【可选】base64数据
     *
     * @param name 【可选】文件名
     *
     * @param headers 【可选】请求头
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "下载文件",
        description = "下载网络文件到本地临时目录",
        categories = {"Go-CQHTTP"}
    )
    FileRecordStreamData downloadFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                      @ActionParam(description = "下载链接", order = 1, nullable = true) String url,
                                      @ActionParam(description = "base64数据", order = 2, nullable = true) String base64,
                                      @ActionParam(description = "文件名", order = 3, nullable = true) String name,
                                      @ActionParam(description = "请求头", order = 4, nullable = true) String headers);

    /**
     * 下载图片文件流。
     * <p>
     * 分类：流式传输扩展
     * <p>
     * 对应 NapCat API: {@code download_file_image_stream}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【可选】文件路径或 URL
     *
     * @param fileId 【可选】文件 ID
     *
     * @param chunkSize 【可选】分块大小 (字节)（默认 65536）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "下载图片文件流",
        description = "分类：流式传输扩展",
        categories = {"流式传输扩展"}
    )
    FileRecordStreamData downloadFileImageStream(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                                 @ActionParam(description = "文件路径或 URL", order = 1, nullable = true) String file,
                                                 @ActionParam(description = "文件 ID", order = 2, nullable = true) String fileId,
                                                 @ActionParam(description = "分块大小 (字节)（默认 65536）", order = 3, nullable = true) Integer chunkSize);

    /**
     * 下载语音文件流。
     * <p>
     * 分类：流式传输扩展
     * <p>
     * 对应 NapCat API: {@code download_file_record_stream}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【可选】文件路径或 URL
     *
     * @param fileId 【可选】文件 ID
     *
     * @param chunkSize 【可选】分块大小 (字节)（默认 65536）
     *
     * @param outFormat 【可选】输出格式
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "下载语音文件流",
        description = "分类：流式传输扩展",
        categories = {"流式传输扩展"}
    )
    FileRecordStreamData downloadFileRecordStream(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                                  @ActionParam(description = "文件路径或 URL", order = 1, nullable = true) String file,
                                                  @ActionParam(description = "文件 ID", order = 2, nullable = true) String fileId,
                                                  @ActionParam(description = "分块大小 (字节)（默认 65536）", order = 3, nullable = true) Integer chunkSize,
                                                  @ActionParam(description = "输出格式", order = 4, nullable = true) String outFormat);

    /**
     * 下载文件流。
     * <p>
     * 以流式方式从网络或本地下载文件
     * <p>
     * 对应 NapCat API: {@code download_file_stream}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【可选】文件路径或 URL
     *
     * @param fileId 【可选】文件 ID
     *
     * @param chunkSize 【可选】分块大小 (字节)（默认 65536）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "下载文件流",
        description = "以流式方式从网络或本地下载文件",
        categories = {"流式接口"}
    )
    FileStreamData downloadFileStream(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                      @ActionParam(description = "文件路径或 URL", order = 1, nullable = true) String file,
                                      @ActionParam(description = "文件 ID", order = 2, nullable = true) String fileId,
                                      @ActionParam(description = "分块大小 (字节)（默认 65536）", order = 3, nullable = true) Integer chunkSize);

    /**
     * 获取AI角色列表。
     * <p>
     * 获取群聊中的AI角色列表
     * <p>
     * 对应 NapCat API: {@code get_ai_characters}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param chatType 【必填】聊天类型（默认 1）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取AI角色列表",
        description = "获取群聊中的AI角色列表",
        categories = {"扩展接口"}
    )
    List<AiCharactersData> getAiCharacters(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                           @ActionParam(description = "群号", order = 1) Long groupId,
                                           @ActionParam(description = "聊天类型（默认 1）", order = 2) Long chatType);

    /**
     * 获取 AI 语音。
     * <p>
     * 通过 AI 语音引擎获取指定文本的语音 URL
     * <p>
     * 对应 NapCat API: {@code get_ai_record}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param character 【必填】角色ID
     *
     * @param groupId 【必填】群号
     *
     * @param text 【必填】语音文本内容
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取 AI 语音",
        description = "通过 AI 语音引擎获取指定文本的语音 URL",
        categories = {"AI 扩展"}
    )
    String getAiRecord(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "角色ID", order = 1) String character,
                       @ActionParam(description = "群号", order = 2) Long groupId,
                       @ActionParam(description = "语音文本内容", order = 3) String text);

    /**
     * 获取ClientKey。
     * <p>
     * 获取当前登录帐号的ClientKey
     * <p>
     * 对应 NapCat API: {@code get_clientkey}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取ClientKey",
        description = "获取当前登录帐号的ClientKey",
        categories = {"扩展接口"}
    )
    ClientkeyData getClientkey(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取合并转发消息。
     * <p>
     * 获取合并转发消息的具体内容
     * <p>
     * 对应 NapCat API: {@code get_forward_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【可选】消息ID
     *
     * @param id 【可选】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取合并转发消息",
        description = "获取合并转发消息的具体内容",
        categories = {"Go-CQHTTP"}
    )
    GroupMsgHistoryData getForwardMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                      @ActionParam(description = "消息ID", order = 1, nullable = true) String messageId,
                                      @ActionParam(description = "消息ID", order = 2, nullable = true) String id);

    /**
     * 获取好友历史消息。
     * <p>
     * 获取指定好友的历史聊天记录
     * <p>
     * 对应 NapCat API: {@code get_friend_msg_history}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户QQ
     *
     * @param messageSeq 【可选】起始消息序号
     *
     * @param count 【必填】获取消息数量（默认 20）
     *
     * @param reverseOrder 【必填】是否反向排序（默认 False）
     *
     * @param disableGetUrl 【必填】是否禁用获取URL（默认 False）
     *
     * @param parseMultMsg 【必填】是否解析合并消息（默认 True）
     *
     * @param quickReply 【必填】是否快速回复（默认 False）
     *
     * @param reverseOrder2 【必填】是否反向排序(旧版本兼容)（默认 False）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取好友历史消息",
        description = "获取指定好友的历史聊天记录",
        categories = {"Go-CQHTTP"}
    )
    GroupMsgHistoryData getFriendMsgHistory(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                            @ActionParam(description = "用户QQ", order = 1) Long userId,
                                            @ActionParam(description = "起始消息序号", order = 2, nullable = true) String messageSeq,
                                            @ActionParam(description = "获取消息数量（默认 20）", order = 3) Integer count,
                                            @ActionParam(description = "是否反向排序（默认 False）", order = 4) Boolean reverseOrder,
                                            @ActionParam(description = "是否禁用获取URL（默认 False）", order = 5) Boolean disableGetUrl,
                                            @ActionParam(description = "是否解析合并消息（默认 True）", order = 6) Boolean parseMultMsg,
                                            @ActionParam(description = "是否快速回复（默认 False）", order = 7) Boolean quickReply,
                                            @ActionParam(description = "是否反向排序(旧版本兼容)（默认 False）", order = 8) Boolean reverseOrder2);

    /**
     * 获取群艾特全体剩余次数。
     * <p>
     * 获取指定群聊中艾特全体成员的剩余次数
     * <p>
     * 对应 NapCat API: {@code get_group_at_all_remain}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群艾特全体剩余次数",
        description = "获取指定群聊中艾特全体成员的剩余次数",
        categories = {"Go-CQHTTP"}
    )
    GroupAtAllRemainData getGroupAtAllRemain(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                             @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群文件夹文件列表。
     * <p>
     * 获取指定群文件夹下的文件及子文件夹列表
     * <p>
     * 对应 NapCat API: {@code get_group_files_by_folder}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param folderId 【可选】文件夹ID
     *
     * @param folder 【可选】文件夹ID
     *
     * @param fileCount 【必填】文件数量（默认 50）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群文件夹文件列表",
        description = "获取指定群文件夹下的文件及子文件夹列表",
        categories = {"Go-CQHTTP"}
    )
    GroupRootFilesData getGroupFilesByFolder(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                             @ActionParam(description = "群号", order = 1) Long groupId,
                                             @ActionParam(description = "文件夹ID", order = 2, nullable = true) String folderId,
                                             @ActionParam(description = "文件夹ID", order = 3, nullable = true) String folder,
                                             @ActionParam(description = "文件数量（默认 50）", order = 4) Integer fileCount);

    /**
     * 获取群文件系统信息。
     * <p>
     * 获取群聊文件系统的空间及状态信息
     * <p>
     * 对应 NapCat API: {@code get_group_file_system_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群文件系统信息",
        description = "获取群聊文件系统的空间及状态信息",
        categories = {"Go-CQHTTP"}
    )
    GroupFileSystemInfoData getGroupFileSystemInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                                   @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群荣誉信息。
     * <p>
     * 获取指定群聊的荣誉信息，如龙王等
     * <p>
     * 对应 NapCat API: {@code get_group_honor_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param type 【可选】荣誉类型
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群荣誉信息",
        description = "获取指定群聊的荣誉信息，如龙王等",
        categories = {"Go-CQHTTP"}
    )
    GroupHonorInfoData getGroupHonorInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                         @ActionParam(description = "群号", order = 1) Long groupId,
                                         @ActionParam(description = "荣誉类型", order = 2, nullable = true) String type);

    /**
     * 获取群历史消息。
     * <p>
     * 获取指定群聊的历史聊天记录
     * <p>
     * 对应 NapCat API: {@code get_group_msg_history}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param messageSeq 【可选】起始消息序号
     *
     * @param count 【必填】获取消息数量（默认 20）
     *
     * @param reverseOrder 【必填】是否反向排序（默认 False）
     *
     * @param disableGetUrl 【必填】是否禁用获取URL（默认 False）
     *
     * @param parseMultMsg 【必填】是否解析合并消息（默认 True）
     *
     * @param quickReply 【必填】是否快速回复（默认 False）
     *
     * @param reverseOrder2 【必填】是否反向排序(旧版本兼容)（默认 False）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群历史消息",
        description = "获取指定群聊的历史聊天记录",
        categories = {"Go-CQHTTP"}
    )
    GroupMsgHistoryData getGroupMsgHistory(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                           @ActionParam(description = "群号", order = 1) Long groupId,
                                           @ActionParam(description = "起始消息序号", order = 2, nullable = true) String messageSeq,
                                           @ActionParam(description = "获取消息数量（默认 20）", order = 3) Integer count,
                                           @ActionParam(description = "是否反向排序（默认 False）", order = 4) Boolean reverseOrder,
                                           @ActionParam(description = "是否禁用获取URL（默认 False）", order = 5) Boolean disableGetUrl,
                                           @ActionParam(description = "是否解析合并消息（默认 True）", order = 6) Boolean parseMultMsg,
                                           @ActionParam(description = "是否快速回复（默认 False）", order = 7) Boolean quickReply,
                                           @ActionParam(description = "是否反向排序(旧版本兼容)（默认 False）", order = 8) Boolean reverseOrder2);

    /**
     * 获取群根目录文件列表。
     * <p>
     * 获取群文件根目录下的所有文件和文件夹
     * <p>
     * 对应 NapCat API: {@code get_group_root_files}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param fileCount 【必填】文件数量（默认 50）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群根目录文件列表",
        description = "获取群文件根目录下的所有文件和文件夹",
        categories = {"Go-CQHTTP"}
    )
    GroupRootFilesData getGroupRootFiles(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                         @ActionParam(description = "群号", order = 1) Long groupId,
                                         @ActionParam(description = "文件数量（默认 50）", order = 2) Integer fileCount);

    /**
     * 获取频道列表。
     * <p>
     * 获取当前帐号已加入的频道列表
     * <p>
     * 对应 NapCat API: {@code get_guild_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取频道列表",
        description = "获取当前帐号已加入的频道列表",
        categories = {"频道接口"}
    )
    void getGuildList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取频道个人信息。
     * <p>
     * 获取当前帐号在频道中的个人资料
     * <p>
     * 对应 NapCat API: {@code get_guild_service_profile}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取频道个人信息",
        description = "获取当前帐号在频道中的个人资料",
        categories = {"频道接口"}
    )
    void getGuildServiceProfile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取机型显示。
     * <p>
     * 获取当前账号可用的设备机型显示名称列表
     * <p>
     * 对应 NapCat API: {@code _get_model_show}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param model 【可选】模型名称
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取机型显示",
        description = "获取当前账号可用的设备机型显示名称列表",
        categories = {"Go-CQHTTP"}
    )
    List<ModelShowData> getModelShow(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "模型名称", order = 1, nullable = true) String model);

    /**
     * 获取在线客户端。
     * <p>
     * 获取当前登录账号的在线客户端列表
     * <p>
     * 对应 NapCat API: {@code get_online_clients}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取在线客户端",
        description = "获取当前登录账号的在线客户端列表",
        categories = {"Go-CQHTTP"}
    )
    List<String> getOnlineClients(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取陌生人信息。
     * <p>
     * 获取指定非好友用户的信息
     * <p>
     * 对应 NapCat API: {@code get_stranger_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户QQ
     *
     * @param noCache 【必填】是否不使用缓存（默认 False）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取陌生人信息",
        description = "获取指定非好友用户的信息",
        categories = {"Go-CQHTTP"}
    )
    StrangerInfoData getStrangerInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "用户QQ", order = 1) Long userId,
                                     @ActionParam(description = "是否不使用缓存（默认 False）", order = 2) Boolean noCache);

    /**
     * 处理快速操作。
     * <p>
     * 处理来自事件上报的快速操作请求
     * <p>
     * 对应 NapCat API: {@code handle_quick_operation}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param context 【必填】事件上下文
     *
     * @param operation 【必填】快速操作内容
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "处理快速操作",
        description = "处理来自事件上报的快速操作请求",
        categories = {"Go-CQHTTP"}
    )
    void handleQuickOperationInternal(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                      @ActionParam(description = "事件上下文", order = 1) JsonNode context,
                                      @ActionParam(description = "快速操作内容", order = 2) JsonNode operation);

    /**
     * 图片 OCR 识别。
     * <p>
     * 识别图片中的文字内容(仅Windows端支持)
     * <p>
     * 对应 NapCat API: {@code ocr_image}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param image 【必填】图片路径、URL或Base64
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "图片 OCR 识别",
        description = "识别图片中的文字内容(仅Windows端支持)",
        categories = {"扩展接口"}
    )
    ImageData ocrImage(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "图片路径、URL或Base64", order = 1) String image);

    /**
     * 图片 OCR 识别 (内部)。
     * <p>
     * 识别图片中的文字内容(仅Windows端支持)
     * <p>
     * 对应 NapCat API: {@code ocr_image}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param image 【必填】图片路径、URL或Base64
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "图片 OCR 识别 (内部)",
        description = "识别图片中的文字内容(仅Windows端支持)",
        categories = {"扩展接口"}
    )
    ImageData ocrImageInternal(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                               @ActionParam(description = "图片路径、URL或Base64", order = 1) String image);

    /**
     * 发送合并转发消息。
     * <p>
     * 发送合并转发消息
     * <p>
     * 对应 NapCat API: {@code send_forward_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageType 【可选】消息类型 (private/group)
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param message 【必填】OneBot 11 消息混合类型
     *
     * @param autoEscape 【可选】是否作为纯文本发送
     *
     * @param source 【可选】合并转发来源
     *
     * @param news 【可选】合并转发新闻
     *
     * @param summary 【可选】合并转发摘要
     *
     * @param prompt 【可选】合并转发提示
     *
     * @param timeout 【可选】自定义发送超时(毫秒)，覆盖自动计算值
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送合并转发消息",
        description = "发送合并转发消息",
        categories = {"Go-CQHTTP"}
    )
    GroupMsgData sendForwardMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                @ActionParam(description = "消息类型 (private/group)", order = 1, nullable = true) String messageType,
                                @ActionParam(description = "用户QQ", order = 2, nullable = true) Long userId,
                                @ActionParam(description = "群号", order = 3, nullable = true) Long groupId,
                                @ActionParam(description = "OneBot 11 消息混合类型", order = 4) String message,
                                @ActionParam(description = "是否作为纯文本发送", order = 5, nullable = true) Boolean autoEscape,
                                @ActionParam(description = "合并转发来源", order = 6, nullable = true) String source,
                                @ActionParam(description = "合并转发新闻", order = 7, nullable = true) List<JsonNode> news,
                                @ActionParam(description = "合并转发摘要", order = 8, nullable = true) String summary,
                                @ActionParam(description = "合并转发提示", order = 9, nullable = true) String prompt,
                                @ActionParam(description = "自定义发送超时(毫秒)，覆盖自动计算值", order = 10, nullable = true) Long timeout);

    /**
     * 发送群 AI 语音。
     * <p>
     * 发送 AI 生成的语音到指定群聊
     * <p>
     * 对应 NapCat API: {@code send_group_ai_record}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param character 【必填】角色ID
     *
     * @param groupId 【必填】群号
     *
     * @param text 【必填】语音文本内容
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送群 AI 语音",
        description = "发送 AI 生成的语音到指定群聊",
        categories = {"AI 扩展"}
    )
    GroupAiRecordData sendGroupAiRecord(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                        @ActionParam(description = "角色ID", order = 1) String character,
                                        @ActionParam(description = "群号", order = 2) Long groupId,
                                        @ActionParam(description = "语音文本内容", order = 3) String text);

    /**
     * 发送群合并转发消息。
     * <p>
     * 分类：Go-CQHTTP
     * <p>
     * 对应 NapCat API: {@code send_group_forward_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageType 【可选】消息类型 (private/group)
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param message 【必填】OneBot 11 消息混合类型
     *
     * @param autoEscape 【可选】是否作为纯文本发送
     *
     * @param source 【可选】合并转发来源
     *
     * @param news 【可选】合并转发新闻
     *
     * @param summary 【可选】合并转发摘要
     *
     * @param prompt 【可选】合并转发提示
     *
     * @param timeout 【可选】自定义发送超时(毫秒)，覆盖自动计算值
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送群合并转发消息",
        description = "分类：Go-CQHTTP",
        categories = {"Go-CQHTTP"}
    )
    GroupMsgData sendGroupForwardMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "消息类型 (private/group)", order = 1, nullable = true) String messageType,
                                     @ActionParam(description = "用户QQ", order = 2, nullable = true) Long userId,
                                     @ActionParam(description = "群号", order = 3, nullable = true) Long groupId,
                                     @ActionParam(description = "OneBot 11 消息混合类型", order = 4) String message,
                                     @ActionParam(description = "是否作为纯文本发送", order = 5, nullable = true) Boolean autoEscape,
                                     @ActionParam(description = "合并转发来源", order = 6, nullable = true) String source,
                                     @ActionParam(description = "合并转发新闻", order = 7, nullable = true) List<JsonNode> news,
                                     @ActionParam(description = "合并转发摘要", order = 8, nullable = true) String summary,
                                     @ActionParam(description = "合并转发提示", order = 9, nullable = true) String prompt,
                                     @ActionParam(description = "自定义发送超时(毫秒)，覆盖自动计算值", order = 10, nullable = true) Long timeout);

    /**
     * 发送群公告。
     * <p>
     * 在指定群聊中发布新的公告
     * <p>
     * 对应 NapCat API: {@code _send_group_notice}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param content 【必填】公告内容
     *
     * @param image 【可选】公告图片路径或 URL
     *
     * @param pinned 【必填】是否置顶 (0/1)（默认 0）
     *
     * @param type 【必填】类型 (默认为 1)（默认 1）
     *
     * @param confirmRequired 【必填】是否需要确认 (0/1)（默认 1）
     *
     * @param isShowEditCard 【必填】是否显示修改群名片引导 (0/1)（默认 0）
     *
     * @param tipWindowType 【必填】弹窗类型 (默认为 0)（默认 0）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送群公告",
        description = "在指定群聊中发布新的公告",
        categories = {"Go-CQHTTP"}
    )
    void sendGroupNotice(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "群号", order = 1) Long groupId,
                         @ActionParam(description = "公告内容", order = 2) String content,
                         @ActionParam(description = "公告图片路径或 URL", order = 3, nullable = true) String image,
                         @ActionParam(description = "是否置顶 (0/1)（默认 0）", order = 4) Long pinned,
                         @ActionParam(description = "类型 (默认为 1)（默认 1）", order = 5) Long type,
                         @ActionParam(description = "是否需要确认 (0/1)（默认 1）", order = 6) Long confirmRequired,
                         @ActionParam(description = "是否显示修改群名片引导 (0/1)（默认 0）", order = 7) Long isShowEditCard,
                         @ActionParam(description = "弹窗类型 (默认为 0)（默认 0）", order = 8) Long tipWindowType);

    /**
     * 发送私聊合并转发消息。
     * <p>
     * 分类：Go-CQHTTP
     * <p>
     * 对应 NapCat API: {@code send_private_forward_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageType 【可选】消息类型 (private/group)
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param message 【必填】OneBot 11 消息混合类型
     *
     * @param autoEscape 【可选】是否作为纯文本发送
     *
     * @param source 【可选】合并转发来源
     *
     * @param news 【可选】合并转发新闻
     *
     * @param summary 【可选】合并转发摘要
     *
     * @param prompt 【可选】合并转发提示
     *
     * @param timeout 【可选】自定义发送超时(毫秒)，覆盖自动计算值
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送私聊合并转发消息",
        description = "分类：Go-CQHTTP",
        categories = {"Go-CQHTTP"}
    )
    GroupMsgData sendPrivateForwardMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                       @ActionParam(description = "消息类型 (private/group)", order = 1, nullable = true) String messageType,
                                       @ActionParam(description = "用户QQ", order = 2, nullable = true) Long userId,
                                       @ActionParam(description = "群号", order = 3, nullable = true) Long groupId,
                                       @ActionParam(description = "OneBot 11 消息混合类型", order = 4) String message,
                                       @ActionParam(description = "是否作为纯文本发送", order = 5, nullable = true) Boolean autoEscape,
                                       @ActionParam(description = "合并转发来源", order = 6, nullable = true) String source,
                                       @ActionParam(description = "合并转发新闻", order = 7, nullable = true) List<JsonNode> news,
                                       @ActionParam(description = "合并转发摘要", order = 8, nullable = true) String summary,
                                       @ActionParam(description = "合并转发提示", order = 9, nullable = true) String prompt,
                                       @ActionParam(description = "自定义发送超时(毫秒)，覆盖自动计算值", order = 10, nullable = true) Long timeout);

    /**
     * 批量踢出群成员。
     * <p>
     * 从指定群聊中批量踢出多个成员
     * <p>
     * 对应 NapCat API: {@code set_group_kick_members}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】QQ号列表
     *
     * @param rejectAddRequest 【可选】是否拒绝加群请求
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "批量踢出群成员",
        description = "从指定群聊中批量踢出多个成员",
        categories = {"扩展接口"}
    )
    void setGroupKickMembers(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                             @ActionParam(description = "群号", order = 1) Long groupId,
                             @ActionParam(description = "QQ号列表", order = 2) List<Long> userId,
                             @ActionParam(description = "是否拒绝加群请求", order = 3, nullable = true) Boolean rejectAddRequest);

    /**
     * 设置群头像。
     * <p>
     * 修改指定群聊的头像
     * <p>
     * 对应 NapCat API: {@code set_group_portrait}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【必填】头像文件路径或 URL
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群头像",
        description = "修改指定群聊的头像",
        categories = {"Go-CQHTTP"}
    )
    CollectionData setGroupPortrait(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                    @ActionParam(description = "头像文件路径或 URL", order = 1) String file,
                                    @ActionParam(description = "群号", order = 2) Long groupId);

    /**
     * 设置专属头衔。
     * <p>
     * 设置群聊中指定成员的专属头衔
     * <p>
     * 对应 NapCat API: {@code set_group_special_title}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】QQ号
     *
     * @param specialTitle 【必填】专属头衔（默认 ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置专属头衔",
        description = "设置群聊中指定成员的专属头衔",
        categories = {"扩展接口"}
    )
    void setGroupSpecialTitle(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                              @ActionParam(description = "群号", order = 1) Long groupId,
                              @ActionParam(description = "QQ号", order = 2) Long userId,
                              @ActionParam(description = "专属头衔（默认 ）", order = 3) String specialTitle);

    /**
     * 设置机型。
     * <p>
     * 设置当前账号的设备机型名称
     * <p>
     * 对应 NapCat API: {@code _set_model_show}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置机型",
        description = "设置当前账号的设备机型名称",
        categories = {"Go-CQHTTP"}
    )
    void setModelShow(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 设置QQ头像。
     * <p>
     * 修改当前账号的QQ头像
     * <p>
     * 对应 NapCat API: {@code set_qq_avatar}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【必填】图片路径、URL或Base64
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置QQ头像",
        description = "修改当前账号的QQ头像",
        categories = {"扩展接口"}
    )
    void setQqAvatar(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                     @ActionParam(description = "图片路径、URL或Base64", order = 1) String file);

    /**
     * 设置QQ资料。
     * <p>
     * 修改当前账号的昵称、个性签名等资料
     * <p>
     * 对应 NapCat API: {@code set_qq_profile}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param nickname 【必填】昵称
     *
     * @param personalNote 【可选】个性签名
     *
     * @param sex 【可选】性别 (0: 未知, 1: 男, 2: 女)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置QQ资料",
        description = "修改当前账号的昵称、个性签名等资料",
        categories = {"Go-CQHTTP"}
    )
    void setQqProfile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "昵称", order = 1) String nickname,
                      @ActionParam(description = "个性签名", order = 2, nullable = true) String personalNote,
                      @ActionParam(description = "性别 (0: 未知, 1: 男, 2: 女)", order = 3, nullable = true) Long sex);

    /**
     * 设置个性签名。
     * <p>
     * 修改当前登录帐号的个性签名
     * <p>
     * 对应 NapCat API: {@code set_self_longnick}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param longNick 【必填】签名内容
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置个性签名",
        description = "修改当前登录帐号的个性签名",
        categories = {"扩展接口"}
    )
    void setSelfLongnick(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "签名内容", order = 1) String longNick);

    /**
     * 测试下载流。
     * <p>
     * 分类：流式传输扩展
     * <p>
     * 对应 NapCat API: {@code test_download_stream}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param error 【可选】是否触发测试错误（默认 False）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "测试下载流",
        description = "分类：流式传输扩展",
        categories = {"流式传输扩展"}
    )
    DownloadStreamData testDownloadStream(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                          @ActionParam(description = "是否触发测试错误（默认 False）", order = 1, nullable = true) Boolean error);

    /**
     * 英文单词翻译。
     * <p>
     * 将英文单词列表翻译为中文
     * <p>
     * 对应 NapCat API: {@code translate_en2zh}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param words 【必填】待翻译单词列表
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "英文单词翻译",
        description = "将英文单词列表翻译为中文",
        categories = {"扩展接口"}
    )
    En2zhData translateEn2zh(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                             @ActionParam(description = "待翻译单词列表", order = 1) List<String> words);

    /**
     * 上传文件流。
     * <p>
     * 以流式方式上传文件数据到机器人
     * <p>
     * 对应 NapCat API: {@code upload_file_stream}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param streamId 【必填】流 ID
     *
     * @param chunkData 【可选】分块数据 (Base64)
     *
     * @param chunkIndex 【可选】分块索引
     *
     * @param totalChunks 【可选】总分块数
     *
     * @param fileSize 【可选】文件总大小
     *
     * @param expectedSha256 【可选】期望的 SHA256
     *
     * @param isComplete 【可选】是否完成
     *
     * @param filename 【可选】文件名
     *
     * @param reset 【可选】是否重置
     *
     * @param verifyOnly 【可选】是否仅验证
     *
     * @param fileRetention 【必填】文件保留时间 (毫秒)（默认 300000）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "上传文件流",
        description = "以流式方式上传文件数据到机器人",
        categories = {"流式接口"}
    )
    FileStreamData uploadFileStream(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                    @ActionParam(description = "流 ID", order = 1) String streamId,
                                    @ActionParam(description = "分块数据 (Base64)", order = 2, nullable = true) String chunkData,
                                    @ActionParam(description = "分块索引", order = 3, nullable = true) Long chunkIndex,
                                    @ActionParam(description = "总分块数", order = 4, nullable = true) Long totalChunks,
                                    @ActionParam(description = "文件总大小", order = 5, nullable = true) Integer fileSize,
                                    @ActionParam(description = "期望的 SHA256", order = 6, nullable = true) String expectedSha256,
                                    @ActionParam(description = "是否完成", order = 7, nullable = true) Boolean isComplete,
                                    @ActionParam(description = "文件名", order = 8, nullable = true) String filename,
                                    @ActionParam(description = "是否重置", order = 9, nullable = true) Boolean reset,
                                    @ActionParam(description = "是否仅验证", order = 10, nullable = true) Boolean verifyOnly,
                                    @ActionParam(description = "文件保留时间 (毫秒)（默认 300000）", order = 11) Long fileRetention);

    /**
     * 上传群文件。
     * <p>
     * 上传资源路径或URL指定的文件到指定群聊的文件系统中
     * <p>
     * 对应 NapCat API: {@code upload_group_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param file 【必填】资源路径或URL
     *
     * @param name 【必填】文件名
     *
     * @param folder 【可选】父目录 ID
     *
     * @param folderId 【可选】父目录 ID (兼容性字段)
     *
     * @param uploadFile 【必填】是否执行上传（默认 True）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "上传群文件",
        description = "上传资源路径或URL指定的文件到指定群聊的文件系统中",
        categories = {"Go-CQHTTP"}
    )
    GroupFileData uploadGroupFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                  @ActionParam(description = "群号", order = 1) Long groupId,
                                  @ActionParam(description = "资源路径或URL", order = 2) String file,
                                  @ActionParam(description = "文件名", order = 3) String name,
                                  @ActionParam(description = "父目录 ID", order = 4, nullable = true) String folder,
                                  @ActionParam(description = "父目录 ID (兼容性字段)", order = 5, nullable = true) String folderId,
                                  @ActionParam(description = "是否执行上传（默认 True）", order = 6) Boolean uploadFile);

    /**
     * 上传私聊文件。
     * <p>
     * 上传本地文件到指定私聊会话中
     * <p>
     * 对应 NapCat API: {@code upload_private_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     *
     * @param file 【必填】资源路径或URL
     *
     * @param name 【必填】文件名
     *
     * @param uploadFile 【必填】是否执行上传（默认 True）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "上传私聊文件",
        description = "上传本地文件到指定私聊会话中",
        categories = {"Go-CQHTTP"}
    )
    GroupFileData uploadPrivateFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                    @ActionParam(description = "用户 QQ", order = 1) Long userId,
                                    @ActionParam(description = "资源路径或URL", order = 2) String file,
                                    @ActionParam(description = "文件名", order = 3) String name,
                                    @ActionParam(description = "是否执行上传（默认 True）", order = 4) Boolean uploadFile);

    /**
     * 取消在线文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code cancel_online_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     *
     * @param msgId 【必填】消息 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "取消在线文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void cancelOnlineFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "用户 QQ", order = 1) Long userId,
                          @ActionParam(description = "消息 ID", order = 2) String msgId);

    /**
     * 创建闪传任务。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code create_flash_task}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param files 【必填】文件列表或单个文件路径
     *
     * @param name 【可选】任务名称
     *
     * @param thumbPath 【可选】缩略图路径
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "创建闪传任务",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    FlashTaskData createFlashTask(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                  @ActionParam(description = "文件列表或单个文件路径", order = 1) List<String> files,
                                  @ActionParam(description = "任务名称", order = 2, nullable = true) String name,
                                  @ActionParam(description = "缩略图路径", order = 3, nullable = true) String thumbPath);

    /**
     * 下载文件集。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code download_fileset}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param filesetId 【必填】文件集 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "下载文件集",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void downloadFileset(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "文件集 ID", order = 1) String filesetId);

    /**
     * 获取文件。
     * <p>
     * 获取指定文件的详细信息及下载路径
     * <p>
     * 对应 NapCat API: {@code get_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【可选】文件路径、URL或Base64
     *
     * @param fileId 【可选】文件ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取文件",
        description = "获取指定文件的详细信息及下载路径",
        categories = {"文件接口"}
    )
    FileData getFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                     @ActionParam(description = "文件路径、URL或Base64", order = 1, nullable = true) String file,
                     @ActionParam(description = "文件ID", order = 2, nullable = true) String fileId);

    /**
     * 获取文件集 ID。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code get_fileset_id}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param shareCode 【必填】分享码或分享链接
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取文件集 ID",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    FilesetIdData getFilesetId(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                               @ActionParam(description = "分享码或分享链接", order = 1) String shareCode);

    /**
     * 获取文件集信息。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code get_fileset_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param filesetId 【必填】文件集 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取文件集信息",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    FilesetInfoData getFilesetInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                   @ActionParam(description = "文件集 ID", order = 1) String filesetId);

    /**
     * 获取闪传文件列表。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code get_flash_file_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param filesetId 【必填】文件集 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取闪传文件列表",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    List<FlashFileData> getFlashFileList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                         @ActionParam(description = "文件集 ID", order = 1) String filesetId);

    /**
     * 获取闪传文件链接。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code get_flash_file_url}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param filesetId 【必填】文件集 ID
     *
     * @param fileName 【可选】文件名
     *
     * @param fileIndex 【可选】文件索引
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取闪传文件链接",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    GroupFileUrlData getFlashFileUrl(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "文件集 ID", order = 1) String filesetId,
                                     @ActionParam(description = "文件名", order = 2, nullable = true) String fileName,
                                     @ActionParam(description = "文件索引", order = 3, nullable = true) Long fileIndex);

    /**
     * 获取群文件URL。
     * <p>
     * 获取指定群文件的下载链接
     * <p>
     * 对应 NapCat API: {@code get_group_file_url}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param fileId 【必填】文件ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群文件URL",
        description = "获取指定群文件的下载链接",
        categories = {"文件接口"}
    )
    GroupFileUrlData getGroupFileUrl(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "群号", order = 1) Long groupId,
                                     @ActionParam(description = "文件ID", order = 2) String fileId);

    /**
     * 获取图片。
     * <p>
     * 获取指定图片的信息及路径
     * <p>
     * 对应 NapCat API: {@code get_image}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【可选】文件路径、URL或Base64
     *
     * @param fileId 【可选】文件ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取图片",
        description = "获取指定图片的信息及路径",
        categories = {"文件接口"}
    )
    FileData getImage(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "文件路径、URL或Base64", order = 1, nullable = true) String file,
                      @ActionParam(description = "文件ID", order = 2, nullable = true) String fileId);

    /**
     * 获取在线文件消息。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code get_online_file_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取在线文件消息",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void getOnlineFileMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "用户 QQ", order = 1) Long userId);

    /**
     * 获取私聊文件URL。
     * <p>
     * 获取指定私聊文件的下载链接
     * <p>
     * 对应 NapCat API: {@code get_private_file_url}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param fileId 【必填】文件ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取私聊文件URL",
        description = "获取指定私聊文件的下载链接",
        categories = {"文件接口"}
    )
    GroupFileUrlData getPrivateFileUrl(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                       @ActionParam(description = "文件ID", order = 1) String fileId);

    /**
     * 获取语音。
     * <p>
     * 获取指定语音文件的信息，并支持格式转换
     * <p>
     * 对应 NapCat API: {@code get_record}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【可选】文件路径、URL或Base64
     *
     * @param fileId 【可选】文件ID
     *
     * @param outFormat 【必填】输出格式
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取语音",
        description = "获取指定语音文件的信息，并支持格式转换",
        categories = {"文件接口"}
    )
    FileData getRecord(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "文件路径、URL或Base64", order = 1, nullable = true) String file,
                       @ActionParam(description = "文件ID", order = 2, nullable = true) String fileId,
                       @ActionParam(description = "输出格式", order = 3) String outFormat);

    /**
     * 获取文件分享链接。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code get_share_link}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param filesetId 【必填】文件集 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取文件分享链接",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void getShareLink(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "文件集 ID", order = 1) String filesetId);

    /**
     * 移动群文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code move_group_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param fileId 【必填】文件ID
     *
     * @param currentParentDirectory 【必填】当前父目录
     *
     * @param targetParentDirectory 【必填】目标父目录
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "移动群文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    GroupFileData moveGroupFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                @ActionParam(description = "群号", order = 1) Long groupId,
                                @ActionParam(description = "文件ID", order = 2) String fileId,
                                @ActionParam(description = "当前父目录", order = 3) String currentParentDirectory,
                                @ActionParam(description = "目标父目录", order = 4) String targetParentDirectory);

    /**
     * 接收在线文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code receive_online_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     *
     * @param msgId 【必填】消息 ID
     *
     * @param elementId 【必填】元素 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "接收在线文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void receiveOnlineFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                           @ActionParam(description = "用户 QQ", order = 1) Long userId,
                           @ActionParam(description = "消息 ID", order = 2) String msgId,
                           @ActionParam(description = "元素 ID", order = 3) String elementId);

    /**
     * 拒绝在线文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code refuse_online_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     *
     * @param msgId 【必填】消息 ID
     *
     * @param elementId 【必填】元素 ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "拒绝在线文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void refuseOnlineFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "用户 QQ", order = 1) Long userId,
                          @ActionParam(description = "消息 ID", order = 2) String msgId,
                          @ActionParam(description = "元素 ID", order = 3) String elementId);

    /**
     * 重命名群文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code rename_group_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param fileId 【必填】文件ID
     *
     * @param currentParentDirectory 【必填】当前父目录
     *
     * @param newName 【必填】新文件名
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "重命名群文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    GroupFileData renameGroupFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                  @ActionParam(description = "群号", order = 1) Long groupId,
                                  @ActionParam(description = "文件ID", order = 2) String fileId,
                                  @ActionParam(description = "当前父目录", order = 3) String currentParentDirectory,
                                  @ActionParam(description = "新文件名", order = 4) String newName);

    /**
     * 发送闪传消息。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code send_flash_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param filesetId 【必填】文件集 ID
     *
     * @param userId 【可选】用户 QQ
     *
     * @param groupId 【可选】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送闪传消息",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    GroupAiRecordData sendFlashMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                   @ActionParam(description = "文件集 ID", order = 1) String filesetId,
                                   @ActionParam(description = "用户 QQ", order = 2, nullable = true) Long userId,
                                   @ActionParam(description = "群号", order = 3, nullable = true) Long groupId);

    /**
     * 发送在线文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code send_online_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     *
     * @param filePath 【必填】本地文件路径
     *
     * @param fileName 【可选】文件名 (可选)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送在线文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void sendOnlineFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "用户 QQ", order = 1) Long userId,
                        @ActionParam(description = "本地文件路径", order = 2) String filePath,
                        @ActionParam(description = "文件名 (可选)", order = 3, nullable = true) String fileName);

    /**
     * 发送在线文件夹。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code send_online_folder}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】用户 QQ
     *
     * @param folderPath 【必填】本地文件夹路径
     *
     * @param folderName 【可选】文件夹名称 (可选)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送在线文件夹",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    void sendOnlineFolder(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "用户 QQ", order = 1) Long userId,
                          @ActionParam(description = "本地文件夹路径", order = 2) String folderPath,
                          @ActionParam(description = "文件夹名称 (可选)", order = 3, nullable = true) String folderName);

    /**
     * 传输群文件。
     * <p>
     * 分类：文件扩展
     * <p>
     * 对应 NapCat API: {@code trans_group_file}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param fileId 【必填】文件ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "传输群文件",
        description = "分类：文件扩展",
        categories = {"文件扩展"}
    )
    GroupFileData transGroupFile(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                 @ActionParam(description = "群号", order = 1) Long groupId,
                                 @ActionParam(description = "文件ID", order = 2) String fileId);

    /**
     * 获取 Cookies。
     * <p>
     * 获取指定域名的 Cookies
     * <p>
     * 对应 NapCat API: {@code get_cookies}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param domain 【必填】需要获取 cookies 的域名
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取 Cookies",
        description = "获取指定域名的 Cookies",
        categories = {"用户接口"}
    )
    CookiesData getCookies(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                           @ActionParam(description = "需要获取 cookies 的域名", order = 1) String domain);

    /**
     * 获取好友列表。
     * <p>
     * 获取当前帐号的好友列表
     * <p>
     * 对应 NapCat API: {@code get_friend_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param noCache 【可选】是否不使用缓存
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取好友列表",
        description = "获取当前帐号的好友列表",
        categories = {"用户接口"}
    )
    List<String> getFriendList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                               @ActionParam(description = "是否不使用缓存", order = 1, nullable = true) Boolean noCache);

    /**
     * 获取带分组的好友列表。
     * <p>
     * 分类：用户扩展
     * <p>
     * 对应 NapCat API: {@code get_friends_with_category}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取带分组的好友列表",
        description = "分类：用户扩展",
        categories = {"用户扩展"}
    )
    List<FriendsWithCategoryData> getFriendsWithCategory(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取资料点赞。
     * <p>
     * 分类：用户扩展
     * <p>
     * 对应 NapCat API: {@code get_profile_like}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【可选】QQ号
     *
     * @param start 【必填】起始位置（默认 0）
     *
     * @param count 【必填】获取数量（默认 10）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取资料点赞",
        description = "分类：用户扩展",
        categories = {"用户扩展"}
    )
    ProfileLikeData getProfileLike(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                   @ActionParam(description = "QQ号", order = 1, nullable = true) Long userId,
                                   @ActionParam(description = "起始位置（默认 0）", order = 2) Long start,
                                   @ActionParam(description = "获取数量（默认 10）", order = 3) Integer count);

    /**
     * 获取最近会话。
     * <p>
     * 获取最近会话
     * <p>
     * 对应 NapCat API: {@code get_recent_contact}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param count 【必填】获取的数量（默认 10）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取最近会话",
        description = "获取最近会话",
        categories = {"用户接口"}
    )
    List<RecentContactData> getRecentContact(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                             @ActionParam(description = "获取的数量（默认 10）", order = 1) Integer count);

    /**
     * 获取单向好友列表。
     * <p>
     * 分类：用户扩展
     * <p>
     * 对应 NapCat API: {@code get_unidirectional_friend_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取单向好友列表",
        description = "分类：用户扩展",
        categories = {"用户扩展"}
    )
    List<UnidirectionalFriendData> getUnidirectionalFriendList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 点赞。
     * <p>
     * 给指定用户点赞
     * <p>
     * 对应 NapCat API: {@code send_like}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】对方 QQ 号
     *
     * @param times 【必填】点赞次数（默认 1）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 点赞失败（频率过快或用户不存在）}
     */
    @BotAction(
        name = "点赞",
        description = "给指定用户点赞",
        categories = {"用户接口"}
    )
    void sendLike(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                  @ActionParam(description = "对方 QQ 号", order = 1) Long userId,
                  @ActionParam(description = "点赞次数（默认 1）", order = 2) Long times);

    /**
     * 设置自定义在线状态。
     * <p>
     * 设置自定义在线状态
     * <p>
     * 对应 NapCat API: {@code set_diy_online_status}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param faceId 【必填】图标ID
     *
     * @param faceType 【必填】图标类型（默认 1）
     *
     * @param wording 【必填】状态文字内容（默认  ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置自定义在线状态",
        description = "设置自定义在线状态",
        categories = {"用户扩展"}
    )
    String setDiyOnlineStatus(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                              @ActionParam(description = "图标ID", order = 1) Long faceId,
                              @ActionParam(description = "图标类型（默认 1）", order = 2) Long faceType,
                              @ActionParam(description = "状态文字内容（默认  ）", order = 3) String wording);

    /**
     * 处理加好友请求。
     * <p>
     * 同意或拒绝加好友请求
     * <p>
     * 对应 NapCat API: {@code set_friend_add_request}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param flag 【必填】加好友请求的 flag (需从上报中获取)
     *
     * @param approve 【可选】是否同意请求
     *
     * @param remark 【可选】添加后的好友备注
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "处理加好友请求",
        description = "同意或拒绝加好友请求",
        categories = {"用户接口"}
    )
    void setFriendAddRequest(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                             @ActionParam(description = "加好友请求的 flag (需从上报中获取)", order = 1) String flag,
                             @ActionParam(description = "是否同意请求", order = 2, nullable = true) String approve,
                             @ActionParam(description = "添加后的好友备注", order = 3, nullable = true) String remark);

    /**
     * 设置好友备注。
     * <p>
     * 设置好友备注
     * <p>
     * 对应 NapCat API: {@code set_friend_remark}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】对方 QQ 号
     *
     * @param remark 【必填】备注内容
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 备注设置失败（好友不存在或非法输入）}
     */
    @BotAction(
        name = "设置好友备注",
        description = "设置好友备注",
        categories = {"用户接口"}
    )
    void setFriendRemark(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "对方 QQ 号", order = 1) Long userId,
                         @ActionParam(description = "备注内容", order = 2) String remark);

    /**
     * 取消点赞群相册媒体。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code cancel_group_album_media_like}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param albumId 【必填】相册ID
     *
     * @param batchId 【必填】batch_id
     *
     * @param lloc 【可选】lloc，若对整个上传操作则不填
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "取消点赞群相册媒体",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    DelGroupAlbumMediaData cancelGroupAlbumMediaLike(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                                     @ActionParam(description = "群号", order = 1) Long groupId,
                                                     @ActionParam(description = "相册ID", order = 2) String albumId,
                                                     @ActionParam(description = "batch_id", order = 3) String batchId,
                                                     @ActionParam(description = "lloc，若对整个上传操作则不填", order = 4, nullable = true) String lloc);

    /**
     * 移出精华消息。
     * <p>
     * 将一条消息从群精华消息列表中移出
     * <p>
     * 对应 NapCat API: {@code delete_essence_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【可选】消息ID
     *
     * @param msgSeq 【可选】消息序号
     *
     * @param msgRandom 【可选】消息随机数
     *
     * @param groupId 【可选】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "移出精华消息",
        description = "将一条消息从群精华消息列表中移出",
        categories = {"群组接口"}
    )
    void deleteEssenceMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "消息ID", order = 1, nullable = true) Long messageId,
                          @ActionParam(description = "消息序号", order = 2, nullable = true) String msgSeq,
                          @ActionParam(description = "消息随机数", order = 3, nullable = true) String msgRandom,
                          @ActionParam(description = "群号", order = 4, nullable = true) Long groupId);

    /**
     * 删除群相册媒体。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code del_group_album_media}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param albumId 【必填】相册ID
     *
     * @param lloc 【必填】媒体ID (lloc)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "删除群相册媒体",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    DelGroupAlbumMediaData delGroupAlbumMedia(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                              @ActionParam(description = "群号", order = 1) Long groupId,
                                              @ActionParam(description = "相册ID", order = 2) String albumId,
                                              @ActionParam(description = "媒体ID (lloc)", order = 3) String lloc);

    /**
     * 删除群公告。
     * <p>
     * 删除群聊中的公告
     * <p>
     * 对应 NapCat API: {@code _del_group_notice}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param noticeId 【必填】公告ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "删除群公告",
        description = "删除群聊中的公告",
        categories = {"群组接口"}
    )
    void delGroupNotice(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "群号", order = 1) Long groupId,
                        @ActionParam(description = "公告ID", order = 2) String noticeId);

    /**
     * 发表群相册评论。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code do_group_album_comment}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param albumId 【必填】相册 ID
     *
     * @param lloc 【必填】图片 ID
     *
     * @param content 【必填】评论内容
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发表群相册评论",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    DelGroupAlbumMediaData doGroupAlbumComment(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                               @ActionParam(description = "群号", order = 1) Long groupId,
                                               @ActionParam(description = "相册 ID", order = 2) String albumId,
                                               @ActionParam(description = "图片 ID", order = 3) String lloc,
                                               @ActionParam(description = "评论内容", order = 4) String content);

    /**
     * 获取群精华消息。
     * <p>
     * 获取指定群聊中的精华消息列表
     * <p>
     * 对应 NapCat API: {@code get_essence_msg_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群精华消息",
        description = "获取指定群聊中的精华消息列表",
        categories = {"群组接口"}
    )
    List<EssenceMsgData> getEssenceMsgList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                           @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群相册媒体列表。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code get_group_album_media_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param albumId 【必填】相册ID
     *
     * @param attachInfo 【可选】附加信息（用于分页）（默认 ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群相册媒体列表",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    GroupAlbumMediaData getGroupAlbumMediaList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                               @ActionParam(description = "群号", order = 1) Long groupId,
                                               @ActionParam(description = "相册ID", order = 2) String albumId,
                                               @ActionParam(description = "附加信息（用于分页）（默认 ）", order = 3, nullable = true) String attachInfo);

    /**
     * 获取群详细信息。
     * <p>
     * 获取群聊的详细信息，包括成员数、最大成员数等
     * <p>
     * 对应 NapCat API: {@code get_group_detail_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群详细信息",
        description = "获取群聊的详细信息，包括成员数、最大成员数等",
        categories = {"群组接口"}
    )
    GroupDetailInfoData getGroupDetailInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                           @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群被忽略的加群请求。
     * <p>
     * 分类：群组接口
     * <p>
     * 对应 NapCat API: {@code get_group_ignore_add_request}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群被忽略的加群请求",
        description = "分类：群组接口",
        categories = {"群组接口"}
    )
    List<GroupIgnoreAddRequestData> getGroupIgnoreAddRequest(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取群忽略通知。
     * <p>
     * 获取被忽略的入群申请和邀请通知
     * <p>
     * 对应 NapCat API: {@code get_group_ignored_notifies}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群忽略通知",
        description = "获取被忽略的入群申请和邀请通知",
        categories = {"群组接口"}
    )
    GroupIgnoredNotifiesData getGroupIgnoredNotifies(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取群信息。
     * <p>
     * 获取群聊的基本信息
     * <p>
     * 对应 NapCat API: {@code get_group_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群信息",
        description = "获取群聊的基本信息",
        categories = {"群组接口"}
    )
    GroupInfoData getGroupInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                               @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群详细信息 (扩展)。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code get_group_info_ex}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群详细信息 (扩展)",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    void getGroupInfoEx(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群列表。
     * <p>
     * 获取当前帐号的群聊列表
     * <p>
     * 对应 NapCat API: {@code get_group_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param noCache 【可选】是否不使用缓存
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群列表",
        description = "获取当前帐号的群聊列表",
        categories = {"群组接口"}
    )
    List<String> getGroupList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                              @ActionParam(description = "是否不使用缓存", order = 1, nullable = true) Boolean noCache);

    /**
     * 获取群成员信息。
     * <p>
     * 获取群聊中指定成员的信息
     * <p>
     * 对应 NapCat API: {@code get_group_member_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】QQ号
     *
     * @param noCache 【可选】是否不使用缓存
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群成员信息",
        description = "获取群聊中指定成员的信息",
        categories = {"群组接口"}
    )
    GroupMemberInfoData getGroupMemberInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                           @ActionParam(description = "群号", order = 1) Long groupId,
                                           @ActionParam(description = "QQ号", order = 2) Long userId,
                                           @ActionParam(description = "是否不使用缓存", order = 3, nullable = true) Boolean noCache);

    /**
     * 获取群成员列表。
     * <p>
     * 获取群聊中的所有成员列表
     * <p>
     * 对应 NapCat API: {@code get_group_member_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param noCache 【可选】是否不使用缓存
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群成员列表",
        description = "获取群聊中的所有成员列表",
        categories = {"群组接口"}
    )
    List<GroupMemberData> getGroupMemberList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                             @ActionParam(description = "群号", order = 1) Long groupId,
                                             @ActionParam(description = "是否不使用缓存", order = 2, nullable = true) Boolean noCache);

    /**
     * 获取群公告。
     * <p>
     * 获取指定群聊中的公告列表
     * <p>
     * 对应 NapCat API: {@code _get_group_notice}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群公告",
        description = "获取指定群聊中的公告列表",
        categories = {"群组接口"}
    )
    List<GroupNoticeData> getGroupNotice(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                         @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群禁言列表。
     * <p>
     * 分类：群组接口
     * <p>
     * 对应 NapCat API: {@code get_group_shut_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群禁言列表",
        description = "分类：群组接口",
        categories = {"群组接口"}
    )
    List<GroupShutData> getGroupShutList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                         @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群组今日打卡列表。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code get_group_signed_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群组今日打卡列表",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    List<GroupSignedData> getGroupSignedList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                             @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 获取群相册列表。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code get_qun_album_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param attachInfo 【可选】附加信息（用于分页，从上一次返回结果中获取）（默认 ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群相册列表",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    QunAlbumData getQunAlbumList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                 @ActionParam(description = "群号", order = 1) Long groupId,
                                 @ActionParam(description = "附加信息（用于分页，从上一次返回结果中获取）（默认 ）", order = 2, nullable = true) String attachInfo);

    /**
     * 发送群消息。
     * <p>
     * 发送群消息
     * <p>
     * 对应 NapCat API: {@code send_group_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageType 【可选】消息类型 (private/group)
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param message 【必填】OneBot 11 消息混合类型
     *
     * @param autoEscape 【可选】是否作为纯文本发送
     *
     * @param source 【可选】合并转发来源
     *
     * @param news 【可选】合并转发新闻
     *
     * @param summary 【可选】合并转发摘要
     *
     * @param prompt 【可选】合并转发提示
     *
     * @param timeout 【可选】自定义发送超时(毫秒)，覆盖自动计算值
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送群消息",
        description = "发送群消息",
        categories = {"群组接口"}
    )
    GroupMsgData sendGroupMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                              @ActionParam(description = "消息类型 (private/group)", order = 1, nullable = true) String messageType,
                              @ActionParam(description = "用户QQ", order = 2, nullable = true) Long userId,
                              @ActionParam(description = "群号", order = 3, nullable = true) Long groupId,
                              @ActionParam(description = "OneBot 11 消息混合类型", order = 4) String message,
                              @ActionParam(description = "是否作为纯文本发送", order = 5, nullable = true) Boolean autoEscape,
                              @ActionParam(description = "合并转发来源", order = 6, nullable = true) String source,
                              @ActionParam(description = "合并转发新闻", order = 7, nullable = true) List<JsonNode> news,
                              @ActionParam(description = "合并转发摘要", order = 8, nullable = true) String summary,
                              @ActionParam(description = "合并转发提示", order = 9, nullable = true) String prompt,
                              @ActionParam(description = "自定义发送超时(毫秒)，覆盖自动计算值", order = 10, nullable = true) Long timeout);

    /**
     * 群打卡。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code send_group_sign}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "群打卡",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    void sendGroupSign(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 设置精华消息。
     * <p>
     * 将一条消息设置为群精华消息
     * <p>
     * 对应 NapCat API: {@code set_essence_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置精华消息",
        description = "将一条消息设置为群精华消息",
        categories = {"群组接口"}
    )
    void setEssenceMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "消息ID", order = 1) Long messageId);

    /**
     * 设置群加群选项。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code set_group_add_option}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param addType 【必填】加群方式
     *
     * @param groupQuestion 【可选】加群问题
     *
     * @param groupAnswer 【可选】加群答案
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群加群选项",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    void setGroupAddOption(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                           @ActionParam(description = "群号", order = 1) Long groupId,
                           @ActionParam(description = "加群方式", order = 2) Long addType,
                           @ActionParam(description = "加群问题", order = 3, nullable = true) String groupQuestion,
                           @ActionParam(description = "加群答案", order = 4, nullable = true) String groupAnswer);

    /**
     * 处理加群请求。
     * <p>
     * 同意或拒绝加群请求或邀请
     * <p>
     * 对应 NapCat API: {@code set_group_add_request}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param flag 【必填】请求flag
     *
     * @param approve 【可选】是否同意
     *
     * @param reason 【可选】拒绝理由
     *
     * @param count 【可选】搜索通知数量（默认 100）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "处理加群请求",
        description = "同意或拒绝加群请求或邀请",
        categories = {"群组接口"}
    )
    void setGroupAddRequest(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                            @ActionParam(description = "请求flag", order = 1) String flag,
                            @ActionParam(description = "是否同意", order = 2, nullable = true) Boolean approve,
                            @ActionParam(description = "拒绝理由", order = 3, nullable = true) String reason,
                            @ActionParam(description = "搜索通知数量（默认 100）", order = 4, nullable = true) Integer count);

    /**
     * 设置群管理员。
     * <p>
     * 设置或取消群聊中的管理员
     * <p>
     * 对应 NapCat API: {@code set_group_admin}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param enable 【可选】是否设置为管理员
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群管理员",
        description = "设置或取消群聊中的管理员",
        categories = {"群组接口"}
    )
    void setGroupAdmin(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "群号", order = 1) Long groupId,
                       @ActionParam(description = "用户QQ", order = 2) Long userId,
                       @ActionParam(description = "是否设置为管理员", order = 3, nullable = true) Boolean enable);

    /**
     * 点赞群相册媒体。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code set_group_album_media_like}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param albumId 【必填】相册ID
     *
     * @param batchId 【必填】batch_id
     *
     * @param lloc 【可选】lloc，若对整个上传操作则不填
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "点赞群相册媒体",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    DelGroupAlbumMediaData setGroupAlbumMediaLike(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                                  @ActionParam(description = "群号", order = 1) Long groupId,
                                                  @ActionParam(description = "相册ID", order = 2) String albumId,
                                                  @ActionParam(description = "batch_id", order = 3) String batchId,
                                                  @ActionParam(description = "lloc，若对整个上传操作则不填", order = 4, nullable = true) String lloc);

    /**
     * 群组禁言。
     * <p>
     * 禁言群聊中的指定成员
     * <p>
     * 对应 NapCat API: {@code set_group_ban}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param duration 【必填】禁言时长(秒)（默认 0）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "群组禁言",
        description = "禁言群聊中的指定成员",
        categories = {"群组接口"}
    )
    void setGroupBan(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                     @ActionParam(description = "群号", order = 1) Long groupId,
                     @ActionParam(description = "用户QQ", order = 2) Long userId,
                     @ActionParam(description = "禁言时长(秒)（默认 0）", order = 3) Long duration);

    /**
     * 设置群名片。
     * <p>
     * 设置群聊中指定成员的群名片
     * <p>
     * 对应 NapCat API: {@code set_group_card}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param card 【可选】群名片
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群名片",
        description = "设置群聊中指定成员的群名片",
        categories = {"群组接口"}
    )
    void setGroupCard(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "群号", order = 1) Long groupId,
                      @ActionParam(description = "用户QQ", order = 2) Long userId,
                      @ActionParam(description = "群名片", order = 3, nullable = true) String card);

    /**
     * 群组踢人。
     * <p>
     * 将指定成员踢出群聊
     * <p>
     * 对应 NapCat API: {@code set_group_kick}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param rejectAddRequest 【可选】是否拒绝加群请求
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "群组踢人",
        description = "将指定成员踢出群聊",
        categories = {"群组接口"}
    )
    void setGroupKick(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "群号", order = 1) Long groupId,
                      @ActionParam(description = "用户QQ", order = 2) Long userId,
                      @ActionParam(description = "是否拒绝加群请求", order = 3, nullable = true) Boolean rejectAddRequest);

    /**
     * 退出群组。
     * <p>
     * 退出或解散指定群聊
     * <p>
     * 对应 NapCat API: {@code set_group_leave}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param isDismiss 【可选】是否解散
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "退出群组",
        description = "退出或解散指定群聊",
        categories = {"群组接口"}
    )
    void setGroupLeave(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "群号", order = 1) Long groupId,
                       @ActionParam(description = "是否解散", order = 2, nullable = true) Boolean isDismiss);

    /**
     * 设置群名称。
     * <p>
     * 修改指定群聊的名称
     * <p>
     * 对应 NapCat API: {@code set_group_name}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param groupName 【必填】群名称
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群名称",
        description = "修改指定群聊的名称",
        categories = {"群组接口"}
    )
    void setGroupName(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "群号", order = 1) Long groupId,
                      @ActionParam(description = "群名称", order = 2) String groupName);

    /**
     * 设置群备注。
     * <p>
     * 设置群备注
     * <p>
     * 对应 NapCat API: {@code set_group_remark}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param remark 【必填】备注
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群备注",
        description = "设置群备注",
        categories = {"群组扩展"}
    )
    void setGroupRemark(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "群号", order = 1) Long groupId,
                        @ActionParam(description = "备注", order = 2) String remark);

    /**
     * 设置群机器人加群选项。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code set_group_robot_add_option}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param robotMemberSwitch 【可选】机器人成员开关
     *
     * @param robotMemberExamine 【可选】机器人成员审核
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群机器人加群选项",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    void setGroupRobotAddOption(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                @ActionParam(description = "群号", order = 1) Long groupId,
                                @ActionParam(description = "机器人成员开关", order = 2, nullable = true) Long robotMemberSwitch,
                                @ActionParam(description = "机器人成员审核", order = 3, nullable = true) Long robotMemberExamine);

    /**
     * 设置群搜索选项。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code set_group_search}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param noCodeFingerOpen 【可选】未知
     *
     * @param noFingerOpen 【可选】未知
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群搜索选项",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    void setGroupSearch(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "群号", order = 1) Long groupId,
                        @ActionParam(description = "未知", order = 2, nullable = true) Long noCodeFingerOpen,
                        @ActionParam(description = "未知", order = 3, nullable = true) Long noFingerOpen);

    /**
     * 群打卡。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code set_group_sign}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "群打卡",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    void setGroupSign(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 全员禁言。
     * <p>
     * 开启或关闭指定群聊的全员禁言
     * <p>
     * 对应 NapCat API: {@code set_group_whole_ban}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param enable 【可选】是否开启全员禁言
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "全员禁言",
        description = "开启或关闭指定群聊的全员禁言",
        categories = {"群组接口"}
    )
    void setGroupWholeBan(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "群号", order = 1) Long groupId,
                          @ActionParam(description = "是否开启全员禁言", order = 2, nullable = true) Boolean enable);

    /**
     * 上传图片到群相册。
     * <p>
     * 分类：群组扩展
     * <p>
     * 对应 NapCat API: {@code upload_image_to_qun_album}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param albumId 【必填】相册ID
     *
     * @param albumName 【必填】相册名称
     *
     * @param file 【必填】图片路径、URL或Base64
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "上传图片到群相册",
        description = "分类：群组扩展",
        categories = {"群组扩展"}
    )
    ImageToQunAlbumData uploadImageToQunAlbum(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                              @ActionParam(description = "群号", order = 1) Long groupId,
                                              @ActionParam(description = "相册ID", order = 2) String albumId,
                                              @ActionParam(description = "相册名称", order = 3) String albumName,
                                              @ActionParam(description = "图片路径、URL或Base64", order = 4) String file);

    /**
     * 分享群 (Ark)。
     * <p>
     * 获取群分享的 Ark 内容
     * <p>
     * 对应 NapCat API: {@code ArkShareGroup}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "分享群 (Ark)",
        description = "获取群分享的 Ark 内容",
        categories = {"消息扩展"}
    )
    String arksharegroup(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 分享用户 (Ark)。
     * <p>
     * 获取用户推荐的 Ark 内容
     * <p>
     * 对应 NapCat API: {@code ArkSharePeer}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【可选】QQ号
     *
     * @param groupId 【可选】群号
     *
     * @param phoneNumber 【必填】手机号（默认 ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "分享用户 (Ark)",
        description = "获取用户推荐的 Ark 内容",
        categories = {"消息扩展"}
    )
    ArksharepeerData arksharepeer(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                  @ActionParam(description = "QQ号", order = 1, nullable = true) Long userId,
                                  @ActionParam(description = "群号", order = 2, nullable = true) Long groupId,
                                  @ActionParam(description = "手机号（默认 ）", order = 3) String phoneNumber);

    /**
     * 取消群待办。
     * <p>
     * 将指定消息对应的群待办取消
     * <p>
     * 对应 NapCat API: {@code cancel_group_todo}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param messageId 【可选】消息ID
     *
     * @param messageSeq 【可选】消息Seq (可选)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "取消群待办",
        description = "将指定消息对应的群待办取消",
        categories = {"核心接口"}
    )
    void cancelGroupTodo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "群号", order = 1) Long groupId,
                         @ActionParam(description = "消息ID", order = 2, nullable = true) String messageId,
                         @ActionParam(description = "消息Seq (可选)", order = 3, nullable = true) String messageSeq);

    /**
     * 点击内联键盘按钮。
     * <p>
     * 分类：消息扩展
     * <p>
     * 对应 NapCat API: {@code click_inline_keyboard_button}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param botAppid 【必填】机器人AppID
     *
     * @param buttonId 【必填】按钮ID（默认 ）
     *
     * @param callbackData 【必填】回调数据（默认 ）
     *
     * @param msgSeq 【必填】消息序列号（默认 10086）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "点击内联键盘按钮",
        description = "分类：消息扩展",
        categories = {"消息扩展"}
    )
    void clickInlineKeyboardButton(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                   @ActionParam(description = "群号", order = 1) Long groupId,
                                   @ActionParam(description = "机器人AppID", order = 2) String botAppid,
                                   @ActionParam(description = "按钮ID（默认 ）", order = 3) String buttonId,
                                   @ActionParam(description = "回调数据（默认 ）", order = 4) String callbackData,
                                   @ActionParam(description = "消息序列号（默认 10086）", order = 5) String msgSeq);

    /**
     * 完成群待办。
     * <p>
     * 将指定消息对应的群待办标记为已完成
     * <p>
     * 对应 NapCat API: {@code complete_group_todo}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param messageId 【可选】消息ID
     *
     * @param messageSeq 【可选】消息Seq (可选)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "完成群待办",
        description = "将指定消息对应的群待办标记为已完成",
        categories = {"核心接口"}
    )
    void completeGroupTodo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                           @ActionParam(description = "群号", order = 1) Long groupId,
                           @ActionParam(description = "消息ID", order = 2, nullable = true) String messageId,
                           @ActionParam(description = "消息Seq (可选)", order = 3, nullable = true) String messageSeq);

    /**
     * 撤回消息。
     * <p>
     * 撤回已发送的消息
     * <p>
     * 对应 NapCat API: {@code delete_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "撤回消息",
        description = "撤回已发送的消息",
        categories = {"消息接口"}
    )
    void deleteMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                   @ActionParam(description = "消息ID", order = 1) Long messageId);

    /**
     * 获取表情点赞详情。
     * <p>
     * 分类：消息扩展
     * <p>
     * 对应 NapCat API: {@code fetch_emoji_like}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     *
     * @param emojiId 【必填】表情ID
     *
     * @param emojiType 【必填】表情类型
     *
     * @param count 【必填】获取数量（默认 20）
     *
     * @param cookie 【必填】分页Cookie（默认 ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取表情点赞详情",
        description = "分类：消息扩展",
        categories = {"消息扩展"}
    )
    EmojiLikeData fetchEmojiLike(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                 @ActionParam(description = "消息ID", order = 1) Long messageId,
                                 @ActionParam(description = "表情ID", order = 2) Long emojiId,
                                 @ActionParam(description = "表情类型", order = 3) Long emojiType,
                                 @ActionParam(description = "获取数量（默认 20）", order = 4) Integer count,
                                 @ActionParam(description = "分页Cookie（默认 ）", order = 5) String cookie);

    /**
     * 获取语音转文字结果。
     * <p>
     * 分类：消息扩展
     * <p>
     * 对应 NapCat API: {@code fetch_ptt_text}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取语音转文字结果",
        description = "分类：消息扩展",
        categories = {"消息扩展"}
    )
    PttTextData fetchPttText(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                             @ActionParam(description = "消息ID", order = 1) Long messageId);

    /**
     * 转发单条消息。
     * <p>
     * 转发单条消息
     * <p>
     * 对应 NapCat API: {@code forward_friend_single_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     *
     * @param groupId 【可选】目标群号
     *
     * @param userId 【可选】目标用户QQ
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "转发单条消息",
        description = "转发单条消息",
        categories = {"消息接口"}
    )
    void forwardFriendSingleMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                @ActionParam(description = "消息ID", order = 1) Long messageId,
                                @ActionParam(description = "目标群号", order = 2, nullable = true) Long groupId,
                                @ActionParam(description = "目标用户QQ", order = 3, nullable = true) Long userId);

    /**
     * 转发单条消息。
     * <p>
     * 转发单条消息
     * <p>
     * 对应 NapCat API: {@code forward_group_single_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     *
     * @param groupId 【可选】目标群号
     *
     * @param userId 【可选】目标用户QQ
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "转发单条消息",
        description = "转发单条消息",
        categories = {"消息接口"}
    )
    void forwardGroupSingleMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                               @ActionParam(description = "消息ID", order = 1) Long messageId,
                               @ActionParam(description = "目标群号", order = 2, nullable = true) Long groupId,
                               @ActionParam(description = "目标用户QQ", order = 3, nullable = true) Long userId);

    /**
     * 发送戳一戳。
     * <p>
     * 在群聊或私聊中发送戳一戳动作
     * <p>
     * 对应 NapCat API: {@code friend_poke}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【可选】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param targetId 【可选】目标QQ
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送戳一戳",
        description = "在群聊或私聊中发送戳一戳动作",
        categories = {"核心接口"}
    )
    void friendPoke(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                    @ActionParam(description = "群号", order = 1, nullable = true) Long groupId,
                    @ActionParam(description = "用户QQ", order = 2) Long userId,
                    @ActionParam(description = "目标QQ", order = 3, nullable = true) String targetId);

    /**
     * 获取消息表情点赞列表。
     * <p>
     * 分类：消息扩展
     * <p>
     * 对应 NapCat API: {@code get_emoji_likes}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【可选】群号，短ID可不传
     *
     * @param messageId 【必填】消息ID，可以传递长ID或短ID
     *
     * @param emojiId 【必填】表情ID
     *
     * @param emojiType 【可选】表情类型
     *
     * @param count 【必填】数量，0代表全部（默认 0）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取消息表情点赞列表",
        description = "分类：消息扩展",
        categories = {"消息扩展"}
    )
    EmojiLikesData getEmojiLikes(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                 @ActionParam(description = "群号，短ID可不传", order = 1, nullable = true) Long groupId,
                                 @ActionParam(description = "消息ID，可以传递长ID或短ID", order = 2) String messageId,
                                 @ActionParam(description = "表情ID", order = 3) String emojiId,
                                 @ActionParam(description = "表情类型", order = 4, nullable = true) String emojiType,
                                 @ActionParam(description = "数量，0代表全部（默认 0）", order = 5) Integer count);

    /**
     * 获取消息。
     * <p>
     * 根据消息 ID 获取消息详细信息
     * <p>
     * 对应 NapCat API: {@code get_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取消息",
        description = "根据消息 ID 获取消息详细信息",
        categories = {"消息接口"}
    )
    MsgData getMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                   @ActionParam(description = "消息ID", order = 1) Long messageId);

    /**
     * 发送戳一戳。
     * <p>
     * 在群聊或私聊中发送戳一戳动作
     * <p>
     * 对应 NapCat API: {@code group_poke}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【可选】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param targetId 【可选】目标QQ
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送戳一戳",
        description = "在群聊或私聊中发送戳一戳动作",
        categories = {"核心接口"}
    )
    void groupPoke(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                   @ActionParam(description = "群号", order = 1, nullable = true) Long groupId,
                   @ActionParam(description = "用户QQ", order = 2) Long userId,
                   @ActionParam(description = "目标QQ", order = 3, nullable = true) String targetId);

    /**
     * 标记所有消息已读。
     * <p>
     * 分类：消息接口
     * <p>
     * 对应 NapCat API: {@code _mark_all_as_read}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "标记所有消息已读",
        description = "分类：消息接口",
        categories = {"消息接口"}
    )
    String markAllAsRead(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 标记群聊已读。
     * <p>
     * 标记指定渠道的消息为已读
     * <p>
     * 对应 NapCat API: {@code mark_group_msg_as_read}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param messageId 【可选】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "标记群聊已读",
        description = "标记指定渠道的消息为已读",
        categories = {"消息接口"}
    )
    void markGroupMsgAsRead(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                            @ActionParam(description = "用户QQ", order = 1, nullable = true) Long userId,
                            @ActionParam(description = "群号", order = 2, nullable = true) Long groupId,
                            @ActionParam(description = "消息ID", order = 3, nullable = true) String messageId);

    /**
     * 标记消息已读 (Go-CQHTTP)。
     * <p>
     * 标记指定渠道的消息为已读
     * <p>
     * 对应 NapCat API: {@code mark_msg_as_read}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param messageId 【可选】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "标记消息已读 (Go-CQHTTP)",
        description = "标记指定渠道的消息为已读",
        categories = {"消息接口"}
    )
    void markMsgAsRead(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "用户QQ", order = 1, nullable = true) Long userId,
                       @ActionParam(description = "群号", order = 2, nullable = true) Long groupId,
                       @ActionParam(description = "消息ID", order = 3, nullable = true) String messageId);

    /**
     * 标记私聊已读。
     * <p>
     * 标记指定渠道的消息为已读
     * <p>
     * 对应 NapCat API: {@code mark_private_msg_as_read}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param messageId 【可选】消息ID
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "标记私聊已读",
        description = "标记指定渠道的消息为已读",
        categories = {"消息接口"}
    )
    void markPrivateMsgAsRead(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                              @ActionParam(description = "用户QQ", order = 1, nullable = true) Long userId,
                              @ActionParam(description = "群号", order = 2, nullable = true) Long groupId,
                              @ActionParam(description = "消息ID", order = 3, nullable = true) String messageId);

    /**
     * 分享用户 (Ark)。
     * <p>
     * 获取用户推荐的 Ark 内容
     * <p>
     * 对应 NapCat API: {@code send_ark_share}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【可选】QQ号
     *
     * @param groupId 【可选】群号
     *
     * @param phoneNumber 【必填】手机号（默认 ）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "分享用户 (Ark)",
        description = "获取用户推荐的 Ark 内容",
        categories = {"消息扩展"}
    )
    ArksharepeerData sendArkShare(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                  @ActionParam(description = "QQ号", order = 1, nullable = true) Long userId,
                                  @ActionParam(description = "群号", order = 2, nullable = true) Long groupId,
                                  @ActionParam(description = "手机号（默认 ）", order = 3) String phoneNumber);

    /**
     * 分享群 (Ark)。
     * <p>
     * 获取群分享的 Ark 内容
     * <p>
     * 对应 NapCat API: {@code send_group_ark_share}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "分享群 (Ark)",
        description = "获取群分享的 Ark 内容",
        categories = {"消息扩展"}
    )
    String sendGroupArkShare(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                             @ActionParam(description = "群号", order = 1) Long groupId);

    /**
     * 发送消息。
     * <p>
     * 发送私聊或群聊消息
     * <p>
     * 对应 NapCat API: {@code send_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageType 【可选】消息类型 (private/group)
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param message 【必填】OneBot 11 消息混合类型
     *
     * @param autoEscape 【可选】是否作为纯文本发送
     *
     * @param source 【可选】合并转发来源
     *
     * @param news 【可选】合并转发新闻
     *
     * @param summary 【可选】合并转发摘要
     *
     * @param prompt 【可选】合并转发提示
     *
     * @param timeout 【可选】自定义发送超时(毫秒)，覆盖自动计算值
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送消息",
        description = "发送私聊或群聊消息",
        categories = {"消息接口"}
    )
    GroupMsgData sendMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "消息类型 (private/group)", order = 1, nullable = true) String messageType,
                         @ActionParam(description = "用户QQ", order = 2, nullable = true) Long userId,
                         @ActionParam(description = "群号", order = 3, nullable = true) Long groupId,
                         @ActionParam(description = "OneBot 11 消息混合类型", order = 4) String message,
                         @ActionParam(description = "是否作为纯文本发送", order = 5, nullable = true) Boolean autoEscape,
                         @ActionParam(description = "合并转发来源", order = 6, nullable = true) String source,
                         @ActionParam(description = "合并转发新闻", order = 7, nullable = true) List<JsonNode> news,
                         @ActionParam(description = "合并转发摘要", order = 8, nullable = true) String summary,
                         @ActionParam(description = "合并转发提示", order = 9, nullable = true) String prompt,
                         @ActionParam(description = "自定义发送超时(毫秒)，覆盖自动计算值", order = 10, nullable = true) Long timeout);

    /**
     * 发送戳一戳。
     * <p>
     * 在群聊或私聊中发送戳一戳动作
     * <p>
     * 对应 NapCat API: {@code send_poke}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【可选】群号
     *
     * @param userId 【必填】用户QQ
     *
     * @param targetId 【可选】目标QQ
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送戳一戳",
        description = "在群聊或私聊中发送戳一戳动作",
        categories = {"核心接口"}
    )
    void sendPoke(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                  @ActionParam(description = "群号", order = 1, nullable = true) Long groupId,
                  @ActionParam(description = "用户QQ", order = 2) Long userId,
                  @ActionParam(description = "目标QQ", order = 3, nullable = true) String targetId);

    /**
     * 发送私聊消息。
     * <p>
     * 发送私聊消息
     * <p>
     * 对应 NapCat API: {@code send_private_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageType 【可选】消息类型 (private/group)
     *
     * @param userId 【可选】用户QQ
     *
     * @param groupId 【可选】群号
     *
     * @param message 【必填】OneBot 11 消息混合类型
     *
     * @param autoEscape 【可选】是否作为纯文本发送
     *
     * @param source 【可选】合并转发来源
     *
     * @param news 【可选】合并转发新闻
     *
     * @param summary 【可选】合并转发摘要
     *
     * @param prompt 【可选】合并转发提示
     *
     * @param timeout 【可选】自定义发送超时(毫秒)，覆盖自动计算值
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送私聊消息",
        description = "发送私聊消息",
        categories = {"消息接口"}
    )
    GroupMsgData sendPrivateMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                @ActionParam(description = "消息类型 (private/group)", order = 1, nullable = true) String messageType,
                                @ActionParam(description = "用户QQ", order = 2, nullable = true) Long userId,
                                @ActionParam(description = "群号", order = 3, nullable = true) Long groupId,
                                @ActionParam(description = "OneBot 11 消息混合类型", order = 4) String message,
                                @ActionParam(description = "是否作为纯文本发送", order = 5, nullable = true) Boolean autoEscape,
                                @ActionParam(description = "合并转发来源", order = 6, nullable = true) String source,
                                @ActionParam(description = "合并转发新闻", order = 7, nullable = true) List<JsonNode> news,
                                @ActionParam(description = "合并转发摘要", order = 8, nullable = true) String summary,
                                @ActionParam(description = "合并转发提示", order = 9, nullable = true) String prompt,
                                @ActionParam(description = "自定义发送超时(毫秒)，覆盖自动计算值", order = 10, nullable = true) Long timeout);

    /**
     * 设置群待办。
     * <p>
     * 将指定消息设置为群待办
     * <p>
     * 对应 NapCat API: {@code set_group_todo}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param groupId 【必填】群号
     *
     * @param messageId 【可选】消息ID
     *
     * @param messageSeq 【可选】消息Seq (可选)
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置群待办",
        description = "将指定消息设置为群待办",
        categories = {"核心接口"}
    )
    void setGroupTodo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "群号", order = 1) Long groupId,
                      @ActionParam(description = "消息ID", order = 2, nullable = true) String messageId,
                      @ActionParam(description = "消息Seq (可选)", order = 3, nullable = true) String messageSeq);

    /**
     * 设置消息表情点赞。
     * <p>
     * 分类：消息扩展
     * <p>
     * 对应 NapCat API: {@code set_msg_emoji_like}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param messageId 【必填】消息ID
     *
     * @param emojiId 【必填】表情ID
     *
     * @param set 【可选】是否设置
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置消息表情点赞",
        description = "分类：消息扩展",
        categories = {"消息扩展"}
    )
    MsgEmojiLikeData setMsgEmojiLike(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "消息ID", order = 1) Long messageId,
                                     @ActionParam(description = "表情ID", order = 2) Long emojiId,
                                     @ActionParam(description = "是否设置", order = 3, nullable = true) Boolean set);

    /**
     * 添加自定义表情。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code add_custom_face}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param file 【必填】本地表情文件路径
     *
     * @param emojiId 【可选】表情ID，未提供时传空字符串
     *
     * @param packageId 【可选】表情包ID，未提供时传0
     *
     * @param fileName 【可选】文件名，未提供时从file路径取basename
     *
     * @param fileSize 【可选】文件大小，未提供时读取本地文件
     *
     * @param md5 【可选】文件MD5，未提供时读取本地文件计算
     *
     * @param isMarkFace 【可选】是否商城表情
     *
     * @param isOrigin 【可选】是否原图
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "添加自定义表情",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    void addCustomFace(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                       @ActionParam(description = "本地表情文件路径", order = 1) String file,
                       @ActionParam(description = "表情ID，未提供时传空字符串", order = 2, nullable = true) String emojiId,
                       @ActionParam(description = "表情包ID，未提供时传0", order = 3, nullable = true) String packageId,
                       @ActionParam(description = "文件名，未提供时从file路径取basename", order = 4, nullable = true) String fileName,
                       @ActionParam(description = "文件大小，未提供时读取本地文件", order = 5, nullable = true) String fileSize,
                       @ActionParam(description = "文件MD5，未提供时读取本地文件计算", order = 6, nullable = true) String md5,
                       @ActionParam(description = "是否商城表情", order = 7, nullable = true) Boolean isMarkFace,
                       @ActionParam(description = "是否原图", order = 8, nullable = true) Boolean isOrigin);

    /**
     * 退出登录。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code bot_exit}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "退出登录",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    void botExit(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 是否可以发送图片。
     * <p>
     * 检查是否可以发送图片
     * <p>
     * 对应 NapCat API: {@code can_send_image}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "是否可以发送图片",
        description = "检查是否可以发送图片",
        categories = {"系统接口"}
    )
    RecordData canSendImage(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 是否可以发送语音。
     * <p>
     * 检查是否可以发送语音
     * <p>
     * 对应 NapCat API: {@code can_send_record}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "是否可以发送语音",
        description = "检查是否可以发送语音",
        categories = {"系统接口"}
    )
    RecordData canSendRecord(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 清理缓存。
     * <p>
     * 清理缓存
     * <p>
     * 对应 NapCat API: {@code clean_cache}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "清理缓存",
        description = "清理缓存",
        categories = {"系统接口"}
    )
    void cleanCache(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 删除自定义表情。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code delete_custom_face}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param resId 【可选】fetch_custom_face_detail返回的resId
     *
     * @param id 【可选】native deleteFavEmoji字符串ID，通常为resId
     *
     * @param ids 【可选】native deleteFavEmoji字符串ID列表，通常为resId列表
     *
     * @param md5 【可选】表情MD5，不能直接删除，请先通过fetch_custom_face_detail获取resId
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "删除自定义表情",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    void deleteCustomFace(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                          @ActionParam(description = "fetch_custom_face_detail返回的resId", order = 1, nullable = true) String resId,
                          @ActionParam(description = "native deleteFavEmoji字符串ID，通常为resId", order = 2, nullable = true) String id,
                          @ActionParam(description = "native deleteFavEmoji字符串ID列表，通常为resId列表", order = 3, nullable = true) List<String> ids,
                          @ActionParam(description = "表情MD5，不能直接删除，请先通过fetch_custom_face_detail获取resId", order = 4, nullable = true) String md5);

    /**
     * 获取自定义表情。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code fetch_custom_face}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param count 【必填】获取数量（默认 48）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取自定义表情",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    List<String> fetchCustomFace(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                 @ActionParam(description = "获取数量（默认 48）", order = 1) Integer count);

    /**
     * 获取自定义表情详情。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code fetch_custom_face_detail}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param count 【必填】获取数量（默认 48）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取自定义表情详情",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    void fetchCustomFaceDetail(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                               @ActionParam(description = "获取数量（默认 48）", order = 1) Integer count);

    /**
     * 获取收藏列表。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code get_collection_list}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param category 【必填】分类ID
     *
     * @param count 【必填】获取数量（默认 50）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取收藏列表",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    CollectionData getCollectionList(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "分类ID", order = 1) String category,
                                     @ActionParam(description = "获取数量（默认 50）", order = 2) String count);

    /**
     * 获取登录凭证。
     * <p>
     * 获取登录凭证
     * <p>
     * 对应 NapCat API: {@code get_credentials}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param domain 【必填】需要获取 cookies 的域名
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取登录凭证",
        description = "获取登录凭证",
        categories = {"系统接口"}
    )
    CredentialsData getCredentials(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                   @ActionParam(description = "需要获取 cookies 的域名", order = 1) String domain);

    /**
     * 获取 CSRF Token。
     * <p>
     * 获取 CSRF Token
     * <p>
     * 对应 NapCat API: {@code get_csrf_token}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取 CSRF Token",
        description = "获取 CSRF Token",
        categories = {"系统接口"}
    )
    CsrfTokenData getCsrfToken(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取可疑好友申请。
     * <p>
     * 获取系统的可疑好友申请列表
     * <p>
     * 对应 NapCat API: {@code get_doubt_friends_add_request}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param count 【必填】获取数量（默认 50）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取可疑好友申请",
        description = "获取系统的可疑好友申请列表",
        categories = {"系统接口"}
    )
    List<DoubtFriendsAddRequestData> getDoubtFriendsAddRequest(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                                               @ActionParam(description = "获取数量（默认 50）", order = 1) Integer count);

    /**
     * 获取群系统消息。
     * <p>
     * 获取群系统消息
     * <p>
     * 对应 NapCat API: {@code get_group_system_msg}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param count 【必填】获取的消息数量（默认 50）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取群系统消息",
        description = "获取群系统消息",
        categories = {"系统接口"}
    )
    GroupIgnoredNotifiesData getGroupSystemMsg(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                               @ActionParam(description = "获取的消息数量（默认 50）", order = 1) Integer count);

    /**
     * 获取登录号信息。
     * <p>
     * 获取当前登录帐号的信息
     * <p>
     * 对应 NapCat API: {@code get_login_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取登录号信息",
        description = "获取当前登录帐号的信息",
        categories = {"系统接口"}
    )
    LoginInfoData getLoginInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取小程序 Ark。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code get_mini_app_ark}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取小程序 Ark",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    MiniAppArkData getMiniAppArk(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取扩展 RKey。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code get_rkey}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取扩展 RKey",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    List<RkeyData> getRkey(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取 RKey 服务器。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code get_rkey_server}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取 RKey 服务器",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    RkeyServerData getRkeyServer(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取机器人 UIN 范围。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code get_robot_uin_range}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取机器人 UIN 范围",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    List<RobotUinRangeData> getRobotUinRange(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取运行状态。
     * <p>
     * 获取运行状态
     * <p>
     * 对应 NapCat API: {@code get_status}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取运行状态",
        description = "获取运行状态",
        categories = {"系统接口"}
    )
    StatusData getStatus(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取版本信息。
     * <p>
     * 获取版本信息
     * <p>
     * 对应 NapCat API: {@code get_version_info}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取版本信息",
        description = "获取版本信息",
        categories = {"系统接口"}
    )
    VersionInfoData getVersionInfo(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取Packet状态。
     * <p>
     * 获取底层Packet服务的运行状态
     * <p>
     * 对应 NapCat API: {@code nc_get_packet_status}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取Packet状态",
        description = "获取底层Packet服务的运行状态",
        categories = {"系统接口"}
    )
    void ncGetPacketStatus(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取 RKey。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code nc_get_rkey}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取 RKey",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    List<RkeyData> ncGetRkey(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

    /**
     * 获取用户在线状态。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code nc_get_user_status}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】QQ号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "获取用户在线状态",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    UserStatusData ncGetUserStatus(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                   @ActionParam(description = "QQ号", order = 1) Long userId);

    /**
     * 发送原始数据包。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code send_packet}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param cmd 【必填】命令字
     *
     * @param data 【必填】十六进制数据
     *
     * @param rsp 【必填】是否等待响应（默认 True）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "发送原始数据包",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    String sendPacket(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                      @ActionParam(description = "命令字", order = 1) String cmd,
                      @ActionParam(description = "十六进制数据", order = 2) String data,
                      @ActionParam(description = "是否等待响应（默认 True）", order = 3) String rsp);

    /**
     * 修改自定义表情描述。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code set_custom_face_desc}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param emojiId 【必填】表情ID
     *
     * @param resId 【必填】资源ID
     *
     * @param md5 【必填】表情MD5
     *
     * @param desc 【必填】新的表情描述
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "修改自定义表情描述",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    void setCustomFaceDesc(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                           @ActionParam(description = "表情ID", order = 1) Long emojiId,
                           @ActionParam(description = "资源ID", order = 2) String resId,
                           @ActionParam(description = "表情MD5", order = 3) String md5,
                           @ActionParam(description = "新的表情描述", order = 4) String desc);

    /**
     * 处理可疑好友申请。
     * <p>
     * 同意或拒绝系统的可疑好友申请
     * <p>
     * 对应 NapCat API: {@code set_doubt_friends_add_request}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param flag 【必填】请求 flag
     *
     * @param approve 【必填】是否同意 (强制为 true)（默认 True）
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "处理可疑好友申请",
        description = "同意或拒绝系统的可疑好友申请",
        categories = {"系统接口"}
    )
    String setDoubtFriendsAddRequest(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                                     @ActionParam(description = "请求 flag", order = 1) String flag,
                                     @ActionParam(description = "是否同意 (强制为 true)（默认 True）", order = 2) Boolean approve);

    /**
     * 设置输入状态。
     * <p>
     * 分类：系统扩展
     * <p>
     * 对应 NapCat API: {@code set_input_status}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param userId 【必填】QQ号
     *
     * @param eventType 【必填】事件类型
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置输入状态",
        description = "分类：系统扩展",
        categories = {"系统扩展"}
    )
    void setInputStatus(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                        @ActionParam(description = "QQ号", order = 1) Long userId,
                        @ActionParam(description = "事件类型", order = 2) Long eventType);

    /**
     * 设置在线状态。
     * <p>
     * ## 状态列表
     * <p>
     * 对应 NapCat API: {@code set_online_status}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     *
     * @param status 【必填】在线状态
     *
     * @param extStatus 【必填】扩展状态
     *
     * @param batteryStatus 【必填】电量状态
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "设置在线状态",
        description = "## 状态列表",
        categories = {"系统扩展"}
    )
    void setOnlineStatus(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ,
                         @ActionParam(description = "在线状态", order = 1) Long status,
                         @ActionParam(description = "扩展状态", order = 2) Long extStatus,
                         @ActionParam(description = "电量状态", order = 3) Long batteryStatus);

    /**
     * 重启服务。
     * <p>
     * 重启服务
     * <p>
     * 对应 NapCat API: {@code set_restart}
     *
     * @param botQQ 【必填】目标 Bot 的 QQ 号
     * <p>
     * <b>可能的错误情况：</b>
     * {@code retcode=1400: 请求参数错误或业务逻辑执行失败}
     * {@code retcode=1401: 权限不足}
     * {@code retcode=1404: 资源不存在}
     */
    @BotAction(
        name = "重启服务",
        description = "重启服务",
        categories = {"系统接口"}
    )
    void setRestart(@ActionParam(description = "目标 Bot 的 QQ 号") long botQQ);

}
