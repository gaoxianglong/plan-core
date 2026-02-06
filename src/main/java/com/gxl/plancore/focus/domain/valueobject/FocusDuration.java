package com.gxl.plancore.focus.domain.valueobject;

import java.util.Objects;

/**
 * 专注时长值对象
 * 以秒为单位，范围 600 ~ 3600 秒（即 10 ~ 60 分钟）
 */
public class FocusDuration {

    private static final int MIN_SECONDS = 600;   // 10 分钟
    private static final int MAX_SECONDS = 3600;  // 60 分钟

    private final int seconds;

    private FocusDuration(int seconds) {
        this.seconds = seconds;
    }

    public static FocusDuration ofSeconds(int seconds) {
        if (seconds < MIN_SECONDS || seconds > MAX_SECONDS) {
            throw new IllegalArgumentException(
                    String.format("专注时长必须在 %d ~ %d 秒（10 ~ 60 分钟）之间", MIN_SECONDS, MAX_SECONDS));
        }
        return new FocusDuration(seconds);
    }

    public int getSeconds() {
        return seconds;
    }

    /**
     * 获取分钟数（向下取整，用于展示）
     */
    public int getMinutes() {
        return seconds / 60;
    }

    /**
     * 判断指定秒数是否达到 50% 阈值
     */
    public boolean isHalfway(int elapsedSeconds) {
        return elapsedSeconds >= seconds / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FocusDuration that = (FocusDuration) o;
        return seconds == that.seconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seconds);
    }

    @Override
    public String toString() {
        int min = seconds / 60;
        int sec = seconds % 60;
        if (sec == 0) {
            return min + " 分钟";
        }
        return min + " 分 " + sec + " 秒";
    }
}
