package com.gxl.plancore.user.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 邮箱值对象
 */
public class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    
    private final String value;
    
    private Email(String value) {
        this.value = value;
    }
    
    public static Email of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        String trimmed = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        return new Email(trimmed);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
