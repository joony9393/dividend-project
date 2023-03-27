package com.dayone.scheduler;
import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.model.constants.CacheKey;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.scraper.YahooFinanceScraper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j  //  스크랩이 실행될 때마다 로깅 남기기
@Component
@AllArgsConstructor
@EnableCaching
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final YahooFinanceScraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;
    /*
    @Scheduled(fixedDelay = 1000)    // 1초마다 실행된다.
    public void test1() throws InterruptedException{
        Thread.sleep(10000);    // 10초가 일시 정지
        System.out.println( Thread.currentThread().getName() +  " -> 테스트 1 : " + LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 1000)
    public void test2(){
        System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : " + LocalDateTime.now());
    }
     */

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling(){
        log.info("== Scheduler started ==");

        // 저장된 회사의 목록 조회
        List<CompanyEntity> companies =  this.companyRepository.findAll();

        // 회사 마다 배당금 정보를 새로 스크래핑
        for(var company : companies){
            log.info("회사명 : " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getName(), company.getName()));

            // 스크래핑한 배당금 정보 중 DB에 없는 값은 저장한다.
            scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(company.getId(), e))
                // dividend 엔티티 하나하나를 repo에 삽입
                .forEach(e -> {
                    boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if(!exists){
                            this.dividendRepository.save(e);
                            log.info("INSERT new Dividend = > " + e.toString());
                        }
                });
            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
