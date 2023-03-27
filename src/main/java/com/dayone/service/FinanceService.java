package com.dayone.service;
import static com.dayone.model.constants.CacheKey.KEY_FINANCE;
import com.dayone.exception.impl.NoCompanyException;
import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = KEY_FINANCE)       //  여기에 어노테이션이 붙어 있으니까
    public ScrapedResult  getDividendByCompanyName(String companyName){

        log.info("search company => " + companyName);

        // 1. 회사명으로 회사 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                                    .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사의 id로 배당금 조회
        List<DividendEntity> dividendEntities =  this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                                                    .map(e -> new Dividend(e.getDate(), e.getDividend()))
                                                    .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName())
                                    , dividends
        );    // 엔티티를 모델로 바꾼다.
    }
}