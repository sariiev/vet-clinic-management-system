package mas.vetclinic.model.entity.person;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import mas.vetclinic.model.entity.procedure.Certification;
import mas.vetclinic.model.entity.procedure.PerformedProcedure;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Nurse extends Person {
    @ManyToMany
    @JoinTable(
            name = "nurse_certifications",
            joinColumns = @JoinColumn(name = "nurse_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    private Set<Certification> certifications = new HashSet<>();

    @ManyToMany(mappedBy = "assistingNurses")
    private Set<PerformedProcedure> assistedAt = new HashSet<>();

    protected Nurse() {}

    public Nurse(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, LocalDate hireDate, String hourlyRate, Set<Certification> certifications) throws IllegalArgumentException {
        super(firstName, lastName, dateOfBirth, phoneNumber, hireDate, hourlyRate);
        if (certifications == null || certifications.isEmpty()) {
            throw new IllegalArgumentException("Certifications cannot be null or empty");
        }
        for (Certification certification : certifications) {
            if (certification == null) throw new IllegalArgumentException("Certification cannot be null");
        }
        for (Certification certification : certifications) {
            addCertification(certification);
        }
    }

    public Nurse(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, LocalDate hireDate, String hourlyRate, String emailAddress, Set<Certification> certifications) throws IllegalArgumentException {
        super(firstName, lastName, dateOfBirth, phoneNumber, hireDate, hourlyRate, emailAddress);
        if (certifications == null || certifications.isEmpty()) {
            throw new IllegalArgumentException("Certifications cannot be null or empty");
        }
        for (Certification certification : certifications) {
            if (certification == null) throw new IllegalArgumentException("Certification cannot be null");
        }
        for (Certification certification : certifications) {
            addCertification(certification);
        }
    }

    // Association-related methods
    public void addCertification(Certification certification) throws IllegalArgumentException {
        if (certification == null) {
            throw new IllegalArgumentException("Certification cannot be null");
        }
        certifications.add(certification);
        certification.addNurseInternal(this);
    }

    public void removeCertification(Certification certification) throws IllegalStateException {
        if (certification == null) {
            throw new IllegalArgumentException("Certification cannot be null");
        }
        if (certifications.contains(certification) && certifications.size() == 1) {
            throw new IllegalStateException("Last nurse's certification cannot be removed");
        }
        certifications.remove(certification);
        certification.removeNurseInternal(this);
    }

    public void addAssistedAtInternal(PerformedProcedure performedProcedure) throws IllegalArgumentException {
        if (performedProcedure == null) {
            throw new IllegalArgumentException("Performed procedure cannot be null");
        }
        assistedAt.add(performedProcedure);
    }

    public void removeAssistedAtInternal(PerformedProcedure performedProcedure) throws IllegalArgumentException {
        assistedAt.remove(performedProcedure);
    }

    // Getters and setters
    public Set<Certification> getCertifications() {
        return Collections.unmodifiableSet(certifications);
    }

    public Set<PerformedProcedure> getAssistedAt() {
        return Collections.unmodifiableSet(assistedAt);
    }

    // Other methods
    @Override
    public String toString() {
        return "Nurse{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", dateOfBirth=" + getDateOfBirth() +
                ", age=" + getAge() +
                (getRoles().contains(PersonRole.INDIVIDUAL_CLIENT) ? ", emailAddress='" + getEmailAddress()+ '\'' : "") +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", certifications=" + certifications.stream().map(c -> c.getId()).collect(Collectors.toList()) +
                ", assistedAt=" + assistedAt.stream().map(a -> a.getId()).collect(Collectors.toList()) +
                '}';
    }
}
