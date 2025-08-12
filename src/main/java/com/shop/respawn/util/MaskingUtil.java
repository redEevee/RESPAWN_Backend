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

    public static String maskUsername(String username) {
        if (username == null || username.length() <= 3) {
            // 3글자 이하이면 전체 * 처리하거나 그대로 반환
            return username == null ? "" : username.replaceAll(".", "*");
        }
        String visible = username.substring(0, 4);
        String masked = "*".repeat(username.length() - 3);
        return visible + masked;
    }

    /**
     * 전화번호 마스킹 (가운데까지 표시하고 나머지는 * 처리)
     * 예) 010-2446-****
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) return phoneNumber;
        String digits = phoneNumber.replaceAll("[^0-9]", ""); // 숫자만 추출
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" +
                    digits.substring(3, 7) + "-****";
        } else if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" +
                    digits.substring(3, 6) + "-****";
        }
        return phoneNumber; // 형식 맞지 않으면 그대로 반환
    }

    /**
     * 이메일 마스킹
     * 예) test***@naver.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String id = parts[0];
        if (id.length() <= 3) {
            id = id.charAt(0) + "**";
        } else {
            id = id.substring(0, 4) + "****";
        }
        return id + "@" + parts[1];
    }
}
