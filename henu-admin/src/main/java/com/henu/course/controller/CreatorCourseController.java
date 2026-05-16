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
import com.henu.common.utils.SecurityUtils;

/**
 * 创作者资源上传中心Controller (Scheme 1)
 * 
 * @author henu
 * @date 2026-03-08
 */
@RestController
@RequestMapping("/creator/course")
public class CreatorCourseController extends BaseController {
    @Autowired
    private IBizCourseService bizCourseService;

    /**
     * 查询创作者的在线课程资源列表
     */
    @GetMapping("/list")
    public TableDataInfo list(BizCourse bizCourse) {
        startPage();
        // 核心隔离逻辑：强制查询条件仅限当前登录用
        bizCourse.setCreatorId(SecurityUtils.getUserId());
        List<BizCourse> list = bizCourseService.selectBizCourseList(bizCourse);
        return getDataTable(list);
    }

    /**
     * 导出在线课程资源列表
     */
    @Log(title = "创作者素材", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizCourse bizCourse) {
        bizCourse.setCreatorId(SecurityUtils.getUserId());
        List<BizCourse> list = bizCourseService.selectBizCourseList(bizCourse);
        ExcelUtil<BizCourse> util = new ExcelUtil<BizCourse>(BizCourse.class);
        util.exportExcel(response, list, "创作者资源数据");
    }

    /**
     * 获取详细信息
     */
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId) {
        return success(bizCourseService.selectBizCourseByCourseId(courseId));
    }

    /**
     * 新增上传素材
     */
    @Log(title = "创作者素材", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCourse bizCourse) {
        // 核心防篡改逻辑：锁死关键字段
        bizCourse.setCreatorId(SecurityUtils.getUserId()); // 绑定创作者
        bizCourse.setAuditStatus("0"); // 强制待审核
        bizCourse.setPlayCount(0L); // 重置播放量
        return toAjax(bizCourseService.insertBizCourse(bizCourse));
    }

    /**
     * 修改上传素材
     */
    @Log(title = "创作者素材", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCourse bizCourse) {
        // 修改时同样覆盖这些字段，防止越权
        bizCourse.setCreatorId(SecurityUtils.getUserId());
        bizCourse.setAuditStatus("0"); // 修改后需重新审核
        return toAjax(bizCourseService.updateBizCourse(bizCourse));
    }

    /**
     * 删除素材
     */
    @Log(title = "创作者素材", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds) {
        return toAjax(bizCourseService.deleteBizCourseByCourseIds(courseIds));
    }
}
