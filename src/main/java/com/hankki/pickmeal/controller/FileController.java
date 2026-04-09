package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/file/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException{

        try{
            String imgUrl = fileService.uploadImageToS3(multipartFile);

            return ResponseEntity.ok(imgUrl);
        }catch (Exception e){
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body("이미지 업로드에 실패했습니다.");
        }
    }


}
