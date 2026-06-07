package mas.vetclinic.controller;

import jakarta.servlet.http.HttpServletResponse;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.service.AppointmentService;
import mas.vetclinic.service.VeterinarianService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final VeterinarianService veterinarianService;

    public AppointmentController(AppointmentService appointmentService, VeterinarianService veterinarianService) {
        this.appointmentService = appointmentService;
        this.veterinarianService = veterinarianService;
    }

    @GetMapping("/veterinarians/{id}/schedule")
    public String schedule(@PathVariable Long id,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           Model model) {
        Veterinarian veterinarian = veterinarianService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));

        date = (date != null) ? date : LocalDate.now();

        model.addAttribute("veterinarian", veterinarian);
        model.addAttribute("week", appointmentService.getWeekSchedule(id, date));
        model.addAttribute("today", LocalDate.now());
        return "fragments/schedule :: schedule";
    }

    @GetMapping("/veterinarians/{veterinarianId}/booked-slots")
    @ResponseBody
    public List<AppointmentService.BookedSlot> bookedSlots(
            @PathVariable Long veterinarianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.getBookedSlots(veterinarianId, date);
    }

    @PostMapping("/appointments")
    public String create(@RequestParam Long veterinarianId,
                         @RequestParam Long petId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                         @RequestParam AppointmentDuration duration,
                         Model model, HttpServletResponse response) {
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        Appointment appointment = appointmentService.scheduleAppointment(veterinarianId, petId, startDateTime, duration);

        response.setHeader("X-Owner-Email", appointment.getPet().getOwner().getEmailAddress());

        Veterinarian veterinarian = veterinarianService.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));
        model.addAttribute("veterinarian", veterinarian);
        model.addAttribute("week", appointmentService.getWeekSchedule(veterinarianId, date));
        model.addAttribute("today", LocalDate.now());
        return "fragments/schedule :: schedule";
    }

    @GetMapping("/appointments/dialog")
    public String dialog(@RequestParam Long veterinarianId, Model model) {
        Veterinarian veterinarian = veterinarianService.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));
        LocalDate today = LocalDate.now();
        model.addAttribute("veterinarian", veterinarian);
        model.addAttribute("durations", AppointmentDuration.values());
        model.addAttribute("today", today.toString());
        model.addAttribute("maxDate", today.plusDays(Appointment.getSchedulingWindowSizeDays()).toString());
        model.addAttribute("workingHoursStart", Appointment.getWorkingHoursStart().toString());
        model.addAttribute("workingHoursEnd", Appointment.getWorkingHoursEnd().toString());
        return "fragments/appointment-dialog :: dialog";
    }
}
