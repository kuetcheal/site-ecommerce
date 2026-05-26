package com.kuetche.siteecommerce.repository;

import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByClientOrderByCreatedAtDesc(Client client);
}