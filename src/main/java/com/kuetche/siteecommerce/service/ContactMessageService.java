package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.dto.ContactMessageRequest;
import com.kuetche.siteecommerce.dto.ContactMessageResponse;
import com.kuetche.siteecommerce.dto.UpdateContactStatusRequest;
import com.kuetche.siteecommerce.entity.ContactMessage;
import com.kuetche.siteecommerce.enums.StatutContact;
import com.kuetche.siteecommerce.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageResponse creerMessage(ContactMessageRequest request) {
        ContactMessage contactMessage = ContactMessage.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .tel(request.tel())
                .typeDemande(request.typeDemande())
                .sujet(request.sujet())
                .numeroCommande(request.numeroCommande())
                .message(request.message())
                .accepteContact(request.accepteContact())
                .statut(StatutContact.NOUVEAU)
                .build();

        ContactMessage savedMessage = contactMessageRepository.save(contactMessage);

        return ContactMessageResponse.from(savedMessage);
    }

    public List<ContactMessageResponse> listerMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ContactMessageResponse::from)
                .toList();
    }

    public List<ContactMessageResponse> listerMessagesParStatut(StatutContact statut) {
        return contactMessageRepository.findByStatutOrderByCreatedAtDesc(statut)
                .stream()
                .map(ContactMessageResponse::from)
                .toList();
    }

    public ContactMessageResponse obtenirMessage(Long id) {
        ContactMessage contactMessage = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Message de contact introuvable"
                ));

        return ContactMessageResponse.from(contactMessage);
    }

    public ContactMessageResponse modifierStatut(Long id, UpdateContactStatusRequest request) {
        ContactMessage contactMessage = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Message de contact introuvable"
                ));

        contactMessage.setStatut(request.statut());

        ContactMessage updatedMessage = contactMessageRepository.save(contactMessage);

        return ContactMessageResponse.from(updatedMessage);
    }

    public void supprimerMessage(Long id) {
        ContactMessage contactMessage = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Message de contact introuvable"
                ));

        contactMessageRepository.delete(contactMessage);
    }
}