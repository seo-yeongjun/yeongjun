package com.yeongjun.yeongjun.tools.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ImageService {

    private static final String UPLOAD_DIR = "/env/uploads/images";
    private static final String PROCESSED_DIR = "/env/processed/images";

    public File processImages(List<MultipartFile> files, long targetSizeKB) throws IOException {
        if (files.isEmpty()) {
            return null;
        }

        List<File> processedFiles = new ArrayList<>();
        String batchId = UUID.randomUUID().toString();
        
        // 1. 개별 파일 처리 (항상 batchId 폴더 내에서)
        for (MultipartFile file : files) {
            File processedFile = processSingleImage(file, targetSizeKB, batchId);
            if (processedFile != null) {
                processedFiles.add(processedFile);
            }
        }

        if (processedFiles.isEmpty()) {
            return null;
        }

        // 2. 결과 파일 개수에 따라 분기
        // 결과가 하나라면 그 파일 그대로 반환 (단, 상위 폴더로 이동)
        if (processedFiles.size() == 1) {
            File singleFile = processedFiles.get(0);
            Path sourcePath = singleFile.toPath();
            Path destPath = Paths.get(PROCESSED_DIR, singleFile.getName());
            
            // 파일을 PROCESSED_DIR 루트로 이동
            Files.move(sourcePath, destPath);
            
            return destPath.toFile();
        }

        // 2개 이상이면 ZIP 파일로 묶어서 반환
        return createZipFile(processedFiles, batchId);
    }

    private File processSingleImage(MultipartFile file, long targetSizeKB, String batchId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        String safeFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        
        Path uploadPath = Paths.get(UPLOAD_DIR, batchId, safeFilename);
        Path processedPath = Paths.get(PROCESSED_DIR, batchId, safeFilename);

        Files.createDirectories(uploadPath.getParent());
        Files.createDirectories(processedPath.getParent());

        file.transferTo(uploadPath);

        long targetSizeBytes = targetSizeKB * 1024;
        File processedFile = processedPath.toFile();

        double quality = 1.0;
        double scale = 1.0;
        boolean success = false;

        for (quality = 0.9; quality >= 0.1; quality -= 0.1) {
            Thumbnails.of(uploadPath.toFile()).scale(1.0).outputQuality(quality).toFile(processedFile);
            if (processedFile.length() <= targetSizeBytes) {
                success = true;
                break;
            }
        }

        if (!success) {
            quality = 0.5;
            for (scale = 0.9; scale >= 0.1; scale -= 0.1) {
                Thumbnails.of(uploadPath.toFile()).scale(scale).outputQuality(quality).toFile(processedFile);
                if (processedFile.length() <= targetSizeBytes) {
                    success = true;
                    break;
                }
            }
        }
        
        if (!success) {
             Thumbnails.of(uploadPath.toFile()).scale(0.1).outputQuality(0.1).toFile(processedFile);
        }

        return processedFile;
    }

    private File createZipFile(List<File> files, String batchId) throws IOException {
        String zipFileName = "compressed_images_" + batchId + ".zip";
        Path zipFilePath = Paths.get(PROCESSED_DIR, zipFileName);
        
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    String entryName = file.getName().substring(file.getName().indexOf("_") + 1);
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
        return zipFilePath.toFile();
    }
    
    public File getProcessedFile(String filename) {
        if (filename.contains("..")) return null;
        
        Path filePath = Paths.get(PROCESSED_DIR, filename);
        File file = filePath.toFile();
        if (file.exists()) {
            return file;
        }
        return null;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupOldFiles() {
        cleanupDirectory(new File(UPLOAD_DIR));
        cleanupDirectory(new File(PROCESSED_DIR));
    }

    private void cleanupDirectory(File directory) {
        if (!directory.exists()) return;
        
        File[] files = directory.listFiles();
        if (files != null) {
            long now = System.currentTimeMillis();
            long tenMinutesInMillis = 10 * 60 * 1000;

            for (File file : files) {
                if (now - file.lastModified() > tenMinutesInMillis) {
                    deleteRecursively(file);
                }
            }
        }
    }
    
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
