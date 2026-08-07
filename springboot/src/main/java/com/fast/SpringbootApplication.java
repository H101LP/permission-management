package com.fast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SpringbootApplication {

	public static void main(String[] args) {

		SpringApplication.run(SpringbootApplication.class, args);

		System.out.println("后端启动成功~\n"+ "   ____   _           _   _           /\\/|\n" +
				"  / ___| (_)   __ _  | | | |   ___   |/\\/ \n" +
				" | |     | |  / _` | | | | |  / _ \\       \n" +
				" | |___  | | | (_| | | | | | | (_) |      \n" +
				"  \\____| |_|  \\__,_| |_| |_|  \\___/       \n" +
				"                                          ");
	}

}
