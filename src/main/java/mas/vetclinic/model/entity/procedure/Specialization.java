package mas.vetclinic.model.entity.procedure;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.person.Veterinarian;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Specialization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @ManyToMany(mappedBy = "specializations")
    private Set<Veterinarian> veterinarians = new HashSet<>();

    @ManyToMany(mappedBy = "requiredSpecializations")
    private Set<Procedure> procedures = new HashSet<>();

    protected Specialization() {}

    public Specialization(String name) throws IllegalArgumentException {
        setName(name);
    }

    // Associations-related methods
    public void addVeterinarianInternal(Veterinarian veterinarian) throws IllegalArgumentException {
        if (veterinarian == null) {
            throw new IllegalArgumentException("Veterinarian cannot be null");
        }
        veterinarians.add(veterinarian);
    }

    public void removeVeterinarianInternal(Veterinarian veterinarian) {
        veterinarians.remove(veterinarian);
    }

    public void addVeterinarian(Veterinarian veterinarian) throws IllegalArgumentException {
        if (veterinarian == null) {
            throw new IllegalArgumentException("Veterinarian cannot be null");
        }
        veterinarian.addSpecialization(this);
    }

    public void removeVeterinarian(Veterinarian veterinarian) throws IllegalArgumentException {
        if (veterinarian == null) {
            throw new IllegalArgumentException("Veterinarian cannot be null");
        }
        veterinarian.removeSpecialization(this);
    }

    protected void addProcedureInternal(Procedure procedure) throws IllegalArgumentException {
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        procedures.add(procedure);
    }

    protected void removeProcedureInternal(Procedure procedure) {
        procedures.remove(procedure);
    }

    public void addProcedure(Procedure procedure) throws IllegalArgumentException {
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        procedure.addRequiredSpecialization(this);
    }

    public void removeProcedure(Procedure procedure) throws IllegalArgumentException {
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        procedure.removeRequiredSpecialization(this);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public Set<Veterinarian> getVeterinarians() {
        return Collections.unmodifiableSet(veterinarians);
    }

    public Set<Procedure> getProcedures() {
        return Collections.unmodifiableSet(procedures);
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Specialization that)) return false;
        return getId() != null && getId().equals(that.getId());
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Specialization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", veterinarians=" + veterinarians.stream().map(v -> v.getId()).collect(Collectors.toList()) +
                ", procedures=" + procedures.stream().map(Procedure::getId).collect(Collectors.toList()) +
                '}';
    }
}
