package com.kuetche.siteecommerce.service;

import com.kuetche.siteecommerce.entity.Client;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String expediteur;

    @Async
    public void envoyerEmailBienvenue(Client client) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(expediteur);
            helper.setTo(client.getEmail());
            helper.setSubject("Bienvenue sur Site E-commerce");

            String contenuHtml = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                        <h2>Bienvenue %s %s 👋</h2>
                        <p>Votre compte client a été créé avec succès sur notre site e-commerce.</p>
                        <p>Vous pouvez maintenant vous connecter, consulter nos produits et passer vos commandes.</p>
                        <hr>
                        <p style="font-size: 13px; color: #666;">
                            Ceci est un message automatique, merci de ne pas répondre directement à cet e-mail.
                        </p>
                    </div>
                    """.formatted(client.getPrenom(), client.getNom());

            helper.setText(contenuHtml, true);
            mailSender.send(message);

            System.out.println("E-mail de bienvenue envoyé à : " + client.getEmail());

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }
}