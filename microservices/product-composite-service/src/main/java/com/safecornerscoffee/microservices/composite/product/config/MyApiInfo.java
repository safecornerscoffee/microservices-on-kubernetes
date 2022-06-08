package com.safecornerscoffee.microservices.composite.product.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "api.common")
public class MyApiInfo {

    String version;
    String title;
    String description;
    String termsOfServiceUrl;
    String license;
    String licenseUrl;

    private final Contact contact = new Contact();

    @Getter @Setter
    public static class Contact {
        String name;
        String url;
        String email;
    }

}
