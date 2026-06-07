package mas.vetclinic.controller;

import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.service.AppointmentService;
import mas.vetclinic.service.VeterinarianService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class VeterinarianController {
    private final AppointmentService appointmentService;
    private final VeterinarianService veterinarianService;

    public VeterinarianController(AppointmentService appointmentService,  VeterinarianService veterinarianService) {
        this.appointmentService = appointmentService;
        this.veterinarianService = veterinarianService;
    }

    @GetMapping("/veterinarians")
    public String index(Model model) {
        model.addAttribute("veterinarians", veterinarianService.getAllVeterinarians());
        return "veterinarians";
    }

    @GetMapping("/veterinarians/search")
    public String search(@RequestParam String query,
                         Model model) {
        List<Veterinarian> veterinarians = veterinarianService.searchByName(query);

        model.addAttribute("veterinarians", veterinarians);
        return "fragments/veterinarian-search-results :: results";
    }
}
