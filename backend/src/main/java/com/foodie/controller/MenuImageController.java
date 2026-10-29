
package com.foodie.controller;

import com.foodie.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/menu-images")
public class MenuImageController {

    private final CloudinaryService cloudinaryService;

    public MenuImageController(
            CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, String> uploadImage(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        String imageUrl = cloudinaryService.uploadImage(file);

        return Map.of("imageUrl", imageUrl);
    }
}
