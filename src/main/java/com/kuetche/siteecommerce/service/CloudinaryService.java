package com.kuetche.siteecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, String> uploadImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Aucune image fournie");
            }

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "site-ecommerce/produits",
                            "resource_type", "image"
                    )
            );

            String secureUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            return Map.of(
                    "secureUrl", secureUrl,
                    "publicId", publicId
            );

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier image", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload vers Cloudinary", e);
        }
    }

    public void deleteImage(String publicId) {
        try {
            if (publicId != null && !publicId.isBlank()) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'image Cloudinary : " + e.getMessage());
        }
    }
}