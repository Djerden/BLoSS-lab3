package com.djeno.lab1.init;

import com.djeno.lab1.persistence.models.Category;
import com.djeno.lab1.persistence.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> categoryNames = List.of(
                "Игры",
                "Образование",
                "Социальные сети",
                "Фото и видео",
                "Музыка",
                "Здоровье и фитнес",
                "Финансы",
                "Путешествия",
                "Еда и напитки",
                "Продуктивность"
        );

        List<Category> existingCategories = categoryRepository.findAll();
        Set<String> existingNames = existingCategories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());

        List<Category> newCategories = categoryNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> {
                    Category category = new Category();
                    category.setName(name);
                    return category;
                })
                .collect(Collectors.toList());

        if (!newCategories.isEmpty()) {
            categoryRepository.saveAll(newCategories);
            System.out.println("Добавлено " + newCategories.size() + " новых категорий");
        } else {
            System.out.println("Все категории уже существуют в базе данных");
        }
    }
}