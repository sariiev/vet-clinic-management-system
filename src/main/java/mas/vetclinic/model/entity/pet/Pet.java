package mas.vetclinic.model.entity.pet;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.PetOwner;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 12)
    private String registrationNumber;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, length = 30)
    private String name;

    @NotNull(message = "Gender cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetGender gender;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Chip number cannot exceed 20 characters")
    @Column(nullable = true, unique = true, length = 20)
    private String chipNumber;

    @NotNull(message = "Species cannot be null")
    @ManyToOne
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private PetOwner owner;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Appointment> appointments = new HashSet<>();

    protected Pet() {}

    public Pet(String name, PetGender gender, LocalDate dateOfBirth, String chipNumber, Species species, PetOwner owner) throws IllegalArgumentException {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be null or blank");
        if (gender == null)
            throw new IllegalArgumentException("Gender cannot be null");
        if (dateOfBirth == null)
            throw new IllegalArgumentException("Date of birth cannot be null");
        if (dateOfBirth.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        if (species == null)
            throw new IllegalArgumentException("Species cannot be null");
        if (owner == null)
            throw new IllegalArgumentException("Owner cannot be null");
        if (owner instanceof Person person && !person.isIndividualClient()) {
            throw new IllegalArgumentException("Person must be an individual client to have a pet");
        }

        this.registrationNumber = "PET-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.chipNumber = (chipNumber == null || chipNumber.isBlank()) ? null : chipNumber;
        setSpecies(species);
        setOwner(owner);
    }

    // Associations-related methods
    public void clearOwnerInternal() {
        this.owner = null;
    }

    public void setSpecies(Species species) throws IllegalArgumentException {
        if (species == null) {
            throw new IllegalArgumentException("Species cannot be null");
        }
        if (this.species != null) {
            this.species.removePetInternal(this);
        }
        this.species = species;
        species.addPetInternal(this);
    }

    public void setOwner(PetOwner owner) throws IllegalArgumentException {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        if (owner instanceof Person person && !person.isIndividualClient()) {
            throw new IllegalArgumentException("Person must be an individual client to have a pet");
        }
        if (this.owner != null) {
            this.owner.removePetInternal(this);
        }
        this.owner = owner;
        owner.addPetInternal(this);
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
    public Long getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws IllegalArgumentException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public PetGender getGender() {
        return gender;
    }

    public void setGender(PetGender gender) throws IllegalArgumentException {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) throws IllegalArgumentException {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public String getChipNumber() {
        return chipNumber;
    }

    public void setChipNumber(String chipNumber) {
        this.chipNumber = (chipNumber == null || chipNumber.isBlank()) ? null : chipNumber;
    }

    public Species getSpecies() {
        return species;
    }

    public PetOwner getOwner() {
        return owner;
    }

    public Set<Appointment> getAppointments() {
        return Collections.unmodifiableSet(appointments);
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pet that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", chipNumber=" + chipNumber +
                ", species=" + species.getId() +
                ", owner=" + (owner != null ? owner.getId() : "null") +
                ", appointments=" + appointments.stream().map(a -> a.getId()).collect(Collectors.toList()) +
                '}';
    }
}
