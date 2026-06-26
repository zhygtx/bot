package com.example.demo.handler;


import com.example.demo.annotation.BotEvent;
import com.github.zhygtx.napcat.event.EventFilter;
import com.github.zhygtx.napcat.event.OneBotEventListener;
import com.github.zhygtx.napcat.event.message.*;
import com.github.zhygtx.napcat.event.notice.*;
import com.github.zhygtx.napcat.event.request.FriendRequestEvent;
import com.github.zhygtx.napcat.event.request.GroupAddRequestEvent;
import com.github.zhygtx.napcat.event.request.GroupInviteRequestEvent;
import com.github.zhygtx.napcat.event.request.GroupRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Slf4j
@Component
public class EventHandler implements OneBotEventListener {

    private final BotWorkflowHandler botWorkflowHandler;

    public EventHandler(BotWorkflowHandler botWorkflowHandler) {
        this.botWorkflowHandler = botWorkflowHandler;
    }

    @Bean
    public EventFilter botFilter(){
        return ((botQQ, event) -> !(event instanceof MessageEvent && botQQ == ((MessageEvent) event).getUserId()) || event instanceof MessageSentEvent);
    }


    /**
     * 好友添加时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友添加通知",
        categories = {"好友相关事件", "通知事件"},
        description = "好友添加时调用"
    )
    @Override
    public void onFriendAdd(Long botQQ, FriendAddNoticeEvent event) {
        OneBotEventListener.super.onFriendAdd(botQQ, event);
    }

    /**
     * 好友消息撤回时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友消息撤回",
        categories = {"好友相关事件", "通知事件", "好友通知"},
        description = "好友消息撤回时调用"
    )
    @Override
    public void onFriendRecall(Long botQQ, FriendRecallNoticeEvent event) {
        OneBotEventListener.super.onFriendRecall(botQQ, event);
    }

    /**
     * 收到好友请求时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友请求",
        categories = {"好友相关事件", "请求事件", "好友请求"},
        description = "收到好友请求时调用"
    )
    @Override
    public void onFriendRequest(Long botQQ, FriendRequestEvent event) {
        OneBotEventListener.super.onFriendRequest(botQQ, event);
    }

    /**
     * 收到加群申请时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "加群申请",
        categories = {"群相关事件", "请求事件"},
        description = "收到加群申请时调用"
    )
    @Override
    public void onGroupAddRequest(Long botQQ, GroupAddRequestEvent event) {
        OneBotEventListener.super.onGroupAddRequest(botQQ, event);
    }

    /**
     * 群管理员变动时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群管理员变动",
        categories = {"群相关事件", "通知事件"},
        description = "群管理员变动时调用"
    )
    @Override
    public void onGroupAdmin(Long botQQ, GroupAdminNoticeEvent event) {
        OneBotEventListener.super.onGroupAdmin(botQQ, event);
    }

    /**
     * 群管理员被设置时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群管理员被设置",
        categories = {"群相关事件", "通知事件"},
        description = "群管理员被设置时调用"
    )
    @Override
    public void onGroupAdminSet(Long botQQ, GroupAdminSetNoticeEvent event) {
        OneBotEventListener.super.onGroupAdminSet(botQQ, event);
    }

    /**
     * 群管理员被取消时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群管理员被取消",
        categories = {"群相关事件", "通知事件"},
        description = "群管理员被取消时调用"
    )
    @Override
    public void onGroupAdminUnset(Long botQQ, GroupAdminUnsetNoticeEvent event) {
        OneBotEventListener.super.onGroupAdminUnset(botQQ, event);
    }

    /**
     * 群禁言/解除禁言时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群禁言/解除禁言",
        categories = {"群相关事件", "通知事件", "群管理通知"},
        description = "群禁言/解除禁言时调用"
    )
    @Override
    public void onGroupBan(Long botQQ, GroupBanNoticeEvent event) {
        OneBotEventListener.super.onGroupBan(botQQ, event);
    }

    /**
     * 群成员被禁言时调用。
     *
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被禁言",
        categories = {"群相关事件", "通知事件"},
        description = "群成员被禁言时调用"
    )
    @Override
    public void onGroupBanBan(Long botQQ, GroupBanBanNoticeEvent event) {
        OneBotEventListener.super.onGroupBanBan(botQQ, event);
    }

    /**
     * 群成员被解除禁言时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被解除禁言",
        categories = {"群相关事件", "通知事件"},
        description = "群成员被解除禁言时调用"
    )
    @Override
    public void onGroupBanLiftBan(Long botQQ, GroupBanLiftBanNoticeEvent event) {
        OneBotEventListener.super.onGroupBanLiftBan(botQQ, event);
    }

    /**
     * 群名片变更时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群名片变更",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群名片变更时调用"
    )
    @Override
    public void onGroupCard(Long botQQ, GroupCardNoticeEvent event) {
        OneBotEventListener.super.onGroupCard(botQQ, event);
    }

    /**
     * 群成员减少时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员减少",
        categories = {"群相关事件", "通知事件"},
        description = "群成员减少时调用"
    )
    @Override
    public void onGroupDecrease(Long botQQ, GroupDecreaseNoticeEvent event) {
        OneBotEventListener.super.onGroupDecrease(botQQ, event);
    }

    /**
     * 群成员被踢出时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被踢出",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员被踢出时调用"
    )
    @Override
    public void onGroupDecreaseKick(Long botQQ, GroupDecreaseKickNoticeEvent event) {
        OneBotEventListener.super.onGroupDecreaseKick(botQQ, event);
    }

    /**
     * 机器人自己被踢出群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人自己被踢出群",
        categories = {"群相关事件", "BOT事件", "通知事件"},
        description = "机器人自己被踢出群时调用"
    )
    @Override
    public void onGroupDecreaseKickMe(Long botQQ, GroupDecreaseKickMeNoticeEvent event) {
        OneBotEventListener.super.onGroupDecreaseKickMe(botQQ, event);
    }

    /**
     * 群成员主动退群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员主动退群",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员主动退群时调用"
    )
    @Override
    public void onGroupDecreaseLeave(Long botQQ, GroupDecreaseLeaveNoticeEvent event) {
        OneBotEventListener.super.onGroupDecreaseLeave(botQQ, event);
    }

    /**
     * 群精华消息变更时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群精华消息变更",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群精华消息变更时调用"
    )
    @Override
    public void onGroupEssence(Long botQQ, GroupEssenceNoticeEvent event) {
        OneBotEventListener.super.onGroupEssence(botQQ, event);
    }

    /**
     * 群精华消息被添加时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群精华消息被添加",
        categories = {"群相关事件", "通知事件"},
        description = "群精华消息被添加时调用"
    )
    @Override
    public void onGroupEssenceAdd(Long botQQ, GroupEssenceAddNoticeEvent event) {
        OneBotEventListener.super.onGroupEssenceAdd(botQQ, event);
    }

    /**
     * 群成员增加时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员增加",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员增加时调用"
    )
    @Override
    public void onGroupIncrease(Long botQQ, GroupIncreaseNoticeEvent event) {
        OneBotEventListener.super.onGroupIncrease(botQQ, event);
    }

    /**
     * 群成员被管理员同意入群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被管理员同意入群",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员被管理员同意入群时调用"
    )
    @Override
    public void onGroupIncreaseApprove(Long botQQ, GroupIncreaseApproveNoticeEvent event) {
        OneBotEventListener.super.onGroupIncreaseApprove(botQQ, event);
    }

    /**
     * 群成员被管理员邀请入群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被管理员邀请入群",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员被管理员邀请入群时调用"
    )
    @Override
    public void onGroupIncreaseInvite(Long botQQ, GroupIncreaseInviteNoticeEvent event) {
        OneBotEventListener.super.onGroupIncreaseInvite(botQQ, event);
    }

    /**
     * 机器人被邀请入群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人被邀请入群",
        categories = {"群相关事件", "BOT事件", "请求事件"},
        description = "机器人被邀请入群时调用"
    )
    @Override
    public void onGroupInviteRequest(Long botQQ, GroupInviteRequestEvent event) {
        OneBotEventListener.super.onGroupInviteRequest(botQQ, event);
    }

    /**
     * Bot 收到群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到群聊消息",
        categories = {"群相关事件", "消息事件"},
        description = "Bot 收到群聊消息时调用"
    )
    @Override
    public void onGroupMessage(Long botQQ, GroupMessageEvent event) {
        OneBotEventListener.super.onGroupMessage(botQQ, event);
    }

    /**
     * 机器人发送群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送群聊消息",
        categories = {"群相关事件", "消息发送事件", "群聊消息发送"},
        description = "机器人发送群聊消息时调用"
    )
    @Override
    public void onGroupMessageSent(Long botQQ, GroupMessageSentEvent event) {
        OneBotEventListener.super.onGroupMessageSent(botQQ, event);
    }

    /**
     * 群表情回应时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群表情回应",
        categories = {"群相关事件", "通知事件", "群互动通知"},
        description = "群表情回应时调用"
    )
    @Override
    public void onGroupMsgEmojiLike(Long botQQ, GroupMsgEmojiLikeNoticeEvent event) {
        OneBotEventListener.super.onGroupMsgEmojiLike(botQQ, event);
    }

    /**
     * Bot 收到普通群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到普通群聊消息",
        categories = {"群相关事件", "消息事件", "群聊消息"},
        description = "Bot 收到普通群聊消息时调用"
    )
    @Override
    public void onGroupNormalMessage(Long botQQ, GroupNormalMessageEvent event) {
        OneBotEventListener.super.onGroupNormalMessage(botQQ, event);
    }

    /**
     * 机器人发送普通群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送普通群聊消息",
        categories = {"群相关事件", "消息发送事件", "群聊消息发送"},
        description = "机器人发送普通群聊消息时调用"
    )
    @Override
    public void onGroupNormalMessageSent(Long botQQ, GroupNormalMessageSentEvent event) {
        OneBotEventListener.super.onGroupNormalMessageSent(botQQ, event);
    }

    /**
     * 群消息被撤回时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群消息被撤回",
        categories = {"群相关事件", "通知事件", "群互动通知"},
        description = "群消息被撤回时调用"
    )
    @Override
    public void onGroupRecall(Long botQQ, GroupRecallNoticeEvent event) {
        OneBotEventListener.super.onGroupRecall(botQQ, event);
    }

    /**
     * 收到群请求时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "收到群请求",
        categories = {"群相关事件", "请求事件", "群请求"},
        description = "收到群请求时调用"
    )
    @Override
    public void onGroupRequest(Long botQQ, GroupRequestEvent event) {
        OneBotEventListener.super.onGroupRequest(botQQ, event);
    }

    /**
     * 群成员头衔变更时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象            
     */
    @BotEvent(
        name = "群成员头衔变更",
        categories = {"群相关事件", "通知事件", "群互动通知"},
        description = "群成员头衔变更时调用"
    )
    @Override
    public void onGroupTitle(Long botQQ, TitleNoticeEvent event) {
        OneBotEventListener.super.onGroupTitle(botQQ, event);
    }

