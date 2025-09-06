package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    Page<App> findByCategories_Id(Long categoryId, Pageable pageable);

}
