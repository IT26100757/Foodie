
package com.foodie.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {

        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");

        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException(
                    "CLOUDINARY_URL environment variable is missing"
            );
        }

        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;

        return cloudinary;
    }
}
