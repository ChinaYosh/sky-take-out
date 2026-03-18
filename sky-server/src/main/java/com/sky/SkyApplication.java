package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching
public class SkyApplication {
    public static void main(String[] args) {
       log.info("                   _ooOoo_");
       log.info("                  o8888888o");
       log.info("                  88\" . \"88");
       log.info("                  (| -_- |)");
       log.info("                  O\\  =  /O");
       log.info("               ____/`---'\\____");
       log.info("             .'  \\\\|     |//  `.");
       log.info("            /  \\\\|||  :  |||//  \\");
       log.info("           /  _||||| -:- |||||-  \\");
       log.info("           |   | \\\\\\  -  /// |   |");
       log.info("           | \\_|  ''\\---/''  |   |");
       log.info("           \\  .-\\__  `-`  ___/-. /");
       log.info("         ___`. .'  /--.--\\  `. . __");
       log.info( "      .\"\" '<  `.___\\_<|>_/___.'  >'\"\".");
       log.info("     | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |");
       log.info("     \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /");
       log.info("======`-.____`-.___\\_____/___.-`____.-'======");
       log.info("                   `=---='");
       log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
       log.info("               佛祖保佑       永无BUG");
        SpringApplication.run(SkyApplication.class, args);


    }
}
