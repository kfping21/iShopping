package com.example.ishopping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/image")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            // 验证文件类型
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }

            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/jpg"))) {
                return ResponseEntity.badRequest().body("只支持 JPG、PNG 格式的图片");
            }

            // 验证文件大小 (2MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("图片大小不能超过 2MB");
            }

            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 返回文件访问URL
            String imageUrl = "/uploads/" + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "图片上传成功");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }
}