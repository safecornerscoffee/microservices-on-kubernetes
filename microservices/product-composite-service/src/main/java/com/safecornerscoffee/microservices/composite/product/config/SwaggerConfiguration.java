package com.safecornerscoffee.microservices.composite.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
@EnableConfigurationProperties(MyApiInfo.class)
public class SwaggerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConfiguration.class);

    private final MyApiInfo myApiInfo;

    public SwaggerConfiguration(MyApiInfo myApiInfo) {
        this.myApiInfo = myApiInfo;
    }

    @Bean
    public Docket apiDocumentation() {
        return new Docket(SWAGGER_2)
                .select()
                .apis(basePackage("com.safecornerscoffee.microservices.composite.product"))
                .paths(PathSelectors.any())
                .build()
                    .globalResponseMessage(RequestMethod.GET, Collections.emptyList())
                    .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        LOG.debug("MyApiInfo: " + myApiInfo.toString());
        
        return new ApiInfo(
                myApiInfo.getTitle(),
                myApiInfo.getDescription(),
                myApiInfo.getVersion(),
                myApiInfo.getTermsOfServiceUrl(),
                new Contact(myApiInfo.getContact().getName(), myApiInfo.getContact().getUrl(), myApiInfo.getContact().getEmail()),
                myApiInfo.getLicense(),
                myApiInfo.getLicenseUrl(),
                Collections.emptyList()
        );
    }

}
