package com.kuetche.siteecommerce.repository;

import com.kuetche.siteecommerce.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {
}