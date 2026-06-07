package mas.vetclinic.controller;

import jakarta.validation.Valid;
import mas.vetclinic.model.dto.IndividualClientDTO;
import mas.vetclinic.model.dto.ShelterDTO;
import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.Shelter;
import mas.vetclinic.service.PersonService;
import mas.vetclinic.service.ShelterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PetOwnerController {
    private final PersonService personService;
    private final ShelterService shelterService;

    public PetOwnerController(PersonService personService, ShelterService shelterService) {
        this.personService = personService;
        this.shelterService = shelterService;
    }

    @GetMapping("pet-owners/search")
    public String search(@RequestParam String query,
                         @RequestParam Long veterinarianId,
                         Model model) {
        List<Person> individualClients = personService.searchIndividualClientsByNameOrPhoneNumber(query);
        List<Shelter> shelters = shelterService.searchByName(query);
        model.addAttribute("individualClients", individualClients);
        model.addAttribute("shelters", shelters);
        model.addAttribute("truncated",
                individualClients.size() >= PersonService.SEARCH_LIMIT
                        || shelters.size() >= ShelterService.SEARCH_LIMIT);
        model.addAttribute("searchLimit", PersonService.SEARCH_LIMIT);
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/owner-search-results :: results";
    }

    @GetMapping("/individual-clients/register")
    public String registerClientDialog(@RequestParam Long veterinarianId, Model model) {
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/register-individual-client-dialog :: dialog";
    }

    @GetMapping("/shelters/register")
    public String registerShelterDialog(@RequestParam Long veterinarianId, Model model) {
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/register-shelter-dialog :: dialog";
    }

    @PostMapping("/individual-clients")
    public String createIndividualClient(@Valid @ModelAttribute IndividualClientDTO individualClientDTO,
                                         @RequestParam Long veterinarianId,
                                         Model model) {
        Person individualClient = personService.createIndividualClient(
                individualClientDTO.getFirstName(),
                individualClientDTO.getLastName(),
                individualClientDTO.getDateOfBirth(),
                individualClientDTO.getPhoneNumber(),
                individualClientDTO.getEmailAddress()
        );
        model.addAttribute("owner", individualClient);
        model.addAttribute("pets", individualClient.getPets());
        model.addAttribute("petsTruncated", false);
        model.addAttribute("type", "individual-client");
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/owner-pets :: pets";
    }

    @PostMapping("/shelters")
    public String createShelter(@Valid @ModelAttribute ShelterDTO shelterDTO,
                                @RequestParam Long veterinarianId,
                                Model model) {
        Shelter shelter = shelterService.createShelter(
                shelterDTO.getName(),
                shelterDTO.getEmailAddress(),
                shelterDTO.getPhoneNumbers()
        );
        model.addAttribute("owner", shelter);
        model.addAttribute("pets", shelter.getPets());
        model.addAttribute("petsTruncated", false);
        model.addAttribute("type", "shelter");
        model.addAttribute("veterinarianId", veterinarianId);
        return "fragments/owner-pets :: pets";
    }
}
