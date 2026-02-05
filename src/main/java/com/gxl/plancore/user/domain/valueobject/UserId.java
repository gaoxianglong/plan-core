package com.gxl.plancore.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * 用户ID值对象
 */
public class UserId {
    
    private final String value;
    
    private UserId(String value) {
        this.value = value;
    }
    
    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }
    
    public static UserId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return new UserId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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
