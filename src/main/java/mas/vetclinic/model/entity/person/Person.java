package mas.vetclinic.model.entity.person;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import mas.vetclinic.model.entity.payment.Payment;
import mas.vetclinic.model.entity.pet.Pet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Person extends PetOwner {
    private static final int MINIMUM_AGE = 18;
    private static final String PHONE_REGEX = "^\\+\\d{7,15}$";

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 30, message = "First name cannot exceed 30 characters")
    @Column(nullable = false, length = 30)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 30, message = "Last name cannot exceed 30 characters")
    @Column(nullable = false, length = 30)
    private String lastName;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number cannot be blank")
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "person_roles", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "role")
    private Set<PersonRole> roles = new HashSet<>();

    @PastOrPresent(message = "Hire date must not be in the future")
    @Column(nullable = true)
    private LocalDate hireDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be positive")
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    protected Person() {}

    public Person(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, String emailAddress) throws IllegalArgumentException {
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setPhoneNumber(phoneNumber);
        roles.add(PersonRole.INDIVIDUAL_CLIENT);
        setEmailAddress(emailAddress);
    }

    protected Person(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, LocalDate hireDate, String hourlyRate) throws IllegalArgumentException {
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setPhoneNumber(phoneNumber);
        roles.add(PersonRole.EMPLOYEE);
        setHireDate(hireDate);
        setHourlyRate(hourlyRate);
    }

    protected Person(String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber, LocalDate hireDate, String hourlyRate, String emailAddress) throws IllegalArgumentException {
        setFirstName(firstName);
        setLastName(lastName);
        setDateOfBirth(dateOfBirth);
        setPhoneNumber(phoneNumber);
        roles.add(PersonRole.EMPLOYEE);
        setHireDate(hireDate);
        setHourlyRate(hourlyRate);
        roles.add(PersonRole.INDIVIDUAL_CLIENT);
        setEmailAddress(emailAddress);
    }

    // Associations-related methods
    @Override
    public void addPetInternal(Pet pet) {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        super.addPetInternal(pet);
    }

    @Override
    public void removePetInternal(Pet pet) {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        super.removePetInternal(pet);
    }

    @Override
    public void addPet(Pet pet) {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        super.addPet(pet);
    }

    @Override
    public void removePet(Pet pet) {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        super.removePet(pet);
    }

    // Getters and setters
    public Long getId() {
        return super.getId();
    }

    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) throws IllegalArgumentException {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < MINIMUM_AGE) {
            throw new IllegalArgumentException("Person must be at least " + MINIMUM_AGE + " years old");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) throws IllegalArgumentException {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        if (!phoneNumber.matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Phone must contain 7-15 digits, starting with +");
        }
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getEmailAddress() throws IllegalStateException {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        return super.getEmailAddress();
    }

    public void setEmailAddress(String emailAddress) throws IllegalStateException {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        super.setEmailAddress(emailAddress);
    }

    public BigDecimal getHourlyRate() {
        if (!isEmployee()) {
            throw new IllegalStateException("Person is not an employee");
        }
        return hourlyRate;
    }

    public void setHourlyRate(String hourlyRate) throws IllegalArgumentException {
        if (hourlyRate == null) {
            throw new IllegalArgumentException("Hourly rate cannot be null");
        }
        if (!isEmployee()) {
            throw new IllegalStateException("Person is not an employee");
        }
        try {
            BigDecimal value = new BigDecimal(hourlyRate);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Hourly rate must be positive");
            }
            this.hourlyRate = value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Hourly rate must be a valid number");
        }
    }

    public LocalDate getHireDate() {
        if (!isEmployee()) {
            throw new IllegalStateException("Person is not an employee");
        }
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        if (hireDate == null) {
            throw new IllegalArgumentException("Hire date cannot be null");
        }
        if (!isEmployee()) {
            throw new IllegalStateException("Person is not an employee");
        }
        if (hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hire date cannot be in the future");
        }
        this.hireDate = hireDate;
    }

    public Set<PersonRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public Set<Pet> getPets() {
        if (!isIndividualClient()) {
            throw new IllegalStateException("Person is not an individual client");
        }
        return super.getPets();
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }

    public boolean isIndividualClient() {
        return roles.contains(PersonRole.INDIVIDUAL_CLIENT);
    }

    public boolean isEmployee() {
        return roles.contains(PersonRole.EMPLOYEE);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + getId() +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", roles=" + roles +
                (isEmployee() ? ", hireDate=" + hireDate +
                        ", hourlyRate=" + hourlyRate : "") +
                (isIndividualClient() ? ", emailAddress='" + getEmailAddress() + '\'' : "") +
                (isIndividualClient() ? ", pets=" + getPets().stream().map(p -> p.getId()).collect(Collectors.toList()) : "") +
                '}';
    }
}
