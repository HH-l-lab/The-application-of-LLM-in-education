package com.henu.course.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.henu.common.annotation.Log;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.enums.BusinessType;
import com.henu.course.domain.BizCourse;
import com.henu.course.service.IBizCourseService;
import com.henu.common.utils.poi.ExcelUtil;
import com.henu.common.core.page.TableDataInfo;
import com.henu.message.domain.BizUserMessage;
import com.henu.message.service.IBizUserMessageService;
import com.henu.web.controller.message.MessageWebSocket;
import com.henu.common.utils.DateUtils;

/**
 * 在线课程资源Controller
 * 
 * @author henu
 * @date 2026-03-06
 */
@RestController
@RequestMapping("/course/course")
public class BizCourseController extends BaseController {
    @Autowired
    private IBizCourseService bizCourseService;

    @Autowired
    private IBizUserMessageService bizUserMessageService;

    /**
     * 查询在线课程资源列表
     */
    // @PreAuthorize("@ss.hasPermi('course:course:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCourse bizCourse) {
        startPage();
        List<BizCourse> list = bizCourseService.selectBizCourseList(bizCourse);
        return getDataTable(list);
    }

    /**
     * 导出在线课程资源列表
     */
    @PreAuthorize("@ss.hasPermi('course:course:export')")
    @Log(title = "在线课程资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizCourse bizCourse) {
        List<BizCourse> list = bizCourseService.selectBizCourseList(bizCourse);
        ExcelUtil<BizCourse> util = new ExcelUtil<BizCourse>(BizCourse.class);
        util.exportExcel(response, list, "在线课程资源数据");
    }

    /**
     * 获取在线课程资源详细信息
     */
    // @PreAuthorize("@ss.hasPermi('course:course:query')")
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId) {
        return success(bizCourseService.selectBizCourseByCourseId(courseId));
    }

    /**
     * 新增在线课程资源
     */
    @PreAuthorize("@ss.hasPermi('course:course:add')")
    @Log(title = "在线课程资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCourse bizCourse) {
        return toAjax(bizCourseService.insertBizCourse(bizCourse));
    }

    /**
     * 修改在线课程资源
     */
    @PreAuthorize("@ss.hasPermi('course:course:edit')")
    @Log(title = "在线课程资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCourse bizCourse) {
        // 如果是审核操作，需要发送站内信
        if (bizCourse.getAuditStatus() != null) {
            BizCourse oldCourse = bizCourseService.selectBizCourseByCourseId(bizCourse.getCourseId());
            if (oldCourse != null && oldCourse.getCreatorId() != null) {
                String newStatus = bizCourse.getAuditStatus();
                // 状态发生变化才发消息
                if (!newStatus.equals(oldCourse.getAuditStatus())) {
                    if ("1".equals(newStatus) || "2".equals(newStatus)) {
                        BizUserMessage msg = new BizUserMessage();
                        msg.setUserId(oldCourse.getCreatorId());
                        msg.setType("1"); // 1代表审核提醒
                        msg.setIsRead("0");
                        msg.setRelatedId(bizCourse.getCourseId());
                        msg.setCreateTime(DateUtils.getNowDate());
                        msg.setCreateBy("system");

                        if ("1".equals(newStatus)) {
                            msg.setTitle("课程审核通过通知");
                            msg.setContent("恭喜！您上传的实验课程《" + oldCourse.getCourseTitle() + "》已通过管理员审核，现已正式上架！");
                        } else if ("2".equals(newStatus)) {
                            msg.setTitle("课程审核驳回通知");
                            String reason = bizCourse.getAuditMessage() != null ? bizCourse.getAuditMessage() : "无详细理由";
                            msg.setContent("很遗憾，您上传的实验课程《" + oldCourse.getCourseTitle() + "》未能通过审核。驳回理由：" + reason
                                    + "。请仔细修改后重新提交。");
                        }
                        bizUserMessageService.insertBizUserMessage(msg);
                        MessageWebSocket.sendMessageToUser(oldCourse.getCreatorId().toString(), "NEW_MESSAGE");
                    }
                }
            }
        }
        return toAjax(bizCourseService.updateBizCourse(bizCourse));
    }

    /**
     * 删除在线课程资源
     */
    @PreAuthorize("@ss.hasPermi('course:course:remove')")
    @Log(title = "在线课程资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds) {
        return toAjax(bizCourseService.deleteBizCourseByCourseIds(courseIds));
    }

    /**
     * 视频播放量+1（无需特殊权限，任何已登录用户均可触发）
     */
    @PutMapping("/play/{courseId}")
    public AjaxResult incrementPlayCount(@PathVariable Long courseId) {
        return toAjax(bizCourseService.incrementPlayCount(courseId));
    }
}
