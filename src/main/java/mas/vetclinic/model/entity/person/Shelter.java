package mas.vetclinic.model.entity.person;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Shelter extends PetOwner {
    private static final String PHONE_REGEX = "^\\+\\d{7,15}$";

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 30, message = "Name cannot exceed 30 characters")
    @Column(nullable = false, length = 30)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shelter_phones", joinColumns = @JoinColumn(name = "shelter_id"))
    @Column(name = "phone_number", unique = true)
    private Set<String> phoneNumbers = new HashSet<>();

    protected Shelter() {}

    public Shelter(String name, String emailAddress, Set<String> phoneNumbers) throws IllegalArgumentException {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            throw new IllegalArgumentException("Phone numbers cannot be null or empty");
        }
        for (String phoneNumber : phoneNumbers) {
            addPhoneNumber(phoneNumber);
        }
        setName(name);
        setEmailAddress(emailAddress);
    }

    // Associations-related methods

    // Getters and setters
    public Long getId() {
        return super.getId();
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

    public Set<String> getPhoneNumbers() {
        return Collections.unmodifiableSet(phoneNumbers);
    }

    public void addPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        if (!phoneNumber.matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("Phone must contain 7-15 digits, starting with +");
        }
        phoneNumbers.add(phoneNumber);
    }

    public void removePhoneNumber(String phoneNumber) {
        phoneNumbers.remove(phoneNumber);
    }

    @Override
    public String toString() {
        return "Shelter{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", emailAddress='" + getEmailAddress() + '\'' +
                ", phoneNumbers=" + phoneNumbers +
                ", pets=" + getPets().stream().map(p -> p.getId()).collect(Collectors.toList()) +
                '}';
    }
}
