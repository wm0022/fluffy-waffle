package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    /**
     * 获取上传根目录的绝对路径
     */
    private Path getUploadRootPath() {
        Path path = Paths.get(uploadPath).toAbsolutePath().normalize();
        return path;
    }

    /**
     * 安全创建目录（递归创建所有缺失的父级目录）
     */
    private void ensureDirectoryExists(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    /**
     * 安全写入文件：使用 try-with-resources 确保 InputStream 关闭，
     * 避免 Tomcat 临时文件被锁定导致删除失败
     */
    private void safeTransfer(MultipartFile file, Path targetPath) throws IOException {
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

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
            // 获取上传根目录的绝对路径
            Path rootPath = getUploadRootPath();
            Path uploadDir = rootPath.resolve("books");
            // 递归创建所有缺失的父级目录
            ensureDirectoryExists(uploadDir);

            // 生成文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "book_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
            
            // 保存文件（使用 Files.copy 避免 transferTo 的 FileNotFoundException）
            Path filePath = uploadDir.resolve(fileName);
            safeTransfer(file, filePath);

            // 返回访问路径
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/books/" + fileName);
            result.put("filename", fileName);
            result.put("originalName", originalFilename);

            return Result.success(result);
        } catch (IOException e) {
            e.printStackTrace();
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
            // 获取上传根目录的绝对路径
            Path rootPath = getUploadRootPath();
            Path uploadDir = rootPath.resolve(type);
            // 递归创建所有缺失的父级目录
            ensureDirectoryExists(uploadDir);

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".tmp";
            String fileName = type + "_" + System.currentTimeMillis() + 
                "_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;
            
            // 保存文件（使用 Files.copy 避免 transferTo 的 FileNotFoundException）
            Path filePath = uploadDir.resolve(fileName);
            safeTransfer(file, filePath);

            // 返回访问路径
            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + type + "/" + fileName);
            result.put("filename", fileName);

            return Result.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}
