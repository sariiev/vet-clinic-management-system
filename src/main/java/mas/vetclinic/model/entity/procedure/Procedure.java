package mas.vetclinic.model.entity.procedure;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Procedure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(nullable = true, length = 500)
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToMany
    @JoinTable(
            name = "procedure_specializations",
            joinColumns = @JoinColumn(name = "procedure_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<Specialization> requiredSpecializations = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "procedure_certifications",
            joinColumns = @JoinColumn(name = "procedure_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    private Set<Certification> requiredCertifications = new HashSet<>();

    @OneToMany(mappedBy = "procedure")
    private Set<PerformedProcedure> performedProcedures = new HashSet<>();

    protected Procedure() {}

    public Procedure(Set<Certification> requiredCertifications, Set<Specialization> requiredSpecializations, String price, String description, String name) throws IllegalArgumentException{
        setName(name);
        setPrice(price);
        setDescription(description);
        if (requiredCertifications != null) {
            for (Certification c : requiredCertifications) {
                if (c == null) {
                    throw new IllegalArgumentException("Certification cannot be null");
                }
            }
        }
        if (requiredSpecializations != null) {
            for (Specialization s : requiredSpecializations) {
                if (s == null) {
                    throw new IllegalArgumentException("Specialization cannot be null");
                }
            }
        }
        if (requiredCertifications != null) {
            for (Certification c : requiredCertifications) {
                addRequiredCertification(c);
            }
        }
        if (requiredSpecializations != null) {
            for (Specialization s : requiredSpecializations) {
                addRequiredSpecialization(s);
            }
        }
    }

    // Associations-related methods
    public void addRequiredSpecialization(Specialization specialization) throws IllegalArgumentException {
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null");
        }
        requiredSpecializations.add(specialization);
        specialization.addProcedureInternal(this);
    }

    public void removeRequiredSpecialization(Specialization specialization) throws IllegalArgumentException {
        if (specialization == null) {
            throw new IllegalArgumentException("Specialization cannot be null");
        }
        requiredSpecializations.remove(specialization);
        specialization.removeProcedureInternal(this);
    }

    public void addRequiredCertification(Certification certification) throws IllegalArgumentException {
        if (certification == null) {
            throw new IllegalArgumentException("Certification cannot be null");
        }
        requiredCertifications.add(certification);
        certification.addProcedureInternal(this);
    }

    public void removeRequiredCertification(Certification certification) throws IllegalArgumentException {
        if (certification == null) {
            throw new IllegalArgumentException("Certification cannot be null");
        }
        requiredCertifications.remove(certification);
        certification.removeProcedureInternal(this);
    }

    protected void addPerformedProcedureInternal(PerformedProcedure performedProcedure) throws IllegalArgumentException {
        if (performedProcedure == null) {
            throw new IllegalArgumentException("Performed procedure cannot be null");
        }
        performedProcedures.add(performedProcedure);
    }

    protected void removePerformedProcedureInternal(PerformedProcedure performedProcedure) {
        performedProcedures.remove(performedProcedure);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Set<Specialization> getRequiredSpecializations() {
        return Collections.unmodifiableSet(requiredSpecializations);
    }

    public Set<Certification> getRequiredCertifications() {
        return Collections.unmodifiableSet(requiredCertifications);
    }

    public void setName(String name) throws IllegalArgumentException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public void setDescription(String description) throws IllegalArgumentException {
        this.description = description;
    }

    public void setPrice(String price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        try {
            BigDecimal value = new BigDecimal(price);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be positive");
            }
            this.price = value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a valid number");
        }
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Procedure that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Procedure{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", requiredSpecializations=" + requiredSpecializations.stream().map(s -> s.getId()).collect(Collectors.toList()) +
                ", requiredCertifications=" + requiredCertifications.stream().map(s -> s.getId()).collect(Collectors.toList()) +
                ", performedProcedures=" + performedProcedures.stream().map(s -> s.getId()).collect(Collectors.toList()) +
                '}';
    }
}
