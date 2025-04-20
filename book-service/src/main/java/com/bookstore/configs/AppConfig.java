/*
 * @ (#) AppConfig.java    1.0    21/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package com.bookstore.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 21/04/2025
 * @version: 1.0
 */

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}