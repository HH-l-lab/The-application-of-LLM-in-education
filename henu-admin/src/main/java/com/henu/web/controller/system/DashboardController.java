package com.henu.web.controller.system;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.utils.SecurityUtils;
import com.henu.course.service.IBizCourseService;
import com.henu.web.mapper.DashboardMapper;
import com.henu.system.domain.SysNotice;
import com.henu.system.service.ISysNoticeService;
import com.henu.common.core.domain.entity.SysDictData;
import com.henu.system.service.ISysDictTypeService;
import com.henu.system.service.ISysConfigService;
import com.henu.system.domain.SysConfig;
import java.util.List;

/**
 * 仪表盘统计Controller
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private IBizCourseService bizCourseService;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private ISysConfigService configService;

    @GetMapping("/stats")
    public AjaxResult getStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("courseCount", bizCourseService.selectCourseCount());
        data.put("userCount", dashboardMapper.selectUserCount());
        data.put("recordCount", dashboardMapper.selectRecordCount());
        data.put("orderCount", dashboardMapper.selectOrderCount());
        return AjaxResult.success(data);
    }

    @GetMapping("/creator")
    public AjaxResult getCreatorStats() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("totalPlayCount", bizCourseService.selectPlayCountByCreatorId(userId));
        data.put("courseCount", bizCourseService.selectCourseCountByCreatorId(userId));
        data.put("totalRevenue", dashboardMapper.selectRevenueByCreatorId(userId));
        data.put("pendingRevenue", dashboardMapper.selectPendingRevenueByCreatorId(userId));
        data.put("settledRevenue", dashboardMapper.selectSettledRevenueByCreatorId(userId));
        data.put("orderCount", dashboardMapper.selectOrderCountByCreatorId(userId));
        return AjaxResult.success(data);
    }

    @GetMapping("/notices")
    public AjaxResult getNotices() {
        SysNotice query = new SysNotice();
        query.setStatus("0");
        List<SysNotice> list = noticeService.selectNoticeList(query);
        if (list.size() > 5) {
            list = list.subList(0, 5);
        }
        return AjaxResult.success(list);
    }

    /**
     * 学情诊断 — 按学科+年级维度统计（当前用户 vs 全平台平均）
     * 使用字典编码查数据库，返回中文标签给前端
     */
    @GetMapping("/diagnostic")
    public AjaxResult getDiagnostic() {
        Long userId = SecurityUtils.getUserId();

        // 动态读取字典 code→label
        List<SysDictData> subjectDicts = dictTypeService.selectDictDataByType("sys_subject");
        List<SysDictData> gradeDicts = dictTypeService.selectDictDataByType("sys_grade");

        // 学科label → 允许的年级label列表
        Map<String, String[]> subjectGradeLabels = new java.util.LinkedHashMap<>();
        subjectGradeLabels.put("物理", new String[]{"初二", "初三", "高一", "高二", "高三"});
        subjectGradeLabels.put("化学", new String[]{"初三", "高一", "高二", "高三"});
        subjectGradeLabels.put("生物", new String[]{"初一", "初二", "初三", "高一", "高二", "高三"});

        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<String, String[]> entry : subjectGradeLabels.entrySet()) {
            String subLabel = entry.getKey();
            String subCode = findDictCode(subjectDicts, subLabel);
            if (subCode == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("subject", subLabel);
            java.util.List<Map<String, Object>> gradeList = new java.util.ArrayList<>();
            for (String gradeLabel : entry.getValue()) {
                String gradeCode = findDictCode(gradeDicts, gradeLabel);
                if (gradeCode == null) continue;
                Map<String, Object> g = new HashMap<>();
                g.put("grade", gradeLabel);
                g.put("userExp", dashboardMapper.selectUserExpBySubjectGrade(userId, subCode, gradeCode));
                g.put("userScore", dashboardMapper.selectUserScoreBySubjectGrade(userId, subCode, gradeCode));
                g.put("userCourse", dashboardMapper.selectUserCourseBySubjectGrade(userId, subCode, gradeCode));
                g.put("avgExp", dashboardMapper.selectAvgExpBySubjectGrade(subCode, gradeCode));
                g.put("avgScore", dashboardMapper.selectAvgScoreBySubjectGrade(subCode, gradeCode));
                g.put("avgCourse", dashboardMapper.selectAvgCourseBySubjectGrade(subCode, gradeCode));
                gradeList.add(g);
            }
            item.put("grades", gradeList);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /** 根据label查字典编码 */
    private String findDictCode(List<SysDictData> dicts, String label) {
        for (SysDictData d : dicts) {
            if (label.equals(d.getDictLabel())) {
                return d.getDictValue();
            }
        }
        return null;
    }

    /**
     * 获取当前分润比例（百分比整数，如 50）
     */
    @GetMapping("/shareRate")
    public AjaxResult getShareRate() {
        String val = configService.selectConfigByKey("sys.settlement.shareRate");
        int rate = 50; // 默认
        try {
            if (val != null && !val.isEmpty()) rate = Integer.parseInt(val);
        } catch (Exception ignored) {}
        return AjaxResult.success(rate);
    }

    /**
     * 管理员修改分润比例
     */
    @PutMapping("/shareRate")
    public AjaxResult updateShareRate(@RequestBody Map<String, Object> body) {
        // 仅管理员可操作
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()) && !SecurityUtils.hasRole("admin") && !SecurityUtils.hasRole("ladmin")) {
            return error("没有权限修改分润比例");
        }
        Object rateObj = body.get("shareRate");
        if (rateObj == null) return error("参数缺失");
        int rate;
        try {
            rate = Integer.parseInt(rateObj.toString());
        } catch (Exception e) {
            return error("分润比例必须为整数");
        }
        if (rate < 0 || rate > 100) return error("分润比例必须在 0~100 之间");

        // 尝试更新 sys_config 中的记录
        SysConfig query = new SysConfig();
        query.setConfigKey("sys.settlement.shareRate");
        List<SysConfig> list = configService.selectConfigList(query);
        if (list != null && !list.isEmpty()) {
            SysConfig config = list.get(0);
            config.setConfigValue(String.valueOf(rate));
            configService.updateConfig(config);
        } else {
            // 首次设置，新增记录
            SysConfig config = new SysConfig();
            config.setConfigName("创作者分润比例");
            config.setConfigKey("sys.settlement.shareRate");
            config.setConfigValue(String.valueOf(rate));
            config.setConfigType("Y");
            config.setRemark("创作者课程收益分润百分比，默认50");
            configService.insertConfig(config);
        }
        return success();
    }
}
