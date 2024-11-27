package com.eh.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Component
public class FileUtil {
    public static String getExtension(String filename) {


        // 检查文件名是否包含.
        if (filename != null && filename.contains(".")) {
            // 通过最后一次出现 . 的索引来截取扩展名
            int dotIndex = filename.lastIndexOf(".");
            // 截取最后一个 . 之后的所有字符，即扩展名
            String extension = filename.substring(dotIndex + 1);
            return extension;
        } else {
            // 如果没有找到 . 或者文件名为空，返回 null 或者一个默认的扩展名
            return null;
        }
    }

    public static boolean isIllegalExtension(String url) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("jpg", "jpg");
        map.put("jpeg", "jpeg");
        map.put("png", "png");
        map.put("gif", "gif");
        map.put("bmp", "bmp");
        map.put("tif", "tif");
        map.put("tiff", "tiff");
        map.put("wav", "wav");
        map.put("mp3", "mp3");
        map.put("mp4", "mp4");
        map.put("avi", "avi");
        map.put("flv", "flv");
        map.put("m4a", "m4a");
        if (map.containsKey(url)) {
            return true;
        }
        return false;
    }
}
