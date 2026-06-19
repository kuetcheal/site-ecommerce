package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.dto.ProduitResponse;
import com.kuetche.siteecommerce.entity.Produit;
import com.kuetche.siteecommerce.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private ProduitService produitService;

    @Test
    void creerProduit_doitUploaderImageEtSauvegarderProduit() {
        // Arrange
        Map<String, String> imageData = Map.of(
                "secureUrl", "https://cloudinary.com/image.jpg",
                "publicId", "site-ecommerce/produits/image123"
        );

        when(cloudinaryService.uploadImage(image)).thenReturn(imageData);

        Produit savedProduit = Produit.builder()
                .id(1L)
                .name("T-shirt Nike")
                .price(BigDecimal.valueOf(29.99))
                .description("T-shirt sport")
                .stock(20)
                .color("Noir")
                .imageUrl("https://cloudinary.com/image.jpg")
                .imagePublicId("site-ecommerce/produits/image123")
                .build();

        when(produitRepository.save(any(Produit.class))).thenReturn(savedProduit);

        // Act
        ProduitResponse response = produitService.creerProduit(
                "T-shirt Nike",
                BigDecimal.valueOf(29.99),
                "T-shirt sport",
                20,
                "Noir",
                image
        );

        // Assert
        assertThat(response).isNotNull();

        ArgumentCaptor<Produit> produitCaptor = ArgumentCaptor.forClass(Produit.class);
        verify(produitRepository).save(produitCaptor.capture());

        Produit produitCapture = produitCaptor.getValue();

        assertThat(produitCapture.getName()).isEqualTo("T-shirt Nike");
        assertThat(produitCapture.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
        assertThat(produitCapture.getDescription()).isEqualTo("T-shirt sport");
        assertThat(produitCapture.getStock()).isEqualTo(20);
        assertThat(produitCapture.getColor()).isEqualTo("Noir");
        assertThat(produitCapture.getImageUrl()).isEqualTo("https://cloudinary.com/image.jpg");
        assertThat(produitCapture.getImagePublicId()).isEqualTo("site-ecommerce/produits/image123");

        verify(cloudinaryService, times(1)).uploadImage(image);
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void listerProduits_doitRetournerListeProduits() {
        // Arrange
        Produit produit1 = Produit.builder()
                .id(1L)
                .name("Produit 1")
                .price(BigDecimal.valueOf(10))
                .description("Description 1")
                .stock(5)
                .color("Rouge")
                .imageUrl("image1.jpg")
                .imagePublicId("public1")
                .build();

        Produit produit2 = Produit.builder()
                .id(2L)
                .name("Produit 2")
                .price(BigDecimal.valueOf(20))
                .description("Description 2")
                .stock(8)
                .color("Bleu")
                .imageUrl("image2.jpg")
                .imagePublicId("public2")
                .build();

        when(produitRepository.findAll()).thenReturn(List.of(produit1, produit2));

        // Act
        List<ProduitResponse> result = produitService.listerProduits();

        // Assert
        assertThat(result).hasSize(2);
        verify(produitRepository, times(1)).findAll();
    }

    @Test
    void obtenirProduit_doitRetournerProduitSiExiste() {
        // Arrange
        Produit produit = Produit.builder()
                .id(1L)
                .name("Produit test")
                .price(BigDecimal.valueOf(15))
                .description("Description test")
                .stock(10)
                .color("Vert")
                .imageUrl("image.jpg")
                .imagePublicId("publicId")
                .build();

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));

        // Act
        ProduitResponse response = produitService.obtenirProduit(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(produitRepository, times(1)).findById(1L);
    }

    @Test
    void obtenirProduit_doitLeverExceptionSiProduitIntrouvable() {
        // Arrange
        when(produitRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> produitService.obtenirProduit(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Produit introuvable");

        verify(produitRepository, times(1)).findById(99L);
    }

    @Test
    void modifierProduit_sansNouvelleImage_doitModifierProduitSansUploaderImage() {
        // Arrange
        Produit produit = Produit.builder()
                .id(1L)
                .name("Ancien nom")
                .price(BigDecimal.valueOf(10))
                .description("Ancienne description")
                .stock(5)
                .color("Rouge")
                .imageUrl("ancienne-image.jpg")
                .imagePublicId("ancien-public-id")
                .build();

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // Act
        ProduitResponse response = produitService.modifierProduit(
                1L,
                "Nouveau nom",
                BigDecimal.valueOf(25),
                "Nouvelle description",
                15,
                "Bleu",
                null
        );

        // Assert
        assertThat(response).isNotNull();

        assertThat(produit.getName()).isEqualTo("Nouveau nom");
        assertThat(produit.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(25));
        assertThat(produit.getDescription()).isEqualTo("Nouvelle description");
        assertThat(produit.getStock()).isEqualTo(15);
        assertThat(produit.getColor()).isEqualTo("Bleu");

        verify(cloudinaryService, never()).uploadImage(any());
        verify(cloudinaryService, never()).deleteImage(any());
        verify(produitRepository, times(1)).save(produit);
    }

    @Test
    void modifierProduit_avecNouvelleImage_doitSupprimerAncienneImageEtUploaderNouvelle() {
        // Arrange
        Produit produit = Produit.builder()
                .id(1L)
                .name("Ancien nom")
                .price(BigDecimal.valueOf(10))
                .description("Ancienne description")
                .stock(5)
                .color("Rouge")
                .imageUrl("ancienne-image.jpg")
                .imagePublicId("ancien-public-id")
                .build();

        Map<String, String> newImageData = Map.of(
                "secureUrl", "nouvelle-image.jpg",
                "publicId", "nouveau-public-id"
        );

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(image.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadImage(image)).thenReturn(newImageData);
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // Act
        ProduitResponse response = produitService.modifierProduit(
                1L,
                "Nouveau nom",
                BigDecimal.valueOf(25),
                "Nouvelle description",
                15,
                "Bleu",
                image
        );

        // Assert
        assertThat(response).isNotNull();

        assertThat(produit.getImageUrl()).isEqualTo("nouvelle-image.jpg");
        assertThat(produit.getImagePublicId()).isEqualTo("nouveau-public-id");

        verify(cloudinaryService, times(1)).deleteImage("ancien-public-id");
        verify(cloudinaryService, times(1)).uploadImage(image);
        verify(produitRepository, times(1)).save(produit);
    }

    @Test
    void supprimerProduit_doitSupprimerImageEtProduit() {
        // Arrange
        Produit produit = Produit.builder()
                .id(1L)
                .name("Produit à supprimer")
                .imagePublicId("public-id-test")
                .build();

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));

        // Act
        produitService.supprimerProduit(1L);

        // Assert
        verify(cloudinaryService, times(1)).deleteImage("public-id-test");
        verify(produitRepository, times(1)).delete(produit);
    }
}