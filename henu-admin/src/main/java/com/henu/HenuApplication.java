package com.henu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author henu
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class HenuApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(HenuApplication.class, args);
        System.out.println("项目启动成功 \n" +
                "  _    _  ______  _   _  _    _  \n" +
                " | |  | ||  ____|| \\ | || |  | | \n" +
                " | |__| || |__   |  \\| || |  | | \n" +
                " |  __  ||  __|  | . ` || |  | | \n" +
                " | |  | || |____ | |\\  || |__| | \n" +
                " |_|  |_||______||_| \\_| \\____/  \n" +
                "                                 \n");
    }
}
