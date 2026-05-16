package com.henu.settlement.controller;

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
import com.henu.settlement.domain.BizCreatorSettlement;
import com.henu.settlement.service.IBizCreatorSettlementService;
import com.henu.common.utils.poi.ExcelUtil;
import com.henu.common.core.page.TableDataInfo;

import com.henu.common.utils.SecurityUtils;
import com.henu.common.utils.DateUtils;

/**
 * 创作者收益结算流水Controller
 * 
 * @author henu
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/settlement/settlement")
public class BizCreatorSettlementController extends BaseController
{
    @Autowired
    private IBizCreatorSettlementService bizCreatorSettlementService;

    /**
     * 获取当前创作者的结算流水列表
     */
    @GetMapping("/user/list")
    public TableDataInfo userList(BizCreatorSettlement bizCreatorSettlement)
    {
        startPage();
        bizCreatorSettlement.setCreatorId(SecurityUtils.getUserId());
        List<BizCreatorSettlement> list = bizCreatorSettlementService.selectBizCreatorSettlementList(bizCreatorSettlement);
        return getDataTable(list);
    }

    /**
     * 一键结算：将当前创作者所有"待结算"流水标记为"已结算"
     */
    @PostMapping("/user/settle")
    public AjaxResult settleAll()
    {
        Long userId = SecurityUtils.getUserId();
        BizCreatorSettlement query = new BizCreatorSettlement();
        query.setCreatorId(userId);
        query.setSettlementStatus("0");
        List<BizCreatorSettlement> pendingList = bizCreatorSettlementService.selectBizCreatorSettlementList(query);
        if (pendingList == null || pendingList.isEmpty()) {
            return error("没有待结算的流水");
        }
        for (BizCreatorSettlement s : pendingList) {
            s.setSettlementStatus("1");
            s.setSettlementTime(DateUtils.getNowDate());
            bizCreatorSettlementService.updateBizCreatorSettlement(s);
        }
        return success("成功结算 " + pendingList.size() + " 笔收益");
    }

    /**
     * 查询创作者收益结算流水列表
     */
    @PreAuthorize("@ss.hasPermi('settlement:settlement:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCreatorSettlement bizCreatorSettlement)
    {
        startPage();
        List<BizCreatorSettlement> list = bizCreatorSettlementService.selectBizCreatorSettlementList(bizCreatorSettlement);
        return getDataTable(list);
    }

    /**
     * 导出创作者收益结算流水列表
     */
    @PreAuthorize("@ss.hasPermi('settlement:settlement:export')")
    @Log(title = "创作者收益结算流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizCreatorSettlement bizCreatorSettlement)
    {
        List<BizCreatorSettlement> list = bizCreatorSettlementService.selectBizCreatorSettlementList(bizCreatorSettlement);
        ExcelUtil<BizCreatorSettlement> util = new ExcelUtil<BizCreatorSettlement>(BizCreatorSettlement.class);
        util.exportExcel(response, list, "创作者收益结算流水数据");
    }

    /**
     * 获取创作者收益结算流水详细信息
     */
    @PreAuthorize("@ss.hasPermi('settlement:settlement:query')")
    @GetMapping(value = "/{settlementId}")
    public AjaxResult getInfo(@PathVariable("settlementId") Long settlementId)
    {
        return success(bizCreatorSettlementService.selectBizCreatorSettlementBySettlementId(settlementId));
    }

    /**
     * 新增创作者收益结算流水
     */
    @PreAuthorize("@ss.hasPermi('settlement:settlement:add')")
    @Log(title = "创作者收益结算流水", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCreatorSettlement bizCreatorSettlement)
    {
        return toAjax(bizCreatorSettlementService.insertBizCreatorSettlement(bizCreatorSettlement));
    }

    /**
     * 修改创作者收益结算流水
     */
    @PreAuthorize("@ss.hasPermi('settlement:settlement:edit')")
    @Log(title = "创作者收益结算流水", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCreatorSettlement bizCreatorSettlement)
    {
        return toAjax(bizCreatorSettlementService.updateBizCreatorSettlement(bizCreatorSettlement));
    }

    /**
     * 删除创作者收益结算流水
     */
    @PreAuthorize("@ss.hasPermi('settlement:settlement:remove')")
    @Log(title = "创作者收益结算流水", businessType = BusinessType.DELETE)
	@DeleteMapping("/{settlementIds}")
    public AjaxResult remove(@PathVariable Long[] settlementIds)
    {
        return toAjax(bizCreatorSettlementService.deleteBizCreatorSettlementBySettlementIds(settlementIds));
    }
}
