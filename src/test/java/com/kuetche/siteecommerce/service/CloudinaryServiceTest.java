package com.kuetche.siteecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    void uploadImage_doitUploaderImageEtRetournerUrls() throws IOException {
        // Arrange
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("image-content".getBytes());
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> cloudinaryResponse = Map.of(
                "secure_url", "https://cloudinary.com/image.jpg",
                "public_id", "site-ecommerce/produits/image123"
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(cloudinaryResponse);

        // Act
        Map<String, String> result = cloudinaryService.uploadImage(file);

        // Assert
        assertThat(result).containsEntry("secureUrl", "https://cloudinary.com/image.jpg");
        assertThat(result).containsEntry("publicId", "site-ecommerce/produits/image123");

        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_doitLeverExceptionSiFichierNull() {
        // Act & Assert
        assertThatThrownBy(() -> cloudinaryService.uploadImage(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur lors de l'upload vers Cloudinary");
    }

    @Test
    void uploadImage_doitLeverExceptionSiFichierVide() {
        // Arrange
        when(file.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> cloudinaryService.uploadImage(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur lors de l'upload vers Cloudinary");
    }

    @Test
    void uploadImage_doitLeverExceptionSiErreurLectureFichier() throws IOException {
        // Arrange
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("Erreur lecture"));

        // Act & Assert
        assertThatThrownBy(() -> cloudinaryService.uploadImage(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur lors de la lecture du fichier image");
    }

    @Test
    void deleteImage_doitSupprimerImageSiPublicIdValide() throws Exception {
        // Arrange
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq("public-id-test"), anyMap())).thenReturn(Map.of("result", "ok"));

        // Act
        cloudinaryService.deleteImage("public-id-test");

        // Assert
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).destroy(eq("public-id-test"), anyMap());
    }

    @Test
    void deleteImage_neDoitRienFaireSiPublicIdNullOuVide() throws Exception {
        // Act
        cloudinaryService.deleteImage(null);
        cloudinaryService.deleteImage("");
        cloudinaryService.deleteImage("   ");

        // Assert
        verify(cloudinary, never()).uploader();
        verify(uploader, never()).destroy(anyString(), anyMap());
    }
}