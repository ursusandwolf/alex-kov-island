package com.island.config;

import com.island.controller.SimulationType;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, SimulationType>() {
            @Override
            public SimulationType convert(String source) {
                return SimulationType.valueOf(source.toUpperCase());
            }
        });
    }
}
