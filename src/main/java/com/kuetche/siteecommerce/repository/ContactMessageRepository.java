package com.kuetche.siteecommerce.repository;

import com.kuetche.siteecommerce.entity.ContactMessage;
import com.kuetche.siteecommerce.enums.StatutContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findByStatutOrderByCreatedAtDesc(StatutContact statut);

    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}