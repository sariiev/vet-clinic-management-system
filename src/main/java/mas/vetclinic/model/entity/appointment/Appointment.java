package mas.vetclinic.model.entity.appointment;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import mas.vetclinic.model.entity.medication.Prescription;
import mas.vetclinic.model.entity.payment.Payment;
import mas.vetclinic.model.entity.procedure.PerformedProcedure;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Appointment {
    private static LocalTime WORKING_HOURS_START = LocalTime.of(10, 0);
    private static LocalTime WORKING_HOURS_END = LocalTime.of(18, 0);
    private static int SCHEDULING_WINDOW_SIZE_DAYS = 90;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @AssertTrue(message = "End date and time time must be after start date and time")
    public boolean isEndDateTimeAfterStartDateTime() {
        if (startDateTime == null || endDateTime == null) return true;
        return endDateTime.isAfter(startDateTime);
    }
    @Column(nullable = true)
    private LocalDateTime endDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentDuration expectedDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private Veterinarian veterinarian;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PerformedProcedure> performedProcedures = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prescription> prescriptions = new HashSet<>();

    protected Appointment() {}

    public Appointment(Veterinarian veterinarian, Pet pet, LocalDateTime startDateTime, AppointmentDuration expectedDuration) throws IllegalArgumentException {
        if (veterinarian == null) {
            throw new IllegalArgumentException("Veterinarian cannot be null");
        }
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date and time time cannot be null");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date and time cannot be in the past");
        }
        if (expectedDuration == null) {
            throw new IllegalArgumentException("Expected duration cannot be null");
        }
        if(!isWithinSchedulingWindow(startDateTime.toLocalDate())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled more than " + SCHEDULING_WINDOW_SIZE_DAYS + " days in advance");
        }
        if (!isWithinWorkingHours(startDateTime.toLocalTime(), expectedDuration)) {
            throw new IllegalArgumentException("Appointment must be within working hours");
        }

        if (startDateTime.getMinute() % 15 != 0 || startDateTime.getSecond() != 0) {
            throw new IllegalArgumentException("Appointment time must be in 15-minute steps");
        }

        this.veterinarian = veterinarian;
        this.pet = pet;
        this.startDateTime = startDateTime;
        this.expectedDuration = expectedDuration;
        veterinarian.addAppointmentInternal(this);
        pet.addAppointmentInternal(this);
    }

    // Business methods
    public void markAsInProcess() throws IllegalStateException {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Status cannot be changed to \"In-process\" if current status is not \"Scheduled\"");
        }
        if (LocalDateTime.now().isBefore(startDateTime)) {
            throw new IllegalStateException("Appointment cannot be started before its scheduled start date and time");
        }
        status = AppointmentStatus.IN_PROCESS;
    }

    public void markAsNoShow() throws IllegalStateException {
        if (status != AppointmentStatus.IN_PROCESS) {
            throw new IllegalStateException("Status cannot be changed to \"No-show\" if current status is not \"In process\"");
        }
        status = AppointmentStatus.NO_SHOW;
    }

    public void cancel() throws IllegalStateException {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Appointment cannot be cancelled if current status is not \"Scheduled\"");
        }
        status = AppointmentStatus.CANCELLED;
    }

    public void markAsCompleted(LocalDateTime endDateTime) throws IllegalArgumentException, IllegalStateException {
        if (status != AppointmentStatus.IN_PROCESS) {
            throw new IllegalStateException("Appointment cannot be completed if current status is not \"In process\"");
        }
        if (performedProcedures.isEmpty()) {
            throw new IllegalStateException("Completed appointment must have at least one performed procedure");
        }
        if (endDateTime == null) {
            throw new IllegalArgumentException("End date and time time cannot be null");
        }
        if (endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("End date and time cannot be before start date and time");
        }
        if (endDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date and time cannot be in the future");
        }
        this.endDateTime = endDateTime;
        status = AppointmentStatus.COMPLETED;
    }

    public BigDecimal calculateTotalPrice() throws IllegalStateException {
        if (status != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Total price can be calculated only if status = \"Completed\"");
        }
        return performedProcedures.stream().map(p -> p.getProcedure().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static boolean isWithinSchedulingWindow(LocalDate date) {
        return !date.isAfter(LocalDate.now().plusDays(SCHEDULING_WINDOW_SIZE_DAYS));
    }

    public static boolean isWithinWorkingHours(LocalTime startTime, AppointmentDuration appointmentDuration) {
        LocalTime endTime = startTime.plus(appointmentDuration.getDuration());
        return !startTime.isBefore(WORKING_HOURS_START) && !endTime.isAfter(WORKING_HOURS_END);
    }

    public boolean overlapsWith(LocalDateTime startDateTime, AppointmentDuration duration) {
        if (this.status == AppointmentStatus.CANCELLED) return false;
        LocalDateTime endDateTime = startDateTime.plus(duration.getDuration());
        LocalDateTime thisEndDateTime = this.startDateTime.plus(this.expectedDuration.getDuration());
        return this.startDateTime.isBefore(endDateTime) && thisEndDateTime.isAfter(startDateTime);
    }

    // Associations-related methods
    public void addPerformedProcedureInternal(PerformedProcedure performedProcedure) throws IllegalArgumentException {
        if (performedProcedure == null) {
            throw new IllegalArgumentException("Performed procedure cannot be null");
        }
        performedProcedures.add(performedProcedure);
    }

    public void removePerformedProcedureInternal(PerformedProcedure performedProcedure) throws IllegalArgumentException {
        performedProcedures.remove(performedProcedure);
    }

    public void addPrescriptionInternal(Prescription prescription) throws IllegalArgumentException {
        if (prescription == null) {
            throw new IllegalArgumentException("Prescription cannot be null");
        }
        prescriptions.add(prescription);
    }

    public void removePrescriptionInternal(Prescription prescription) {
        prescriptions.remove(prescription);
    }

    public void setPayment(Payment payment) throws IllegalArgumentException, IllegalStateException {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }
        if (!this.equals(payment.getAppointment())) {
            throw new IllegalArgumentException("Payment does not belong to this appointment");
        }
        if (status != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Payment can only be set if status = \"Completed\"");
        }
        this.payment = payment;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public AppointmentDuration getExpectedDuration() {
        return expectedDuration;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public Veterinarian getVeterinarian() {
        return veterinarian;
    }

    public Pet getPet() {
        return pet;
    }

    public Set<PerformedProcedure> getPerformedProcedures() {
        return Collections.unmodifiableSet(performedProcedures);
    }

    public Payment getPayment() {
        return payment;
    }

    public Set<Prescription> getPrescriptions() {
        return Collections.unmodifiableSet(prescriptions);
    }

    public static LocalTime getWorkingHoursStart() {
        return WORKING_HOURS_START;
    }

    public static LocalTime getWorkingHoursEnd() {
        return WORKING_HOURS_END;
    }

    public static int getSchedulingWindowSizeDays() {
        return SCHEDULING_WINDOW_SIZE_DAYS;
    }

    public LocalDateTime getExpectedEndDateTime() {
        return startDateTime.plus(expectedDuration.getDuration());
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", startDateTime=" + startDateTime +
                ", expectedDuration=" + expectedDuration.getName() +
                ", endDateTime=" + endDateTime +
                ", status=" + status +
                ", veterinarian=" + veterinarian.getId() +
                ", pet=" + pet.getId() +
                ", performedProcedures=" + performedProcedures.stream().map(p -> p.getId()).collect(Collectors.toList()) +
                ", prescriptions=" + prescriptions.stream().map(p -> p.getId()).collect(Collectors.toList()) +
                ", payment=" + (payment != null ? payment.getId() : "null") +
                '}';
    }
}
