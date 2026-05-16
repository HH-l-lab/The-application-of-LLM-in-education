package com.henu.message.controller;

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
import com.henu.message.domain.BizUserMessage;
import com.henu.message.service.IBizUserMessageService;
import com.henu.common.utils.poi.ExcelUtil;
import com.henu.common.core.page.TableDataInfo;
import com.henu.common.utils.SecurityUtils;
import java.util.Date;

/**
 * 站内系统消息Controller
 * 
 * @author henu
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/message/message")
public class BizUserMessageController extends BaseController {
    @Autowired
    private IBizUserMessageService bizUserMessageService;

    /**
     * 获取当前登录用户的消息列表 (站内信组件专用)
     */
    @GetMapping("/user/list")
    public TableDataInfo userList(BizUserMessage bizUserMessage) {
        startPage();
        bizUserMessage.setUserId(SecurityUtils.getUserId());
        List<BizUserMessage> list = bizUserMessageService.selectBizUserMessageList(bizUserMessage);
        return getDataTable(list);
    }

    /**
     * 将消息标记为已读
     */
    @PostMapping("/user/read/{msgId}")
    public AjaxResult markAsRead(@PathVariable("msgId") Long msgId) {
        BizUserMessage msg = bizUserMessageService.selectBizUserMessageByMsgId(msgId);
        if (msg != null && msg.getUserId().equals(SecurityUtils.getUserId())) {
            msg.setIsRead("1");
            msg.setUpdateTime(new Date());
            return toAjax(bizUserMessageService.updateBizUserMessage(msg));
        }
        return error("消息不存在或无权操作");
    }

    /**
     * 查询站内系统消息列表
     */
    @PreAuthorize("@ss.hasPermi('message:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizUserMessage bizUserMessage) {
        startPage();
        List<BizUserMessage> list = bizUserMessageService.selectBizUserMessageList(bizUserMessage);
        return getDataTable(list);
    }

    /**
     * 导出站内系统消息列表
     */
    @PreAuthorize("@ss.hasPermi('message:message:export')")
    @Log(title = "站内系统消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizUserMessage bizUserMessage) {
        List<BizUserMessage> list = bizUserMessageService.selectBizUserMessageList(bizUserMessage);
        ExcelUtil<BizUserMessage> util = new ExcelUtil<BizUserMessage>(BizUserMessage.class);
        util.exportExcel(response, list, "站内系统消息数据");
    }

    /**
     * 获取站内系统消息详细信息
     */
    @PreAuthorize("@ss.hasPermi('message:message:query')")
    @GetMapping(value = "/{msgId}")
    public AjaxResult getInfo(@PathVariable("msgId") Long msgId) {
        return success(bizUserMessageService.selectBizUserMessageByMsgId(msgId));
    }

    /**
     * 新增站内系统消息
     */
    @PreAuthorize("@ss.hasPermi('message:message:add')")
    @Log(title = "站内系统消息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizUserMessage bizUserMessage) {
        return toAjax(bizUserMessageService.insertBizUserMessage(bizUserMessage));
    }

    /**
     * 修改站内系统消息
     */
    @PreAuthorize("@ss.hasPermi('message:message:edit')")
    @Log(title = "站内系统消息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizUserMessage bizUserMessage) {
        return toAjax(bizUserMessageService.updateBizUserMessage(bizUserMessage));
    }

    /**
     * 删除站内系统消息
     */
    @PreAuthorize("@ss.hasPermi('message:message:remove')")
    @Log(title = "站内系统消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{msgIds}")
    public AjaxResult remove(@PathVariable Long[] msgIds) {
        return toAjax(bizUserMessageService.deleteBizUserMessageByMsgIds(msgIds));
    }
}
