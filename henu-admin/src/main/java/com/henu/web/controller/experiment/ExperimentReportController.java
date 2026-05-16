package com.henu.web.controller.experiment;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.core.domain.entity.SysUser;
import com.henu.common.utils.DateUtils;
import com.henu.common.utils.SecurityUtils;
import com.henu.common.utils.StringUtils;
import com.henu.course.domain.BizCourse;
import com.henu.course.service.IBizCourseService;
import com.henu.record.domain.BizExperimentRecord;
import com.henu.record.service.IBizExperimentRecordService;
import com.henu.system.service.ISysUserService;
import org.apache.poi.xwpf.usermodel.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Api(tags = "实验报告生成接口")
@RestController
@RequestMapping("/experiment/report")
public class ExperimentReportController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ExperimentReportController.class);

    @Autowired
    private IBizExperimentRecordService bizExperimentRecordService;

    @Autowired
    private IBizCourseService bizCourseService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 提交实验数据并生成记录
     */
    @ApiOperation("提交实验数据并返回记录ID")
    @PostMapping("/submit")
    public AjaxResult submitExperimentRecord(@RequestBody BizExperimentRecord record) {
        try {
            Long userId = SecurityUtils.getUserId();
            record.setStudentId(userId);

            if (record.getScore() == null) {
                // 优先从 AI 分析文本中提取真实评分
                long extractedScore = 85L; // 兜底默认
                if (StringUtils.isNotEmpty(record.getAiAnalysis())) {
                    java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("评估得分[：:]\\s*(\\d{1,3})")
                        .matcher(record.getAiAnalysis());
                    if (m.find()) {
                        try {
                            extractedScore = Long.parseLong(m.group(1));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                record.setScore(extractedScore);
            }
            if (StringUtils.isEmpty(record.getStatus())) {
                record.setStatus("1"); // 1为已完成
            }

            record.setCreateTime(DateUtils.getNowDate());
            bizExperimentRecordService.insertBizExperimentRecord(record);

            return AjaxResult.success("提交成功", record.getRecordId());
        } catch (Exception e) {
            log.error("提交实验数据异常", e);
            return AjaxResult.error("提交实验数据失败");
        }
    }

    /**
     * 生成实验报告 (Word/PDF)。
     * 动态生成包含 JSON 操作步骤、打分和AI评价的 Word 报告。
     *
     * @param recordId 学生实验记录ID
     * @param response HTTP 响应流
     */
    @ApiOperation("动态生成Word实验报告")
    @RequestMapping(value = "/generate/{recordId}", method = { RequestMethod.GET, RequestMethod.POST })
    public void generateReport(@ApiParam("实验记录ID") @PathVariable("recordId") Long recordId,
            HttpServletResponse response) {
        log.info("收到生成实验报告的请求, recordId: {}", recordId);

        try {
            // 1. 查询实验记录表 `biz_experiment_record`
            BizExperimentRecord record = bizExperimentRecordService.selectBizExperimentRecordByRecordId(recordId);
            if (record == null) {
                produceErrorRes(response, "找不到关联的实验记录");
                return;
            }

            // 获取相应的实验课程名称与学生昵称
            BizCourse course = bizCourseService.selectBizCourseByCourseId(record.getCourseId());
            String courseTitle = (course != null) ? course.getCourseTitle() : "未知实验";

            SysUser student = sysUserService.selectUserById(record.getStudentId());
            String studentName = (student != null) ? student.getNickName() : "未知学生";

            // 2. 创建一个空的 Word XWPFDocument 文档底板
            try (XWPFDocument document = new XWPFDocument()) {

                // 标题
                XWPFParagraph titleParagraph = document.createParagraph();
                titleParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun titleRun = titleParagraph.createRun();
                titleRun.setText(courseTitle + " - 实验告与分析");
                titleRun.setBold(true);
                titleRun.setFontSize(18);
                titleRun.addCarriageReturn(); // 换行

                // ==== 新增：先尝试从 AI 回复中提取真实的分数 ====
                String analysis = StringUtils.isNotEmpty(record.getAiAnalysis()) ? record.getAiAnalysis() : "暂无分析建议。";
                String finalScoreStr = (record.getScore() != null ? record.getScore().toString() : "未评分");

                java.util.regex.Matcher m = java.util.regex.Pattern.compile("评估得分[：:]\\s*(\\d{1,3})").matcher(analysis);
                if (m.find()) {
                    finalScoreStr = m.group(1);
                    // 清理掉回复末尾的打分标记，不让它在 Word 正文中显得突兀
                    analysis = analysis.replaceAll("评估得分[：:]\\s*\\d{1,3}分?", "").trim();
                }

                // 基本信息段落
                XWPFParagraph infoParagraph = document.createParagraph();
                XWPFRun infoRun = infoParagraph.createRun();
                infoRun.setText("学生姓名：" + studentName);
                infoRun.addCarriageReturn();
                infoRun.setText("评估得分：" + finalScoreStr + " 分");
                infoRun.addCarriageReturn();
                infoRun.addCarriageReturn();

                // 解析学生的实验数据 (JSON)
                if (StringUtils.isNotEmpty(record.getExperimentData())) {
                    XWPFParagraph dataTitlePara = document.createParagraph();
                    XWPFRun dataTitleRun = dataTitlePara.createRun();
                    dataTitleRun.setBold(true);
                    dataTitleRun.setFontSize(14);
                    dataTitleRun.setText("一、 实验原始测量数据：");
                    dataTitleRun.addCarriageReturn();

                    try {
                        JSONObject jsonData = JSON.parseObject(record.getExperimentData());

                        // 创建一个两列的漂亮表格
                        XWPFTable table = document.createTable(jsonData.size() + 1, 2);
                        // 设置表格居中和宽度约束等基础样式 (如果需要可进一步拓展)
                        table.setTableAlignment(TableRowAlign.CENTER);

                        // 表头
                        XWPFTableRow headerRow = table.getRow(0);
                        headerRow.getCell(0).setText("实验数据维度");
                        headerRow.getCell(1).setText("实际录入/测量值");

                        // 遍历 JSON 键值对灌入表格
                        int rowIndex = 1;
                        for (String key : jsonData.keySet()) {
                            XWPFTableRow row = table.getRow(rowIndex++);
                            row.getCell(0).setText(key);
                            row.getCell(1).setText(jsonData.getString(key));
                        }
                    } catch (Exception e) {
                        XWPFParagraph dataPara = document.createParagraph();
                        XWPFRun dataRun = dataPara.createRun();
                        dataRun.setText("原始JSON格式数据: " + record.getExperimentData());
                        dataRun.addCarriageReturn();
                    }

                    // 添加一个空行与下一段落隔开
                    document.createParagraph().createRun().addCarriageReturn();
                }

                // AI 大模型诊断与预检分析反馈
                XWPFParagraph aiTitlePara = document.createParagraph();
                XWPFRun aiTitleRun = aiTitlePara.createRun();
                aiTitleRun.setBold(true);
                aiTitleRun.setFontSize(14);
                aiTitleRun.setText("二、 AI 助教诊断与实验分析：");
                aiTitleRun.addCarriageReturn();

                XWPFParagraph aiPara = document.createParagraph();
                XWPFRun aiRun = aiPara.createRun();

                // 简单的 Markdown 去除与排版清洗
                analysis = analysis.replace("**", "") // 去除粗体
                        .replace("### ", "") // 去除三级标题
                        .replace("## ", "") // 去除二级标题
                        .replace("# ", "") // 去除一级标题
                        .replaceAll("\\*([^*]+)\\*", "$1") // 去除单星号斜体或列表
                        .replace("```json", "") // 去除代码块
                        .replace("```", "")
                        // 清洗 LaTeX 符号保证 Word 导出不会乱码错位
                        .replace("\\[", "")
                        .replace("\\]", "")
                        .replace("\\(", "")
                        .replace("\\)", "")
                        .replace("\\frac", " / ")
                        .replace("\\Delta", "Δ")
                        .replace("\\text", "")
                        .replace("\\", ""); // 去除所有反斜杠修饰符

                String[] lines = analysis.split("\n");
                for (String line : lines) {
                    if (StringUtils.isEmpty(line.trim()))
                        continue;
                    aiRun.setText(line.trim());
                    aiRun.addCarriageReturn();
                }

                // 3. 将 Word 字节流写入 HTTP 响应体下载
                String filename = URLEncoder.encode(studentName + "_" + courseTitle + "实验报告.docx",
                        StandardCharsets.UTF_8.toString());
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);

                try (OutputStream os = response.getOutputStream()) {
                    document.write(os);
                    os.flush();
                }
            }

            log.info("成功为 recordId: {} 动态生成并下发了 Word 实验报告!", recordId);

        } catch (Exception e) {
            log.error("生成实验报告出现异常", e);
            produceErrorRes(response, "服务器开小差了，报告生成失败！");
        }
    }

    private void produceErrorRes(HttpServletResponse response, String msg) {
        try {
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().write(msg);
        } catch (Exception ignore) {
        }
    }
}
