package com.example.demo.utils;

import com.example.demo.interceptor.ExcludeFromMD5;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类，用于计算文件或字符串的MD5值
 */
public class MD5Util {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 计算MultipartFile的MD5值
     * @param file 上传的文件
     * @return 文件的MD5值（十六进制字符串）
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 没有MD5算法异常
     */
    public static String calculateFileMD5(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        InputStream fis = file.getInputStream();
        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        fis.close();

        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    /**
     * 计算类的MD5值
     * @param classObject 类对象
     * @return 类的MD5值（十六进制字符串）
     * @throws NoSuchAlgorithmException 没有MD5算法异常
     */
    public static String calculateClassMD5(Object classObject) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        StringBuilder content = new StringBuilder();

        // 加入类名避免不同类型对象冲突
        content.append(classObject.getClass().getName());

        Field[] fields = classObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 跳过被ExcludeFromMD5注解标记的字段
            if (field.isAnnotationPresent(ExcludeFromMD5.class)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(classObject);
                content.append(value != null ? value.toString() : "null");
            } catch (IllegalAccessException e) {
                // 处理访问异常
            }
        }

        md.update(content.toString().getBytes());
        return bytesToHex(md.digest());
    }




    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}