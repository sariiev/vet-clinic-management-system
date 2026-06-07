package mas.vetclinic.controller;

import jakarta.validation.Valid;
import mas.vetclinic.model.dto.PetDTO;
import mas.vetclinic.model.entity.person.PetOwner;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.service.*;
import mas.vetclinic.view.PetView;
import mas.vetclinic.view.SelectedPetView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class PetController {
    private final PetService petService;
    private final SpeciesService speciesService;
    private final PetOwnerService petOwnerService;

    public PetController(PetService petService, SpeciesService speciesService, PetOwnerService petOwnerService) {
        this.petService = petService;
        this.speciesService = speciesService;
        this.petOwnerService = petOwnerService;
    }

    @GetMapping("pet-owners/{ownerId}/pets")
    public String ownerPets(@PathVariable Long ownerId,
                       @RequestParam Long veterinarianId,
                       Model model) {
        PetOwner owner = petOwnerService.findById(ownerId);

        List<PetView> pets = petService.getOwnerPetViews(ownerId);

        model.addAttribute("owner", owner);
        model.addAttribute("pets", pets.stream().limit(PetService.OWNER_PETS_LIMIT).toList());
        model.addAttribute("petsTruncated", pets.size() > PetService.OWNER_PETS_LIMIT);
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/owner-pets :: pets";
    }

    @GetMapping("/pets/search")
    public String search(@RequestParam String query,
                         @RequestParam Long veterinarianId,
                         Model model) {
        Optional<PetView> pet = petService.findPetViewByRegistrationNumberOrChipNumber(query);
        model.addAttribute("pet", pet.orElse(null));
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/pet-search-results :: results";
    }

    @GetMapping("/pet-owners/{ownerId}/pets/search")
    public String searchOwnerPets(@PathVariable Long ownerId,
                                  @RequestParam String query,
                                  @RequestParam Long veterinarianId,
                                  Model model) {
        String queryLower = query.toLowerCase();

        List<PetView> pets = petService.getOwnerPetViews(ownerId).stream()
                .filter(pet -> pet.name().toLowerCase().equals(queryLower)
                || pet.registrationNumber().toLowerCase().equals(queryLower)
                || (pet.chipNumber() != null && pet.chipNumber().toLowerCase().equals(queryLower))).toList();

        model.addAttribute("pets", pets.stream().limit(PetService.OWNER_PETS_LIMIT).toList());
        model.addAttribute("petsTruncated", pets.size() > PetService.OWNER_PETS_LIMIT);
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/owner-pets :: petList";
    }

    @GetMapping("/pets/{id}/select")
    public String selectPet(@PathVariable Long id, Model model) {
        SelectedPetView pet = petService.findSelectedPetView(id)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        model.addAttribute("pet", pet);
        return "fragments/selected-pet :: selectedPet";
    }


    @GetMapping("/pets/register")
    public String register(@RequestParam Long ownerId,
                           @RequestParam Long veterinarianId,
                           Model model) {
        model.addAttribute("species", speciesService.getAllSpecies());
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/register-pet-dialog :: dialog";
    }

    @PostMapping("/pets")
    public String create(@Valid @ModelAttribute PetDTO petDTO,
                         Model model) {
        Pet pet = petService.createPet(
                petDTO.getName(),
                petDTO.getGender(),
                petDTO.getDateOfBirth(),
                petDTO.getChipNumber(),
                petDTO.getSpeciesId(),
                petDTO.getOwnerId()
        );

        model.addAttribute("pet", petService.findSelectedPetView(pet.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pet not found")));
        return "fragments/selected-pet :: selectedPet";
    }
}
