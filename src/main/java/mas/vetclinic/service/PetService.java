package mas.vetclinic.service;

import mas.vetclinic.model.entity.person.PetOwner;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;
import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.repository.*;
import mas.vetclinic.view.PetView;
import mas.vetclinic.view.SelectedPetView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PetService {
    public static final int OWNER_PETS_LIMIT = 20;

    private final SpeciesRepository speciesRepository;
    private final PetRepository petRepository;
    private final PetOwnerRepository petOwnerRepository;

    public PetService(SpeciesRepository speciesRepository, PetRepository petRepository, PetOwnerRepository petOwnerRepository) {
        this.speciesRepository = speciesRepository;
        this.petRepository = petRepository;
        this.petOwnerRepository = petOwnerRepository;
    }

    @Transactional
    public Pet createPet(String name, PetGender gender, LocalDate dateOfBirth, String chipNumber, Long speciesId,
                         Long ownerId) throws IllegalArgumentException {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Species not found"));

        PetOwner owner = petOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        if (chipNumber != null && !chipNumber.isBlank() && petRepository.existsByChipNumberEquals(chipNumber)) {
            throw new IllegalArgumentException("Chip number already exists");
        }

        Pet pet = new Pet(name, gender, dateOfBirth, chipNumber, species, owner);
        return petRepository.save(pet);
    }

    public List<PetView> getOwnerPetViews(Long ownerId) {
        PetOwner owner = petOwnerRepository.findByIdFetchingPets(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        return owner.getPets().stream().map(p -> PetView.of(p)).toList();
    }

    public Optional<PetView> findPetViewByRegistrationNumberOrChipNumber(String query) {
        return petRepository.findByRegistrationNumberFetchingSpecies(query)
                .or(() -> petRepository.findByChipNumberFetchingSpecies(query))
                .map(p -> PetView.of(p));
    }

    public Optional<SelectedPetView> findSelectedPetView(Long id) {
        return petRepository.findByIdFetchingOwner(id).map(p -> SelectedPetView.of(p));
    }
}
