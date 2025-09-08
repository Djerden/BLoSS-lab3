package com.djeno.lab1.services;

import com.djeno.lab1.persistence.DTO.statistics.StatisticsDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;

@Service
public class ReportScheduler {

    private final StatisticsService statisticsService;
    private final PdfReportService pdfReportService;

    public ReportScheduler(StatisticsService statisticsService, PdfReportService pdfReportService) {
        this.statisticsService = statisticsService;
        this.pdfReportService = pdfReportService;
    }

    // каждый день в полночь
    //@Scheduled(cron = "0 0 0 * * *")
    @Scheduled(fixedRate = 60000)
    public void generateDailyReport() {
        StatisticsDto stats = statisticsService.collectStatistics();

        File reportsDir = new File("/app/data/reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        String filePath = "reports/statistics-" + LocalDate.now() + ".pdf";
        pdfReportService.generateReport(stats, filePath);
    }
}
