package com.easypan.utils;

import com.easypan.entity.constants.Constants;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

public class FFmpegUtils {
    private static final Logger logger = LoggerFactory.getLogger(FFmpegUtils.class);


    /**
    * 创建缩略图
    */
    public static Boolean createThumbnail(File file, int thumbnailWidth, File targetFile, Boolean delSource) {
        try {
            BufferedImage src = ImageIO.read(file);
            int sourceW = src.getWidth();
            int sourceH = src.getHeight();

            if (sourceW <= thumbnailWidth) {
                return false;
            }

            // 计算缩放高度，保持比例
            int targetHeight = (thumbnailWidth * sourceH) / sourceW;
            BufferedImage scaled = new BufferedImage(thumbnailWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.drawImage(src, 0, 0, thumbnailWidth, targetHeight, null);
            g2d.dispose();

            ImageIO.write(scaled, "jpg", targetFile);

            if (delSource) {
                Files.deleteIfExists(file.toPath());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 压缩图片到指定宽度
     */
    public static void compressImageWidthPercentage(File sourceFile, BigDecimal widthPercentage, File targetFile) {
        try {
            BufferedImage src = ImageIO.read(sourceFile);
            int newWidth = widthPercentage.multiply(BigDecimal.valueOf(src.getWidth())).intValue();
            int newHeight = (newWidth * src.getHeight()) / src.getWidth();

            BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.drawImage(src, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            ImageIO.write(scaled, "jpg", targetFile);

            Files.deleteIfExists(sourceFile.toPath());
        } catch (Exception e) {
            logger.error("压缩图片失败", e);
        }
    }


    /**
     * 生成视频封面
     */
    public static void createCoverFromVideo(File sourceFile, Integer width, File targetFile) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(sourceFile)) {
            grabber.start();
            Frame frame = grabber.grabImage(); // 抓第一帧

            if (frame != null) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage image = converter.convert(frame);

                int originalWidth = image.getWidth();
                int originalHeight = image.getHeight();
                int newHeight = (width * originalHeight) / originalWidth;

                BufferedImage scaled = new BufferedImage(width, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaled.createGraphics();
                g2d.drawImage(image, 0, 0, width, newHeight, null);
                g2d.dispose();

                ImageIO.write(scaled, "jpg", targetFile);
            }

            grabber.stop();
        } catch (Exception e) {
            logger.error("生成视频封面失败", e);
        }
    }

    /**
     * 切割视频
     * 直接对 MP4 文件进行切片并生成 .m3u8 + .ts 分片，不再需要中间的 index.ts 文件。
     */
    public static void cutFile4Video(String fileId, String videoFilePath) {
        // 创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }

        // 合并后的命令：直接从 MP4 切片
        final String CMD_CUT_MP4_DIRECT =
                "ffmpeg -y -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        String m3u8Path = tsFolder.getPath() + "/" + Constants.M3U8_NAME;
        String cmd = String.format(CMD_CUT_MP4_DIRECT, videoFilePath, m3u8Path, tsFolder.getPath(), fileId);

        ProcessUtils.executeCommand(cmd, false);
    }

//    /**
//     * 将 MP4 文件转为 TS 格式（h264_mp4 to annexb）
//     */
//    public static void convertMp4ToTs(String inputPath, String outputTsPath) throws Exception {
//        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
//             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputTsPath, 0)) {
//
//            grabber.start();
//
//            recorder.setFormat("mpegts");
//            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//
//            recorder.setFrameRate(grabber.getFrameRate());
//            recorder.setSampleRate(grabber.getSampleRate());
//            recorder.setAudioChannels(grabber.getAudioChannels());
//            recorder.setVideoBitrate(grabber.getVideoBitrate());
//            recorder.setAudioBitrate(grabber.getAudioBitrate());
//
//            recorder.start();
//
//            Frame frame;
//            while ((frame = grabber.grab()) != null) {
//                recorder.record(frame);
//            }
//
//            recorder.stop();
//            grabber.stop();
//        }
//    }
//
//    /**
//     * 将 ts 文件分片成若干 .ts 并生成 .m3u8（30 秒一段）
//     * 注意：JavaCV 不支持 m3u8 分片录制，这一步仍需要调用 FFmpeg 命令行
//     */
//    public static void segmentTsToM3u8(String inputTs, String m3u8Path, String outputFolder, String prefix) {
//        String cmd = String.format("ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts",
//                inputTs, m3u8Path, outputFolder, prefix);
//        ProcessUtils.executeCommand(cmd, false);
//    }


//    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
//        try {
//            BufferedImage src = ImageIO.read(sourceFile);
//            if (src == null) {
//                logger.error("读取源图片失败：" + sourceFile.getAbsolutePath());
//                return;
//            }
//
//            int originalWidth = src.getWidth();
//            int originalHeight = src.getHeight();
//            int targetHeight = (width * originalHeight) / originalWidth;
//
//            // 创建缩放后的 BufferedImage
//            BufferedImage scaled = new BufferedImage(width, targetHeight, BufferedImage.TYPE_INT_RGB);
//            Graphics2D g2d = scaled.createGraphics();
//            g2d.drawImage(src, 0, 0, width, targetHeight, null);
//            g2d.dispose();
//
//            // 输出格式根据文件后缀判断（默认 jpg）
//            String formatName = getImageFormat(targetFile.getName());
//            ImageIO.write(scaled, formatName, targetFile);
//
//            // 删除原图
//            if (delSource != null && delSource) {
//                Files.deleteIfExists(sourceFile.toPath());
//            }
//
//        } catch (Exception e) {
//            logger.error("压缩图片失败", e);
//        }
//    }

//    private static String getImageFormat(String filename) {
//        String lower = filename.toLowerCase();
//        if (lower.endsWith(".png")) return "png";
//        if (lower.endsWith(".bmp")) return "bmp";
//        if (lower.endsWith(".gif")) return "gif";
//        return "jpg"; // 默认
//    }

//    public static void main(String[] args) {
//        // 请根据你的实际路径修改下面的文件路径
//        File originalImage = new File("X:/测试文件/原图.jpg");
//        File compressedImage = new File("X:/测试文件/压缩图.jpg");
//        File scaledImage = new File("X:/测试文件/压缩比例图.jpg");
//        File thumbnailImage = new File("X:/测试文件/缩略图.jpg");
//        File videoFile = new File("X:/测试文件/爱因斯坦_demo.mp4");
//        File videoCover = new File("X:/测试文件/封面图.jpg");
//
//        // 1. 测试压缩图片到指定宽度
//        FFmpegUtils.compressImage(originalImage, 400, compressedImage, false);
//        System.out.println("压缩图片到400px宽度完成");
//
//        // 2. 测试按比例压缩图片（宽度压缩为原图的 60%）
//        FFmpegUtils.compressImageWidthPercentage(originalImage, new BigDecimal("0.6"), scaledImage);
//        System.out.println("按比例压缩图片完成");
//
//        // 3. 测试生成视频封面图
//        FFmpegUtils.createCoverFromVideo(videoFile, 500, videoCover);
//        System.out.println("视频封面生成完成");
//
//        // 4. 测试缩略图逻辑（判断原图是否大于设定宽度）
//        boolean created = FFmpegUtils.createThumbnailWithJavaCV(originalImage, 300, thumbnailImage, false);
//        System.out.println("缩略图是否生成: " + created);
//    }
}