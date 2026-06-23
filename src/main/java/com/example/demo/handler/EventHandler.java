package com.example.demo.handler;


import com.github.zhygtx.napcat.event.EventFilter;
import com.github.zhygtx.napcat.event.OneBotEventListener;
import com.github.zhygtx.napcat.event.message.*;
import com.github.zhygtx.napcat.event.meta.HeartbeatMetaEvent;
import com.github.zhygtx.napcat.event.meta.LifecycleConnectMetaEvent;
import com.github.zhygtx.napcat.event.meta.LifecycleMetaEvent;
import com.github.zhygtx.napcat.event.notice.*;
import com.github.zhygtx.napcat.event.request.FriendRequestEvent;
import com.github.zhygtx.napcat.event.request.GroupAddRequestEvent;
import com.github.zhygtx.napcat.event.request.GroupInviteRequestEvent;
import com.github.zhygtx.napcat.event.request.GroupRequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@SuppressWarnings("unused")
@Slf4j
public class EventHandler implements OneBotEventListener {

    @Bean
    public EventFilter botFilter(){
        return ((botQQ, event) -> !(event instanceof MessageEvent && botQQ == ((MessageEvent) event).getUserId()) || event instanceof MessageSentEvent);
    }

    @Override
    public void onLifecycle(Long botQQ, LifecycleMetaEvent event) {
        OneBotEventListener.super.onLifecycle(botQQ, event);
    }

    @Override
    public void onFriendAdd(Long botQQ, FriendAddNoticeEvent event) {
        OneBotEventListener.super.onFriendAdd(botQQ, event);
    }

    @Override
    public void onFriendRecall(Long botQQ, FriendRecallNoticeEvent event) {
        OneBotEventListener.super.onFriendRecall(botQQ, event);
    }

    @Override
    public void onFriendRequest(Long botQQ, FriendRequestEvent event) {
        OneBotEventListener.super.onFriendRequest(botQQ, event);
    }

    @Override
    public void onGroupAddRequest(Long botQQ, GroupAddRequestEvent event) {
        OneBotEventListener.super.onGroupAddRequest(botQQ, event);
    }

    @Override
    public void onGroupAdmin(Long botQQ, GroupAdminNoticeEvent event) {
        OneBotEventListener.super.onGroupAdmin(botQQ, event);
    }

    @Override
    public void onGroupAdminSet(Long botQQ, GroupAdminSetNoticeEvent event) {
        OneBotEventListener.super.onGroupAdminSet(botQQ, event);
    }

    @Override
    public void onGroupAdminUnset(Long botQQ, GroupAdminUnsetNoticeEvent event) {
        OneBotEventListener.super.onGroupAdminUnset(botQQ, event);
    }

    @Override
    public void onGroupBan(Long botQQ, GroupBanNoticeEvent event) {
        OneBotEventListener.super.onGroupBan(botQQ, event);
    }

    @Override
    public void onGroupBanBan(Long botQQ, GroupBanBanNoticeEvent event) {
        OneBotEventListener.super.onGroupBanBan(botQQ, event);
    }

    @Override
    public void onGroupBanLiftBan(Long botQQ, GroupBanLiftBanNoticeEvent event) {
        OneBotEventListener.super.onGroupBanLiftBan(botQQ, event);
    }

    @Override
    public void onGroupCard(Long botQQ, GroupCardNoticeEvent event) {
        OneBotEventListener.super.onGroupCard(botQQ, event);
    }

    @Override
    public void onGroupDecrease(Long botQQ, GroupDecreaseNoticeEvent event) {
        OneBotEventListener.super.onGroupDecrease(botQQ, event);
    }

    @Override
    public void onGroupDecreaseKick(Long botQQ, GroupDecreaseKickNoticeEvent event) {
        OneBotEventListener.super.onGroupDecreaseKick(botQQ, event);
    }

    @Override
    public void onGroupDecreaseKickMe(Long botQQ, GroupDecreaseKickMeNoticeEvent event) {
        OneBotEventListener.super.onGroupDecreaseKickMe(botQQ, event);
    }

    @Override
    public void onGroupDecreaseLeave(Long botQQ, GroupDecreaseLeaveNoticeEvent event) {
        OneBotEventListener.super.onGroupDecreaseLeave(botQQ, event);
    }

    @Override
    public void onGroupEssence(Long botQQ, GroupEssenceNoticeEvent event) {
        OneBotEventListener.super.onGroupEssence(botQQ, event);
    }

    @Override
    public void onGroupEssenceAdd(Long botQQ, GroupEssenceAddNoticeEvent event) {
        OneBotEventListener.super.onGroupEssenceAdd(botQQ, event);
    }

