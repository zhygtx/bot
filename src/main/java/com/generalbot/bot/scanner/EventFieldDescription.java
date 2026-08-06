package com.generalbot.bot.scanner;

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
 * 用户自定义实体类字段描述使用 {@link com.generalbot.plugin.annotation.DisplayField @DisplayField} 注解。
 */
public final class EventFieldDescription {

    private static final Map<String, String> DESCRIPTIONS = new ConcurrentHashMap<>();

    static {
        // === GENERATED START ===

        // ================================================================
        //  BaseEvent（事件基类）
        // ================================================================
        // BaseEvent
        put("BaseEvent.time", "事件发生的时间戳（秒）");
        put("BaseEvent.selfId", "收到事件的机器人 QQ 号");
        put("BaseEvent.postType", "事件类型（message / message_sent / notice / request / meta_event）");
        put("BaseEvent.triggeredTypes", "当前事件会触发的回调类型列表");


        // ================================================================
        //  消息事件（MessageEvent 系列）
        // ================================================================
        // GroupMessageEvent
        put("GroupMessageEvent.subType", "子类型（normal / anonymous / notice）");
        put("GroupMessageEvent.groupId", "群号");
        put("GroupMessageEvent.anonymous", "匿名信息（匿名消息时不为 null）");
        put("GroupMessageEvent.sender", "发送者信息");

        // MessageEvent
        put("MessageEvent.messageId", "消息 ID");
        put("MessageEvent.userId", "发送者 QQ 号");
        put("MessageEvent.message", "消息内容（消息段列表）");
        put("MessageEvent.rawMessage", "CQ 码格式的原始消息文本");
        put("MessageEvent.font", "字体 ID");

        // PrivateGroupMessageEvent
        put("PrivateGroupMessageEvent.targetId", "临时会话来源 ID（群号）");
        put("PrivateGroupMessageEvent.tempSource", "临时会话来源类型");

        // PrivateMessageEvent
        put("PrivateMessageEvent.subType", "子类型（friend / group / other）");
        put("PrivateMessageEvent.targetId", "临时会话来源 ID（临时会话时有效）");
        put("PrivateMessageEvent.tempSource", "临时会话来源类型");
        put("PrivateMessageEvent.sender", "发送者信息");


        // ================================================================
        //  消息发送事件（MessageSentEvent 系列）
        // ================================================================
        // GroupMessageSentEvent
        put("GroupMessageSentEvent.messageType", "消息类型，固定为 \"group\"");
        put("GroupMessageSentEvent.subType", "子类型（normal 等）");
        put("GroupMessageSentEvent.groupId", "群号");
        put("GroupMessageSentEvent.sender", "发送者信息");

        // MessageSentEvent
        put("MessageSentEvent.messageType", "消息类型（private / group）");
        put("MessageSentEvent.targetId", "消息接收者 ID（群号或用户 QQ）");
        put("MessageSentEvent.sender", "发送者信息");

        // PrivateMessageSentEvent
        put("PrivateMessageSentEvent.privateMessageType", "消息类型，固定为 \"private\"");
        put("PrivateMessageSentEvent.subType", "子类型（friend / group 等）");
        put("PrivateMessageSentEvent.sender", "发送者信息");


        // ================================================================
        //  通知事件（NoticeEvent 系列）
        // ================================================================
        // BotOfflineNoticeEvent
        put("BotOfflineNoticeEvent.userId", "Bot QQ 号");
        put("BotOfflineNoticeEvent.tag", "离线原因标签");
        put("BotOfflineNoticeEvent.message", "离线原因详情");

        // FriendAddNoticeEvent
        put("FriendAddNoticeEvent.userId", "新添加的好友 QQ 号");

        // FriendRecallNoticeEvent
        put("FriendRecallNoticeEvent.userId", "好友 QQ 号");
        put("FriendRecallNoticeEvent.messageId", "被撤回的消息 ID");

        // GroupAdminNoticeEvent
        put("GroupAdminNoticeEvent.subType", "子类型（set / unset）");

        // GroupBanBanNoticeEvent
        put("GroupBanBanNoticeEvent.duration", "禁言时长（秒）");

        // GroupBanLiftBanNoticeEvent
        put("GroupBanLiftBanNoticeEvent.duration", "解除前已禁言的时长（秒）");

        // GroupBanNoticeEvent
        put("GroupBanNoticeEvent.subType", "子类型（ban / lift_ban）");
        put("GroupBanNoticeEvent.operatorId", "操作者 QQ 号（管理员或群主）");
        put("GroupBanNoticeEvent.duration", "禁言时长（秒），解除禁言时为 0");

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
        put("GroupDecreaseNoticeEvent.subType", "子类型（leave / kick / kick_me / disband）");
        put("GroupDecreaseNoticeEvent.operatorId", "操作者 QQ 号（管理员或退群者本人）");

        // GroupEssenceNoticeEvent
        put("GroupEssenceNoticeEvent.subType", "子类型（add / delete）");
        put("GroupEssenceNoticeEvent.messageId", "消息 ID");
        put("GroupEssenceNoticeEvent.senderId", "消息发送者 QQ 号");
        put("GroupEssenceNoticeEvent.operatorId", "操作者 QQ 号（设置精华的管理员）");

        // GroupIncreaseApproveNoticeEvent
        put("GroupIncreaseApproveNoticeEvent.operatorId", "同意入群的管理员 QQ 号");

        // GroupIncreaseInviteNoticeEvent
        put("GroupIncreaseInviteNoticeEvent.operatorId", "邀请者 QQ 号");

        // GroupIncreaseNoticeEvent
        put("GroupIncreaseNoticeEvent.subType", "子类型（approve / invite）");
        put("GroupIncreaseNoticeEvent.operatorId", "操作者 QQ 号（同意入群的管理员或邀请者）");

        // GroupMsgEmojiLikeNoticeEvent
        put("GroupMsgEmojiLikeNoticeEvent.messageId", "被点赞的消息 ID");
        put("GroupMsgEmojiLikeNoticeEvent.likes", "表情回应列表");

        // GroupNoticeEvent
        put("GroupNoticeEvent.groupId", "群号");
        put("GroupNoticeEvent.userId", "相关用户 QQ 号");

        // GroupRecallNoticeEvent
        put("GroupRecallNoticeEvent.operatorId", "操作者 QQ 号（撤回消息的人，可能是发送者本人或管理员）");
        put("GroupRecallNoticeEvent.messageId", "被撤回的消息 ID");

        // GroupTitleNoticeEvent
        put("GroupTitleNoticeEvent.title", "新的专属头衔");

        // GroupUploadNoticeEvent
        put("GroupUploadNoticeEvent.file", "上传的文件信息");

        // InputStatusNoticeEvent
        put("InputStatusNoticeEvent.userId", "正在输入的用户 QQ 号");
        put("InputStatusNoticeEvent.groupId", "群号（群聊输入状态时有效）");
        put("InputStatusNoticeEvent.statusText", "输入状态文本");
        put("InputStatusNoticeEvent.eventType", "输入事件类型");

        // NoticeEvent
        put("NoticeEvent.noticeType", "通知类型（group_upload / group_admin / group_decrease 等）");

        // PokeNoticeEvent
        put("PokeNoticeEvent.groupId", "群号（好友戳一戳时为 null）");
        put("PokeNoticeEvent.userId", "发送戳一戳的用户 QQ 号");
        put("PokeNoticeEvent.targetId", "被戳的用户 QQ 号");

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
        put("GroupRequestEvent.subType", "子类型（add / invite）");
        put("GroupRequestEvent.groupId", "群号");

        // RequestEvent
        put("RequestEvent.requestType", "请求类型（friend / group）");
        put("RequestEvent.userId", "请求发送者 QQ 号");
        put("RequestEvent.comment", "验证信息（加好友/加群的附言）");
        put("RequestEvent.flag", "请求标识（用于处理请求的 flag）");


        // ================================================================
        //  元事件（MetaEvent 系列）
        // ================================================================
        // HeartbeatMetaEvent
        put("HeartbeatMetaEvent.status", "状态信息（包含 online、good 等字段）");
        put("HeartbeatMetaEvent.interval", "心跳间隔（毫秒）");

        // LifecycleMetaEvent
        put("LifecycleMetaEvent.subType", "子类型（enable / disable / connect）");

        // MetaEvent
        put("MetaEvent.metaEventType", "元事件类型（heartbeat / lifecycle）");


        // ================================================================
        //  Sender 辅助类
        // ================================================================
        // FriendSender
        put("FriendSender.userId", "发送者 QQ 号");
        put("FriendSender.nickname", "昵称");
        put("FriendSender.sex", "性别（male / female / unknown）");
        put("FriendSender.age", "年龄");

        // GroupSender
        put("GroupSender.userId", "发送者 QQ 号");
        put("GroupSender.nickname", "昵称");
        put("GroupSender.sex", "性别（male / female / unknown）");
        put("GroupSender.age", "年龄");
        put("GroupSender.card", "群名片/群昵称");
        put("GroupSender.role", "角色（owner / admin / member）");
        put("GroupSender.title", "专属头衔");
        put("GroupSender.level", "成员等级");


        // ================================================================
        //  文件信息类
        // ================================================================
        // GroupUploadFileInfo
        put("GroupUploadFileInfo.id", "文件 ID");
        put("GroupUploadFileInfo.name", "文件名");
        put("GroupUploadFileInfo.size", "文件大小（字节）");
        put("GroupUploadFileInfo.busid", "文件总线 ID");


        // ================================================================
        //  API 响应数据类
        // ================================================================
        // AiCharactersData
        put("AiCharactersData.type", "角色类型");
        put("AiCharactersData.characters", "角色列表");

        // AiCharactersItemData
        put("AiCharactersItemData.characterId", "角色ID");
        put("AiCharactersItemData.characterName", "角色名称");
        put("AiCharactersItemData.previewUrl", "预览URL");

        // ClientkeyData
        put("ClientkeyData.clientkey", "客户端Key");

        // CookiesData
        put("CookiesData.cookies", "Cookies");
        put("CookiesData.bkn", "CSRF Token");

        // CredentialsData
        put("CredentialsData.cookies", "Cookies");
        put("CredentialsData.token", "CSRF Token");

        // CsrfTokenData
        put("CsrfTokenData.token", "CSRF Token");

        // EmojiLikeData
        put("EmojiLikeData.emojiLikesList", "表情回应列表");
        put("EmojiLikeData.cookie", "分页Cookie");
        put("EmojiLikeData.isLastPage", "是否最后一页");
        put("EmojiLikeData.isFirstPage", "是否第一页");
        put("EmojiLikeData.result", "结果状态码");
        put("EmojiLikeData.errMsg", "错 误信息");

        // EmojiLikeItemData
        put("EmojiLikeItemData.tinyId", "TinyID");
        put("EmojiLikeItemData.nickName", "昵称");
        put("EmojiLikeItemData.headUrl", "头像URL");

        // EmojiLikesData
        put("EmojiLikesData.emojiLikeList", "表情回应列表");

        // EmojiLikesItemData
        put("EmojiLikesItemData.userId", "点击者QQ号");
        put("EmojiLikesItemData.nickName", "昵称?");

        // En2zhData
        put("En2zhData.words", "翻译结果列表");

        // EssenceMsgData
        put("EssenceMsgData.msgSeq", "消息序号");
        put("EssenceMsgData.msgRandom", "消息随机数");
        put("EssenceMsgData.senderId", "发送者QQ");
        put("EssenceMsgData.senderNick", "发送者昵称");
        put("EssenceMsgData.operatorId", "操作者QQ");
        put("EssenceMsgData.operatorNick", "操作者昵称");
        put("EssenceMsgData.messageId", "消息ID");
        put("EssenceMsgData.operatorTime", "操作时间");
        put("EssenceMsgData.content", "消息内容");

        // FileData
        put("FileData.file", "本地路径");
        put("FileData.url", "下载URL");
        put("FileData.fileSize", "文件大小");
        put("FileData.fileName", "文件名");
        put("FileData.base64", "Base64编码");

        // FilesetIdData
        put("FilesetIdData.filesetId", "文件集 ID");

        // FriendsWithCategoryData
        put("FriendsWithCategoryData.categoryId", "分组ID");
        put("FriendsWithCategoryData.categoryName", "分组名称");
        put("FriendsWithCategoryData.categoryMbCount", "分组内好友数量");
        put("FriendsWithCategoryData.buddyList", "好友列表");

        // GroupAiRecordData
        put("GroupAiRecordData.messageId", "消息ID");

        // GroupAtAllRemainData
        put("GroupAtAllRemainData.canAtAll", "是否可以艾特全体");
        put("GroupAtAllRemainData.remainAtAllCountForGroup", "群艾特全体剩余次数");
        put("GroupAtAllRemainData.remainAtAllCountForUin", "个人艾特全体剩余次数");

        // GroupDetailInfoData
        put("GroupDetailInfoData.groupId", "群号");
        put("GroupDetailInfoData.groupName", "群名称");
        put("GroupDetailInfoData.memberCount", "成员数量");
        put("GroupDetailInfoData.maxMemberCount", "最大成员数量");
        put("GroupDetailInfoData.groupAllShut", "全员禁言状态");
        put("GroupDetailInfoData.groupRemark", "群备注");

        // GroupFileData
        put("GroupFileData.fileId", "文件 ID");

        // GroupFileFolderData
        put("GroupFileFolderData.result", "操作结果");
        put("GroupFileFolderData.groupItem", "群项信息");

        // GroupFileSystemInfoData
        put("GroupFileSystemInfoData.fileCount", "文件总数");
        put("GroupFileSystemInfoData.limitCount", "文件上限");
        put("GroupFileSystemInfoData.usedSpace", "已使用空间");
        put("GroupFileSystemInfoData.totalSpace", "总空间");

        // GroupFileUrlData
        put("GroupFileUrlData.url", "文件下载链接");

        // GroupHonorInfoData
        put("GroupHonorInfoData.groupId", "群号");
        put("GroupHonorInfoData.currentTalkative", "当前龙王");
        put("GroupHonorInfoData.talkativeList", "龙王列表");
        put("GroupHonorInfoData.performerList", "群聊之火列表");
        put("GroupHonorInfoData.legendList", "群聊炽热列表");
        put("GroupHonorInfoData.emotionList", "快乐源泉列表");
        put("GroupHonorInfoData.strongNewbieList", "冒尖小春笋列表");

        // GroupIgnoreAddRequestData
        put("GroupIgnoreAddRequestData.requestId", "请求ID");
        put("GroupIgnoreAddRequestData.invitorUin", "邀请者QQ");
        put("GroupIgnoreAddRequestData.invitorNick", "邀请者昵称");
        put("GroupIgnoreAddRequestData.groupId", "群号");
        put("GroupIgnoreAddRequestData.message", "验证信息");
        put("GroupIgnoreAddRequestData.groupName", "群名称");
        put("GroupIgnoreAddRequestData.checked", "是否已处理");
        put("GroupIgnoreAddRequestData.actor", "处理者QQ");
        put("GroupIgnoreAddRequestData.requesterNick", "请求者昵称");

        // GroupIgnoredNotifiesData
        put("GroupIgnoredNotifiesData.invitedRequests", "邀请请求列表");
        put("GroupIgnoredNotifiesData.InvitedRequest", "邀请请求列表");
        put("GroupIgnoredNotifiesData.joinRequests", "加入请求列表");

        // GroupMsgData
        put("GroupMsgData.messageId", "消息ID");
        put("GroupMsgData.resId", "转发消息的 res_id");
        put("GroupMsgData.forwardId", "转发消息的 forward_id");

        // GroupMsgHistoryData
        put("GroupMsgHistoryData.messages", "消息列表");

        // GroupNoticeData
        put("GroupNoticeData.senderId", "发送者QQ");
        put("GroupNoticeData.publishTime", "发布时间");
        put("GroupNoticeData.noticeId", "公告ID");
        put("GroupNoticeData.message", "公告内容");
        put("GroupNoticeData.settings", "设置项");
        put("GroupNoticeData.readNum", "阅读数");

        // GroupNoticeMessageData
        put("GroupNoticeMessageData.text", "文本内容");
        put("GroupNoticeMessageData.image", "图片列表");
        put("GroupNoticeMessageData.images", "图片列表");

        // GroupRootFilesData
        put("GroupRootFilesData.files", "文件列表");
        put("GroupRootFilesData.folders", "文件夹列表");

        // GroupSignedData
        put("GroupSignedData.userId", "打卡者QQ");
        put("GroupSignedData.nick", "打卡者昵称");
        put("GroupSignedData.time", "打卡时间");
        put("GroupSignedData.rank", "打卡排名");

        // MiniAppArkData
        put("MiniAppArkData.data", "Ark数据");

        // ModelShowVariantsData
        put("ModelShowVariantsData.modelShow", "显示名称");
        put("ModelShowVariantsData.needPay", "是否需要付费");

        // MsgData
        put("MsgData.time", "发送时间");
        put("MsgData.messageType", "消息类型");
        put("MsgData.messageId", "消息ID");
        put("MsgData.realId", "真实ID");
        put("MsgData.messageSeq", "消息序号");
        put("MsgData.sender", "发送者");
        put("MsgData.message", "消息内容");
        put("MsgData.rawMessage", "原始消息内容");
        put("MsgData.font", "字体");
        put("MsgData.groupId", "群号");
        put("MsgData.userId", "发送者QQ号");
        put("MsgData.emojiLikesList", "表情回应列表");

        // ProfileLikeData
        put("ProfileLikeData.uid", "用户UID");
        put("ProfileLikeData.time", "时间");

        // ProfileLikeFavoriteInfoData
        put("ProfileLikeFavoriteInfoData.userInfos", "点赞用户信息");
        put("ProfileLikeFavoriteInfoData.totalCount", "总点赞数");
        put("ProfileLikeFavoriteInfoData.lastTime", "最后点赞时间");
        put("ProfileLikeFavoriteInfoData.todayCount", "今日点赞数");

        // ProfileLikeVoteInfoData
        put("ProfileLikeVoteInfoData.totalCount", "总点赞数");
        put("ProfileLikeVoteInfoData.newCount", "新增点赞数");
        put("ProfileLikeVoteInfoData.newNearbyCount", "新增附近点赞数");
        put("ProfileLikeVoteInfoData.lastVisitTime", "最后访问时间");
        put("ProfileLikeVoteInfoData.userInfos", "点赞用户信息");

        // PttTextData
        put("PttTextData.text", "得到的文本");

        // QunAlbumData
        put("QunAlbumData.albumList", "群相册列表");
        put("QunAlbumData.attachInfo", "分页附加信息，传入下一次请求以获取更多数据");
        put("QunAlbumData.hasMore", "是否有更多数据");

        // RecentContactData
        put("RecentContactData.lastestMsg", "最后一条消息");
        put("RecentContactData.peerUin", "对象QQ");
        put("RecentContactData.remark", "备注");
        put("RecentContactData.msgTime", "消息时间");
        put("RecentContactData.chatType", "聊天类型");
        put("RecentContactData.msgId", "消息ID");
        put("RecentContactData.sendNickName", "发送者昵称");
        put("RecentContactData.sendMemberName", "发送者群名片");
        put("RecentContactData.peerName", "对象名称");

        // RecordData
        put("RecordData.yes", "是否可以发送");

        // RkeyData
        put("RkeyData.type", "类型 (private/group)");
        put("RkeyData.rkey", "RKey");
        put("RkeyData.createdAt", "创建时间");
        put("RkeyData.ttl", "有效期");

        // RkeyServerData
        put("RkeyServerData.privateRkey", "私聊 RKey");
        put("RkeyServerData.groupRkey", "群聊 RKey");
        put("RkeyServerData.expiredTime", "过期时间");
        put("RkeyServerData.name", "名称");

        // StatusData
        put("StatusData.online", "是否在线");
        put("StatusData.good", "状态是否良好");
        put("StatusData.stat", "统计信息");

        // StrangerInfoData
        put("StrangerInfoData.userId", "用户QQ");
        put("StrangerInfoData.uid", "UID");
        put("StrangerInfoData.nickname", "昵称");
        put("StrangerInfoData.age", "年龄");
        put("StrangerInfoData.qid", "QID");
        put("StrangerInfoData.qqLevel", "QQ等级");
        put("StrangerInfoData.sex", "性别");
        put("StrangerInfoData.longNick", "个性签名");
        put("StrangerInfoData.regTime", "注册时间");
        put("StrangerInfoData.isVip", "是否VIP");
        put("StrangerInfoData.isYearsVip", "是否年费VIP");
        put("StrangerInfoData.vipLevel", "VIP等级");
        put("StrangerInfoData.remark", "备注");
        put("StrangerInfoData.status", "状态");
        put("StrangerInfoData.loginDays", "登录天数");

        // UnidirectionalFriendData
        put("UnidirectionalFriendData.uin", "QQ号");
        put("UnidirectionalFriendData.uid", "用户UID");
        put("UnidirectionalFriendData.nickName", "昵称");
        put("UnidirectionalFriendData.age", "年龄");
        put("UnidirectionalFriendData.source", "来源");

        // UrlSafelyData
        put("UrlSafelyData.level", "安全等级 (1: 安全, 2: 未知, 3: 危险)");

        // UserStatusData
        put("UserStatusData.status", "在线状态");
        put("UserStatusData.extStatus", "扩展状态");

        // VersionInfoData
        put("VersionInfoData.appName", "应用名称");
        put("VersionInfoData.protocolVersion", "协议版本");
        put("VersionInfoData.appVersion", "应用版本");


        // ================================================================
        //  其他
        // ================================================================
        // ApiResponse
        put("ApiResponse.status", "状态：\"ok\" 或 \"failed\"");
        put("ApiResponse.retcode", "返回码：0 表示成功，非零表示错误");
        put("ApiResponse.data", "业务数据，类型由泛型决定");
        put("ApiResponse.message", "错误消息");
        put("ApiResponse.wording", "提示");
        put("ApiResponse.stream", "流式响应类型（如 \"normal-action\"）");

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
