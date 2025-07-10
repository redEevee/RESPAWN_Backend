package com.shop.respawn.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.shop.respawn.entity.mongodb.MainBanner;
import com.shop.respawn.repository.mainBannerRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ImageController {

    private final GridFsTemplate gridFsTemplate;
    private final mainBannerRepository mainBannerRepository;

    @GetMapping("/mainBanner/upload")
    public String createForm(Model model) {
        model.addAttribute("mainBannerForm", new MainBannerForm());
        return "mainBanner/mainBannerForm";
    }

    @PostMapping("/mainBanner/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute MainBannerForm mainBannerForm) throws IOException {

        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );

        MainBanner mainBanner = new MainBanner();
        mainBanner.setImageFileId(fileId.toString());
        mainBanner.setTitle(mainBannerForm.getTitle()); // 입력받은 타이틀 저장
        mainBannerRepository.save(mainBanner);

        return ResponseEntity.ok(fileId.toString());
    }

    @GetMapping("/mainBanner/view")
    public String showMainBanners(Model model) {
        List<MainBanner> banners = mainBannerRepository.findAll();
        model.addAttribute("banners", banners);
        return "mainBanner/mainBannerView";
    }

    @GetMapping("/mainBanner/image/{id}")
    public void getImage(@PathVariable String id, HttpServletResponse response) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        if (file != null) {
            GridFsResource resource = gridFsTemplate.getResource(file);
            response.setContentType(file.getMetadata().get("_contentType").toString());
            StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
