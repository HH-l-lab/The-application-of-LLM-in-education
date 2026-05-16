package com.henu.web.controller.experiment;

import com.henu.common.core.controller.BaseController;
import com.henu.common.core.domain.AjaxResult;
import com.henu.common.utils.FFmpegUtils;
import com.henu.common.utils.ai.DeepSeekClient;
import com.henu.common.utils.ai.BaiduAsrClient;
import com.henu.common.annotation.Anonymous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import com.henu.extraction.service.IBizVideoExtractionService;
import com.henu.extraction.domain.BizVideoExtraction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Api("视频上传及AI转写预处理接口")
@RestController
@RequestMapping("/experiment/video")
public class VideoUploadController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(VideoUploadController.class);

    // 简单的线程池用于异步处理视频提取任务，生产环境建议使用 Spring 的 ThreadPoolTaskExecutor
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    private BaiduAsrClient baiduAsrClient;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private IBizVideoExtractionService bizVideoExtractionService;

    /**
     * 上传视频并触发AI总结与考点提取
     */
    @ApiOperation("上传视频并异步提取知识")
    @Anonymous
    @PostMapping(value = "/uploadAndExtract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult uploadAndExtract(@RequestPart("file") MultipartFile file,
            @RequestParam("courseId") Long courseId) {
        if (file.isEmpty()) {
            return AjaxResult.error("上传的视频文件不能为空");
        }
        try {
            // 1. 保存上传的视频文件到临时目录或本地存储
            String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "henu-video";
            File dir = new File(tempDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return AjaxResult.error("无法创建视频临时目录");
            }

            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".mp4";
            String videoPath = tempDir + File.separator + UUID.randomUUID().toString() + ext;
            String audioPath = tempDir + File.separator + UUID.randomUUID().toString() + ".wav";

            file.transferTo(new File(videoPath));
            log.info("视频已上传至本地: {}", videoPath);

            // 2. 扔到异步线程里进行深度处理 FFmpeg -> ASR -> DeepSeek
            executorService.submit(() -> processVideoExtraction(videoPath, audioPath, courseId));

            return AjaxResult.success("视频上传成功，后台AI知识提取正在进行中...");
        } catch (Exception e) {
            log.error("上传视频异常", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    private void processVideoExtraction(String videoPath, String audioPath, Long courseId) {
        log.info("开始处理课程ID: [{}] 的视频转写与知识提取", courseId);
        try {
            // Step 1: 提取音频并分块
            String audioDir = audioPath.replace(".wav", "_chunks");
            boolean extracted = FFmpegUtils.extractAudio(videoPath, audioDir, "chunk");
            if (!extracted) {
                log.error("视频 [{}] 提取音频切片失败", videoPath);
                // TODO: 可以在此处更新数据库表 biz_video_extraction 的状态为 失败
                return;
            }

            // Step 2: 百度智能云 ASR 语音合并转文本
            String asrText = baiduAsrClient.transcribeAudioChunks(audioDir);
            if (asrText == null || asrText.isEmpty()) {
                log.error("音频 [{}] ASR 转写失败", audioPath);
                return;
            }
            log.info("ASR 转写成功，文本长度: {}", asrText.length());

            // Step 3: DeepSeek 大模型提炼主旨
            String systemPrompt = "你是一个优秀的实验教学系统课程提炼专家，请根据提供的课程语音转写文字，总结出该实验视频的主旨、核心理论及容易出错的地方。分点作答。";
            String aiSummary = deepSeekClient.generateText(systemPrompt, asrText);
            log.info("大模型生成总结成功:\n{}", aiSummary);

            // Step 4: 将结果入库
            BizVideoExtraction extraction = new BizVideoExtraction();
            extraction.setCourseId(courseId);
            extraction.setAsrText(asrText);
            extraction.setAiSummary(aiSummary);
            extraction.setExtractionStatus("1"); // 1成功
            bizVideoExtractionService.insertBizVideoExtraction(extraction);

            log.info("课程ID: [{}] 的AI知识提取流程全部跑通！", courseId);

        } catch (Throwable e) {
            log.error("AI提取视频知识异常", e);
            BizVideoExtraction failRecord = new BizVideoExtraction();
            failRecord.setCourseId(courseId);
            failRecord.setExtractionStatus("2"); // 2失败
            failRecord.setAiSummary("提取失败: " + e.getMessage());
            bizVideoExtractionService.insertBizVideoExtraction(failRecord);
        } finally {
            // 简单清理临时文件
            new File(videoPath).delete();
            new File(audioPath).delete();

            // 清理切片目录
            String audioDir = audioPath.replace(".wav", "_chunks");
            File dir = new File(audioDir);
            if (dir.exists()) {
                File[] chunks = dir.listFiles();
                if (chunks != null) {
                    for (File f : chunks)
                        f.delete();
                }
                dir.delete();
            }
        }
    }
}
