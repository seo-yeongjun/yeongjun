package com.yeongjun.yeongjun.global.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.yeongjun.yeongjun.global.DTO.FileResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private final AmazonS3 amazonS3;

    public FileService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void uploadFile(String bucketName, String key, MultipartFile file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            // 필요시 content type 설정: metadata.setContentType(file.getContentType());
            amazonS3.putObject(bucketName, key, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public void deleteFile(String bucketName,String key) {
        amazonS3.deleteObject(bucketName, key);
    }

    public String getFileUrl(String bucketName,String key) {
        return amazonS3.getUrl(bucketName, key).toString();
    }

    public String getLocalFileUrl(String bucketName, String key) {
        return "/files/getFile?bucketName=" + bucketName + "&key=" + key;
    }

    public FileResponse getFileStreamingResponse(String bucketName, String key) {
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        String contentType = s3Object.getObjectMetadata().getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        StreamingResponseBody responseBody = outputStream -> {
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                inputStream.close();
            }
        };

        return new FileResponse(responseBody, contentType);
    }


    public boolean isExistFile(String bucketName,String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    public void createBucket(String bucketName) {
        amazonS3.createBucket(bucketName);
    }

    public void deleteBucket(String bucketName) {
        amazonS3.deleteBucket(bucketName);
    }

    public List<String> getObjectsInBucket(String bucketName) {
        List<String> keys = new ArrayList<>();
        ObjectListing objectListing = amazonS3.listObjects(bucketName);
        for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
            keys.add(os.getKey());
        }
        return keys;
    }
}
