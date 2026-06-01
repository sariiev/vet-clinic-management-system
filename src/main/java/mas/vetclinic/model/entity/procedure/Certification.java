package mas.vetclinic.model.entity.procedure;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mas.vetclinic.model.entity.person.Nurse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @ManyToMany(mappedBy = "certifications")
    private Set<Nurse> nurses = new HashSet<>();

    @ManyToMany(mappedBy = "requiredCertifications")
    private Set<Procedure> procedures = new HashSet<>();

    protected Certification() {}

    public Certification(String name) throws IllegalArgumentException {
        setName(name);
    }

    // Associations-related methods
    public void addNurseInternal(Nurse nurse) {
        if (nurse == null) {
            throw new IllegalArgumentException("Nurse cannot be null");
        }
        nurses.add(nurse);
    }

    public void removeNurseInternal(Nurse nurse) {
        nurses.remove(nurse);
    }

    public void addNurse(Nurse nurse) {
        if (nurse == null) {
            throw new IllegalArgumentException("Nurse cannot be null");
        }
        nurse.addCertification(this);
    }

    public void removeNurse(Nurse nurse) throws IllegalStateException {
        if (nurse == null) {
            throw new IllegalArgumentException("Nurse cannot be null");
        }
        nurse.removeCertification(this);
    }

    protected void addProcedureInternal(Procedure procedure) {
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        procedures.add(procedure);
    }

    protected void removeProcedureInternal(Procedure procedure) {
        procedures.remove(procedure);
    }

    public void addProcedure(Procedure procedure) {
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        procedure.addRequiredCertification(this);
    }

    public void removeProcedure(Procedure procedure) {
        if (procedure == null) {
            throw new IllegalArgumentException("Procedure cannot be null");
        }
        procedure.removeRequiredCertification(this);
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

    public Set<Nurse> getNurses() {
        return Collections.unmodifiableSet(nurses);
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
        if (!(o instanceof Certification that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Certification{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nurses=" + nurses.stream().map(n -> n.getId()).collect(Collectors.toList()) +
                '}';
    }
}
