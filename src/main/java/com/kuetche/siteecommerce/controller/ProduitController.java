package com.kuetche.siteecommerce.controller;

import com.kuetche.siteecommerce.dto.ProduitResponse;
import com.kuetche.siteecommerce.service.ProduitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProduitResponse> creerProduit(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam Integer stock,
            @RequestParam(required = false) String color,
            @RequestParam MultipartFile image
    ) {
        ProduitResponse response = produitService.creerProduit(
                name,
                price,
                description,
                stock,
                color,
                image
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProduitResponse>> listerProduits() {
        return ResponseEntity.ok(produitService.listerProduits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponse> obtenirProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.obtenirProduit(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProduitResponse> modifierProduit(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String description,
            @RequestParam Integer stock,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) MultipartFile image
    ) {
        ProduitResponse response = produitService.modifierProduit(
                id,
                name,
                price,
                description,
                stock,
                color,
                image
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerProduit(@PathVariable Long id) {
        produitService.supprimerProduit(id);
        return ResponseEntity.noContent().build();
    }
}