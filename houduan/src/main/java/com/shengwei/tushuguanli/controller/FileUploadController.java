package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传图书封面
     */
    @PostMapping("/bookCover")
    public Result<Map<String, String>> uploadBookCover(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            return Result.error("只支持 JPG、PNG、GIF 格式图片");
        }

        try {
            // 创建上传目录
            String bookUploadPath = uploadPath + "/books";
            Path uploadDir = Paths.get(bookUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "book_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
            
            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 返回访问路径
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/books/" + fileName);
            result.put("filename", fileName);
            result.put("originalName", originalFilename);

            return Result.success(result);
        } catch (IOException e) {
            log.error("上传图书封面失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 通用文件上传
     */
    @PostMapping("/file")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "other") String type) {
        if (file.isEmpty()) {
            return Result.error("上传文件为空");
        }

        try {
            // 创建上传目录
            String typeUploadPath = uploadPath + "/" + type;
            Path uploadDir = Paths.get(typeUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".tmp";
            String fileName = type + "_" + System.currentTimeMillis() + 
                "_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
            
            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            // 返回访问路径
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + type + "/" + fileName);
            result.put("filename", fileName);

            return Result.success(result);
        } catch (IOException e) {
            log.error("上传文件失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
