package mas.vetclinic.model.entity.person;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import mas.vetclinic.model.entity.pet.Pet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PetOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Email address must be valid")
    @Column(nullable = true)
    private String emailAddress;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pet> pets = new HashSet<>();

    protected PetOwner() {}

    public Long getId() {
        return id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) throws IllegalArgumentException {
        if (emailAddress == null || emailAddress.isBlank())
            throw new IllegalArgumentException("Email address cannot be null or blank");
        this.emailAddress = emailAddress;
    }

    public void addPetInternal(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        pets.add(pet);
    }

    public void removePetInternal(Pet pet) {
        pets.remove(pet);
    }

    public void addPet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        pet.setOwner(this);
    }

    public void removePet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (!this.equals(pet.getOwner())) {
            throw new IllegalArgumentException("Pet does not belong to this owner");
        }
        pets.remove(pet);
        pet.clearOwnerInternal();
    }

    public Set<Pet> getPets() {
        return Collections.unmodifiableSet(pets);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PetOwner that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 17;
    }

}
