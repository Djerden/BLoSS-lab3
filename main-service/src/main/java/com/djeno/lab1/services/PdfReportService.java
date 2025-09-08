package com.djeno.lab1.services;

import com.djeno.lab1.persistence.DTO.statistics.StatisticsDto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.time.LocalDateTime;

@Service
public class PdfReportService {

    public void generateReport(StatisticsDto stats, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("📊 СТАТИСТИКА СИСТЕМЫ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("Сгенерировано: " + LocalDateTime.now()));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Общее количество приложений: " + stats.getTotalApps()));
            document.add(new Paragraph("Новых приложений за сутки: " + stats.getNewAppsLastDay()));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Общее количество покупок: " + stats.getTotalPurchases()));
            document.add(new Paragraph("Общая выручка: " + stats.getTotalRevenue() + " ₽"));
            document.add(new Paragraph("Новых покупок за сутки: " + stats.getNewPurchasesLastDay()));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Общее количество пользователей: " + stats.getTotalUsers()));
            document.add(new Paragraph("Новых пользователей за сутки: " + stats.getNewUsersLastDay()));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Общее количество отзывов: " + stats.getTotalReviews()));
            document.add(new Paragraph("Средний рейтинг: " + stats.getAverageRating()));
            document.add(new Paragraph("Новых отзывов за сутки: " + stats.getNewReviewsLastDay()));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Топ-5 приложений по загрузкам:"));
            for (StatisticsDto.AppSummary app : stats.getTopAppsByDownloads()) {
                document.add(new Paragraph(" - " + app.getName() + " (" + app.getDownloads() + " загрузок)"));
            }
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Топ-5 приложений по рейтингу:"));
            for (StatisticsDto.AppSummary app : stats.getTopAppsByRating()) {
                document.add(new Paragraph(" - " + app.getName() + " (" + app.getRating() + "⭐)"));
            }
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Топ-5 приложений по выручке:"));
            for (StatisticsDto.AppSummary app : stats.getTopAppsByRevenue()) {
                document.add(new Paragraph(" - " + app.getName() + " (" + app.getRevenue() + " ₽)"));
            }
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Топ-5 покупателей:"));
            for (StatisticsDto.UserSummary user : stats.getTopBuyers()) {
                document.add(new Paragraph(" - " + user.getUsername() + " (" + user.getPurchasesCount() + " покупок)"));
            }
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Топ-5 приложений по отзывам:"));
            for (StatisticsDto.AppSummary app : stats.getTopAppsByReviews()) {
                document.add(new Paragraph(" - " + app.getName() + " (" + app.getReviews() + " отзывов)"));
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации PDF: " + e.getMessage(), e);
        }
    }
}

