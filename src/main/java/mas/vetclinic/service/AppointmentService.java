package mas.vetclinic.service;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.repository.AppointmentRepository;
import mas.vetclinic.repository.PetRepository;
import mas.vetclinic.repository.VeterinarianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final PetRepository petRepository;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository, VeterinarianRepository veterinarianRepository, PetRepository petRepository, EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.petRepository = petRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Appointment scheduleAppointment(Long veterinarianId, Long petId, LocalDateTime startDateTime,
                                           AppointmentDuration duration) throws IllegalArgumentException {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));

        if (!Appointment.isWithinSchedulingWindow(startDateTime.toLocalDate())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled more than"
                    + Appointment.getSchedulingWindowSizeDays() + " days in advance");
        }

        if (!Appointment.isWithinWorkingHours(startDateTime.toLocalTime(), duration)) {
            throw new IllegalArgumentException("Appointment must be within working hours");
        }

        boolean hasCollision = veterinarian.getAppointments().stream()
                .anyMatch(a -> a.overlapsWith(startDateTime, duration));
        if (hasCollision) {
            throw new IllegalArgumentException("Selected time slot overlaps with an existing appointment");
        }

        Appointment appointment = appointmentRepository.save(new Appointment(veterinarian, pet, startDateTime, duration));

        String ownerEmail = pet.getOwner().getEmailAddress();
        emailService.sendAppointmentConfirmationEmail(ownerEmail,
                veterinarian.getFirstName() + " " + veterinarian.getLastName(),
                pet.getName(),
                startDateTime.toString()
        );

        return appointment;
    }

    @Transactional(readOnly = true)
    public List<BookedSlot> getBookedSlots(Long veterinarianId, LocalDate date) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        List<Appointment> appointments = veterinarian.getAppointments().stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> a.getStartDateTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(a -> a.getStartDateTime()))
                .toList();

        List<BookedSlot> bookedSlots = appointments.stream()
                .map(a -> {
                    LocalDateTime startDateTime = a.getStartDateTime();
                    LocalDateTime endDateTime = startDateTime.plus(a.getExpectedDuration().getDuration());
                    return new BookedSlot(startDateTime.format(formatter),  endDateTime.format(formatter));
                }).toList();
        return bookedSlots;
    }

    public record BookedSlot(String startTime, String endTime) {}

    @Transactional(readOnly = true)
    public WeekSchedule getWeekSchedule(Long veterinarianId, LocalDate dateInWeek) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));

        LocalDate monday = dateInWeek.minusDays(dateInWeek.getDayOfWeek().getValue() - 1);

        List<DaySchedule> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            List<Appointment> dayAppointments = veterinarian.getAppointments().stream()
                    .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                    .filter(a -> a.getStartDateTime().toLocalDate().equals(day))
                    .sorted(Comparator.comparing(a -> a.getStartDateTime()))
                    .toList();
            days.add(new DaySchedule(day, dayAppointments));
        }
        return new WeekSchedule(monday, monday.plusDays(6), monday.minusWeeks(1), monday.plusWeeks(1), days);
    }

    public record DaySchedule(LocalDate date, List<Appointment> appointments) {}

    public record WeekSchedule(LocalDate monday, LocalDate sunday,
                               LocalDate previousWeek, LocalDate nextWeek, List<DaySchedule> days) {}
}
