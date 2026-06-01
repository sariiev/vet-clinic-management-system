package mas.vetclinic.model.entity.pet;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Species {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @OneToMany(mappedBy = "species")
    private Set<Pet> pets = new HashSet<>();

    protected Species() {}

    public Species(String name) throws IllegalArgumentException {
        setName(name);
    }

    // Associations-related methods
    protected void addPetInternal(Pet pet) throws IllegalArgumentException {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        pets.add(pet);
    }

    protected void removePetInternal(Pet pet) {
        pets.remove(pet);
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

    public Set<Pet> getPets() {
        return Collections.unmodifiableSet(pets);
    }

    // Other methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Species that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Species{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pets=" + pets.stream().map(p -> p.getId()).collect(Collectors.toList()) +
                '}';
    }
}
