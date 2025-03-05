package com.yeongjun.yeongjun.ajeGag.controller;

import com.yeongjun.yeongjun.ajeGag.model.Ajegag;
import com.yeongjun.yeongjun.ajeGag.repository.AjegagDAO;
import com.yeongjun.yeongjun.global.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/ajeGag")
@Slf4j
public class AjeGagController {

    private final AjegagDAO ajeGagDAO;
    private final FileService fileService;

    public AjeGagController(AjegagDAO ajeGagDAO, FileService fileService) {
        this.ajeGagDAO = ajeGagDAO;
        this.fileService = fileService;
    }

    @GetMapping({"", "/"})
    public String getHome(Model model) {
        return "ajeGag/home";
    }

    @GetMapping("/getList")
    @ResponseBody
    public List<Ajegag> getList() {
        return ajeGagDAO.selectAjegagList();
    }

    @GetMapping("/getHumor")
    @ResponseBody
    public Ajegag getHumor(@RequestParam("id") Long id) {
        Ajegag ajegag = ajeGagDAO.findById(id);
        return ajegag != null ? ajegag : new Ajegag();
    }

    @PostMapping("/create")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<String> createAjegag(@RequestBody Ajegag ajegag) {
        int result = ajeGagDAO.insertAjegag(ajegag);
        if(result > 0) {
            return ResponseEntity.ok("자료 등록 성공");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("자료 등록 실패");
        }
    }

    // 수정된 업로드 메서드: CKEditor의 simpleUpload가 기대하는 "upload" 파라미터와 JSON 응답 처리
    @PostMapping("/uploadFile")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("upload") MultipartFile file) {
        String bucketName = "ajegag"; // 실제 사용 중인 버킷명으로 수정
        // UUID를 이용하여 고유한 파일 키 생성 (원본 파일명 앞에 UUID_를 붙임)
        String originalFilename = file.getOriginalFilename();
        String key = UUID.randomUUID().toString() + "_" + originalFilename;

        // 파일 업로드 처리
        fileService.uploadFile(bucketName, key, file);

        // 업로드 후 파일 URL 획득
        String fileUrl = fileService.getLocalFileUrl(bucketName, key);

        // CKEditor가 요구하는 JSON 응답 형식 {"url": "파일 URL"} 반환
        Map<String, Object> response = new HashMap<>();
        response.put("url", fileUrl);
        return ResponseEntity.ok(response);
    }
}
