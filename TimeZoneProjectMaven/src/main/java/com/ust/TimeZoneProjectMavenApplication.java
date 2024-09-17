package com.ust;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TimeZoneProjectMavenApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeZoneProjectMavenApplication.class, args);
    }

}
