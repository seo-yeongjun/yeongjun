package com.yeongjun.yeongjun.tools.controller;

import com.yeongjun.yeongjun.tools.service.ImageService;
import com.yeongjun.yeongjun.tools.service.IpService;
import io.ipinfo.api.model.IPResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/tools")
public class ToolsController {

    private final IpService ipService;
    private final ImageService imageService;

    public ToolsController(IpService ipService, ImageService imageService) {
        this.ipService = ipService;
        this.imageService = imageService;
    }


    @GetMapping({"", "/"})
    public String toolsHome() {
        return "tools/home";
    }

    @GetMapping("myIp")
    public String myIp(HttpServletRequest request, Model model) {
        IPResponse ipResponse = ipService.getIpInfo(request);

        model.addAttribute("ipInfo", ipResponse);

        return "tools/myIp";
    }

    @GetMapping("letterCount")
    public String letterCount() {
        return "tools/letterCount";
    }

    @GetMapping("textCompare")
    public String textCompare() {return "tools/textCompare";}

    @GetMapping("duplicateCheck")
    public String duplicateCheck() {return "tools/duplicateCheck";}

    @GetMapping("imageCompress")
    public String imageCompress() {
        return "tools/imageCompress";
    }

    @PostMapping("imageCompress")
    @ResponseBody
    public ResponseEntity<?> processImage(@RequestParam("files") List<MultipartFile> files,
                                          @RequestParam("targetSize") long targetSize) {
        try {
            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body("파일을 선택해주세요.");
            }
            
            // 전체 크기 체크는 여기서 하기 어려움 (개별 파일 체크는 프론트에서 1차, 서비스에서 2차)
            // 서비스 호출
            File resultZipFile = imageService.processImages(files, targetSize);
            
            return ResponseEntity.ok().body(resultZipFile.getName()); // ZIP 파일명 반환

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("download/{filename}")
    public ResponseEntity<InputStreamResource> downloadImage(@PathVariable String filename) throws IOException {
        File file = imageService.getProcessedFile(filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }
}
