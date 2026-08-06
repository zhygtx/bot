package com.generalbot.bot.handler;


import com.generalbot.bot.annotation.BotEvent;
import com.github.zhygtx.napcat.event.BaseEvent;
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

    @BotEvent(
            name = "任意事件(BaseEvent)",
            categories = {"任意事件"},
            description = "任意事件时调用",
            order = -1
    )
    @Override
    public void onAnyEvent(Long botQQ, BaseEvent event){
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 好友添加时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友添加通知(FriendAddNoticeEvent)",
        categories = {"好友相关事件", "通知事件"},
        description = "好友添加时调用"
    )
    @Override
    public void onFriendAdd(Long botQQ, FriendAddNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 好友消息撤回时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友消息撤回(FriendRecallNoticeEvent)",
        categories = {"好友相关事件", "通知事件", "好友通知"},
        description = "好友消息撤回时调用"
    )
    @Override
    public void onFriendRecall(Long botQQ, FriendRecallNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 收到好友请求时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友请求(FriendRequestEvent)",
        categories = {"好友相关事件", "请求事件", "好友请求"},
        description = "收到好友请求时调用"
    )
    @Override
    public void onFriendRequest(Long botQQ, FriendRequestEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 收到加群申请时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "加群申请(GroupAddRequestEvent)",
        categories = {"群相关事件", "请求事件"},
        description = "收到加群申请时调用"
    )
    @Override
    public void onGroupAddRequest(Long botQQ, GroupAddRequestEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群管理员变动时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群管理员变动(GroupAdminNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群管理员变动时调用"
    )
    @Override
    public void onGroupAdmin(Long botQQ, GroupAdminNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群管理员被设置时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群管理员被设置(GroupAdminSetNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群管理员被设置时调用"
    )
    @Override
    public void onGroupAdminSet(Long botQQ, GroupAdminSetNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群管理员被取消时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群管理员被取消(GroupAdminUnsetNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群管理员被取消时调用"
    )
    @Override
    public void onGroupAdminUnset(Long botQQ, GroupAdminUnsetNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群禁言/解除禁言时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群禁言/解除禁言(GroupBanNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群管理通知"},
        description = "群禁言/解除禁言时调用"
    )
    @Override
    public void onGroupBan(Long botQQ, GroupBanNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员被禁言时调用。
     *
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被禁言(GroupBanBanNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群成员被禁言时调用"
    )
    @Override
    public void onGroupBanBan(Long botQQ, GroupBanBanNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员被解除禁言时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被解除禁言(GroupBanLiftBanNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群成员被解除禁言时调用"
    )
    @Override
    public void onGroupBanLiftBan(Long botQQ, GroupBanLiftBanNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群名片变更时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群名片变更(GroupCardNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群名片变更时调用"
    )
    @Override
    public void onGroupCard(Long botQQ, GroupCardNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员减少时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员减少(GroupDecreaseNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群成员减少时调用"
    )
    @Override
    public void onGroupDecrease(Long botQQ, GroupDecreaseNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员被踢出时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被踢出(GroupDecreaseKickNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员被踢出时调用"
    )
    @Override
    public void onGroupDecreaseKick(Long botQQ, GroupDecreaseKickNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人自己被踢出群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人自己被踢出群(GroupDecreaseKickMeNoticeEvent)",
        categories = {"群相关事件", "BOT事件", "通知事件"},
        description = "机器人自己被踢出群时调用"
    )
    @Override
    public void onGroupDecreaseKickMe(Long botQQ, GroupDecreaseKickMeNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员主动退群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员主动退群(GroupDecreaseLeaveNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员主动退群时调用"
    )
    @Override
    public void onGroupDecreaseLeave(Long botQQ, GroupDecreaseLeaveNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群精华消息变更时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群精华消息变更(GroupEssenceNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群精华消息变更时调用"
    )
    @Override
    public void onGroupEssence(Long botQQ, GroupEssenceNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群精华消息被添加时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群精华消息被添加(GroupEssenceAddNoticeEvent)",
        categories = {"群相关事件", "通知事件"},
        description = "群精华消息被添加时调用"
    )
    @Override
    public void onGroupEssenceAdd(Long botQQ, GroupEssenceAddNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员增加时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员增加(GroupIncreaseNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员增加时调用"
    )
    @Override
    public void onGroupIncrease(Long botQQ, GroupIncreaseNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员被管理员同意入群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被管理员同意入群(GroupIncreaseApproveNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员被管理员同意入群时调用"
    )
    @Override
    public void onGroupIncreaseApprove(Long botQQ, GroupIncreaseApproveNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员被管理员邀请入群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群成员被管理员邀请入群(GroupIncreaseInviteNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群成员通知"},
        description = "群成员被管理员邀请入群时调用"
    )
    @Override
    public void onGroupIncreaseInvite(Long botQQ, GroupIncreaseInviteNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人被邀请入群时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人被邀请入群(GroupInviteRequestEvent)",
        categories = {"群相关事件", "BOT事件", "请求事件"},
        description = "机器人被邀请入群时调用"
    )
    @Override
    public void onGroupInviteRequest(Long botQQ, GroupInviteRequestEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * Bot 收到群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到群聊消息(GroupMessageEvent)",
        categories = {"群相关事件", "消息事件"},
        description = "Bot 收到群聊消息时调用"
    )
    @Override
    public void onGroupMessage(Long botQQ, GroupMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人发送群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送群聊消息(GroupMessageSentEvent)",
        categories = {"群相关事件", "消息发送事件", "群聊消息发送"},
        description = "机器人发送群聊消息时调用"
    )
    @Override
    public void onGroupMessageSent(Long botQQ, GroupMessageSentEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群表情回应时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群表情回应(GroupMsgEmojiLikeNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群互动通知"},
        description = "群表情回应时调用"
    )
    @Override
    public void onGroupMsgEmojiLike(Long botQQ, GroupMsgEmojiLikeNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * Bot 收到普通群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到普通群聊消息(GroupNormalMessageEvent)",
        categories = {"群相关事件", "消息事件", "群聊消息"},
        description = "Bot 收到普通群聊消息时调用"
    )
    @Override
    public void onGroupNormalMessage(Long botQQ, GroupNormalMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人发送普通群聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送普通群聊消息(GroupNormalMessageSentEvent)",
        categories = {"群相关事件", "消息发送事件", "群聊消息发送"},
        description = "机器人发送普通群聊消息时调用"
    )
    @Override
    public void onGroupNormalMessageSent(Long botQQ, GroupNormalMessageSentEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群消息被撤回时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "群消息被撤回(GroupRecallNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群互动通知"},
        description = "群消息被撤回时调用"
    )
    @Override
    public void onGroupRecall(Long botQQ, GroupRecallNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 收到群请求时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "收到群请求(GroupRequestEvent)",
        categories = {"群相关事件", "请求事件", "群请求"},
        description = "收到群请求时调用"
    )
    @Override
    public void onGroupRequest(Long botQQ, GroupRequestEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群成员头衔变更时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象            
     */
    @BotEvent(
        name = "群成员头衔变更(TitleNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群互动通知"},
        description = "群成员头衔变更时调用"
    )
    @Override
    public void onGroupTitle(Long botQQ, TitleNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 群文件上传时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象            
     */
    @BotEvent(
        name = "群文件上传(GroupUploadNoticeEvent)",
        categories = {"群相关事件", "通知事件", "群互动通知", "文件相关事件"},
        description = "群文件上传时调用"
    )
    @Override
    public void onGroupUpload(Long botQQ, GroupUploadNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 好友输入状态变化时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "好友输入状态变化(InputStatusNoticeEvent)",
        categories = {"好友相关事件", "通知事件", "私聊通知"},
        description = "好友输入状态变化时调用"
    )
    @Override
    public void onInputStatus(Long botQQ, InputStatusNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人自身发送消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人自身发送消息(MessageSentEvent)",
        categories = {"BOT事件", "消息发送事件"},
        description = "机器人自身发送消息时调用"
    )
    @Override
    public void onMessageSent(Long botQQ, MessageSentEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 戳一戳时调用（好友或群聊场景均可）。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "戳一戳(PokeNoticeEvent)",
        categories = {"通知事件", "互动通知"},
        description = "戳一戳时调用"
    )
    @Override
    public void onPoke(Long botQQ, PokeNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * Bot 收到好友私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象    
     */
    @BotEvent(
        name = "Bot 收到好友私聊消息(PrivateFriendMessageEvent)",
        categories = {"好友相关事件", "消息事件", "私聊消息"},
        description = "Bot 收到好友私聊消息时调用"
    )
    @Override
    public void onPrivateFriendMessage(Long botQQ, PrivateFriendMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人发送好友私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送好友私聊消息(PrivateFriendMessageSentEvent)",
        categories = {"好友相关事件", "消息发送事件", "私聊消息发送"},
        description = "机器人发送好友私聊消息时调用"
    )
    @Override
    public void onPrivateFriendMessageSent(Long botQQ, PrivateFriendMessageSentEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * Bot 收到群临时会话消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到群临时会话消息(PrivateGroupMessageEvent)",
        categories = {"群相关事件", "消息事件", "私聊消息"},
        description = "Bot 收到群临时会话消息时调用"
    )
    @Override
    public void onPrivateGroupMessage(Long botQQ, PrivateGroupMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人发送群临时会话消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送群临时会话消息(PrivateGroupMessageSentEvent)",
        categories = {"群相关事件", "消息发送事件", "私聊消息发送"},
        description = "机器人发送群临时会话消息时调用"
    )
    @Override
    public void onPrivateGroupMessageSent(Long botQQ, PrivateGroupMessageSentEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * Bot 收到私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "Bot 收到私聊消息(PrivateMessageEvent)",
        categories = {"消息事件", "私聊消息"},
        description = "Bot 收到私聊消息时调用"
    )
    @Override
    public void onPrivateMessage(Long botQQ, PrivateMessageEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 机器人发送私聊消息时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "机器人发送私聊消息(PrivateMessageSentEvent)",
        categories = {"消息发送事件", "Bot通知", "私聊消息发送"},
        description = "机器人发送私聊消息时调用"
    )
    @Override
    public void onPrivateMessageSent(Long botQQ, PrivateMessageSentEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }

    /**
     * 个人资料被点赞时调用。
     * @param botQQ Bot QQ 账号
     * @param event 事件对象
     */
    @BotEvent(
        name = "个人资料被点赞(ProfileLikeNoticeEvent)",
        categories = {"通知事件", "Bot通知"},
        description = "个人资料被点赞时调用"
    )
    @Override
    public void onProfileLike(Long botQQ, ProfileLikeNoticeEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }
}