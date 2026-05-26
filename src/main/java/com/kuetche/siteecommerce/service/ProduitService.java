package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.dto.ProduitResponse;
import com.kuetche.siteecommerce.entity.Produit;
import com.kuetche.siteecommerce.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CloudinaryService cloudinaryService;

    public ProduitResponse creerProduit(
            String name,
            BigDecimal price,
            String description,
            Integer stock,
            String color,
            MultipartFile image
    ) {
        Map<String, String> imageData = cloudinaryService.uploadImage(image);

        Produit produit = Produit.builder()
                .name(name)
                .price(price)
                .description(description)
                .stock(stock)
                .color(color)
                .imageUrl(imageData.get("secureUrl"))
                .imagePublicId(imageData.get("publicId"))
                .build();

        Produit savedProduit = produitRepository.save(produit);

        return ProduitResponse.from(savedProduit);
    }

    public List<ProduitResponse> listerProduits() {
        return produitRepository.findAll()
                .stream()
                .map(ProduitResponse::from)
                .toList();
    }

    public ProduitResponse obtenirProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));

        return ProduitResponse.from(produit);
    }

    public ProduitResponse modifierProduit(
            Long id,
            String name,
            BigDecimal price,
            String description,
            Integer stock,
            String color,
            MultipartFile image
    ) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));

        produit.setName(name);
        produit.setPrice(price);
        produit.setDescription(description);
        produit.setStock(stock);
        produit.setColor(color);

        if (image != null && !image.isEmpty()) {
            cloudinaryService.deleteImage(produit.getImagePublicId());

            Map<String, String> imageData = cloudinaryService.uploadImage(image);
            produit.setImageUrl(imageData.get("secureUrl"));
            produit.setImagePublicId(imageData.get("publicId"));
        }

        Produit updatedProduit = produitRepository.save(produit);

        return ProduitResponse.from(updatedProduit);
    }

    public void supprimerProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));

        cloudinaryService.deleteImage(produit.getImagePublicId());

        produitRepository.delete(produit);
    }
}