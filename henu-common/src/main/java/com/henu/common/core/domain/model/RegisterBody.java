package com.henu.common.core.domain.model;

/**
 * 用户注册对象
 * 
 * @author henu
 */
public class RegisterBody extends LoginBody
{
    /** 学校ID */
    private Long deptId;

    /** 年级ID */
    private Long postId;

    /** 角色ID (身份) */
    private Long roleId;

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
