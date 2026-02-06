package com.gxl.plancore.focus.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * 专注会话ID值对象
 */
public class SessionId {

    private final String value;

    private SessionId(String value) {
        this.value = value;
    }

    public static SessionId newId() {
        return new SessionId(UUID.randomUUID().toString());
    }

    public static SessionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        return new SessionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
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
