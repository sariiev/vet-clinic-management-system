package mas.vetclinic.model.entity.medication;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class MedicationCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @OneToMany(mappedBy = "category")
    private Set<Medication> medications = new HashSet<>();

    protected MedicationCategory() {}

    public MedicationCategory(String name) {
        setName(name);
    }

    protected void addMedicationInternal(Medication medication) throws IllegalArgumentException {
        if (medication == null) {
            throw new IllegalArgumentException("Medication cannot be null");
        }
        medications.add(medication);
    }

    protected void removeMedicationInternal(Medication medication) {
        medications.remove(medication);
    }

    // Getters and setters
    public Long getId() {
        return id;
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

    public Set<Medication> getMedications() {
        return Collections.unmodifiableSet(medications);
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MedicationCategory that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MedicationCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", medications=" + medications.stream().map(m -> m.getId()).collect(Collectors.toList()) +
                '}';
    }
}
