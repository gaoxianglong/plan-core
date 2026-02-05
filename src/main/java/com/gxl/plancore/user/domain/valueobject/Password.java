package com.gxl.plancore.user.domain.valueobject;

import java.util.Objects;

/**
 * 密码值对象
 * 存储加密后的密码
 */
public class Password {
    
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 32;
    
    private final String hashedValue;
    
    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }
    
    /**
     * 从已加密的密码创建（用于从数据库加载）
     */
    public static Password fromHashed(String hashedValue) {
        if (hashedValue == null || hashedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return new Password(hashedValue);
    }
    
    /**
     * 校验明文密码格式
     */
    public static void validatePlainText(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (plainText.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("密码长度不能少于" + MIN_LENGTH + "位");
        }
        if (plainText.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("密码长度不能超过" + MAX_LENGTH + "位");
        }
    }
    
    public String getHashedValue() {
        return hashedValue;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }
    
    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
