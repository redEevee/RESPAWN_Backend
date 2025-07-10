package com.shop.respawn.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MainBannerForm {

    @NotEmpty(message = "이름은 필수입니다.")
    private String title;

    private String imageFileId; // GridFS에 저장된 이미지의 fileId

}
