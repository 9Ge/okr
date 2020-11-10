package com.pm.okr;

import com.pm.okr.common.ProgressUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
public class OkrApplication {

    public static void main(String[] args) {
        System.setProperty("jasypt.encryptor.password", "SHSR201903");
        SpringApplication.run(OkrApplication.class, args);
    }

}
