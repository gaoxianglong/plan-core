package com.gxl.plancore.user.domain.valueobject;

import java.util.Objects;
import java.util.Set;

/**
 * 昵称值对象
 */
public class Nickname {
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;
    private static final Set<String> FORBIDDEN_WORDS = Set.of("管理员", "客服", "系统", "admin", "system");
    
    private final String value;
    
    private Nickname(String value) {
        this.value = value;
    }
    
    public static Nickname of(String value) {
        validate(value);
        return new Nickname(value.trim());
    }
    
    private static void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("昵称长度必须在 %d-%d 字符之间", MIN_LENGTH, MAX_LENGTH)
            );
        }
        
        if (containsForbiddenWords(trimmed)) {
            throw new IllegalArgumentException("昵称包含违规词");
        }
    }
    
    private static boolean containsForbiddenWords(String value) {
        String lowerValue = value.toLowerCase();
        return FORBIDDEN_WORDS.stream().anyMatch(lowerValue::contains);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nickname nickname = (Nickname) o;
        return Objects.equals(value, nickname.value);
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
