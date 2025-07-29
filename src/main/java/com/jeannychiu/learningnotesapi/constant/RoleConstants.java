package com.jeannychiu.learningnotesapi.constant;

/**
 * 角色常數定義
 *
 * 統一管理系統中使用的角色字串常數，避免重複和拼寫錯誤。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
public final class RoleConstants {
    
    /**
     * 一般使用者角色
     */
    public static final String USER = "USER";
    
    /**
     * 管理員角色
     */
    public static final String ADMIN = "ADMIN";
    
    /**
     * Spring Security 角色前綴 - 一般使用者
     */
    public static final String ROLE_USER = "ROLE_USER";
    
    /**
     * Spring Security 角色前綴 - 管理員
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    // 防止實例化
    private RoleConstants() {
        throw new UnsupportedOperationException("這是一個工具類，不應該被實例化");
    }
}