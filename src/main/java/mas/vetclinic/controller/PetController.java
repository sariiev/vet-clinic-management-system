package mas.vetclinic.controller;

import jakarta.validation.Valid;
import mas.vetclinic.model.dto.PetDTO;
import mas.vetclinic.model.entity.person.PetOwner;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.service.PersonService;
import mas.vetclinic.service.PetService;
import mas.vetclinic.service.ShelterService;
import mas.vetclinic.service.SpeciesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class PetController {
    private final PetService petService;
    private final ShelterService shelterService;
    private final PersonService personService;
    private final SpeciesService speciesService;

    public PetController(PetService petService, ShelterService shelterService, PersonService personService, SpeciesService speciesService) {
        this.petService = petService;
        this.shelterService = shelterService;
        this.personService = personService;
        this.speciesService = speciesService;
    }

    @GetMapping("pet-owners/{ownerId}/pets")
    public String ownerPets(@PathVariable Long ownerId,
                       @RequestParam String ownerType,
                       @RequestParam Long veterinarianId,
                       Model model) {
        PetOwner owner;
        if (ownerType.equals("shelter")) {
            owner = shelterService.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        } else if (ownerType.equals("individual-client")) {
            owner = personService.findIndividualClientById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Individual client not found"));
        } else {
            throw new IllegalArgumentException("Owner type must be shelter or individual client");
        }

        List<Pet> pets = owner.getPets().stream().toList();

        model.addAttribute("owner", owner);
        model.addAttribute("pets", pets.stream().limit(PetService.OWNER_PETS_LIMIT).toList());
        model.addAttribute("petsTruncated", pets.size() > PetService.OWNER_PETS_LIMIT);
        model.addAttribute("veterinarianId", veterinarianId);
        model.addAttribute("type", ownerType);
        return "fragments/owner-pets :: pets";
    }

    @GetMapping("/pets/search")
    public String search(@RequestParam String query,
                         @RequestParam Long veterinarianId,
                         Model model) {
        Optional<Pet> pet = petService.findByRegistrationNumber(query).or(() -> petService.findByChipNumber(query));
        model.addAttribute("pet", pet.orElse(null));
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/pet-search-results :: results";
    }

    @GetMapping("/pet-owners/{ownerId}/pets/search")
    public String searchOwnerPets(@PathVariable Long ownerId,
                                  @RequestParam String ownerType,
                                  @RequestParam String query,
                                  @RequestParam Long veterinarianId,
                                  Model model) {
        PetOwner owner;
        if (ownerType.equals("shelter")) {
            owner = shelterService.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Shelter not found"));
        } else if (ownerType.equals("individual-client")) {
            owner = personService.findIndividualClientById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Individual client not found"));
        } else {
            throw new IllegalArgumentException("Owner type must be shelter or individual client");
        }
        String queryLower = query.toLowerCase();
        List<Pet> pets = owner.getPets().stream()
                .filter(pet -> pet.getName().toLowerCase().contains(queryLower)
                || pet.getRegistrationNumber().toLowerCase().equals(queryLower)
                || (pet.getChipNumber() != null && pet.getChipNumber().toLowerCase().equals(queryLower))).toList();
        model.addAttribute("pets", pets.stream().limit(PetService.OWNER_PETS_LIMIT).toList());
        model.addAttribute("petsTruncated", pets.size() > PetService.OWNER_PETS_LIMIT);
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/owner-pets :: petList";
    }

    @GetMapping("/pets/{id}/select")
    public String selectPet(@PathVariable Long id, Model model) {
        Pet pet = petService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        model.addAttribute("pet", pet);
        model.addAttribute("owner", pet.getOwner());
        return "fragments/selected-pet :: selectedPet";
    }


    @GetMapping("/pets/register")
    public String register(@RequestParam Long ownerId,
                           @RequestParam String ownerType,
                           @RequestParam Long veterinarianId,
                           Model model) {
        model.addAttribute("species", speciesService.getAllSpecies());
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("type", ownerType);
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/register-pet-dialog :: dialog";
    }

    @PostMapping("/pets")
    public String create(@Valid @ModelAttribute PetDTO petDTO,
                         @RequestParam Long veterinarianId,
                         Model model) {
        boolean ownerIsShelter = petDTO.getOwnerType().equals("shelter");
        Pet pet = petService.createPet(
                petDTO.getName(),
                petDTO.getGender(),
                petDTO.getDateOfBirth(),
                petDTO.getChipNumber(),
                petDTO.getSpeciesId(),
                petDTO.getOwnerId(),
                ownerIsShelter
        );

        PetOwner owner = ownerIsShelter ?
                shelterService.findById(petDTO.getOwnerId())
                        .orElseThrow(() -> new IllegalArgumentException("Shelter not found")) :
                personService.findIndividualClientById(petDTO.getOwnerId())
                        .orElseThrow(() -> new IllegalArgumentException("Individual client not found"));

        List<Pet> pets = owner.getPets().stream().toList();

        model.addAttribute("owner", owner);
        model.addAttribute("pet", pet);
        model.addAttribute("pets", pets.stream().limit(PetService.OWNER_PETS_LIMIT).toList());
        model.addAttribute("petsTruncated", pets.size() > PetService.OWNER_PETS_LIMIT);
        model.addAttribute("type", petDTO.getOwnerType());
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/selected-pet :: selectedPet";
    }
}
