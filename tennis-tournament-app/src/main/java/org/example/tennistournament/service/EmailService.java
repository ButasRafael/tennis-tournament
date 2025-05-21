package org.example.tennistournament.service;

import org.example.tennistournament.model.Tournament;
import org.example.tennistournament.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationOutcome(User player, Tournament tournament, boolean approved) {
        try {
            String subject = approved
                    ? "Tournament Registration Approved"
                    : "Tournament Registration Denied";
            String text = String.format(
                    "Dear %s,\n\nYour registration for tournament '%s' has been %s.\n\nRegards,\nTennis Tournament Admin",
                    player.getUsername(),
                    tournament.getName(),
                    approved ? "APPROVED" : "DENIED"
            );

            SimpleMailMessage msg = new SimpleMailMessage();
            // <— here!
            msg.setFrom("butas.rafael@yahoo.com");
            msg.setTo(player.getEmail());
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);

        } catch (MailException ex) {
            log.warn("Failed to send email to {}: {}", player.getEmail(), ex.getMessage());
        }
    }
}
