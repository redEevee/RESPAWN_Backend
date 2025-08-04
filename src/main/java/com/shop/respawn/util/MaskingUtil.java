package com.shop.respawn.util;

public class MaskingUtil {

    /**
     * 아이디의 중간 4글자를 '*'로 가립니다.
     * 아이디 길이가 4글자 미만이면 전체를 '*'로 가림
     * @param username 원본 아이디 문자열
     * @return 중간 4글자가 가려진 아이디
     */
    public static String maskMiddleFourChars(String username) {

        if (username == null || username.isEmpty()) {
            return username;
        }

        int length = username.length();
        if (length <= 4) {
            // 짧은 경우 전부 마스킹 처리
            return "*".repeat(length);
        }

        // 중간 4글자 마스킹 (길이가 8 이상이면 정확히 4글자, 짧으면 가능한 범위 내)
        int maskLength = 4;
        int start = (length - maskLength) / 2;

        // 시작부분은 그대로
        // 중간 4글자 마스킹
        // 나머지 끝부분 그대로
        return username.substring(0, start) + // 시작부분은 그대로
                "*".repeat(maskLength) + // 중간 4글자 마스킹
                username.substring(start + maskLength);
    }
}
