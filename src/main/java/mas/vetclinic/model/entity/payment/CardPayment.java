package mas.vetclinic.model.entity.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.appointment.Appointment;

import java.time.LocalDateTime;

@Entity
public class CardPayment extends Payment {
    @NotBlank
    @Size(max = 100, message = "Transaction number cannot exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String transactionNumber;

    @NotBlank
    @Size(min = 4, max = 4, message = "Last four digits must be exactly 4 digits")
    @Column(nullable = false, length = 4)
    private String lastFourDigits;

    protected CardPayment() {}

    public CardPayment(Appointment appointment, LocalDateTime dateTime, String transactionNumber, String lastFourDigits) throws IllegalArgumentException {
        super(appointment, dateTime);
        if (transactionNumber == null || transactionNumber.isBlank()) {
            throw new IllegalArgumentException("Transaction number must not be null or blank");
        }
        this.transactionNumber = transactionNumber;
        if (lastFourDigits == null) {
            throw new IllegalArgumentException("Last four digits cannot be null");
        }
        if (!lastFourDigits.matches("\\d{4}")) {
            throw new IllegalArgumentException("Last four digits must be exactly 4 digits");
        }
        this.lastFourDigits = lastFourDigits;
        appointment.setPayment(this);
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    @Override
    public String toString() {
        return "CardPayment{" +
                "id=" + getId() +
                ", dateTime=" + getDateTime() +
                ", amount=" + getAmount() +
                ", appointment=" + getAppointment().getId() +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", lastFourDigits='" + lastFourDigits + '\'' +
                '}';
    }
}
