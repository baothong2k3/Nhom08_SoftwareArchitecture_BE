/*
 * @ (#) CloudinaryConfig.java    1.0    20/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package com.bookstore.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 20/04/2025
 * @version: 1.0
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "",
                "api_key", "",
                "api_secret", ""
        )
        );
    }
}
