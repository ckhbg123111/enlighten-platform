package com.zhongjia.biz.enums;

/**
 * 媒体平台类别
 */
public enum MediaPlatform {
    xiaohongshu,
    douyin;

    public static boolean isValid(String value) {
        if (value == null) return false;
        for (MediaPlatform p : values()) {
            if (p.name().equals(value)) return true;
        }
        return false;
    }
}


