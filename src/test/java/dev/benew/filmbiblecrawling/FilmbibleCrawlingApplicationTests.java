package dev.benew.filmbiblecrawling;

import dev.benew.filmbiblecrawling.service.TotalCrawlingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@SpringBootTest
class FilmbibleCrawlingApplicationTests {

    @Autowired
    TotalCrawlingService videoCrawlingService;

    @Test
    void contextLoads() {
    }

    @Test
    void videoCrawling() throws GeneralSecurityException, IOException {
        videoCrawlingService.crawlingTotal();
    }


}
