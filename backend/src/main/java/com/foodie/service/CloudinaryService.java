
package com.foodie.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        if (file.getSize() > 8 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Image must be smaller than 8 MB"
            );
        }

        String contentType = file.getContentType();

        if (!"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)
                && !"image/webp".equals(contentType)) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and WebP images are supported"
            );
        }

        byte[] imageBytes = file.getBytes();

        if (ImageIO.read(new ByteArrayInputStream(imageBytes)) == null) {
            throw new IllegalArgumentException(
                    "Invalid or unsupported image file"
            );
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                imageBytes,
                ObjectUtils.asMap(
                        "folder", "foodie/menu-items",
                        "resource_type", "image"
                )
        );

        return (String) result.get("secure_url");
    }
}
