package mas.vetclinic.model.entity.person;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.procedure.Specialization;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Veterinarian extends Person {
    @NotBlank(message = "License number cannot be blank")
    @Size(max = 30, message = "License number cannot exceed 30 characters")
    @Column(nullable = false, length = 30, unique = true)
    private String licenseNumber;

    @NotBlank(message = "License issuing authority cannot be blank")
    @Size(max = 30, message = "License issuing authority cannot exceed 30 characters")
    @Column(nullable = false, length = 30)
    private String licenseIssuingAuthority;

    @NotNull(message = "Licence issue date cannot be null")
    @PastOrPresent(message = "Licence issue date must not be in the future")
    @Column(nullable = false)
    private LocalDate licenseIssueDate;

    @AssertTrue(message = "License expiry date must be after issue date")
    public boolean isExpiryDateAfterIssueDate() {
        if (licenseIssueDate == null || licenseExpiryDate == null) return true;
        return licenseExpiryDate.isAfter(licenseIssueDate);
    }
    @NotNull(message = "Licence expiry date cannot be null")
    @Column(nullable = false)
    private LocalDate licenseExpiryDate;

    @ManyToMany
    @JoinTable(
            name = "veterinarian_specializations",
            joinColumns = @JoinColumn(name = "veterinarian_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<Specialization> specializations = new HashSet<>();

    @OneToMany(mappedBy = "veterinarian")
    private Set<Appointment> appointments = new HashSet<>();

    protected Veterinarian() {}

    public Veterinarian(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, LocalDate hireDate, String hourlyRate, String licenseNumber, String licenseIssuingAuthority, LocalDate licenseIssueDate, LocalDate licenseExpiryDate, Set<Specialization> specializations) throws IllegalArgumentException {
        super(firstName, lastName, dateOfBirth, phoneNumber, hireDate, hourlyRate);
        setLicenseNumber(licenseNumber);
        setLicenseIssuingAuthority(licenseIssuingAuthority);
        setLicenseIssueDate(licenseIssueDate);
        setLicenseExpiryDate(licenseExpiryDate);
        if (specializations == null || specializations.isEmpty()) {
            throw new IllegalArgumentException("Specializations cannot be null or empty");
        }
        for (Specialization specialization : specializations) {
            if (specialization == null) throw new IllegalArgumentException("Specialization cannot be null");
        }
        for (Specialization specialization : specializations) {
            addSpecialization(specialization);
        }
    }

    public Veterinarian(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, LocalDate hireDate, String hourlyRate, String emailAddress, String licenseNumber, String licenseIssuingAuthority, LocalDate licenseIssueDate, LocalDate licenseExpiryDate, Set<Specialization> specializations) throws IllegalArgumentException {
        super(firstName, lastName, dateOfBirth, phoneNumber, hireDate, hourlyRate, emailAddress);
        setLicenseNumber(licenseNumber);
        setLicenseIssuingAuthority(licenseIssuingAuthority);
        setLicenseIssueDate(licenseIssueDate);
        setLicenseExpiryDate(licenseExpiryDate);
        if (specializations == null || specializations.isEmpty()) {
            throw new IllegalArgumentException("Specializations cannot be null or empty");
        }
        for (Specialization specialization : specializations) {
            if (specialization == null) throw new IllegalArgumentException("Specialization cannot be null");
        }
        for (Specialization specialization : specializations) {
            addSpecialization(specialization);
        }
    }

    // Associations-related methods
    public void addSpecialization(Specialization specialization) throws IllegalArgumentException {
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null");
        }
        specializations.add(specialization);
        specialization.addVeterinarianInternal(this);
    }

    public void removeSpecialization(Specialization specialization) throws IllegalArgumentException, IllegalStateException {
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null");
        }
        if (specializations.contains(specialization) && specializations.size() == 1) {
            throw new IllegalStateException("Last veterinarian's specialization cannot be removed");
        }
        specializations.remove(specialization);
        specialization.removeVeterinarianInternal(this);
    }

    public void addAppointmentInternal(Appointment appointment) throws IllegalArgumentException {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }
        appointments.add(appointment);
    }

    public void removeAppointmentInternal(Appointment appointment) {
        appointments.remove(appointment);
    }

    // Getters and setters

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) throws IllegalArgumentException {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("License number cannot be null or empty");
        }
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseIssuingAuthority() {
        return licenseIssuingAuthority;
    }

    public void setLicenseIssuingAuthority(String licenseIssuingAuthority) throws IllegalArgumentException {
        if (licenseIssuingAuthority == null || licenseIssuingAuthority.isBlank()) {
            throw new IllegalArgumentException("License issuing authority cannot be null or empty");
        }
        this.licenseIssuingAuthority = licenseIssuingAuthority;
    }

    public LocalDate getLicenseIssueDate() {
        return licenseIssueDate;
    }

    public void setLicenseIssueDate(LocalDate licenseIssueDate) throws IllegalArgumentException {
        if (licenseIssueDate == null) {
            throw new IllegalArgumentException("License issue date cannot be null");
        }
        this.licenseIssueDate = licenseIssueDate;
    }

    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) throws IllegalArgumentException {
        if (licenseExpiryDate == null) {
            throw new IllegalArgumentException("License expiry date cannot be null");
        }
        if (licenseExpiryDate.isBefore(licenseIssueDate)) {
            throw new IllegalArgumentException("License expiry date cannot be before license issue date");
        }
        this.licenseExpiryDate = licenseExpiryDate;
    }

    public Set<Specialization> getSpecializations() {
        return Collections.unmodifiableSet(specializations);
    }

    public Set<Appointment> getAppointments() {
        return  Collections.unmodifiableSet(appointments);
    }

    // Other methods
    @Override
    public String toString() {
        return "Veterinarian{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", dateOfBirth=" + getDateOfBirth() +
                ", age=" + getAge() +
                (getRoles().contains(PersonRole.INDIVIDUAL_CLIENT) ? ", emailAddress='" + getEmailAddress()+ '\'' : "") +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", licenseIssuingAuthority='" + licenseIssuingAuthority + '\'' +
                ", licenseIssueDate=" + licenseIssueDate +
                ", licenseExpiryDate=" + licenseExpiryDate +
                ", specializations=" + specializations.stream().map(s -> s.getId()).collect(Collectors.toList()) +
                ", appointments=" + appointments.stream().map(a -> a.getId()).collect(Collectors.toList()) +
                '}';
    }
}
