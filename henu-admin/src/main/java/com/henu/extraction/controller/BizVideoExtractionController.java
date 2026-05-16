package com.henu.extraction.controller;

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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.henu.common.annotation.Log;
import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.enums.BusinessType;
import com.henu.extraction.domain.BizVideoExtraction;
import com.henu.extraction.service.IBizVideoExtractionService;
import com.henu.common.utils.poi.ExcelUtil;
import com.henu.common.core.page.TableDataInfo;
import com.henu.course.service.IBizCourseService;
import com.henu.course.domain.BizCourse;
import com.henu.common.utils.ai.BaiduAsrClient;
import com.henu.common.utils.ai.DeepSeekClient;
import com.henu.common.utils.FFmpegUtils;
import com.henu.common.config.HenuConfig;
import java.io.File;
import java.util.UUID;
import org.springframework.util.StringUtils;

/**
 * 视频AI知识提取结果Controller
 * 
 * @author henu
 * @date 2026-03-02
 */
@Api(tags = "视频AI知识提取结果接口")
@RestController
@RequestMapping({ "/extraction/extraction", "/experiment/extraction" })
public class BizVideoExtractionController extends BaseController {
    @Autowired
    private IBizVideoExtractionService bizVideoExtractionService;

    @Autowired
    private IBizCourseService bizCourseService;

    @Autowired
    private BaiduAsrClient baiduAsrClient;

    @Autowired
    private DeepSeekClient deepSeekClient;

    /**
     * 查询视频AI知识提取结果列表
     */
    @PreAuthorize("@ss.hasPermi('extraction:extraction:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizVideoExtraction bizVideoExtraction) {
        startPage();
        List<BizVideoExtraction> list = bizVideoExtractionService.selectBizVideoExtractionList(bizVideoExtraction);
        return getDataTable(list);
    }

    /**
     * 导出视频AI知识提取结果列表
     */
    @PreAuthorize("@ss.hasPermi('extraction:extraction:export')")
    @Log(title = "视频AI知识提取结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizVideoExtraction bizVideoExtraction) {
        List<BizVideoExtraction> list = bizVideoExtractionService.selectBizVideoExtractionList(bizVideoExtraction);
        ExcelUtil<BizVideoExtraction> util = new ExcelUtil<BizVideoExtraction>(BizVideoExtraction.class);
        util.exportExcel(response, list, "视频AI知识提取结果数据");
    }

    /**
     * 获取视频AI知识提取结果详细信息
     */
    @PreAuthorize("@ss.hasPermi('extraction:extraction:query')")
    @GetMapping(value = "/{extractionId}")
    public AjaxResult getInfo(@PathVariable("extractionId") Long extractionId) {
        return success(bizVideoExtractionService.selectBizVideoExtractionByExtractionId(extractionId));
    }

    /**
     * 根据课程ID获取最新的AI实验概要
     */
    @ApiOperation("获取视频的AI实验概要")
    @GetMapping(value = "/summary/{courseId}")
    public AjaxResult getSummaryByCourseId(@ApiParam("课程ID") @PathVariable("courseId") Long courseId) {
        BizVideoExtraction query = new BizVideoExtraction();
        query.setCourseId(courseId);
        List<BizVideoExtraction> list = bizVideoExtractionService.selectBizVideoExtractionList(query);

        if (list != null && !list.isEmpty()) {
            // 取最新的一条记录（假设ID倒序或者按处理时间最新）
            BizVideoExtraction latest = list.get(list.size() - 1);
            return success(latest);
        }
        return error("未找到相关AI概要");
    }

    /**
     * 手动触发指定视频的 ASR 与 AI 知识提取 (供审核员使用)
     */
    @Log(title = "手动触发视频AI提取", businessType = BusinessType.INSERT)
    @PostMapping("/process/{courseId}")
    public AjaxResult processExtraction(@PathVariable("courseId") Long courseId) {
        BizCourse course = bizCourseService.selectBizCourseByCourseId(courseId);
        if (course == null || !StringUtils.hasText(course.getVideoUrl())) {
            return AjaxResult.error("未找到相关视频信息");
        }

        // 修正映射路径到物理磁盘
        String videoUrl = course.getVideoUrl();
        String localVideoPath = HenuConfig.getProfile() + StringUtils.replace(videoUrl, "/profile", "");
        File videoFile = new File(localVideoPath);
        if (!videoFile.exists()) {
            return AjaxResult.error("源视频文件不存在，尝试路径：" + localVideoPath);
        }

        try {
            String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "henu-video";
            File dir = new File(tempDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return AjaxResult.error("无法创建临时目录");
            }
            String audioPath = tempDir + File.separator + UUID.randomUUID().toString() + ".wav";
            String audioDir = audioPath.replace(".wav", "_chunks");

            // 1. FFmpeg
            boolean extracted = FFmpegUtils.extractAudio(localVideoPath, audioDir, "chunk");
            if (!extracted) {
                return AjaxResult.error("视频提取音频失败");
            }

            // 2. ASR
            String asrText = baiduAsrClient.transcribeAudioChunks(audioDir);
            if (!StringUtils.hasText(asrText)) {
                return AjaxResult.error("百度语音识别转写失败，可能无声音或网络异常");
            }

            // 3. DeepSeek
            String systemPrompt = "你是一个优秀的实验教学系统课程提炼专家，请根据提供的课程语音转写文字，总结出该实验视频的主旨、核心理论及容易出错的地方。分点作答。";
            String aiSummary = deepSeekClient.generateText(systemPrompt, asrText);

            // 4. 入库历史逻辑
            BizVideoExtraction extraction = new BizVideoExtraction();
            extraction.setCourseId(courseId);
            extraction.setAsrText(asrText);
            extraction.setAiSummary(aiSummary);
            extraction.setExtractionStatus("1");
            bizVideoExtractionService.insertBizVideoExtraction(extraction);

            // 清理
            try {
                File d = new File(audioDir);
                if (d.exists()) {
                    File[] chunks = d.listFiles();
                    if (chunks != null) {
                        for (File ch : chunks)
                            ch.delete();
                    }
                    d.delete();
                }
            } catch (Exception ignored) {
            }

            return AjaxResult.success("解析成功", asrText);
        } catch (Exception e) {
            return AjaxResult.error("解析过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 新增视频AI知识提取结果
     */
    @PreAuthorize("@ss.hasPermi('extraction:extraction:add')")
    @Log(title = "视频AI知识提取结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizVideoExtraction bizVideoExtraction) {
        return toAjax(bizVideoExtractionService.insertBizVideoExtraction(bizVideoExtraction));
    }

    /**
     * 修改视频AI知识提取结果
     */
    @PreAuthorize("@ss.hasPermi('extraction:extraction:edit')")
    @Log(title = "视频AI知识提取结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizVideoExtraction bizVideoExtraction) {
        return toAjax(bizVideoExtractionService.updateBizVideoExtraction(bizVideoExtraction));
    }

    /**
     * 删除视频AI知识提取结果
     */
    @PreAuthorize("@ss.hasPermi('extraction:extraction:remove')")
    @Log(title = "视频AI知识提取结果", businessType = BusinessType.DELETE)
    @DeleteMapping("/{extractionIds}")
    public AjaxResult remove(@PathVariable Long[] extractionIds) {
        return toAjax(bizVideoExtractionService.deleteBizVideoExtractionByExtractionIds(extractionIds));
    }
}
