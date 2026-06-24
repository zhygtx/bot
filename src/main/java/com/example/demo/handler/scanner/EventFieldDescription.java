package com.example.demo.handler.scanner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件字段描述提供器。
 * <p>
 * 集中管理所有 OneBot SDK 事件/响应字段的中文描述。
 * 扫描器在反射扫描时根据 {@code field.getDeclaringClass().getSimpleName() + "." + field.getName()}
 * 在本 Map 中查找描述。
 * <p>
 * <b>本 static 块由脚本自动生成，请勿手动修改！</b>
 * <p>
 * 生成命令:
 * <pre>python scripts/generate_event_field_desc.py</pre>
 * <p>
 * 用户自定义实体类字段描述使用 {@link com.example.demo.annotation.DisplayField @DisplayField} 注解。
 */
public final class EventFieldDescription {

    private static final Map<String, String> DESCRIPTIONS = new ConcurrentHashMap<>();

    static {
        // === GENERATED START ===

        // ================================================================
        //  BaseEvent（事件基类）
        // ================================================================
        // BaseEvent
        put("BaseEvent.postType", "事件类型（message / message_sent / notice / request / meta_event）");
        put("BaseEvent.selfId", "收到事件的机器人 QQ 号");
        put("BaseEvent.time", "事件发生的时间戳（秒）");


        // ================================================================
        //  消息事件（MessageEvent 系列）
        // ================================================================
        // GroupMessageEvent
        put("GroupMessageEvent.anonymous", "匿名信息（匿名消息时不为 null）");
        put("GroupMessageEvent.groupId", "群号");
        put("GroupMessageEvent.sender", "发送者信息");
        put("GroupMessageEvent.subType", "子类型（normal / anonymous / notice）");

        // MessageEvent
        put("MessageEvent.font", "字体 ID");
        put("MessageEvent.message", "消息内容（消息段列表）");
        put("MessageEvent.messageId", "消息 ID");
        put("MessageEvent.rawMessage", "CQ 码格式的原始消息文本");
        put("MessageEvent.userId", "发送者 QQ 号");

        // PrivateGroupMessageEvent
        put("PrivateGroupMessageEvent.targetId", "临时会话来源 ID（群号）");
        put("PrivateGroupMessageEvent.tempSource", "临时会话来源类型");

        // PrivateMessageEvent
        put("PrivateMessageEvent.sender", "发送者信息");
        put("PrivateMessageEvent.subType", "子类型（friend / group / other）");
        put("PrivateMessageEvent.targetId", "临时会话来源 ID（临时会话时有效）");
        put("PrivateMessageEvent.tempSource", "临时会话来源类型");


        // ================================================================
        //  消息发送事件（MessageSentEvent 系列）
        // ================================================================
        // GroupMessageSentEvent
        put("GroupMessageSentEvent.groupId", "群号");
        put("GroupMessageSentEvent.messageType", "消息类型，固定为 \"group\"");
        put("GroupMessageSentEvent.sender", "发送者信息");
        put("GroupMessageSentEvent.subType", "子类型（normal 等）");

        // MessageSentEvent
        put("MessageSentEvent.messageType", "消息类型（private / group）");
        put("MessageSentEvent.sender", "发送者信息");
        put("MessageSentEvent.targetId", "消息接收者 ID（群号或用户 QQ）");

        // PrivateMessageSentEvent
        put("PrivateMessageSentEvent.privateMessageType", "消息类型，固定为 \"private\"");
        put("PrivateMessageSentEvent.sender", "发送者信息");
        put("PrivateMessageSentEvent.subType", "子类型（friend / group 等）");


        // ================================================================
        //  通知事件（NoticeEvent 系列）
        // ================================================================
        // BotOfflineNoticeEvent
        put("BotOfflineNoticeEvent.message", "离线原因详情");
        put("BotOfflineNoticeEvent.tag", "离线原因标签");
        put("BotOfflineNoticeEvent.userId", "Bot QQ 号");

        // FriendAddNoticeEvent
        put("FriendAddNoticeEvent.userId", "新添加的好友 QQ 号");

        // FriendRecallNoticeEvent
        put("FriendRecallNoticeEvent.messageId", "被撤回的消息 ID");
        put("FriendRecallNoticeEvent.userId", "好友 QQ 号");

        // GroupAdminNoticeEvent
        put("GroupAdminNoticeEvent.subType", "子类型（set / unset）");

        // GroupBanBanNoticeEvent
        put("GroupBanBanNoticeEvent.duration", "禁言时长（秒）");

        // GroupBanLiftBanNoticeEvent
        put("GroupBanLiftBanNoticeEvent.duration", "解除前已禁言的时长（秒）");

        // GroupBanNoticeEvent
        put("GroupBanNoticeEvent.duration", "禁言时长（秒），解除禁言时为 0");
        put("GroupBanNoticeEvent.operatorId", "操作者 QQ 号（管理员或群主）");
        put("GroupBanNoticeEvent.subType", "子类型（ban / lift_ban）");

        // GroupCardNoticeEvent
        put("GroupCardNoticeEvent.cardNew", "新群名片");
        put("GroupCardNoticeEvent.cardOld", "旧群名片");

        // GroupDecreaseKickMeNoticeEvent
        put("GroupDecreaseKickMeNoticeEvent.operatorId", "操作者 QQ 号（踢出机器人的管理员）");

        // GroupDecreaseKickNoticeEvent
        put("GroupDecreaseKickNoticeEvent.operatorId", "操作者 QQ 号（踢人的管理员）");

        // GroupDecreaseLeaveNoticeEvent
        put("GroupDecreaseLeaveNoticeEvent.operatorId", "退群者 QQ 号");

        // GroupDecreaseNoticeEvent
        put("GroupDecreaseNoticeEvent.operatorId", "操作者 QQ 号（管理员或退群者本人）");
        put("GroupDecreaseNoticeEvent.subType", "子类型（leave / kick / kick_me / disband）");

        // GroupEssenceNoticeEvent
        put("GroupEssenceNoticeEvent.messageId", "消息 ID");
        put("GroupEssenceNoticeEvent.operatorId", "操作者 QQ 号（设置精华的管理员）");
        put("GroupEssenceNoticeEvent.senderId", "消息发送者 QQ 号");
        put("GroupEssenceNoticeEvent.subType", "子类型（add / delete）");

        // GroupIncreaseApproveNoticeEvent
        put("GroupIncreaseApproveNoticeEvent.operatorId", "同意入群的管理员 QQ 号");

        // GroupIncreaseInviteNoticeEvent
        put("GroupIncreaseInviteNoticeEvent.operatorId", "邀请者 QQ 号");

        // GroupIncreaseNoticeEvent
        put("GroupIncreaseNoticeEvent.operatorId", "操作者 QQ 号（同意入群的管理员或邀请者）");
        put("GroupIncreaseNoticeEvent.subType", "子类型（approve / invite）");

        // GroupMsgEmojiLikeNoticeEvent
        put("GroupMsgEmojiLikeNoticeEvent.likes", "表情回应列表");
        put("GroupMsgEmojiLikeNoticeEvent.messageId", "被点赞的消息 ID");

        // GroupNoticeEvent
        put("GroupNoticeEvent.groupId", "群号");
        put("GroupNoticeEvent.userId", "相关用户 QQ 号");

        // GroupRecallNoticeEvent
        put("GroupRecallNoticeEvent.messageId", "被撤回的消息 ID");
        put("GroupRecallNoticeEvent.operatorId", "操作者 QQ 号（撤回消息的人，可能是发送者本人或管理员）");

        // GroupTitleNoticeEvent
        put("GroupTitleNoticeEvent.title", "新的专属头衔");

        // GroupUploadNoticeEvent
        put("GroupUploadNoticeEvent.file", "上传的文件信息");

        // InputStatusNoticeEvent
        put("InputStatusNoticeEvent.eventType", "输入事件类型");
        put("InputStatusNoticeEvent.groupId", "群号（群聊输入状态时有效）");
        put("InputStatusNoticeEvent.statusText", "输入状态文本");
        put("InputStatusNoticeEvent.userId", "正在输入的用户 QQ 号");

        // NoticeEvent
        put("NoticeEvent.noticeType", "通知类型（group_upload / group_admin / group_decrease 等）");

        // PokeNoticeEvent
        put("PokeNoticeEvent.groupId", "群号（好友戳一戳时为 null）");
        put("PokeNoticeEvent.targetId", "被戳的用户 QQ 号");
        put("PokeNoticeEvent.userId", "发送戳一戳的用户 QQ 号");

        // ProfileLikeNoticeEvent
        put("ProfileLikeNoticeEvent.operatorId", "点赞者 QQ 号");
        put("ProfileLikeNoticeEvent.operatorNick", "点赞者昵称");
        put("ProfileLikeNoticeEvent.times", "点赞次数");

        // TitleNoticeEvent
        put("TitleNoticeEvent.title", "变更后的头衔名称");


        // ================================================================
        //  请求事件（RequestEvent 系列）
        // ================================================================
        // GroupRequestEvent
        put("GroupRequestEvent.groupId", "群号");
        put("GroupRequestEvent.subType", "子类型（add / invite）");

        // RequestEvent
        put("RequestEvent.comment", "验证信息（加好友/加群的附言）");
        put("RequestEvent.flag", "请求标识（用于处理请求的 flag）");
        put("RequestEvent.requestType", "请求类型（friend / group）");
        put("RequestEvent.userId", "请求发送者 QQ 号");


        // ================================================================
        //  元事件（MetaEvent 系列）
        // ================================================================
        // HeartbeatMetaEvent
        put("HeartbeatMetaEvent.interval", "心跳间隔（毫秒）");
        put("HeartbeatMetaEvent.status", "状态信息（包含 online、good 等字段）");

        // LifecycleMetaEvent
        put("LifecycleMetaEvent.subType", "子类型（enable / disable / connect）");

        // MetaEvent
        put("MetaEvent.metaEventType", "元事件类型（heartbeat / lifecycle）");


        // ================================================================
        //  Sender 辅助类
        // ================================================================
        // FriendSender
        put("FriendSender.age", "年龄");
        put("FriendSender.nickname", "昵称");
        put("FriendSender.sex", "性别（male / female / unknown）");
        put("FriendSender.userId", "发送者 QQ 号");

        // GroupSender
        put("GroupSender.age", "年龄");
        put("GroupSender.card", "群名片/群昵称");
        put("GroupSender.level", "成员等级");
        put("GroupSender.nickname", "昵称");
        put("GroupSender.role", "角色（owner / admin / member）");
        put("GroupSender.sex", "性别（male / female / unknown）");
        put("GroupSender.title", "专属头衔");
        put("GroupSender.userId", "发送者 QQ 号");


        // ================================================================
        //  文件信息类
        // ================================================================
        // GroupUploadFileInfo
        put("GroupUploadFileInfo.busid", "文件总线 ID");
        put("GroupUploadFileInfo.id", "文件 ID");
        put("GroupUploadFileInfo.name", "文件名");
        put("GroupUploadFileInfo.size", "文件大小（字节）");


        // ================================================================
        //  API 响应数据类
        // ================================================================
        // AiCharactersData
        put("AiCharactersData.avatarUrl", "角色头像 URL");
        put("AiCharactersData.characterId", "角色 ID");
        put("AiCharactersData.characterName", "角色名称");
        put("AiCharactersData.characterType", "角色类型");

        // AiRecordData
        put("AiRecordData.record", "AI 语音内容（JSON 字符串或 base64）");

        // ArkShareData
        put("ArkShareData.ark", "Ark JSON 内容");

        // CanSendData
        put("CanSendData.yes", "是否可以发送");

        // ClientKeyData
        put("ClientKeyData.clientKey", "ClientKey 字符串");

        // CollectionListData
        put("CollectionListData.collectionList", "收藏列表");
        put("CollectionListData.hasMore", "是否还有更多");
        put("CollectionListData.nextStartPos", "下一页起始位置");

        // CookiesData
        put("CookiesData.bkn", "bkn（CSRF Token 衍生值）");
        put("CookiesData.cookies", "Cookies 字符串");

        // CreateCollectionData
        put("CreateCollectionData.collectionId", "收藏 ID");
        put("CreateCollectionData.createTime", "创建时间");
        put("CreateCollectionData.errMsg", "错误信息");
        put("CreateCollectionData.result", "结果码（0 表示成功）");

        // CredentialsData
        put("CredentialsData.cookies", "Cookies 字符串");
        put("CredentialsData.csrfToken", "CSRF Token");

        // CsrfTokenData
        put("CsrfTokenData.token", "token 值");

        // CustomFaceData
        put("CustomFaceData.emojiId", "表情 ID");
        put("CustomFaceData.emojiName", "表情名称");
        put("CustomFaceData.filePath", "表情文件路径");
        put("CustomFaceData.height", "表情高度");
        put("CustomFaceData.isAnimation", "是否为动态表情");
        put("CustomFaceData.packageId", "表情包 ID");
        put("CustomFaceData.width", "表情宽度");

        // CustomFaceDetailData
        put("CustomFaceDetailData.emojiDesc", "表情描述");
        put("CustomFaceDetailData.emojiId", "表情 ID");
        put("CustomFaceDetailData.emojiName", "表情名称");
        put("CustomFaceDetailData.fileSize", "文件大小");
        put("CustomFaceDetailData.height", "高度");
        put("CustomFaceDetailData.isAnimation", "是否为动态表情");
        put("CustomFaceDetailData.md5", "MD5");
        put("CustomFaceDetailData.packageId", "表情包 ID");
        put("CustomFaceDetailData.width", "宽度");

        // DoubtFriendAddRequestData
        put("DoubtFriendAddRequestData.flag", "申请标识（flag）");
        put("DoubtFriendAddRequestData.message", "申请消息");
        put("DoubtFriendAddRequestData.requesterNick", "申请者昵称");
        put("DoubtFriendAddRequestData.requesterUin", "申请者 QQ");
        put("DoubtFriendAddRequestData.source", "申请来源描述");
        put("DoubtFriendAddRequestData.time", "申请时间");

        // EmojiLikeDetailData
        put("EmojiLikeDetailData.cookie", "翻页 cookie");
        put("EmojiLikeDetailData.emojiLikesList", "表情点赞列表");
        put("EmojiLikeDetailData.errMsg", "错误信息");
        put("EmojiLikeDetailData.isFirstPage", "是否为第一页");
        put("EmojiLikeDetailData.isLastPage", "是否为最后一页");
        put("EmojiLikeDetailData.result", "结果码");

        // EmojiLikesData
        put("EmojiLikesData.emojiLikeList", "表情点赞列表");

        // EssenceMsgData
        put("EssenceMsgData.content", "消息内容（原始 JSON）");
        put("EssenceMsgData.operatorId", "操作者 QQ");
        put("EssenceMsgData.operatorNick", "操作者昵称");
        put("EssenceMsgData.operatorTime", "操作时间");
        put("EssenceMsgData.senderId", "发送者 QQ");
        put("EssenceMsgData.senderNick", "发送者昵称");
        put("EssenceMsgData.senderTime", "发送时间");

        // FileData
        put("FileData.base64", "Base64 编码内容（可选）");
        put("FileData.file", "文件路径");
        put("FileData.fileName", "文件名（可选）");
        put("FileData.fileSize", "文件大小（可选）");
        put("FileData.url", "URL（可选）");

        // FileDownloadData
        put("FileDownloadData.file", "下载后的本地文件路径");

        // FileUploadData
        put("FileUploadData.fileId", "文件 ID");

        // FileUrlData
        put("FileUrlData.url", "文件下载 URL");

        // FilesetIdData
        put("FilesetIdData.filesetId", "文件集 ID");

        // FilesetInfoData
        put("FilesetInfoData.createTime", "创建时间");
        put("FilesetInfoData.creatorId", "创建者 QQ");
        put("FilesetInfoData.creatorName", "创建者昵称");
        put("FilesetInfoData.filesetId", "文件集 ID");
        put("FilesetInfoData.name", "文件集名称");
        put("FilesetInfoData.totalFileCount", "文件集内文件总数");
        put("FilesetInfoData.totalFileSize", "文件集总大小（字节）");

        // FlashTaskData
        put("FlashTaskData.taskId", "任务 ID");

        // ForwardMsgData
        put("ForwardMsgData.messages", "消息内容（数组形式的转发消息节点列表）");

        // FriendCategoryData
        put("FriendCategoryData.buddyList", "该分组下的好友列表");
        put("FriendCategoryData.categoryId", "分组 ID");
        put("FriendCategoryData.categoryName", "分组名称");
        put("FriendCategoryData.categorySortId", "分组排序");

        // FriendListData
        put("FriendListData.age", "年龄");
        put("FriendListData.birthdayDay", "出生日期");
        put("FriendListData.birthdayMonth", "出生月份");
        put("FriendListData.birthdayYear", "出生年份");
        put("FriendListData.categoryId", "分组 ID");
        put("FriendListData.categoryName", "分组名称");
        put("FriendListData.email", "邮箱");
        put("FriendListData.level", "等级");
        put("FriendListData.loginDays", "登录天数");
        put("FriendListData.nickname", "昵称");
        put("FriendListData.phoneNum", "手机号");
        put("FriendListData.qid", "QID");
        put("FriendListData.remark", "备注");
        put("FriendListData.sex", "性别");
        put("FriendListData.userId", "QQ 号");

        // GetMsgData
        put("GetMsgData.emojiLikesList", "表情回应列表（可选）");
        put("GetMsgData.font", "字体");
        put("GetMsgData.groupId", "群号（仅群消息有）");
        put("GetMsgData.message", "消息内容（支持 String 或 Array）");
        put("GetMsgData.messageId", "消息 ID");
        put("GetMsgData.messageSeq", "消息序号");
        put("GetMsgData.messageType", "消息类型（private/group）");
        put("GetMsgData.rawMessage", "原始消息内容");
        put("GetMsgData.realId", "真实 ID");
        put("GetMsgData.sender", "发送者信息（原始 JSON 节点，sender 结构不固定）");
        put("GetMsgData.time", "发送时间戳");
        put("GetMsgData.userId", "发送者 QQ 号");

        // GroupAlbumMediaListData
        put("GroupAlbumMediaListData.attachInfo", "翻页附加信息");
        put("GroupAlbumMediaListData.hasMore", "是否还有更多");
        put("GroupAlbumMediaListData.mediaList", "媒体列表");

        // GroupAtAllRemainData
        put("GroupAtAllRemainData.canAtAll", "是否可以 @全体");
        put("GroupAtAllRemainData.remainAtAllCountForGroup", "群内 @全体剩余次数");
        put("GroupAtAllRemainData.remainAtAllCountForUin", "当前 Uin @全体剩余次数");

        // GroupDetailInfoData
        put("GroupDetailInfoData.activeMemberCount", "群活跃度");
        put("GroupDetailInfoData.addOption", "加群选项");
        put("GroupDetailInfoData.createTime", "群创建时间");
        put("GroupDetailInfoData.groupClass", "群分类");
        put("GroupDetailInfoData.groupDesc", "群描述");
        put("GroupDetailInfoData.groupFace", "群头像");
        put("GroupDetailInfoData.groupId", "群号");
        put("GroupDetailInfoData.groupLevel", "群等级");
        put("GroupDetailInfoData.groupName", "群名称");
        put("GroupDetailInfoData.maxMemberCount", "最大成员数");
        put("GroupDetailInfoData.memberCount", "群成员数");
        put("GroupDetailInfoData.memo", "群公告");
        put("GroupDetailInfoData.ownerQq", "群主 QQ");
        put("GroupDetailInfoData.robotAddOption", "是否允许群机器人加群");
        put("GroupDetailInfoData.searchable", "是否可搜索");
        put("GroupDetailInfoData.tags", "群标签");

        // GroupFileSystemInfoData
        put("GroupFileSystemInfoData.fileCount", "文件总数");
        put("GroupFileSystemInfoData.limitCount", "文件数量上限");
        put("GroupFileSystemInfoData.totalSpace", "总空间（字节）");
        put("GroupFileSystemInfoData.usedSpace", "已用空间（字节）");

        // GroupHonorInfoData
        put("GroupHonorInfoData.currentTalkative", "当前龙王信息");
        put("GroupHonorInfoData.emotionList", "快乐源泉/表情榜单列表");
        put("GroupHonorInfoData.groupId", "群号");
        put("GroupHonorInfoData.honorList", "荣誉列表（保留兼容，与上述各榜单对应）");
        put("GroupHonorInfoData.legendList", "传说榜单列表");
        put("GroupHonorInfoData.performerList", "群聊之火/达人榜单列表");
        put("GroupHonorInfoData.strongNewbieList", "新兴群友榜单列表");
        put("GroupHonorInfoData.talkativeList", "龙王榜单列表");

        // GroupIgnoreAddRequestData
        put("GroupIgnoreAddRequestData.actorNick", "操作者昵称");
        put("GroupIgnoreAddRequestData.actorUin", "操作者 QQ");
        put("GroupIgnoreAddRequestData.checked", "是否已检查");
        put("GroupIgnoreAddRequestData.groupId", "群号");
        put("GroupIgnoreAddRequestData.groupName", "群名");
        put("GroupIgnoreAddRequestData.message", "请求消息");
        put("GroupIgnoreAddRequestData.raw", "原始 JSON（保留未映射字段）");
        put("GroupIgnoreAddRequestData.requestId", "请求 ID");
        put("GroupIgnoreAddRequestData.requesterNick", "请求者昵称");
        put("GroupIgnoreAddRequestData.requesterUin", "请求者 QQ");
        put("GroupIgnoreAddRequestData.time", "请求时间");

        // GroupIgnoredNotifiesData
        put("GroupIgnoredNotifiesData.invitedRequests", "被邀请入群请求列表");
        put("GroupIgnoredNotifiesData.joinRequests", "加群请求列表");

        // GroupInfoData
        put("GroupInfoData.groupAllShut", "是否全员禁言（0 表示未开启）");
        put("GroupInfoData.groupId", "群号");
        put("GroupInfoData.groupName", "群名称");
        put("GroupInfoData.groupRemark", "群备注");
        put("GroupInfoData.maxMemberCount", "最大成员人数");
        put("GroupInfoData.memberCount", "成员人数");

        // GroupInfoExData
        put("GroupInfoExData.admins", "管理员列表");
        put("GroupInfoExData.groupId", "群号");
        put("GroupInfoExData.groupName", "群名称");
        put("GroupInfoExData.groupRemark", "群备注");
        put("GroupInfoExData.ownerQq", "群主 QQ");

        // GroupMemberData
        put("GroupMemberData.age", "年龄");
        put("GroupMemberData.area", "地区");
        put("GroupMemberData.card", "群名片");
        put("GroupMemberData.cardChangeable", "是否允许修改名片");
        put("GroupMemberData.groupId", "群号");
        put("GroupMemberData.isRobot", "是否为机器人");
        put("GroupMemberData.joinTime", "入群时间戳");
        put("GroupMemberData.lastSentTime", "最后发言时间戳");
        put("GroupMemberData.level", "等级");
        put("GroupMemberData.nickname", "昵称");
        put("GroupMemberData.qage", "Q 龄");
        put("GroupMemberData.qqLevel", "QQ 等级");
        put("GroupMemberData.role", "角色（owner/admin/member）");
        put("GroupMemberData.sex", "性别");
        put("GroupMemberData.shutUpTimestamp", "禁言截止时间戳");
        put("GroupMemberData.title", "头衔");
        put("GroupMemberData.titleExpireTime", "头衔过期时间");
        put("GroupMemberData.unfriendly", "是否不良记录");
        put("GroupMemberData.userId", "QQ 号");

        // GroupNoticeData
        put("GroupNoticeData.message", "公告消息（含 text 和 images）");
        put("GroupNoticeData.noticeId", "公告 ID");
        put("GroupNoticeData.pinned", "是否置顶");
        put("GroupNoticeData.publishTime", "发布时间");
        put("GroupNoticeData.senderId", "发送者 QQ");

        // GroupRootFilesData
        put("GroupRootFilesData.busid", "busid");
        put("GroupRootFilesData.createTime", "创建时间");
        put("GroupRootFilesData.creatorNickname", "创建者昵称");
        put("GroupRootFilesData.creatorUserId", "创建者 QQ");
        put("GroupRootFilesData.deadTime", "过期时间");
        put("GroupRootFilesData.downloadTimes", "下载次数");
        put("GroupRootFilesData.fileId", "文件 ID");
        put("GroupRootFilesData.fileName", "文件名");
        put("GroupRootFilesData.fileSize", "文件大小");
        put("GroupRootFilesData.files", "文件列表");
        put("GroupRootFilesData.folderId", "文件夹 ID");
        put("GroupRootFilesData.folderName", "文件夹名");
        put("GroupRootFilesData.folders", "文件夹列表");
        put("GroupRootFilesData.modifyTime", "修改时间");
        put("GroupRootFilesData.totalFileCount", "文件夹内文件数");
        put("GroupRootFilesData.uploadTime", "上传时间");
        put("GroupRootFilesData.uploaderNickname", "上传者昵称");
        put("GroupRootFilesData.uploaderUserId", "上传者 QQ");

        // GroupShutMemberData
        put("GroupShutMemberData.shutUpTime", "禁言到期时间戳");
        put("GroupShutMemberData.userId", "QQ 号");

        // GroupSignedData
        put("GroupSignedData.nick", "打卡者昵称");
        put("GroupSignedData.rank", "打卡排名");
        put("GroupSignedData.time", "打卡时间");
        put("GroupSignedData.userId", "打卡者 QQ");

        // GroupSystemMsgData
        put("GroupSystemMsgData.invitedRequests", "被邀请入群列表");
        put("GroupSystemMsgData.joinRequests", "加群请求列表");

        // GuildListData
        put("GuildListData.guildDesc", "频道描述");
        put("GuildListData.guildFace", "频道头像 URL");
        put("GuildListData.guildId", "频道 ID");
        put("GuildListData.guildName", "频道名称");
        put("GuildListData.isJoined", "是否已加入");
        put("GuildListData.maxMemberCount", "最大成员数");
        put("GuildListData.memberCount", "频道成员数");
        put("GuildListData.ownerId", "频道主 QQ");

        // GuildServiceProfileData
        put("GuildServiceProfileData.avatarUrl", "用户头像 URL");
        put("GuildServiceProfileData.guildId", "频道 ID");
        put("GuildServiceProfileData.joinTime", "加入时间");
        put("GuildServiceProfileData.nickName", "用户昵称");
        put("GuildServiceProfileData.role", "角色");

        // HistoryMsgData
        put("HistoryMsgData.messages", "消息列表");

        // LoginInfoData
        put("LoginInfoData.age", "年龄");
        put("LoginInfoData.birthdayDay", "出生日期");
        put("LoginInfoData.birthdayMonth", "出生月份");
        put("LoginInfoData.birthdayYear", "出生年份");
        put("LoginInfoData.categoryId", "分组 ID");
        put("LoginInfoData.categoryName", "分组名称");
        put("LoginInfoData.email", "邮箱");
        put("LoginInfoData.level", "等级");
        put("LoginInfoData.loginDays", "登录天数");
        put("LoginInfoData.nickname", "昵称");
        put("LoginInfoData.phoneNum", "手机号");
        put("LoginInfoData.qid", "QID");
        put("LoginInfoData.remark", "备注");
        put("LoginInfoData.sex", "性别");
        put("LoginInfoData.userId", "QQ 号");

        // MiniAppArkData
        put("MiniAppArkData.data", "Ark JSON 数据");

        // ModelShowData
        put("ModelShowData.model", "机型名称");
        put("ModelShowData.modelShow", "机型展示");

        // OcrResultData
        put("OcrResultData.language", "语言");
        put("OcrResultData.texts", "OCR 识别文本列表");

        // OnlineClientData
        put("OnlineClientData.appId", "客户端 ID");
        put("OnlineClientData.clientVersion", "客户端版本");
        put("OnlineClientData.deviceKind", "设备类型");
        put("OnlineClientData.deviceName", "设备名称");
        put("OnlineClientData.platform", "客户端平台");

        // OnlineFileMsgData
        put("OnlineFileMsgData.fileId", "文件 ID");
        put("OnlineFileMsgData.fileName", "文件名称");
        put("OnlineFileMsgData.fileSize", "文件大小（字节）");
        put("OnlineFileMsgData.isOnlineFile", "是否在线文件");
        put("OnlineFileMsgData.senderUid", "发送者 QQ");

        // PacketStatusData
        put("PacketStatusData.enable", "发包是否可用");
        put("PacketStatusData.interval", "发包速率限制（毫秒）");

        // ProfileLikeData
        put("ProfileLikeData.count", "赞的总数");
        put("ProfileLikeData.list", "近期的点赞列表");

        // PttTextData
        put("PttTextData.text", "语音转文字结果文本");

        // QunAlbumListData
        put("QunAlbumListData.albumList", "相册列表");
        put("QunAlbumListData.attachInfo", "翻页附加信息");
        put("QunAlbumListData.hasMore", "是否还有更多");

        // RKeyData
        put("RKeyData.rkey", "RKey 映射");

        // RKeyServerData
        put("RKeyServerData.servers", "服务器列表");

        // RecentContactData
        put("RecentContactData.chatType", "聊天类型");
        put("RecentContactData.lastestMsg", "最后一条消息");
        put("RecentContactData.msgId", "消息 ID");
        put("RecentContactData.msgTime", "消息时间");
        put("RecentContactData.peerName", "对象名称");
        put("RecentContactData.peerUin", "对象 QQ");
        put("RecentContactData.remark", "备注");
        put("RecentContactData.sendMemberName", "发送者群名片");
        put("RecentContactData.sendNickName", "发送者昵称");

        // RobotUinRangeData
        put("RobotUinRangeData.maxUin", "最大 Uin");
        put("RobotUinRangeData.minUin", "最小 Uin");

        // SendMsgData
        put("SendMsgData.forwardId", "转发消息的 forward_id（合并转发时可能有值）");
        put("SendMsgData.messageId", "消息 ID");
        put("SendMsgData.resId", "转发消息的 res_id（合并转发时可能有值）");

        // ShareLinkData
        put("ShareLinkData.content", "内容");
        put("ShareLinkData.title", "标题");
        put("ShareLinkData.url", "短链接");

        // StatusData
        put("StatusData.good", "状态是否良好");
        put("StatusData.online", "是否在线");
        put("StatusData.stat", "统计信息（具体结构不固定，用 JsonNode 承接）");

        // StrangerInfoData
        put("StrangerInfoData.age", "年龄");
        put("StrangerInfoData.isVip", "是否 VIP");
        put("StrangerInfoData.isYearsVip", "是否年费 VIP");
        put("StrangerInfoData.loginDays", "登录天数");
        put("StrangerInfoData.longNick", "个性签名");
        put("StrangerInfoData.nickname", "昵称");
        put("StrangerInfoData.qid", "QID");
        put("StrangerInfoData.qqLevel", "QQ 等级");
        put("StrangerInfoData.regTime", "注册时间");
        put("StrangerInfoData.remark", "备注");
        put("StrangerInfoData.sex", "性别");
        put("StrangerInfoData.status", "状态");
        put("StrangerInfoData.uid", "UID");
        put("StrangerInfoData.userId", "用户 QQ");
        put("StrangerInfoData.vipLevel", "VIP 等级");

        // TranslateResultData
        put("TranslateResultData.words", "翻译后的词汇列表");

        // UnidirectionalFriendData
        put("UnidirectionalFriendData.age", "年龄");
        put("UnidirectionalFriendData.nickName", "昵称");
        put("UnidirectionalFriendData.source", "来源");
        put("UnidirectionalFriendData.uid", "用户UID");
        put("UnidirectionalFriendData.uin", "QQ号");

        // UrlSafetyData
        put("UrlSafetyData.level", "安全等级（0 安全，1 警告，2 风险，3 危险）");

        // UserStatusData
        put("UserStatusData.extStatus", "扩展状态");
        put("UserStatusData.status", "在线状态");

        // VersionInfoData
        put("VersionInfoData.appName", "应用名称");
        put("VersionInfoData.appVersion", "应用版本");
        put("VersionInfoData.protocolVersion", "协议版本");


        // ================================================================
        //  其他
        // ================================================================
        // ApiResponse
        put("ApiResponse.data", "业务数据，类型由泛型决定");
        put("ApiResponse.message", "错误消息");
        put("ApiResponse.retcode", "返回码：0 表示成功，非零表示错误");
        put("ApiResponse.status", "状态：\"ok\" 或 \"failed\"");
        put("ApiResponse.stream", "流式响应类型（如 \"normal-action\"）");
        put("ApiResponse.wording", "提示");

        // FileOperationResult
        put("FileOperationResult.ok", "是否操作成功");

        // GroupPortraitResult
        put("GroupPortraitResult.errMsg", "错误信息");
        put("GroupPortraitResult.result", "结果码（0 表示成功）");

        // === GENERATED END ===
    }

    private static void put(String key, String desc) {
        DESCRIPTIONS.put(key, desc);
    }

    /**
     * 获取字段描述。先从 Map 精确查找，找不到则自动生成。
     */
    public static String resolve(String className, String fieldName) {
        if (className == null || fieldName == null) return "";

        String key = className + "." + fieldName;
        String desc = DESCRIPTIONS.get(key);
        if (desc != null) return desc;

        return autoGenerate(fieldName);
    }

    /**
     * 从 camelCase 字段名自动生成分词（兜底）。
     */
    private static String autoGenerate(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(' ');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    /**
     * 尝试直接从 Map 查询（不自动生成）。
     * 返回 null 表示未命中，调用方继续查找 @DisplayField 等。
     */
    static String tryGet(String className, String fieldName) {
        if (className == null || fieldName == null) return null;
        return DESCRIPTIONS.get(className + "." + fieldName);
    }
}
