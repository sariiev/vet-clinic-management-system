package mas.vetclinic.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendAppointmentConfirmationEmail(String receiverEmailAddress, String veterinarianName,
                                                 String petName, String startDateTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(receiverEmailAddress);
            message.setSubject("Appointment confirmation");
            message.setText(
                    "Your appointment has been confirmed.\n" +
                            "Veterinarian: " + veterinarianName + "\n" +
                            "Pet: " + petName + "\n" +
                            "Date and time: " + startDateTime + "\n"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }
}
