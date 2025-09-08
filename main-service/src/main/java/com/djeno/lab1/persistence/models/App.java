package com.djeno.lab1.persistence.models;

import com.djeno.lab1.persistence.enums.AppStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "apps")
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column
    private String iconId; // ID иконки в MinIO

    @ElementCollection
    @CollectionTable(name = "app_screenshots", joinColumns = @JoinColumn(name = "app_id"))
    @Column(name = "screenshot")
    private List<String> screenshotsIds; // Список ID скриншотов в MinIO

    @Column
    private String fileId; // ID файла в MinIO

    @Column(nullable = false)
    private BigDecimal price; // 0 - если бесплатное

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @ManyToMany
    @JoinTable(
            name = "app_category",
            joinColumns = @JoinColumn(name = "app_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Column(nullable = false)
    private int downloads;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Статус загрузки приложения в minio на сервисе app-service асинхронно через брокера
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppStatus status;

    @PrePersist
    protected void prePersist() {
        this.downloads = 0;
        this.createdAt = LocalDateTime.now();

        if (this.screenshotsIds == null) {
            this.screenshotsIds = new ArrayList<>();
        }

        if (this.status == null) {
            this.status = AppStatus.LOADING;
        }
    }

    /**
     * Метод для получения среднего рейтинга приложения.
     */
    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
}
