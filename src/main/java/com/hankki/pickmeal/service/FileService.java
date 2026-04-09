package com.hankki.pickmeal.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;


    public String uploadImageToS3(MultipartFile multipartFile){
        if(multipartFile.isEmpty()) return null;

        try{
            String originalName = multipartFile.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String storedName = UUID.randomUUID() + extension;

            ObjectMetadata objMeta = new ObjectMetadata();
            objMeta.setContentType(multipartFile.getContentType());
            InputStream inputStream = multipartFile.getInputStream();
            objMeta.setContentLength(inputStream.available());

            amazonS3.putObject(bucket, storedName, inputStream, objMeta);
            inputStream.close();

            return "https://s3.ap-northeast-2.amazonaws.com/" + bucket + "/" + storedName;
        } catch (IOException e){
            throw new RuntimeException("S3 파일 업로드 실패", e);

        }
    }

}