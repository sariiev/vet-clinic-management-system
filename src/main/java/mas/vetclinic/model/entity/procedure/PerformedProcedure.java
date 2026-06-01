package mas.vetclinic.model.entity.procedure;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;
import mas.vetclinic.model.entity.person.Nurse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class PerformedProcedure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(nullable = true, length = 500)
    private String notes;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToMany
    @JoinTable(
            name = "performed_procedure_nurses",
            joinColumns = @JoinColumn(name = "performed_procedure_id"),
            inverseJoinColumns = @JoinColumn(name = "nurse_id")
    )
    private Set<Nurse> assistingNurses = new HashSet<>();

    protected PerformedProcedure() {}

    public PerformedProcedure(Appointment appointment, Procedure procedure, Set<Nurse> assistingNurses) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }
        if (appointment.getStatus() != AppointmentStatus.IN_PROCESS) {
            throw new IllegalArgumentException("Performed procedure cannot be created for an appointment which status is not \"In-process\"");
        }
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        Set<Certification> requiredCertifications = procedure.getRequiredCertifications();
        if (assistingNurses != null) {
            for (Nurse nurse : assistingNurses) {
                if (nurse == null) {
                    throw new IllegalArgumentException("Nurse cannot be null");
                }
                if (!requiredCertifications.isEmpty() && !nurse.getCertifications().containsAll(requiredCertifications)) {
                    throw new IllegalArgumentException("Nurse does not have all required certifications for this procedure");
                }
            }
        }
        Set<Specialization> requiredSpecializations = procedure.getRequiredSpecializations();
        if (!requiredSpecializations.isEmpty() && !appointment.getVeterinarian().getSpecializations().containsAll(requiredSpecializations)) {
            throw new IllegalArgumentException("Veterinarian does not have all required specializations for this procedure");
        }
        this.appointment = appointment;
        this.procedure = procedure;
        if (assistingNurses != null) {
            for (Nurse n : assistingNurses) {
                addAssistingNurse(n);
            }
        }
        appointment.addPerformedProcedureInternal(this);
        procedure.addPerformedProcedureInternal(this);
    }

    // Associations-related methods
    public void addAssistingNurse(Nurse nurse) throws IllegalArgumentException {
        if (nurse == null) {
            throw new IllegalArgumentException("Nurse cannot be null");
        }
        Set<Certification> requiredCertifications = procedure.getRequiredCertifications();
        if (!requiredCertifications.isEmpty() && !nurse.getCertifications().containsAll(requiredCertifications)) {
            throw new IllegalArgumentException("Nurse does not have all required certifications for this procedure");
        }
        assistingNurses.add(nurse);
        nurse.addAssistedAtInternal(this);
    }

    public void removeAssistingNurse(Nurse nurse) throws IllegalArgumentException {
        if (nurse == null) {
            throw new IllegalArgumentException("Nurse cannot be null");
        }
        assistingNurses.remove(nurse);
        nurse.removeAssistedAtInternal(this);
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public Set<Nurse> getAssistingNurses() {
        return Collections.unmodifiableSet(assistingNurses);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PerformedProcedure that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PerformedProcedure{" +
                "id=" + id +
                ", notes='" + notes + '\'' +
                ", procedure=" + procedure.getId() +
                ", appointment=" + appointment.getId() +
                ", assistingNurses=" + assistingNurses.stream().map(n -> n.getId()).collect(Collectors.toList()) +
                '}';
    }
}
