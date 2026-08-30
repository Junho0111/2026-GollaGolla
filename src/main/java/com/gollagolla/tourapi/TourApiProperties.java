package com.gollagolla.tourapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tourapi")
public class TourApiProperties {

    private String serviceKey;
    private String baseUrl;
    private List<String> targetAreaCodes;
    private boolean autoSync;
}
