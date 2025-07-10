package com.shop.respawn.entity.mongodb;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@Document(collection = "main-banner")
@AllArgsConstructor
@NoArgsConstructor
public class MainBanner {

    @Id @GeneratedValue
    private String id;
    private String imageFileId; // GridFS에 저장된 이미지의 fileId
    private String title;

}
