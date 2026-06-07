package mas.vetclinic.service;

import mas.vetclinic.model.entity.person.PetOwner;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;
import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.repository.PersonRepository;
import mas.vetclinic.repository.PetRepository;
import mas.vetclinic.repository.ShelterRepository;
import mas.vetclinic.repository.SpeciesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class PetService {
    public static final int OWNER_PETS_LIMIT = 20;

    private final SpeciesRepository speciesRepository;
    private final ShelterRepository shelterRepository;
    private final PersonRepository personRepository;
    private final PetRepository petRepository;

    public PetService(PersonRepository personRepository, SpeciesRepository speciesRepository, ShelterRepository shelterRepository, PetRepository petRepository) {
        this.personRepository = personRepository;
        this.speciesRepository = speciesRepository;
        this.shelterRepository = shelterRepository;
        this.petRepository = petRepository;
    }

    public Pet createPet(String name, PetGender gender, LocalDate dateOfBirth, String chipNumber, Long speciesId,
                         Long ownerId, boolean ownerIsShelter) throws IllegalArgumentException {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found"));

        PetOwner owner;
        if (ownerIsShelter) {
            owner = shelterRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        } else {
            owner = personRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Individual client not found"));
        }

        if (chipNumber != null && !chipNumber.isBlank() && petRepository.existsByChipNumberEquals(chipNumber)) {
            throw new IllegalArgumentException("Chip number already exists");
        }

        Pet pet = new Pet(name, gender, dateOfBirth, chipNumber, species, owner);
        return petRepository.save(pet);
    }

    public Optional<Pet> findByRegistrationNumber(String registrationNumber) {
        return petRepository.findByRegistrationNumber(registrationNumber);
    }

    public Optional<Pet> findByChipNumber(String chipNumber) {
        return petRepository.findByChipNumber(chipNumber);
    }

    public Optional<Pet> findById(Long petId) {
        return petRepository.findById(petId);
    }
}