    @Override
    public void onGroupIncrease(Long botQQ, GroupIncreaseNoticeEvent event) {
        OneBotEventListener.super.onGroupIncrease(botQQ, event);
    }

    @Override
    public void onGroupIncreaseApprove(Long botQQ, GroupIncreaseApproveNoticeEvent event) {
        OneBotEventListener.super.onGroupIncreaseApprove(botQQ, event);
    }

    @Override
    public void onGroupIncreaseInvite(Long botQQ, GroupIncreaseInviteNoticeEvent event) {
        OneBotEventListener.super.onGroupIncreaseInvite(botQQ, event);
    }

    @Override
    public void onGroupInviteRequest(Long botQQ, GroupInviteRequestEvent event) {
        OneBotEventListener.super.onGroupInviteRequest(botQQ, event);
    }

    @Override
    public void onGroupMessage(Long botQQ, GroupMessageEvent event) {
        OneBotEventListener.super.onGroupMessage(botQQ, event);
    }

    @Override
    public void onGroupMessageSent(Long botQQ, GroupMessageSentEvent event) {
        OneBotEventListener.super.onGroupMessageSent(botQQ, event);
    }

    @Override
    public void onGroupMsgEmojiLike(Long botQQ, GroupMsgEmojiLikeNoticeEvent event) {
        OneBotEventListener.super.onGroupMsgEmojiLike(botQQ, event);
    }

    @Override
    public void onGroupNormalMessage(Long botQQ, GroupNormalMessageEvent event) {
        OneBotEventListener.super.onGroupNormalMessage(botQQ, event);
    }

    @Override
    public void onGroupNormalMessageSent(Long botQQ, GroupNormalMessageSentEvent event) {
        OneBotEventListener.super.onGroupNormalMessageSent(botQQ, event);
    }

    @Override
    public void onGroupRecall(Long botQQ, GroupRecallNoticeEvent event) {
        OneBotEventListener.super.onGroupRecall(botQQ, event);
    }

    @Override
    public void onGroupRequest(Long botQQ, GroupRequestEvent event) {
        OneBotEventListener.super.onGroupRequest(botQQ, event);
    }

    @Override
    public void onGroupTitle(Long botQQ, TitleNoticeEvent event) {
        OneBotEventListener.super.onGroupTitle(botQQ, event);
    }

    @Override
    public void onGroupUpload(Long botQQ, GroupUploadNoticeEvent event) {
        OneBotEventListener.super.onGroupUpload(botQQ, event);
    }

    @Override
    public void onHeartbeat(Long botQQ, HeartbeatMetaEvent event) {
        OneBotEventListener.super.onHeartbeat(botQQ, event);
    }

    @Override
    public void onInputStatus(Long botQQ, InputStatusNoticeEvent event) {
        OneBotEventListener.super.onInputStatus(botQQ, event);
    }

    @Override
    public void onLifecycleConnect(Long botQQ, LifecycleConnectMetaEvent event) {
        OneBotEventListener.super.onLifecycleConnect(botQQ, event);
    }

    @Override
    public void onMessageSent(Long botQQ, MessageSentEvent event) {
        OneBotEventListener.super.onMessageSent(botQQ, event);
    }

    @Override
    public void onPoke(Long botQQ, PokeNoticeEvent event) {
        OneBotEventListener.super.onPoke(botQQ, event);
    }

    @Override
    public void onPrivateFriendMessage(Long botQQ, PrivateFriendMessageEvent event) {
        OneBotEventListener.super.onPrivateFriendMessage(botQQ, event);
    }

    @Override
    public void onPrivateFriendMessageSent(Long botQQ, PrivateFriendMessageSentEvent event) {
        OneBotEventListener.super.onPrivateFriendMessageSent(botQQ, event);
    }

    @Override
    public void onPrivateGroupMessage(Long botQQ, PrivateGroupMessageEvent event) {
        OneBotEventListener.super.onPrivateGroupMessage(botQQ, event);
    }

    @Override
    public void onPrivateGroupMessageSent(Long botQQ, PrivateGroupMessageSentEvent event) {
        OneBotEventListener.super.onPrivateGroupMessageSent(botQQ, event);
    }

    @Override
    public void onPrivateMessage(Long botQQ, PrivateMessageEvent event) {
        OneBotEventListener.super.onPrivateMessage(botQQ, event);
    }

    @Override
    public void onPrivateMessageSent(Long botQQ, PrivateMessageSentEvent event) {
        OneBotEventListener.super.onPrivateMessageSent(botQQ, event);
    }

    @Override
    public void onProfileLike(Long botQQ, ProfileLikeNoticeEvent event) {
        OneBotEventListener.super.onProfileLike(botQQ, event);
    }
}
