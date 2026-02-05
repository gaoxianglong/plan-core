package com.gxl.plancore.user.interfaces.dto;

/**
 * 会员权益响应 DTO
 */
public class EntitlementResponse {
    
    /**
     * 权益状态
     * NOT_ENTITLED: 未激活
     * FREE_TRIAL: 免费期
     * MEMBER_ACTIVE: 会员有效
     * EXPIRED: 已到期
     */
    private String status;
    
    /**
     * 免费期起算时间（ISO 8601 格式）
     */
    private String trialStartAt;
    
    /**
     * 会员到期时间（ISO 8601 格式）
     */
    private String expireAt;
    
    public EntitlementResponse() {
    }
    
    public EntitlementResponse(String status, String trialStartAt, String expireAt) {
        this.status = status;
        this.trialStartAt = trialStartAt;
        this.expireAt = expireAt;
    }
    
    // 创建免费期权益
    public static EntitlementResponse freeTrial(String trialStartAt, String expireAt) {
        return new EntitlementResponse("FREE_TRIAL", trialStartAt, expireAt);
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTrialStartAt() {
        return trialStartAt;
    }
    
    public void setTrialStartAt(String trialStartAt) {
        this.trialStartAt = trialStartAt;
    }
    
    public String getExpireAt() {
        return expireAt;
    }
    
    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }
}
