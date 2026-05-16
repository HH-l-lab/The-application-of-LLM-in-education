package com.henu.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.henu.common.annotation.Anonymous;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.core.domain.entity.SysDept;
import com.henu.common.core.domain.entity.SysRole;
import com.henu.common.core.domain.model.RegisterBody;
import com.henu.common.utils.StringUtils;
import com.henu.framework.web.service.SysRegisterService;
import com.henu.system.domain.SysPost;
import com.henu.system.mapper.SysDeptMapper;
import com.henu.system.mapper.SysPostMapper;
import com.henu.system.mapper.SysRoleMapper;
import com.henu.system.service.ISysConfigService;

/**
 * 注册验证
 * 
 * @author henu
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        if ("ADMIN_PENDING".equals(msg))
        {
            return AjaxResult.success("注册成功！由于您选择的身份为管理员，账号将处于停用状态，请等待超级管理员审核激活。");
        }
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 获取注册页面下拉选项（匿名接口，直接走Mapper绕过DataScope）
     */
    @Anonymous
    @GetMapping("/register/options")
    public AjaxResult getRegisterOptions()
    {
        AjaxResult ajax = AjaxResult.success();
        // 学校列表 — 直接用 mapper 绕过 @DataScope，只保留叶子节点（实际学校）
        SysDept deptQuery = new SysDept();
        deptQuery.setStatus("0");
        List<SysDept> allDepts = deptMapper.selectDeptList(deptQuery);
        // 收集所有 parentId，只保留不是别人 parent 的节点（即叶子节点=真正的学校）
        java.util.Set<Long> parentIds = allDepts.stream()
                .map(SysDept::getParentId)
                .collect(java.util.stream.Collectors.toSet());
        List<SysDept> depts = allDepts.stream()
                .filter(d -> !parentIds.contains(d.getDeptId()))
                .collect(java.util.stream.Collectors.toList());
        ajax.put("depts", depts);
        // 年级列表 — selectPostAll 无 DataScope
        List<SysPost> posts = postMapper.selectPostAll();
        ajax.put("posts", posts);
        // 角色列表 — selectRoleList 过滤 del_flag='0' 和 status='0'，排除超级管理员 roleId=1
        SysRole roleQuery = new SysRole();
        roleQuery.setStatus("0");
        List<SysRole> roles = roleMapper.selectRoleList(roleQuery);
        roles.removeIf(r -> r.getRoleId().equals(1L));
        ajax.put("roles", roles);
        return ajax;
    }
}
