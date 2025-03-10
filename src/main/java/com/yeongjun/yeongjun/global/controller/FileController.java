package com.yeongjun.yeongjun.global.controller;

import com.yeongjun.yeongjun.global.DTO.FileResponse;
import com.yeongjun.yeongjun.global.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("key") String key,
            @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(bucketName, key, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<Void> deleteFile(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("key") String key) {
        fileService.deleteFile(bucketName, key);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getFile")
    public ResponseEntity<StreamingResponseBody> getFile(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("key") String key) {
        FileResponse fileResponse = fileService.getFileStreamingResponse(bucketName, key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + key + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, public")
                .contentType(org.springframework.http.MediaType.parseMediaType(fileResponse.getContentType()))
                .body(fileResponse.getStreamingResponseBody());

    }

    @GetMapping("/existsFile")
    public ResponseEntity<Boolean> isExistFile(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("key") String key) {
        boolean exists = fileService.isExistFile(bucketName, key);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/createBucket")
    public ResponseEntity<Void> createBucket(@RequestParam("bucketName") String bucketName) {
        fileService.createBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteBucket")
    public ResponseEntity<Void> deleteBucket(@RequestParam("bucketName") String bucketName) {
        fileService.deleteBucket(bucketName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getFileList")
    public ResponseEntity<List<String>> getObjectsInBucket(@RequestParam("bucketName") String bucketName) {
        List<String> objects = fileService.getObjectsInBucket(bucketName);
        return ResponseEntity.ok(objects);
    }

    @PostMapping("/uploadFiles")
    public ResponseEntity<Void> uploadFiles(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("files") List<MultipartFile> files) {
        for (MultipartFile file : files) {
            fileService.uploadFile(bucketName, file.getOriginalFilename(), file);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteFiles")
    public ResponseEntity<Void> deleteFiles(
            @RequestParam("bucketName") String bucketName,
            @RequestParam("keys") List<String> keys) {
        for (String key : keys) {
            fileService.deleteFile(bucketName, key);
        }
        return ResponseEntity.ok().build();
    }
}
