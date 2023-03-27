package com.dayone.service;
import com.dayone.exception.impl.AlreadyExistCompanyException;
import com.dayone.exception.impl.NoCompanyException;
import com.dayone.exception.impl.NotExistTickerException;
import com.dayone.model.Company;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.persist.CompanyRepository;
import com.dayone.persist.DividendRepository;
import com.dayone.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;
    private final CompanyRepository companyRepository;

    // 스크랩한 데이터 저장
    // 외부에서 호출할 수 있는 메서드
    public Company save(String ticker){

        boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists){
            throw new AlreadyExistCompanyException();       //repo에 이미 존재하는 회사이므로 예외처리
        }
        return this.storeCompanyAndDividend(ticker);    // repo에 존재하지 않으면 티커를 추가한다.
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return  this.companyRepository.findAll(pageable);
    }

    // 정상으로 저장되면 회사의 Company 인스턴스 반환
    private Company storeCompanyAndDividend(String ticker){
        // ticker 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);    //회사정보 반환



        // 회사 정보가 존재하지 않으면
       if(ObjectUtils.isEmpty(company)){

            throw new RuntimeException("Failed to scrap ticker -> " + ticker);
//            throw  new NotExistTickerException();
        }

        // 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList= scrapedResult.getDividends().stream()
                                                            .map( e -> new DividendEntity(companyEntity.getId(), e))
                                                            .collect(Collectors.toList());  // 결괏값을 리스트로 반환
       try {
           this.dividendRepository.saveAll(dividendEntityList);

       }catch (Exception e){
           e.printStackTrace();
//           throw  new NotExistTickerException();
       }
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword){
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                            .map(e -> e.getName())
                            .collect(Collectors.toList());
    }

    // 저장
    public void addAutoCompleteKeyword(String keyword){
        this.trie.put(keyword, null);
    }

    // 검색
    public List<String> autoComplete(String keyword){
        return (List<String>) this.trie.prefixMap(keyword)
            .keySet()
            .stream()
//            .limit(5)   // 개수 제한
            .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    // 삭제
    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                                        .orElseThrow(() -> new NoCompanyException());
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);
        this.deleteAutocompleteKeyword(company.getName());  // 트라이에서도 데이터를 지운다.
        return company.getName();
    }
}
