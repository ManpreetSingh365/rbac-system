package com.fleetmanagement.config;

import java.util.HashSet;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.modelmapper.Converter;

import java.util.Set;


/**
 * Application Configuration
 * Updated: Removed Redis Cache configuration as per requirements
 * Configures ModelMapper and Security components only
 */
@Configuration
public class AppConfig {
    
    /**
     * Configure ModelMapper for DTO conversions
     * Optimized for performance with strict matching strategy
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);        
        Converter<Set<?>, Set<?>> setConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            return new HashSet<>(ctx.getSource());
        };

        mapper.addConverter(setConverter);

        return mapper;
    }
    
    /**
     * Configure password encoder for security
     * Using BCrypt with strength 12 for enhanced security
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}