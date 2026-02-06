```
用户点"开始专注"
    │
    ▼
focus_session INSERT（status=RUNNING）
    │
    │  ... 专注进行中 ...
    │
用户点"结束专注"
    │
    ▼
focus_session UPDATE（status=COMPLETED, 计算 counted/countedSeconds）
    │
    ├── counted = true（自然结束 或 手动结束且 >= 50%）
    │       │
    │       ▼
    │   user_focus_stats 累加 totalSeconds + sessionCount
    │   （首次则 INSERT，否则 UPDATE）
    │
    └── counted = false（手动结束且 < 50%）
            │
            ▼
        user_focus_stats 不操作
```