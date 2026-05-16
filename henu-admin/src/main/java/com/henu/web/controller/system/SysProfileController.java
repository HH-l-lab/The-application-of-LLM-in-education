package com.henu.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.henu.common.annotation.Log;
import com.henu.common.config.HenuConfig;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.core.domain.entity.SysUser;
import com.henu.common.core.domain.model.LoginUser;
import com.henu.common.enums.BusinessType;
import com.henu.common.utils.DateUtils;
import com.henu.common.utils.SecurityUtils;
import com.henu.common.utils.StringUtils;
import com.henu.common.utils.file.FileUploadUtils;
import com.henu.common.utils.file.FileUtils;
import com.henu.common.utils.file.MimeTypeUtils;
import com.henu.framework.web.service.TokenService;
import com.henu.system.service.ISysUserService;

/**
 * 个人信息 业务处理
 * 
 * @author henu
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private com.henu.order.service.IBizCourseOrderService courseOrderService;

    @Autowired
    private com.henu.record.service.IBizExperimentRecordService experimentRecordService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));

        // 获取该用户的已购课程数量
        com.henu.order.domain.BizCourseOrder orderQuery = new com.henu.order.domain.BizCourseOrder();
        orderQuery.setBuyerId(loginUser.getUserId());
        orderQuery.setPayStatus("1"); // 已支付
        java.util.List<?> orders = courseOrderService.selectBizCourseOrderList(orderQuery);
        ajax.put("purchasedCount", orders != null ? orders.size() : 0);

        // 获取该用户的实验总数
        // 获取该用户的实验总数
        com.henu.record.domain.BizExperimentRecord expQuery = new com.henu.record.domain.BizExperimentRecord();
        expQuery.setStudentId(loginUser.getUserId());
        java.util.List<?> experiments = experimentRecordService.selectBizExperimentRecordList(expQuery);
        ajax.put("experimentCount", experiments != null ? experiments.size() : 0);

        return ajax;
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getUser();
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        // 允许修改学校
        if (user.getDeptId() != null)
        {
            currentUser.setDeptId(user.getDeptId());
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser) > 0)
        {
            // 允许修改年级（岗位）
            if (user.getPostIds() != null && user.getPostIds().length > 0)
            {
                currentUser.setPostIds(user.getPostIds());
                // 先删除旧的岗位关联，再插入新的
                userService.updateUser(currentUser);
            }
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = getLoginUser();
        Long userId = loginUser.getUserId();
        SysUser user = userService.selectUserById(userId);
        String password = user.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userId, newPassword) > 0)
        {
            // 更新缓存用户密码&密码最后更新时间
            loginUser.getUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception
    {
        if (!file.isEmpty())
        {
            LoginUser loginUser = getLoginUser();
            String avatar = FileUploadUtils.upload(HenuConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION, true);
            if (userService.updateUserAvatar(loginUser.getUserId(), avatar))
            {
                String oldAvatar = loginUser.getUser().getAvatar();
                if (StringUtils.isNotEmpty(oldAvatar))
                {
                    FileUtils.deleteFile(HenuConfig.getProfile() + FileUtils.stripPrefix(oldAvatar));
                }
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
