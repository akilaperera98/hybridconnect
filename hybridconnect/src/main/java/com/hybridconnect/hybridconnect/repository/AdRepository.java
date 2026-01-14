package com.hybridconnect.hybridconnect.repository;

import com.hybridconnect.hybridconnect.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    List<Ad> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);

}
