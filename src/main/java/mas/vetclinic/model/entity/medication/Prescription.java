package mas.vetclinic.model.entity.medication;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"appointment_id", "medication_id"}))
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @DecimalMin(value = "0.0", inclusive = false, message = "Dosage amount must be positive")
    @Column(nullable = true, precision = 10, scale = 3)
    private BigDecimal dosageAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private DosageUnit dosageUnit;

    @NotNull(message = "Frequency cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequencyType frequency;

    @NotNull(message = "Start date cannot be null")
    @Column(nullable = false)
    private LocalDate startDate;

    @Positive(message = "Duration days must be positive")
    @Column(nullable = false)
    private int durationDays;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(nullable = true, length = 500)
    private String notes;

    protected Prescription() {}

    public Prescription(Appointment appointment, Medication medication, BigDecimal dosageAmount, DosageUnit dosageUnit, FrequencyType frequency, LocalDate startDate, int durationDays, String notes) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }
        if (appointment.getStatus() != AppointmentStatus.IN_PROCESS) {
            throw new IllegalArgumentException("Prescription cannot be created for an appointment which status is not \"In-process\"");
        }
        if (medication == null) {
            throw new IllegalArgumentException("Medication cannot be null");
        }

        boolean alreadyPrescribed = appointment.getPrescriptions().stream().anyMatch(
                p -> (p.getMedication().equals(medication))
        );
        if (alreadyPrescribed) {
            throw new IllegalArgumentException("Medication already prescribed for this appointment");
        }

        if (frequency == null) {
            throw new IllegalArgumentException("Frequency cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (startDate.isBefore(appointment.getStartDateTime().toLocalDate())) {
            throw new IllegalArgumentException("Start date cannot be before appointment start date");
        }
        if (durationDays <= 0) {
            throw new IllegalArgumentException("Duration days must be positive");
        }
        if (dosageAmount != null && dosageUnit == null) {
            throw new IllegalArgumentException("Dosage unit must be provided if dosage amount is set");
        }
        if (dosageUnit != null && dosageAmount == null) {
            throw new IllegalArgumentException("Dosage amount must be provided if dosage unit is set");
        }
        if (dosageAmount == null && (notes == null || notes.isBlank())) {
            throw new IllegalArgumentException("Notes must be provided if dosage is not specified");
        }
        if (dosageAmount != null && dosageAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Dosage amount must be positive");
        }

        this.appointment = appointment;
        this.medication = medication;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.frequency = frequency;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.notes = notes;

        appointment.addPrescriptionInternal(this);
        medication.addPrescriptionInternal(this);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public Medication getMedication() {
        return medication;
    }

    public BigDecimal getDosageAmount() {
        return dosageAmount;
    }

    public DosageUnit getDosageUnit() {
        return dosageUnit;
    }

    public FrequencyType getFrequency() {
        return frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public String getNotes() {
        return notes;
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prescription that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", appointment=" + appointment.getId() +
                ", medication=" + medication.getId() +
                ", dosageAmount=" + dosageAmount +
                ", dosageUnit=" + dosageUnit +
                ", frequency=" + frequency +
                ", startDate=" + startDate +
                ", durationDays=" + durationDays +
                ", notes='" + notes + '\'' +
                '}';
    }
}
