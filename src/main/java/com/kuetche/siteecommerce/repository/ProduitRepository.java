package com.kuetche.siteecommerce.repository;

import com.kuetche.siteecommerce.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
}