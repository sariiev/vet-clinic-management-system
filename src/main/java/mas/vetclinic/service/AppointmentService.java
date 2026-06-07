package mas.vetclinic.service;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.repository.AppointmentRepository;
import mas.vetclinic.repository.PetRepository;
import mas.vetclinic.repository.VeterinarianRepository;
import mas.vetclinic.view.AppointmentView;
import mas.vetclinic.view.BookedSlot;
import mas.vetclinic.view.DaySchedule;
import mas.vetclinic.view.WeekSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
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
    public ScheduledAppointment scheduleAppointment(Long veterinarianId, Long petId, LocalDateTime startDateTime,
                                           AppointmentDuration duration) throws IllegalArgumentException {
        if (startDateTime.getMinute() % 15 != 0 || startDateTime.getSecond() != 0) {
            throw new IllegalArgumentException("Appointment time must be in 15-minute steps");
        }

        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled for the past");
        }

        if (!Appointment.isWithinSchedulingWindow(startDateTime.toLocalDate())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled more than"
                    + Appointment.getSchedulingWindowSizeDays() + " days in advance");
        }

        if (!Appointment.isWithinWorkingHours(startDateTime.toLocalTime(), duration)) {
            throw new IllegalArgumentException("Appointment must be within working hours");
        }

        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));

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

        return new ScheduledAppointment(appointment, ownerEmail);
    }

    public record ScheduledAppointment(Appointment appointment, String email) {}

    public List<BookedSlot> getBookedSlots(Long veterinarianId, LocalDate date) {
        Veterinarian veterinarian = veterinarianRepository.findByIdFetchingAppointments(veterinarianId)
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

    public WeekSchedule getWeekSchedule(Long veterinarianId, LocalDate dateInWeek) {
        Veterinarian veterinarian = veterinarianRepository.findByIdFetchingSchedule(veterinarianId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinarian not found"));

        LocalDate monday = dateInWeek.minusDays(dateInWeek.getDayOfWeek().getValue() - 1);

        List<DaySchedule> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            List<AppointmentView> dayViews = veterinarian.getAppointments().stream()
                    .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                    .filter(a -> a.getStartDateTime().toLocalDate().equals(day))
                    .sorted(Comparator.comparing(a -> a.getStartDateTime()))
                    .map(a -> AppointmentView.of(a))
                    .toList();
            days.add(new DaySchedule(day, dayViews));
        }
        return new WeekSchedule(monday, monday.plusDays(6), monday.minusWeeks(1), monday.plusWeeks(1), days);
    }
}
