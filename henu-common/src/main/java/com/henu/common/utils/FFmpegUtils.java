package com.henu.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FFmpegUtils {
    private static final Logger log = LoggerFactory.getLogger(FFmpegUtils.class);

    /**
     * 提取视频中的音频并按 50 秒分割保存为多个 WAV 文件，以供百度 ASR 处理短语音限制
     * 
     * @param videoPath 输入的视频本地绝对路径
     * @param audioDir  存放分割后音频的目录绝对路径
     * @param prefix    输出文件前缀
     * @return 是否成功
     */
    public static boolean extractAudio(String videoPath, String audioDir, String prefix) {
        log.info("开始提取并分割视频音频 => 输入: {}, 输出目录: {}", videoPath, audioDir);
        File inputFile = new File(videoPath);
        if (!inputFile.exists()) {
            log.error("FFmpeg 提取错误: 视频文件不存在 -> {}", videoPath);
            return false;
        }

        File dir = new File(audioDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String outputPattern = new File(dir, prefix + "_%03d.wav").getAbsolutePath();

        // 构造 FFmpeg 命令，自动切分为 50 秒的小块，降低 Baidu ASR 大文件被 Reset 的风险
        String[] command = {
                "ffmpeg",
                "-y", // 覆盖输出文件
                "-i", videoPath, // 输入文件
                "-vn", // 禁用视频，只保留音频
                "-ar", "16000", // 采样率 16kHz
                "-ac", "1", // 单声道
                "-c:a", "pcm_s16le", // 编码器 (16bit pcm)
                "-f", "segment", // 分割格式
                "-segment_time", "50", // 每个分片最大 50 秒
                outputPattern // 输出文件模式
        };

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (InputStream is = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                while (reader.readLine() != null) {
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("FFmpeg 音频切分提取成功!");
                return true;
            } else {
                log.error("FFmpeg 执行失败, 退出码: {}", exitCode);
                return false;
            }

        } catch (Exception e) {
            log.error("FFmpeg 调用异常", e);
            return false;
        }
    }
}
