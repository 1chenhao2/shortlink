package com.nageoffer.shortlink.admin.toolkit;

import java.security.SecureRandom;
import java.util.Random;

public class RandomGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final Random RANDOM = new SecureRandom();

    /**
     * 生成6位随机字符串，包含数字和英文字母（大小写）
     *
     * @return 6位随机字符串
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