    /**
     * 群文件上传时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象            
     */
    @BotEvent(
        name = "群文件上传",
        categories = {"群相关事件", "通知事件", "群互动通知", "文件相关事件"},
        description = "群文件上传时调用"
    )
    @Override
    public void onGroupUpload(Long botQQ, GroupUploadNoticeEvent event) {
        OneBotEventListener.super.onGroupUpload(botQQ, event);
    }

    /**
     * 好友输入状态变化时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友输入状态变化",
        categories = {"好友相关事件", "通知事件", "私聊通知"},
        description = "好友输入状态变化时调用"
    )
    @Override
    public void onInputStatus(Long botQQ, InputStatusNoticeEvent event) {
        OneBotEventListener.super.onInputStatus(botQQ, event);
    }

    /**
     * 机器人自身发送消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人自身发送消息",
        categories = {"BOT事件", "消息发送事件"},
        description = "机器人自身发送消息时调用"
    )
    @Override
    public void onMessageSent(Long botQQ, MessageSentEvent event) {
        OneBotEventListener.super.onMessageSent(botQQ, event);
    }

    /**
     * 戳一戳时调用（好友或群聊场景均可）。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "戳一戳",
        categories = {"通知事件", "互动通知"},
        description = "戳一戳时调用"
    )
    @Override
    public void onPoke(Long botQQ, PokeNoticeEvent event) {
        OneBotEventListener.super.onPoke(botQQ, event);
    }

    /**
     * Bot 收到好友私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象    
     */
    @BotEvent(
        name = "Bot 收到好友私聊消息",
        categories = {"好友相关事件", "消息事件", "私聊消息"},
        description = "Bot 收到好友私聊消息时调用"
    )
    @Override
    public void onPrivateFriendMessage(Long botQQ, PrivateFriendMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, "privateFriendMessage", event);
    }

    /**
     * 机器人发送好友私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送好友私聊消息",
        categories = {"好友相关事件", "消息发送事件", "私聊消息发送"},
        description = "机器人发送好友私聊消息时调用"
    )
    @Override
    public void onPrivateFriendMessageSent(Long botQQ, PrivateFriendMessageSentEvent event) {
        OneBotEventListener.super.onPrivateFriendMessageSent(botQQ, event);
    }

    /**
     * Bot 收到群临时会话消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到群临时会话消息",
        categories = {"群相关事件", "消息事件", "私聊消息"},
        description = "Bot 收到群临时会话消息时调用"
    )
    @Override
    public void onPrivateGroupMessage(Long botQQ, PrivateGroupMessageEvent event) {
        OneBotEventListener.super.onPrivateGroupMessage(botQQ, event);
    }

    /**
     * 机器人发送群临时会话消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送群临时会话消息",
        categories = {"群相关事件", "消息发送事件", "私聊消息发送"},
        description = "机器人发送群临时会话消息时调用"
    )
    @Override
    public void onPrivateGroupMessageSent(Long botQQ, PrivateGroupMessageSentEvent event) {
        OneBotEventListener.super.onPrivateGroupMessageSent(botQQ, event);
    }

    /**
     * Bot 收到私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到私聊消息",
        categories = {"消息事件", "私聊消息"},
        description = "Bot 收到私聊消息时调用"
    )
    @Override
    public void onPrivateMessage(Long botQQ, PrivateMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, "privateMessage", event);
    }

    /**
     * 机器人发送私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送私聊消息",
        categories = {"消息发送事件", "Bot通知", "私聊消息发送"},
        description = "机器人发送私聊消息时调用"
    )
    @Override
    public void onPrivateMessageSent(Long botQQ, PrivateMessageSentEvent event) {
        OneBotEventListener.super.onPrivateMessageSent(botQQ, event);
    }

    /**
     * 个人资料被点赞时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "个人资料被点赞时调用" ,
        categories = {"通知事件", "Bot通知"},
        description = "个人资料被点赞时调用"
    )
    @Override
    public void onProfileLike(Long botQQ, ProfileLikeNoticeEvent event) {
        OneBotEventListener.super.onProfileLike(botQQ, event);
    }
}
