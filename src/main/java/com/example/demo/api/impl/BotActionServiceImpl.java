package com.example.demo.api.impl;

import com.example.demo.api.BotActionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.zhygtx.napcat.api.NapCat;
import com.github.zhygtx.napcat.api.response.extra.*;
import com.github.zhygtx.napcat.api.response.file.*;
import com.github.zhygtx.napcat.api.response.friend.*;
import com.github.zhygtx.napcat.api.response.group.*;
import com.github.zhygtx.napcat.api.response.message.*;
import com.github.zhygtx.napcat.api.response.system.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotActionServiceImpl implements BotActionService {

    private final NapCat napCat;

    public BotActionServiceImpl(NapCat napCat) {
        this.napCat = napCat;
    }

    @Override
    public UrlSafelyData checkUrlSafely(long botQQ, String url) {
        return napCat.checkUrlSafely(botQQ, url).join().getData();
    }

    @Override
    public void cleanStreamTempFile(long botQQ) {
        napCat.cleanStreamTempFile(botQQ).join();
    }

    @Override
    public CollectionData createCollection(long botQQ, String rawData, String brief) {
        return napCat.createCollection(botQQ, rawData, brief).join().getData();
    }

    @Override
    public GroupFileFolderData createGroupFileFolder(long botQQ, Long groupId, String folderName, String name) {
        return napCat.createGroupFileFolder(botQQ, groupId, folderName, name).join().getData();
    }

    @Override
    public String deleteFriend(long botQQ, String friendId, Long userId, Boolean tempBlock, Boolean tempBothDel) {
        return napCat.deleteFriend(botQQ, friendId, userId, tempBlock, tempBothDel).join().getData();
    }

    @Override
    public void deleteGroupFile(long botQQ, Long groupId, String fileId) {
        napCat.deleteGroupFile(botQQ, groupId, fileId).join();
    }

    @Override
    public void deleteGroupFolder(long botQQ, Long groupId, String folderId, String folder) {
        napCat.deleteGroupFolder(botQQ, groupId, folderId, folder).join();
    }

    @Override
    public FileRecordStreamData downloadFile(long botQQ, String url, String base64, String name, String headers) {
        return napCat.downloadFile(botQQ, url, base64, name, headers).join().getData();
    }

    @Override
    public FileRecordStreamData downloadFileImageStream(long botQQ, String file, String fileId, Integer chunkSize) {
        return napCat.downloadFileImageStream(botQQ, file, fileId, chunkSize).join().getData();
    }

    @Override
    public FileRecordStreamData downloadFileRecordStream(
            long botQQ,
            String file,
            String fileId,
            Integer chunkSize,
            String outFormat) {
        return napCat.downloadFileRecordStream(botQQ, file, fileId, chunkSize, outFormat).join().getData();
    }

    @Override
    public FileStreamData downloadFileStream(long botQQ, String file, String fileId, Integer chunkSize) {
        return napCat.downloadFileStream(botQQ, file, fileId, chunkSize).join().getData();
    }

    @Override
    public List<AiCharactersData> getAiCharacters(long botQQ, Long groupId, Long chatType) {
        return napCat.getAiCharacters(botQQ, groupId, chatType).join().getData();
    }

    @Override
    public String getAiRecord(long botQQ, String character, Long groupId, String text) {
        return napCat.getAiRecord(botQQ, character, groupId, text).join().getData();
    }

    @Override
    public ClientkeyData getClientkey(long botQQ) {
        return napCat.getClientkey(botQQ).join().getData();
    }

    @Override
    public GroupMsgHistoryData getForwardMsg(long botQQ, String messageId, String id) {
        return napCat.getForwardMsg(botQQ, messageId, id).join().getData();
    }

    @Override
    public GroupMsgHistoryData getFriendMsgHistory(
            long botQQ,
            Long userId,
            String messageSeq,
            Integer count,
            Boolean reverseOrder,
            Boolean disableGetUrl,
            Boolean parseMultMsg,
            Boolean quickReply,
            Boolean reverseOrder2) {
        return napCat.getFriendMsgHistory(botQQ, userId, messageSeq, count, reverseOrder, disableGetUrl, parseMultMsg, quickReply, reverseOrder2).join().getData();
    }

    @Override
    public GroupAtAllRemainData getGroupAtAllRemain(long botQQ, Long groupId) {
        return napCat.getGroupAtAllRemain(botQQ, groupId).join().getData();
    }

    @Override
    public GroupRootFilesData getGroupFilesByFolder(
            long botQQ,
            Long groupId,
            String folderId,
            String folder,
            Integer fileCount) {
        return napCat.getGroupFilesByFolder(botQQ, groupId, folderId, folder, fileCount).join().getData();
    }

    @Override
    public GroupFileSystemInfoData getGroupFileSystemInfo(long botQQ, Long groupId) {
        return napCat.getGroupFileSystemInfo(botQQ, groupId).join().getData();
    }

    @Override
    public GroupHonorInfoData getGroupHonorInfo(long botQQ, Long groupId, String type) {
        return napCat.getGroupHonorInfo(botQQ, groupId, type).join().getData();
    }

    @Override
    public GroupMsgHistoryData getGroupMsgHistory(
            long botQQ,
            Long groupId,
            String messageSeq,
            Integer count,
            Boolean reverseOrder,
            Boolean disableGetUrl,
            Boolean parseMultMsg,
            Boolean quickReply,
            Boolean reverseOrder2) {
        return napCat.getGroupMsgHistory(botQQ, groupId, messageSeq, count, reverseOrder, disableGetUrl, parseMultMsg, quickReply, reverseOrder2).join().getData();
    }

    @Override
    public GroupRootFilesData getGroupRootFiles(long botQQ, Long groupId, Integer fileCount) {
        return napCat.getGroupRootFiles(botQQ, groupId, fileCount).join().getData();
    }

    @Override
    public void getGuildList(long botQQ) {
        napCat.getGuildList(botQQ).join();
    }

    @Override
    public void getGuildServiceProfile(long botQQ) {
        napCat.getGuildServiceProfile(botQQ).join();
    }

    @Override
    public List<ModelShowData> getModelShow(long botQQ, String model) {
        return napCat.getModelShow(botQQ, model).join().getData();
    }

    @Override
    public List<String> getOnlineClients(long botQQ) {
        return napCat.getOnlineClients(botQQ).join().getData();
    }

    @Override
    public StrangerInfoData getStrangerInfo(long botQQ, Long userId, Boolean noCache) {
        return napCat.getStrangerInfo(botQQ, userId, noCache).join().getData();
    }

    @Override
    public void handleQuickOperationInternal(long botQQ, JsonNode context, JsonNode operation) {
        napCat.handleQuickOperationInternal(botQQ, context, operation).join();
    }

    @Override
    public ImageData ocrImage(long botQQ, String image) {
        return napCat.ocrImage(botQQ, image).join().getData();
    }

    @Override
    public ImageData ocrImageInternal(long botQQ, String image) {
        return napCat.ocrImageInternal(botQQ, image).join().getData();
    }

    @Override
    public GroupMsgData sendForwardMsg(
            long botQQ,
            String messageType,
            Long userId,
            Long groupId,
            String message,
            Boolean autoEscape,
            String source,
            List<JsonNode> news,
            String summary,
            String prompt,
            Long timeout) {
        return napCat.sendForwardMsg(botQQ, messageType, userId, groupId, message, autoEscape, source, news, summary, prompt, timeout).join().getData();
    }

    @Override
    public GroupAiRecordData sendGroupAiRecord(long botQQ, String character, Long groupId, String text) {
        return napCat.sendGroupAiRecord(botQQ, character, groupId, text).join().getData();
    }

    @Override
    public GroupMsgData sendGroupForwardMsg(
            long botQQ,
            String messageType,
            Long userId,
            Long groupId,
            String message,
            Boolean autoEscape,
            String source,
            List<JsonNode> news,
            String summary,
            String prompt,
            Long timeout) {
        return napCat.sendGroupForwardMsg(botQQ, messageType, userId, groupId, message, autoEscape, source, news, summary, prompt, timeout).join().getData();
    }

    @Override
    public void sendGroupNotice(
            long botQQ,
            Long groupId,
            String content,
            String image,
            Long pinned,
            Long type,
            Long confirmRequired,
            Long isShowEditCard,
            Long tipWindowType) {
        napCat.sendGroupNotice(botQQ, groupId, content, image, pinned, type, confirmRequired, isShowEditCard, tipWindowType).join();
    }

    @Override
    public GroupMsgData sendPrivateForwardMsg(
            long botQQ,
            String messageType,
            Long userId,
            Long groupId,
            String message,
            Boolean autoEscape,
            String source,
            List<JsonNode> news,
            String summary,
            String prompt,
            Long timeout) {
        return napCat.sendPrivateForwardMsg(botQQ, messageType, userId, groupId, message, autoEscape, source, news, summary, prompt, timeout).join().getData();
    }

    @Override
    public void setGroupKickMembers(long botQQ, Long groupId, List<Long> userId, Boolean rejectAddRequest) {
        napCat.setGroupKickMembers(botQQ, groupId, userId, rejectAddRequest).join();
    }

    @Override
    public CollectionData setGroupPortrait(long botQQ, String file, Long groupId) {
        return napCat.setGroupPortrait(botQQ, file, groupId).join().getData();
    }

    @Override
    public void setGroupSpecialTitle(long botQQ, Long groupId, Long userId, String specialTitle) {
        napCat.setGroupSpecialTitle(botQQ, groupId, userId, specialTitle).join();
    }

    @Override
    public void setModelShow(long botQQ) {
        napCat.setModelShow(botQQ).join();
    }

    @Override
    public void setQqAvatar(long botQQ, String file) {
        napCat.setQqAvatar(botQQ, file).join();
    }

    @Override
    public void setQqProfile(long botQQ, String nickname, String personalNote, Long sex) {
        napCat.setQqProfile(botQQ, nickname, personalNote, sex).join();
    }

    @Override
    public void setSelfLongnick(long botQQ, String longNick) {
        napCat.setSelfLongnick(botQQ, longNick).join();
    }

    @Override
    public DownloadStreamData testDownloadStream(long botQQ, Boolean error) {
        return napCat.testDownloadStream(botQQ, error).join().getData();
    }

    @Override
    public En2zhData translateEn2zh(long botQQ, List<String> words) {
        return napCat.translateEn2zh(botQQ, words).join().getData();
    }

    @Override
    public FileStreamData uploadFileStream(
            long botQQ,
            String streamId,
            String chunkData,
            Long chunkIndex,
            Long totalChunks,
            Integer fileSize,
            String expectedSha256,
            Boolean isComplete,
            String filename,
            Boolean reset,
            Boolean verifyOnly,
            Long fileRetention) {
        return napCat.uploadFileStream(botQQ, streamId, chunkData, chunkIndex, totalChunks, fileSize, expectedSha256, isComplete, filename, reset, verifyOnly, fileRetention).join().getData();
    }

    @Override
    public GroupFileData uploadGroupFile(
            long botQQ,
            Long groupId,
            String file,
            String name,
            String folder,
            String folderId,
            Boolean uploadFile) {
        return napCat.uploadGroupFile(botQQ, groupId, file, name, folder, folderId, uploadFile).join().getData();
    }

    @Override
    public GroupFileData uploadPrivateFile(long botQQ, Long userId, String file, String name, Boolean uploadFile) {
        return napCat.uploadPrivateFile(botQQ, userId, file, name, uploadFile).join().getData();
    }

    @Override
    public void cancelOnlineFile(long botQQ, Long userId, String msgId) {
        napCat.cancelOnlineFile(botQQ, userId, msgId).join();
    }

    @Override
    public FlashTaskData createFlashTask(long botQQ, List<String> files, String name, String thumbPath) {
        return napCat.createFlashTask(botQQ, files, name, thumbPath).join().getData();
    }

    @Override
    public void downloadFileset(long botQQ, String filesetId) {
        napCat.downloadFileset(botQQ, filesetId).join();
    }

    @Override
    public FileData getFile(long botQQ, String file, String fileId) {
        return napCat.getFile(botQQ, file, fileId).join().getData();
    }

    @Override
    public FilesetIdData getFilesetId(long botQQ, String shareCode) {
        return napCat.getFilesetId(botQQ, shareCode).join().getData();
    }

    @Override
    public FilesetInfoData getFilesetInfo(long botQQ, String filesetId) {
        return napCat.getFilesetInfo(botQQ, filesetId).join().getData();
    }

    @Override
    public List<FlashFileData> getFlashFileList(long botQQ, String filesetId) {
        return napCat.getFlashFileList(botQQ, filesetId).join().getData();
    }

    @Override
    public GroupFileUrlData getFlashFileUrl(long botQQ, String filesetId, String fileName, Long fileIndex) {
        return napCat.getFlashFileUrl(botQQ, filesetId, fileName, fileIndex).join().getData();
    }

    @Override
    public GroupFileUrlData getGroupFileUrl(long botQQ, Long groupId, String fileId) {
        return napCat.getGroupFileUrl(botQQ, groupId, fileId).join().getData();
    }

    @Override
    public FileData getImage(long botQQ, String file, String fileId) {
        return napCat.getImage(botQQ, file, fileId).join().getData();
    }

    @Override
    public void getOnlineFileMsg(long botQQ, Long userId) {
        napCat.getOnlineFileMsg(botQQ, userId).join();
    }

    @Override
    public GroupFileUrlData getPrivateFileUrl(long botQQ, String fileId) {
        return napCat.getPrivateFileUrl(botQQ, fileId).join().getData();
    }

    @Override
    public FileData getRecord(long botQQ, String file, String fileId, String outFormat) {
        return napCat.getRecord(botQQ, file, fileId, outFormat).join().getData();
    }

    @Override
    public void getShareLink(long botQQ, String filesetId) {
        napCat.getShareLink(botQQ, filesetId).join();
    }

    @Override
    public GroupFileData moveGroupFile(
            long botQQ,
            Long groupId,
            String fileId,
            String currentParentDirectory,
            String targetParentDirectory) {
        return napCat.moveGroupFile(botQQ, groupId, fileId, currentParentDirectory, targetParentDirectory).join().getData();
    }

    @Override
    public void receiveOnlineFile(long botQQ, Long userId, String msgId, String elementId) {
        napCat.receiveOnlineFile(botQQ, userId, msgId, elementId).join();
    }

    @Override
    public void refuseOnlineFile(long botQQ, Long userId, String msgId, String elementId) {
        napCat.refuseOnlineFile(botQQ, userId, msgId, elementId).join();
    }

    @Override
    public GroupFileData renameGroupFile(
            long botQQ,
            Long groupId,
            String fileId,
            String currentParentDirectory,
            String newName) {
        return napCat.renameGroupFile(botQQ, groupId, fileId, currentParentDirectory, newName).join().getData();
    }

    @Override
    public GroupAiRecordData sendFlashMsg(long botQQ, String filesetId, Long userId, Long groupId) {
        return napCat.sendFlashMsg(botQQ, filesetId, userId, groupId).join().getData();
    }

    @Override
    public void sendOnlineFile(long botQQ, Long userId, String filePath, String fileName) {
        napCat.sendOnlineFile(botQQ, userId, filePath, fileName).join();
    }

    @Override
    public void sendOnlineFolder(long botQQ, Long userId, String folderPath, String folderName) {
        napCat.sendOnlineFolder(botQQ, userId, folderPath, folderName).join();
    }

    @Override
    public GroupFileData transGroupFile(long botQQ, Long groupId, String fileId) {
        return napCat.transGroupFile(botQQ, groupId, fileId).join().getData();
    }

    @Override
    public CookiesData getCookies(long botQQ, String domain) {
        return napCat.getCookies(botQQ, domain).join().getData();
    }

    @Override
    public List<String> getFriendList(long botQQ, Boolean noCache) {
        return napCat.getFriendList(botQQ, noCache).join().getData();
    }

    @Override
    public List<FriendsWithCategoryData> getFriendsWithCategory(long botQQ) {
        return napCat.getFriendsWithCategory(botQQ).join().getData();
    }

    @Override
    public ProfileLikeData getProfileLike(long botQQ, Long userId, Long start, Integer count) {
        return napCat.getProfileLike(botQQ, userId, start, count).join().getData();
    }

    @Override
    public List<RecentContactData> getRecentContact(long botQQ, Integer count) {
        return napCat.getRecentContact(botQQ, count).join().getData();
    }

    @Override
    public List<UnidirectionalFriendData> getUnidirectionalFriendList(long botQQ) {
        return napCat.getUnidirectionalFriendList(botQQ).join().getData();
    }

    @Override
    public void sendLike(long botQQ, Long userId, Long times) {
        napCat.sendLike(botQQ, userId, times).join();
    }

    @Override
    public String setDiyOnlineStatus(long botQQ, Long faceId, Long faceType, String wording) {
        return napCat.setDiyOnlineStatus(botQQ, faceId, faceType, wording).join().getData();
    }

    @Override
    public void setFriendAddRequest(long botQQ, String flag, String approve, String remark) {
        napCat.setFriendAddRequest(botQQ, flag, approve, remark).join();
    }

    @Override
    public void setFriendRemark(long botQQ, Long userId, String remark) {
        napCat.setFriendRemark(botQQ, userId, remark).join();
    }

    @Override
    public DelGroupAlbumMediaData cancelGroupAlbumMediaLike(
            long botQQ,
            Long groupId,
            String albumId,
            String batchId,
            String lloc) {
        return napCat.cancelGroupAlbumMediaLike(botQQ, groupId, albumId, batchId, lloc).join().getData();
    }

    @Override
    public void deleteEssenceMsg(long botQQ, Long messageId, String msgSeq, String msgRandom, Long groupId) {
        napCat.deleteEssenceMsg(botQQ, messageId, msgSeq, msgRandom, groupId).join();
    }

    @Override
    public DelGroupAlbumMediaData delGroupAlbumMedia(long botQQ, Long groupId, String albumId, String lloc) {
        return napCat.delGroupAlbumMedia(botQQ, groupId, albumId, lloc).join().getData();
    }

    @Override
    public void delGroupNotice(long botQQ, Long groupId, String noticeId) {
        napCat.delGroupNotice(botQQ, groupId, noticeId).join();
    }

    @Override
    public DelGroupAlbumMediaData doGroupAlbumComment(
            long botQQ,
            Long groupId,
            String albumId,
            String lloc,
            String content) {
        return napCat.doGroupAlbumComment(botQQ, groupId, albumId, lloc, content).join().getData();
    }

    @Override
    public List<EssenceMsgData> getEssenceMsgList(long botQQ, Long groupId) {
        return napCat.getEssenceMsgList(botQQ, groupId).join().getData();
    }

    @Override
    public GroupAlbumMediaData getGroupAlbumMediaList(long botQQ, Long groupId, String albumId, String attachInfo) {
        return napCat.getGroupAlbumMediaList(botQQ, groupId, albumId, attachInfo).join().getData();
    }

    @Override
    public GroupDetailInfoData getGroupDetailInfo(long botQQ, Long groupId) {
        return napCat.getGroupDetailInfo(botQQ, groupId).join().getData();
    }

    @Override
    public List<GroupIgnoreAddRequestData> getGroupIgnoreAddRequest(long botQQ) {
        return napCat.getGroupIgnoreAddRequest(botQQ).join().getData();
    }

    @Override
    public GroupIgnoredNotifiesData getGroupIgnoredNotifies(long botQQ) {
        return napCat.getGroupIgnoredNotifies(botQQ).join().getData();
    }

    @Override
    public GroupInfoData getGroupInfo(long botQQ, Long groupId) {
        return napCat.getGroupInfo(botQQ, groupId).join().getData();
    }

    @Override
    public void getGroupInfoEx(long botQQ, Long groupId) {
        napCat.getGroupInfoEx(botQQ, groupId).join();
    }

    @Override
    public List<String> getGroupList(long botQQ, Boolean noCache) {
        return napCat.getGroupList(botQQ, noCache).join().getData();
    }

    @Override
    public GroupMemberInfoData getGroupMemberInfo(long botQQ, Long groupId, Long userId, Boolean noCache) {
        return napCat.getGroupMemberInfo(botQQ, groupId, userId, noCache).join().getData();
    }

    @Override
    public List<GroupMemberData> getGroupMemberList(long botQQ, Long groupId, Boolean noCache) {
        return napCat.getGroupMemberList(botQQ, groupId, noCache).join().getData();
    }

    @Override
    public List<GroupNoticeData> getGroupNotice(long botQQ, Long groupId) {
        return napCat.getGroupNotice(botQQ, groupId).join().getData();
    }

    @Override
    public List<GroupShutData> getGroupShutList(long botQQ, Long groupId) {
        return napCat.getGroupShutList(botQQ, groupId).join().getData();
    }

    @Override
    public List<GroupSignedData> getGroupSignedList(long botQQ, Long groupId) {
        return napCat.getGroupSignedList(botQQ, groupId).join().getData();
    }

    @Override
    public QunAlbumData getQunAlbumList(long botQQ, Long groupId, String attachInfo) {
        return napCat.getQunAlbumList(botQQ, groupId, attachInfo).join().getData();
    }

    @Override
    public GroupMsgData sendGroupMsg(
            long botQQ,
            String messageType,
            Long userId,
            Long groupId,
            String message,
            Boolean autoEscape,
            String source,
            List<JsonNode> news,
            String summary,
            String prompt,
            Long timeout) {
        return napCat.sendGroupMsg(botQQ, messageType, userId, groupId, message, autoEscape, source, news, summary, prompt, timeout).join().getData();
    }

    @Override
    public void sendGroupSign(long botQQ, Long groupId) {
        napCat.sendGroupSign(botQQ, groupId).join();
    }

    @Override
    public void setEssenceMsg(long botQQ, Long messageId) {
        napCat.setEssenceMsg(botQQ, messageId).join();
    }

    @Override
    public void setGroupAddOption(long botQQ, Long groupId, Long addType, String groupQuestion, String groupAnswer) {
        napCat.setGroupAddOption(botQQ, groupId, addType, groupQuestion, groupAnswer).join();
    }

    @Override
    public void setGroupAddRequest(long botQQ, String flag, Boolean approve, String reason, Integer count) {
        napCat.setGroupAddRequest(botQQ, flag, approve, reason, count).join();
    }

    @Override
    public void setGroupAdmin(long botQQ, Long groupId, Long userId, Boolean enable) {
        napCat.setGroupAdmin(botQQ, groupId, userId, enable).join();
    }

    @Override
    public DelGroupAlbumMediaData setGroupAlbumMediaLike(
            long botQQ,
            Long groupId,
            String albumId,
            String batchId,
            String lloc) {
        return napCat.setGroupAlbumMediaLike(botQQ, groupId, albumId, batchId, lloc).join().getData();
    }

    @Override
    public void setGroupBan(long botQQ, Long groupId, Long userId, Long duration) {
        napCat.setGroupBan(botQQ, groupId, userId, duration).join();
    }

    @Override
    public void setGroupCard(long botQQ, Long groupId, Long userId, String card) {
        napCat.setGroupCard(botQQ, groupId, userId, card).join();
    }

    @Override
    public void setGroupKick(long botQQ, Long groupId, Long userId, Boolean rejectAddRequest) {
        napCat.setGroupKick(botQQ, groupId, userId, rejectAddRequest).join();
    }

    @Override
    public void setGroupLeave(long botQQ, Long groupId, Boolean isDismiss) {
        napCat.setGroupLeave(botQQ, groupId, isDismiss).join();
    }

    @Override
    public void setGroupName(long botQQ, Long groupId, String groupName) {
        napCat.setGroupName(botQQ, groupId, groupName).join();
    }

    @Override
    public void setGroupRemark(long botQQ, Long groupId, String remark) {
        napCat.setGroupRemark(botQQ, groupId, remark).join();
    }

    @Override
    public void setGroupRobotAddOption(long botQQ, Long groupId, Long robotMemberSwitch, Long robotMemberExamine) {
        napCat.setGroupRobotAddOption(botQQ, groupId, robotMemberSwitch, robotMemberExamine).join();
    }

    @Override
    public void setGroupSearch(long botQQ, Long groupId, Long noCodeFingerOpen, Long noFingerOpen) {
        napCat.setGroupSearch(botQQ, groupId, noCodeFingerOpen, noFingerOpen).join();
    }

    @Override
    public void setGroupSign(long botQQ, Long groupId) {
        napCat.setGroupSign(botQQ, groupId).join();
    }

    @Override
    public void setGroupWholeBan(long botQQ, Long groupId, Boolean enable) {
        napCat.setGroupWholeBan(botQQ, groupId, enable).join();
    }

    @Override
    public ImageToQunAlbumData uploadImageToQunAlbum(
            long botQQ,
            Long groupId,
            String albumId,
            String albumName,
            String file) {
        return napCat.uploadImageToQunAlbum(botQQ, groupId, albumId, albumName, file).join().getData();
    }

    @Override
    public String arksharegroup(long botQQ, Long groupId) {
        return napCat.arksharegroup(botQQ, groupId).join().getData();
    }

    @Override
    public ArksharepeerData arksharepeer(long botQQ, Long userId, Long groupId, String phoneNumber) {
        return napCat.arksharepeer(botQQ, userId, groupId, phoneNumber).join().getData();
    }

    @Override
    public void cancelGroupTodo(long botQQ, Long groupId, String messageId, String messageSeq) {
        napCat.cancelGroupTodo(botQQ, groupId, messageId, messageSeq).join();
    }

    @Override
    public void clickInlineKeyboardButton(
            long botQQ,
            Long groupId,
            String botAppid,
            String buttonId,
            String callbackData,
            String msgSeq) {
        napCat.clickInlineKeyboardButton(botQQ, groupId, botAppid, buttonId, callbackData, msgSeq).join();
    }

    @Override
    public void completeGroupTodo(long botQQ, Long groupId, String messageId, String messageSeq) {
        napCat.completeGroupTodo(botQQ, groupId, messageId, messageSeq).join();
    }

    @Override
    public void deleteMsg(long botQQ, Long messageId) {
        napCat.deleteMsg(botQQ, messageId).join();
    }

    @Override
    public EmojiLikeData fetchEmojiLike(
            long botQQ,
            Long messageId,
            Long emojiId,
            Long emojiType,
            Integer count,
            String cookie) {
        return napCat.fetchEmojiLike(botQQ, messageId, emojiId, emojiType, count, cookie).join().getData();
    }

    @Override
    public PttTextData fetchPttText(long botQQ, Long messageId) {
        return napCat.fetchPttText(botQQ, messageId).join().getData();
    }

    @Override
    public void forwardFriendSingleMsg(long botQQ, Long messageId, Long groupId, Long userId) {
        napCat.forwardFriendSingleMsg(botQQ, messageId, groupId, userId).join();
    }

    @Override
    public void forwardGroupSingleMsg(long botQQ, Long messageId, Long groupId, Long userId) {
        napCat.forwardGroupSingleMsg(botQQ, messageId, groupId, userId).join();
    }

    @Override
    public void friendPoke(long botQQ, Long groupId, Long userId, String targetId) {
        napCat.friendPoke(botQQ, groupId, userId, targetId).join();
    }

    @Override
    public EmojiLikesData getEmojiLikes(
            long botQQ,
            Long groupId,
            String messageId,
            String emojiId,
            String emojiType,
            Integer count) {
        return napCat.getEmojiLikes(botQQ, groupId, messageId, emojiId, emojiType, count).join().getData();
    }

    @Override
    public MsgData getMsg(long botQQ, Long messageId) {
        return napCat.getMsg(botQQ, messageId).join().getData();
    }

    @Override
    public void groupPoke(long botQQ, Long groupId, Long userId, String targetId) {
        napCat.groupPoke(botQQ, groupId, userId, targetId).join();
    }

    @Override
    public String markAllAsRead(long botQQ) {
        return napCat.markAllAsRead(botQQ).join().getData();
    }

    @Override
    public void markGroupMsgAsRead(long botQQ, Long userId, Long groupId, String messageId) {
        napCat.markGroupMsgAsRead(botQQ, userId, groupId, messageId).join();
    }

    @Override
    public void markMsgAsRead(long botQQ, Long userId, Long groupId, String messageId) {
        napCat.markMsgAsRead(botQQ, userId, groupId, messageId).join();
    }

    @Override
    public void markPrivateMsgAsRead(long botQQ, Long userId, Long groupId, String messageId) {
        napCat.markPrivateMsgAsRead(botQQ, userId, groupId, messageId).join();
    }

    @Override
    public ArksharepeerData sendArkShare(long botQQ, Long userId, Long groupId, String phoneNumber) {
        return napCat.sendArkShare(botQQ, userId, groupId, phoneNumber).join().getData();
    }

    @Override
    public String sendGroupArkShare(long botQQ, Long groupId) {
        return napCat.sendGroupArkShare(botQQ, groupId).join().getData();
    }

    @Override
    public GroupMsgData sendMsg(
            long botQQ,
            String messageType,
            Long userId,
            Long groupId,
            String message,
            Boolean autoEscape,
            String source,
            List<JsonNode> news,
            String summary,
            String prompt,
            Long timeout) {
        return napCat.sendMsg(botQQ, messageType, userId, groupId, message, autoEscape, source, news, summary, prompt, timeout).join().getData();
    }

    @Override
    public void sendPoke(long botQQ, Long groupId, Long userId, String targetId) {
        napCat.sendPoke(botQQ, groupId, userId, targetId).join();
    }

    @Override
    public GroupMsgData sendPrivateMsg(
            long botQQ,
            String messageType,
            Long userId,
            Long groupId,
            String message,
            Boolean autoEscape,
            String source,
            List<JsonNode> news,
            String summary,
            String prompt,
            Long timeout) {
        return napCat.sendPrivateMsg(botQQ, messageType, userId, groupId, message, autoEscape, source, news, summary, prompt, timeout).join().getData();
    }

    @Override
    public void setGroupTodo(long botQQ, Long groupId, String messageId, String messageSeq) {
        napCat.setGroupTodo(botQQ, groupId, messageId, messageSeq).join();
    }

    @Override
    public MsgEmojiLikeData setMsgEmojiLike(long botQQ, Long messageId, Long emojiId, Boolean set) {
        return napCat.setMsgEmojiLike(botQQ, messageId, emojiId, set).join().getData();
    }

    @Override
    public void addCustomFace(
            long botQQ,
            String file,
            String emojiId,
            String packageId,
            String fileName,
            String fileSize,
            String md5,
            Boolean isMarkFace,
            Boolean isOrigin) {
        napCat.addCustomFace(botQQ, file, emojiId, packageId, fileName, fileSize, md5, isMarkFace, isOrigin).join();
    }

    @Override
    public void botExit(long botQQ) {
        napCat.botExit(botQQ).join();
    }

    @Override
    public RecordData canSendImage(long botQQ) {
        return napCat.canSendImage(botQQ).join().getData();
    }

    @Override
    public RecordData canSendRecord(long botQQ) {
        return napCat.canSendRecord(botQQ).join().getData();
    }

    @Override
    public void cleanCache(long botQQ) {
        napCat.cleanCache(botQQ).join();
    }

    @Override
    public void deleteCustomFace(long botQQ, String resId, String id, List<String> ids, String md5) {
        napCat.deleteCustomFace(botQQ, resId, id, ids, md5).join();
    }

    @Override
    public List<String> fetchCustomFace(long botQQ, Integer count) {
        return napCat.fetchCustomFace(botQQ, count).join().getData();
    }

    @Override
    public void fetchCustomFaceDetail(long botQQ, Integer count) {
        napCat.fetchCustomFaceDetail(botQQ, count).join();
    }

    @Override
    public CollectionData getCollectionList(long botQQ, String category, String count) {
        return napCat.getCollectionList(botQQ, category, count).join().getData();
    }

    @Override
    public CredentialsData getCredentials(long botQQ, String domain) {
        return napCat.getCredentials(botQQ, domain).join().getData();
    }

    @Override
    public CsrfTokenData getCsrfToken(long botQQ) {
        return napCat.getCsrfToken(botQQ).join().getData();
    }

    @Override
    public List<DoubtFriendsAddRequestData> getDoubtFriendsAddRequest(long botQQ, Integer count) {
        return napCat.getDoubtFriendsAddRequest(botQQ, count).join().getData();
    }

    @Override
    public GroupIgnoredNotifiesData getGroupSystemMsg(long botQQ, Integer count) {
        return napCat.getGroupSystemMsg(botQQ, count).join().getData();
    }

    @Override
    public LoginInfoData getLoginInfo(long botQQ) {
        return napCat.getLoginInfo(botQQ).join().getData();
    }

    @Override
    public MiniAppArkData getMiniAppArk(long botQQ) {
        return napCat.getMiniAppArk(botQQ).join().getData();
    }

    @Override
    public List<RkeyData> getRkey(long botQQ) {
        return napCat.getRkey(botQQ).join().getData();
    }

    @Override
    public RkeyServerData getRkeyServer(long botQQ) {
        return napCat.getRkeyServer(botQQ).join().getData();
    }

    @Override
    public List<RobotUinRangeData> getRobotUinRange(long botQQ) {
        return napCat.getRobotUinRange(botQQ).join().getData();
    }

    @Override
    public StatusData getStatus(long botQQ) {
        return napCat.getStatus(botQQ).join().getData();
    }

    @Override
    public VersionInfoData getVersionInfo(long botQQ) {
        return napCat.getVersionInfo(botQQ).join().getData();
    }

    @Override
    public void ncGetPacketStatus(long botQQ) {
        napCat.ncGetPacketStatus(botQQ).join();
    }

    @Override
    public List<RkeyData> ncGetRkey(long botQQ) {
        return napCat.ncGetRkey(botQQ).join().getData();
    }

    @Override
    public UserStatusData ncGetUserStatus(long botQQ, Long userId) {
        return napCat.ncGetUserStatus(botQQ, userId).join().getData();
    }

    @Override
    public String sendPacket(long botQQ, String cmd, String data, String rsp) {
        return napCat.sendPacket(botQQ, cmd, data, rsp).join().getData();
    }

    @Override
    public void setCustomFaceDesc(long botQQ, Long emojiId, String resId, String md5, String desc) {
        napCat.setCustomFaceDesc(botQQ, emojiId, resId, md5, desc).join();
    }

    @Override
    public String setDoubtFriendsAddRequest(long botQQ, String flag, Boolean approve) {
        return napCat.setDoubtFriendsAddRequest(botQQ, flag, approve).join().getData();
    }

    @Override
    public void setInputStatus(long botQQ, Long userId, Long eventType) {
        napCat.setInputStatus(botQQ, userId, eventType).join();
    }

    @Override
    public void setOnlineStatus(long botQQ, Long status, Long extStatus, Long batteryStatus) {
        napCat.setOnlineStatus(botQQ, status, extStatus, batteryStatus).join();
    }

    @Override
    public void setRestart(long botQQ) {
        napCat.setRestart(botQQ).join();
    }

}
