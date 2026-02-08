package com.gxl.plancore.task.domain.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.valueobject.RepeatType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * 重复任务匹配器
 * 
 * 用于判断一个重复模板任务在某个日期是否应该显示
 */
public class RepeatTaskMatcher {

    /**
     * 判断模板任务在指定日期是否匹配
     * 
     * @param template  重复模板任务
     * @param queryDate 查询日期
     * @return true 表示该模板在查询日期应该显示
     */
    public static boolean matches(Task template, LocalDate queryDate) {
        // 非重复任务不匹配
        if (template.getRepeatType() == RepeatType.NONE) {
            return false;
        }

        // 查询日期早于模板起始日期，不匹配
        LocalDate startDate = template.getDate();
        if (queryDate.isBefore(startDate)) {
            return false;
        }

        // 查询日期超过重复结束日期，不匹配
        LocalDate endDate = template.getRepeatEndDate();
        if (endDate != null && queryDate.isAfter(endDate)) {
            return false;
        }

        // 根据重复类型判断
        RepeatType repeatType = template.getRepeatType();

        if (repeatType == RepeatType.DAILY) {
            // 每日重复：起始日期之后的每一天都匹配
            return true;
        }

        if (repeatType == RepeatType.WEEKLY) {
            return matchesWeekly(template, queryDate);
        }

        if (repeatType == RepeatType.MONTHLY) {
            return matchesMonthly(template, queryDate);
        }

        return false;
    }

    /**
     * 判断每周重复是否匹配
     * 
     * 配置格式：{"weekdays": [1, 3, 5]} 表示周一、周三、周五
     * 1-7 分别表示周一到周日
     */
    private static boolean matchesWeekly(Task template, LocalDate queryDate) {
        String configJson = template.getRepeatConfig();
        if (configJson == null || configJson.isEmpty()) {
            return false;
        }

        JSONObject config = JSON.parseObject(configJson);
        Object weekdaysObj = config.get("weekdays");
        if (!(weekdaysObj instanceof List)) {
            return false;
        }
        
        List<?> weekdaysList = (List<?>) weekdaysObj;
        if (weekdaysList.isEmpty()) {
            return false;
        }

        // 获取查询日期是星期几（1=周一，7=周日）
        DayOfWeek dayOfWeek = queryDate.getDayOfWeek();
        int dayValue = dayOfWeek.getValue(); // 1-7

        // 支持多种类型的数组元素
        for (Object item : weekdaysList) {
            int weekday;
            if (item instanceof Number) {
                weekday = ((Number) item).intValue();
            } else if (item instanceof String) {
                try {
                    weekday = Integer.parseInt((String) item);
                } catch (NumberFormatException e) {
                    continue;
                }
            } else {
                continue;
            }
            if (weekday == dayValue) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 判断每月重复是否匹配
     * 
     * 配置格式：{"dayOfMonth": 15} 表示每月 15 号
     */
    private static boolean matchesMonthly(Task template, LocalDate queryDate) {
        String configJson = template.getRepeatConfig();
        if (configJson == null || configJson.isEmpty()) {
            return false;
        }

        JSONObject config = JSON.parseObject(configJson);
        
        // 支持多种类型的 dayOfMonth 值
        Object dayOfMonthObj = config.get("dayOfMonth");
        if (dayOfMonthObj == null) {
            return false;
        }
        
        int dayOfMonth;
        if (dayOfMonthObj instanceof Number) {
            dayOfMonth = ((Number) dayOfMonthObj).intValue();
        } else if (dayOfMonthObj instanceof String) {
            try {
                dayOfMonth = Integer.parseInt((String) dayOfMonthObj);
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;
        }
        
        // 校验范围
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            return false;
        }

        // 获取查询日期是几号
        int queryDayOfMonth = queryDate.getDayOfMonth();

        // 处理月末情况：如果配置的日期大于当月天数，则月末最后一天匹配
        int lastDayOfMonth = queryDate.lengthOfMonth();
        if (dayOfMonth > lastDayOfMonth) {
            return queryDayOfMonth == lastDayOfMonth;
        }

        return queryDayOfMonth == dayOfMonth;
    }
}
