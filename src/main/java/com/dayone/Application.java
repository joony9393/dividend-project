package com.dayone;
import com.dayone.model.Company;
import com.dayone.scraper.Scraper;
import com.dayone.scraper.YahooFinanceScraper;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class Application {
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
//        System.out.println("Main -> " + Thread.currentThread().getName());

        /* 야후 스크래퍼 테스트 코드
        Sc

*/
    }
}
