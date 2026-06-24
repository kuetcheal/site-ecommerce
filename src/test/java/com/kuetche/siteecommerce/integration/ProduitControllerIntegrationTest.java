package com.kuetche.siteecommerce.integration;

import com.kuetche.siteecommerce.entity.Produit;
import com.kuetche.siteecommerce.repository.ProduitRepository;
import com.kuetche.siteecommerce.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProduitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProduitRepository produitRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        produitRepository.deleteAll();

        when(cloudinaryService.uploadImage(any()))
                .thenReturn(Map.of(
                        "secureUrl", "https://cloudinary.com/test-image.jpg",
                        "publicId", "site-ecommerce/produits/test-image"
                ));
    }

    @Test
    void listerProduits_doitRetournerListeVideAuDepart() throws Exception {
        mockMvc.perform(get("/api/produits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void creerProduit_doitCreerProduitEnBaseEtRetournerCreated() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "produit.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/produits")
                        .file(image)
                        .param("name", "T-shirt Nike")
                        .param("price", "29.99")
                        .param("description", "T-shirt sport")
                        .param("stock", "20")
                        .param("color", "Noir")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("T-shirt Nike"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stock").value(20));

        assertThat(produitRepository.findAll()).hasSize(1);

        Produit produit = produitRepository.findAll().get(0);

        assertThat(produit.getName()).isEqualTo("T-shirt Nike");
        assertThat(produit.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
        assertThat(produit.getDescription()).isEqualTo("T-shirt sport");
        assertThat(produit.getStock()).isEqualTo(20);
        assertThat(produit.getColor()).isEqualTo("Noir");
        assertThat(produit.getImageUrl()).isEqualTo("https://cloudinary.com/test-image.jpg");
        assertThat(produit.getImagePublicId()).isEqualTo("site-ecommerce/produits/test-image");

        verify(cloudinaryService, times(1)).uploadImage(any());
    }

    @Test
    void obtenirProduit_doitRetournerProduitExistant() throws Exception {
        Produit produit = Produit.builder()
                .name("Chaussure Adidas")
                .price(BigDecimal.valueOf(59.99))
                .description("Chaussure sport")
                .stock(10)
                .color("Blanc")
                .imageUrl("https://cloudinary.com/adidas.jpg")
                .imagePublicId("adidas-public-id")
                .build();

        Produit savedProduit = produitRepository.save(produit);

        mockMvc.perform(get("/api/produits/{id}", savedProduit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chaussure Adidas"))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void modifierProduit_doitModifierProduitEnBase() throws Exception {
        Produit produit = Produit.builder()
                .name("Ancien produit")
                .price(BigDecimal.valueOf(10.00))
                .description("Ancienne description")
                .stock(5)
                .color("Rouge")
                .imageUrl("ancienne-image.jpg")
                .imagePublicId("ancien-public-id")
                .build();

        Produit savedProduit = produitRepository.save(produit);

        when(cloudinaryService.uploadImage(any()))
                .thenReturn(Map.of(
                        "secureUrl", "https://cloudinary.com/new-image.jpg",
                        "publicId", "site-ecommerce/produits/new-image"
                ));

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "new-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/produits/{id}", savedProduit.getId())
                        .file(image)
                        .param("name", "Produit modifié")
                        .param("price", "49.99")
                        .param("description", "Nouvelle description")
                        .param("stock", "15")
                        .param("color", "Bleu")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Produit modifié"))
                .andExpect(jsonPath("$.stock").value(15));

        Produit updatedProduit = produitRepository.findById(savedProduit.getId()).orElseThrow();

        assertThat(updatedProduit.getName()).isEqualTo("Produit modifié");
        assertThat(updatedProduit.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(updatedProduit.getStock()).isEqualTo(15);
        assertThat(updatedProduit.getImageUrl()).isEqualTo("https://cloudinary.com/new-image.jpg");

        verify(cloudinaryService, times(1)).deleteImage("ancien-public-id");
        verify(cloudinaryService, times(1)).uploadImage(any());
    }

    @Test
    void supprimerProduit_doitSupprimerProduitEnBase() throws Exception {
        Produit produit = Produit.builder()
                .name("Produit à supprimer")
                .price(BigDecimal.valueOf(15.00))
                .description("Produit test")
                .stock(3)
                .color("Noir")
                .imageUrl("image.jpg")
                .imagePublicId("public-id-delete")
                .build();

        Produit savedProduit = produitRepository.save(produit);

        mockMvc.perform(delete("/api/produits/{id}", savedProduit.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        assertThat(produitRepository.findById(savedProduit.getId())).isEmpty();

        verify(cloudinaryService, times(1)).deleteImage("public-id-delete");
    }
}