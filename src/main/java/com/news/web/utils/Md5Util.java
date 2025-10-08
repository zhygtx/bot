package com.news.web.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密工具类
 * 提供字符串MD5加密功能
 */
public class Md5Util {

    private static final Logger logger = LoggerFactory.getLogger(Md5Util.class);

    /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符
     */
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 使用 final 修饰保证线程安全且可被 synchronized 正确使用
     */
    private static final MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("{}初始化失败，MessageDigest不支持MD5。", Md5Util.class.getName(), e);
            throw new RuntimeException("无法初始化MD5摘要算法", e);
        }
    }

    /**
     * 生成字符串的MD5校验值
     *
     * @param str 待加密的字符串
     * @return 字符串的MD5值，如果输入为null则返回null
     */
    public static String getMD5String(String str) {
        if (str == null) {
            return null;
        }
        return getMD5String(str.getBytes());
    }

    /**
     * 判断字符串的MD5校验码是否与一个已知的MD5码相匹配
     *
     * @param password  要校验的字符串
     * @param md5PwdStr 已知的MD5校验码
     * @return 如果匹配返回true，否则返回false
     */
    public static boolean checkPassword(String password, String md5PwdStr) {
        if (password == null || md5PwdStr == null) {
            return false;
        }
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }

    /**
     * 生成字节数组的MD5校验值
     *
     * @param bytes 待加密的字节数组
     * @return 字节数组的MD5值
     */
    public static String getMD5String(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        synchronized (messageDigest) {
            messageDigest.update(bytes);
            return bufferToHex(messageDigest.digest());
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, bytes.length);
    }

    /**
     * 将字节数组的指定部分转换为十六进制字符串
     *
     * @param bytes  字节数组
     * @param length 长度
     * @return 十六进制字符串
     */
    private static String bufferToHex(byte[] bytes, int length) {
        StringBuilder stringBuilder = new StringBuilder(2 * length);
        for (int i = 0; i < length; i++) {
            appendHexPair(bytes[i], stringBuilder);
        }
        return stringBuilder.toString();
    }

    /**
     * 将单个字节转换为十六进制字符对并追加到字符串构建器
     *
     * @param bt            字节值
     * @param stringBuilder 字符串构建器
     */
    private static void appendHexPair(byte bt, StringBuilder stringBuilder) {
        // 取字节中高 4 位的数字转换
        char c0 = HEX_DIGITS[(bt & 0xf0) >> 4];
        // 取字节中低 4 位的数字转换
        char c1 = HEX_DIGITS[bt & 0xf];
        stringBuilder.append(c0);
        stringBuilder.append(c1);
    }
}