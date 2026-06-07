package mas.vetclinic.model.entity.payment;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent(message = "Date and time cannot be in the future")
    @Column(nullable = false)
    private LocalDateTime dateTime;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @OneToOne(mappedBy = "payment")
    private Appointment appointment;

    protected Payment() {}

    public Payment(Appointment appointment, LocalDateTime dateTime) throws IllegalArgumentException {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment cannot be created for an appointment which status is not \"Completed\"");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Date and time cannot be null");
        }
        if (dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date and time cannot be in the future");
        }
        this.appointment = appointment;
        this.dateTime = dateTime;
        this.amount = appointment.calculateTotalPrice();
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Payment that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", dateTime=" + dateTime +
                ", amount=" + amount +
                ", appointment=" + appointment.getId() +
                '}';
    }
}
