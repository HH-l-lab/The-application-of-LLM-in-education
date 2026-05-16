package com.henu.record.controller;

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
import com.henu.record.domain.BizExperimentRecord;
import com.henu.record.service.IBizExperimentRecordService;
import com.henu.common.utils.poi.ExcelUtil;
import com.henu.common.core.page.TableDataInfo;

import com.henu.common.utils.SecurityUtils;

/**
 * 学生实验过程记录Controller
 * 
 * @author henu
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/record/record")
public class BizExperimentRecordController extends BaseController
{
    @Autowired
    private IBizExperimentRecordService bizExperimentRecordService;

    /**
     * 获取当前登录用户的实验记录列表（我的实验）
     */
    @GetMapping("/user/list")
    public TableDataInfo userList(BizExperimentRecord bizExperimentRecord)
    {
        startPage();
        bizExperimentRecord.setStudentId(SecurityUtils.getUserId());
        List<BizExperimentRecord> list = bizExperimentRecordService.selectBizExperimentRecordList(bizExperimentRecord);
        return getDataTable(list);
    }

    /**
     * 查询学生实验过程记录列表
     */
    @PreAuthorize("@ss.hasPermi('record:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizExperimentRecord bizExperimentRecord)
    {
        startPage();
        List<BizExperimentRecord> list = bizExperimentRecordService.selectBizExperimentRecordList(bizExperimentRecord);
        return getDataTable(list);
    }

    /**
     * 导出学生实验过程记录列表
     */
    @PreAuthorize("@ss.hasPermi('record:record:export')")
    @Log(title = "学生实验过程记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizExperimentRecord bizExperimentRecord)
    {
        List<BizExperimentRecord> list = bizExperimentRecordService.selectBizExperimentRecordList(bizExperimentRecord);
        ExcelUtil<BizExperimentRecord> util = new ExcelUtil<BizExperimentRecord>(BizExperimentRecord.class);
        util.exportExcel(response, list, "学生实验过程记录数据");
    }

    /**
     * 获取学生实验过程记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('record:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(bizExperimentRecordService.selectBizExperimentRecordByRecordId(recordId));
    }

    /**
     * 新增学生实验过程记录
     */
    @PreAuthorize("@ss.hasPermi('record:record:add')")
    @Log(title = "学生实验过程记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizExperimentRecord bizExperimentRecord)
    {
        return toAjax(bizExperimentRecordService.insertBizExperimentRecord(bizExperimentRecord));
    }

    /**
     * 修改学生实验过程记录
     */
    @PreAuthorize("@ss.hasPermi('record:record:edit')")
    @Log(title = "学生实验过程记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizExperimentRecord bizExperimentRecord)
    {
        return toAjax(bizExperimentRecordService.updateBizExperimentRecord(bizExperimentRecord));
    }

    /**
     * 删除学生实验过程记录
     */
    @PreAuthorize("@ss.hasPermi('record:record:remove')")
    @Log(title = "学生实验过程记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(bizExperimentRecordService.deleteBizExperimentRecordByRecordIds(recordIds));
    }
}
