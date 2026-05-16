package com.henu.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.henu.common.constant.CacheConstants;
import com.henu.common.constant.Constants;
import com.henu.common.constant.UserConstants;
import com.henu.common.core.domain.entity.SysRole;
import com.henu.common.core.domain.entity.SysUser;
import com.henu.common.core.domain.model.RegisterBody;
import com.henu.common.core.redis.RedisCache;
import com.henu.common.exception.user.CaptchaException;
import com.henu.common.exception.user.CaptchaExpireException;
import com.henu.common.utils.DateUtils;
import com.henu.common.utils.MessageUtils;
import com.henu.common.utils.SecurityUtils;
import com.henu.common.utils.StringUtils;
import com.henu.framework.manager.AsyncManager;
import com.henu.framework.manager.factory.AsyncFactory;
import com.henu.system.mapper.SysRoleMapper;
import com.henu.system.service.ISysConfigService;
import com.henu.system.service.ISysUserService;

/**
 * 注册校验方法
 * 
 * @author henu
 */
@Component
public class SysRegisterService {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (!userService.checkUserNameUnique(sysUser)) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        } else {
            sysUser.setNickName(username);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));

            // 设置学校
            if (registerBody.getDeptId() != null) {
                sysUser.setDeptId(registerBody.getDeptId());
            }
            // 设置年级
            if (registerBody.getPostId() != null) {
                sysUser.setPostIds(new Long[] { registerBody.getPostId() });
            }

            // 判断是否选择了管理员角色（通过 roleKey 判断，而非硬编码 roleId）
            boolean isAdminRole = false;
            if (registerBody.getRoleId() != null) {
                sysUser.setRoleIds(new Long[] { registerBody.getRoleId() });
                SysRole selectedRole = roleMapper.selectRoleById(registerBody.getRoleId());
                if (selectedRole != null && "ladmin".equals(selectedRole.getRoleKey())) {
                    isAdminRole = true;
                    sysUser.setStatus("1"); // 停用，等待审核
                }
            }

            int rows = userService.insertUser(sysUser);
            if (rows <= 0) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER,
                        MessageUtils.message("user.register.success")));
                if (isAdminRole) {
                    msg = "ADMIN_PENDING";
                }
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
