package mas.vetclinic.model.entity.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.appointment.Appointment;

import java.time.LocalDateTime;

@Entity
public class TransferPayment extends Payment {
    @NotBlank
    @Size(max = 100, message = "Transaction number cannot exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String transactionNumber;

    @Size(min = 15, max = 34, message = "Sender account number must be between 15 and 34 characters")
    @NotBlank
    @Column(nullable = false, length = 34)
    private String senderAccountNumber;

    protected TransferPayment() {}

    public TransferPayment(Appointment appointment, LocalDateTime dateTime, String transactionNumber, String senderAccountNumber) {
        super(appointment, dateTime);
        if (transactionNumber == null || transactionNumber.isBlank()) {
            throw new IllegalArgumentException("Transaction number cannot be null or blank");
        }
        this.transactionNumber = transactionNumber;
        if (senderAccountNumber == null || senderAccountNumber.isBlank()) {
            throw new IllegalArgumentException("Sender account number cannot be null or blank");
        }
        if (senderAccountNumber.length() < 15 || senderAccountNumber.length() > 34) {
            throw new IllegalArgumentException("Sender account number must be between 15 and 34 characters");
        }
        this.senderAccountNumber = senderAccountNumber;
        appointment.setPayment(this);
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    @Override
    public String toString() {
        return "TransferPayment{" +
                "id=" + getId() +
                ", dateTime=" + getDateTime() +
                ", amount=" + getAmount() +
                ", appointment=" + getAppointment().getId() +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", senderAccountNumber='" + senderAccountNumber + '\'' +
                '}';
    }
}
