package mas.vetclinic.model.entity.medication;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @NotNull(message = "Category cannot be null")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private MedicationCategory category;

    @NotNull(message = "Form cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicationForm form;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(nullable = true, length = 500)
    private String description;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prescription> prescriptions = new HashSet<>();

    protected Medication() {}

    public Medication(String name, MedicationCategory category, MedicationForm form, String description) throws IllegalArgumentException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (form == null) {
            throw new IllegalArgumentException("Form cannot be null");
        }
        this.name = name;
        this.form = form;
        this.description = description;
        setCategory(category);
    }

    // Associations-related methods
    public void setCategory(MedicationCategory category) throws IllegalArgumentException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (this.category != null) {
            this.category.removeMedicationInternal(this);
        }
        this.category = category;
        category.addMedicationInternal(this);
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

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MedicationCategory getCategory() {
        return category;
    }

    public MedicationForm getForm() {
        return form;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public void setForm(MedicationForm form) throws IllegalArgumentException {
        if (form == null) {
            throw new IllegalArgumentException("Form cannot be null");
        }
        this.form = form;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Prescription> getPrescriptions() {
        return Collections.unmodifiableSet(prescriptions);
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Medication that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category.getId() +
                ", form=" + form +
                ", description='" + description + '\'' +
                '}';
    }
}
