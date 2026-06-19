package com.kuetche.siteecommerce.controller;

import com.kuetche.siteecommerce.dto.ContactMessageRequest;
import com.kuetche.siteecommerce.dto.ContactMessageResponse;
import com.kuetche.siteecommerce.dto.UpdateContactStatusRequest;
import com.kuetche.siteecommerce.enums.StatutContact;
import com.kuetche.siteecommerce.service.ContactMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    /**
     * Route publique utilisée par le formulaire de contact React.
     */
    @PostMapping
    public ResponseEntity<ContactMessageResponse> creerMessage(
            @Valid @RequestBody ContactMessageRequest request
    ) {
        ContactMessageResponse response = contactMessageService.creerMessage(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Routes admin pour gérer les messages reçus.
     */
    @GetMapping("/admin")
    public ResponseEntity<?> listerMessages(
            @RequestParam(required = false) StatutContact statut
    ) {
        if (statut != null) {
            return ResponseEntity.ok(contactMessageService.listerMessagesParStatut(statut));
        }

        return ResponseEntity.ok(contactMessageService.listerMessages());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<ContactMessageResponse> obtenirMessage(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.obtenirMessage(id));
    }

    @PatchMapping("/admin/{id}/statut")
    public ResponseEntity<ContactMessageResponse> modifierStatut(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactStatusRequest request
    ) {
        return ResponseEntity.ok(contactMessageService.modifierStatut(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> supprimerMessage(@PathVariable Long id) {
        contactMessageService.supprimerMessage(id);
        return ResponseEntity.noContent().build();
    }
}