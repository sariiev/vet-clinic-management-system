package mas.vetclinic;

import jakarta.persistence.EntityManager;
import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;
import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.model.entity.procedure.Specialization;
import mas.vetclinic.repository.*;
import mas.vetclinic.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ServiceTest {
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired VeterinarianRepository veterinarianRepository;
    @Autowired PetRepository petRepository;
    @Autowired PersonRepository personRepository;
    @Autowired ShelterRepository shelterRepository;
    @Autowired SpeciesRepository speciesRepository;
    @Autowired SpecializationRepository specializationRepository;
    @Autowired EntityManager em;

    private AppointmentService appointmentService;
    private PersonService personService;
    private ShelterService shelterService;
    private PetService petService;
    private VeterinarianService veterinarianService;
    private SpeciesService speciesService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, veterinarianRepository,
                petRepository, Mockito.mock(EmailService.class));
        personService = new PersonService(personRepository);
        shelterService = new ShelterService(shelterRepository);
        petService = new PetService(personRepository, speciesRepository, shelterRepository, petRepository);
        veterinarianService = new VeterinarianService(veterinarianRepository);
        speciesService = new SpeciesService(speciesRepository);
    }

    // ---------- fixtures ----------
    private Veterinarian persistVet(String phone, String license) {
        Specialization spec = specializationRepository.save(new Specialization("Surgery-" + license));
        return veterinarianRepository.save(new Veterinarian("Anna", "Vet", LocalDate.of(1985, 1, 1), phone,
                LocalDate.of(2015, 1, 1), "60", license, "Chamber",
                LocalDate.of(2015, 1, 1), LocalDate.of(2035, 1, 1), Set.of(spec)));
    }

    private Pet persistPet(String clientPhone, String petName) {
        Person owner = personRepository.save(new Person("John", "Doe", LocalDate.of(1990, 1, 1), clientPhone, "j@d.com"));
        Species species = speciesRepository.save(new Species("Dog-" + petName));
        return petRepository.save(new Pet(petName, PetGender.MALE, LocalDate.of(2020, 1, 1), null, species, owner));
    }

    private LocalDateTime tomorrowAt(int hour, int minute) {
        return LocalDate.now().plusDays(1).atTime(hour, minute);
    }

    // ========== AppointmentService.scheduleAppointment ==========
    @Test
    void scheduleAppointmentSucceeds() {
        Veterinarian vet = persistVet("+48100000001", "VET-S1");
        Pet pet = persistPet("+48200000001", "Rex");
        Appointment appt = appointmentService.scheduleAppointment(vet.getId(), pet.getId(),
                tomorrowAt(10, 0), AppointmentDuration.ONE_HOUR);
        assertNotNull(appt.getId());
        assertTrue(appointmentRepository.findById(appt.getId()).isPresent());
    }

    @Test
    void overlappingAppointmentRejected() {
        Veterinarian vet = persistVet("+48100000002", "VET-S2");
        Pet pet = persistPet("+48200000002", "Rex");
        appointmentService.scheduleAppointment(vet.getId(), pet.getId(), tomorrowAt(10, 0), AppointmentDuration.ONE_HOUR);
        assertThrows(IllegalArgumentException.class, () ->
                appointmentService.scheduleAppointment(vet.getId(), pet.getId(), tomorrowAt(10, 30), AppointmentDuration.ONE_HOUR));
    }

    @Test
    void adjacentAppointmentAllowed() {
        Veterinarian vet = persistVet("+48100000003", "VET-S3");
        Pet pet = persistPet("+48200000003", "Rex");
        appointmentService.scheduleAppointment(vet.getId(), pet.getId(), tomorrowAt(10, 0), AppointmentDuration.ONE_HOUR);
        assertDoesNotThrow(() ->
                appointmentService.scheduleAppointment(vet.getId(), pet.getId(), tomorrowAt(11, 0), AppointmentDuration.ONE_HOUR));
    }

    @Test
    void appointmentOutsideWorkingHoursRejected() {
        Veterinarian vet = persistVet("+48100000004", "VET-S4");
        Pet pet = persistPet("+48200000004", "Rex");
        assertThrows(IllegalArgumentException.class, () ->        // 09:00 — до открытия
                appointmentService.scheduleAppointment(vet.getId(), pet.getId(), tomorrowAt(9, 0), AppointmentDuration.ONE_HOUR));
    }

    @Test
    void appointmentBeyondSchedulingWindowRejected() {
        Veterinarian vet = persistVet("+48100000005", "VET-S5");
        Pet pet = persistPet("+48200000005", "Rex");
        LocalDateTime farFuture = LocalDate.now().plusDays(120).atTime(10, 0);
        assertThrows(IllegalArgumentException.class, () ->
                appointmentService.scheduleAppointment(vet.getId(), pet.getId(), farFuture, AppointmentDuration.ONE_HOUR));
    }

    @Test
    void scheduleWithUnknownVetOrPetThrows() {
        Veterinarian vet = persistVet("+48100000006", "VET-S6");
        Pet pet = persistPet("+48200000006", "Rex");
        assertThrows(IllegalArgumentException.class, () ->
                appointmentService.scheduleAppointment(999999L, pet.getId(), tomorrowAt(10, 0), AppointmentDuration.ONE_HOUR));
        assertThrows(IllegalArgumentException.class, () ->
                appointmentService.scheduleAppointment(vet.getId(), 999999L, tomorrowAt(10, 0), AppointmentDuration.ONE_HOUR));
    }

    // ========== AppointmentService.getBookedSlots / getWeekSchedule ==========
    @Test
    void getBookedSlotsReturnsSlotsForDate() {
        Veterinarian vet = persistVet("+48100000007", "VET-S7");
        Pet pet = persistPet("+48200000007", "Rex");
        LocalDateTime start = tomorrowAt(10, 0);
        appointmentService.scheduleAppointment(vet.getId(), pet.getId(), start, AppointmentDuration.ONE_HOUR);

        List<AppointmentService.BookedSlot> slots = appointmentService.getBookedSlots(vet.getId(), start.toLocalDate());
        assertEquals(1, slots.size());
        assertEquals("10:00", slots.get(0).startTime());
        assertEquals("11:00", slots.get(0).endTime());
        assertTrue(appointmentService.getBookedSlots(vet.getId(), start.toLocalDate().plusDays(1)).isEmpty());
    }

    @Test
    void getWeekScheduleGroupsByDay() {
        Veterinarian vet = persistVet("+48100000008", "VET-S8");
        Pet pet = persistPet("+48200000008", "Rex");
        LocalDateTime start = tomorrowAt(10, 0);
        appointmentService.scheduleAppointment(vet.getId(), pet.getId(), start, AppointmentDuration.ONE_HOUR);

        AppointmentService.WeekSchedule week = appointmentService.getWeekSchedule(vet.getId(), start.toLocalDate());
        assertEquals(7, week.days().size());
        long total = week.days().stream().mapToLong(d -> d.appointments().size()).sum();
        assertEquals(1, total);
    }

    // ========== PersonService ==========
    @Test
    void createIndividualClientSucceeds() {
        Person client = personService.createIndividualClient("John", "Doe", LocalDate.of(1990, 1, 1), "+48200000010", "j@d.com");
        assertNotNull(client.getId());
        assertTrue(client.isIndividualClient());
    }

    @Test
    void duplicatePhoneRejected() {
        personService.createIndividualClient("John", "Doe", LocalDate.of(1990, 1, 1), "+48200000011", "j@d.com");
        assertThrows(IllegalArgumentException.class, () ->
                personService.createIndividualClient("Jane", "Roe", LocalDate.of(1991, 1, 1), "+48200000011", "ja@r.com"));
    }

    @Test
    void searchClientsByNameAndPhone() {
        personService.createIndividualClient("John", "Smith", LocalDate.of(1990, 1, 1), "+48200000012", "j@s.com");
        assertEquals(1, personService.searchIndividualClientsByNameOrPhoneNumber("John").size());
        assertEquals(1, personService.searchIndividualClientsByNameOrPhoneNumber("Smith").size());
        assertEquals(1, personService.searchIndividualClientsByNameOrPhoneNumber("+48200000012").size());
        assertEquals(0, personService.searchIndividualClientsByNameOrPhoneNumber("Nobody").size());
    }

    @Test
    void findIndividualClientByIdFiltersByRole() {
        Person client = personService.createIndividualClient("John", "Doe", LocalDate.of(1990, 1, 1), "+48200000013", "j@d.com");
        assertTrue(personService.findIndividualClientById(client.getId()).isPresent());
        Veterinarian vet = persistVet("+48100000020", "VET-S20");   // не клиент
        assertTrue(personService.findIndividualClientById(vet.getId()).isEmpty());
    }

    // ========== ShelterService ==========
    @Test
    void createShelterRejectsDuplicatePhone() {
        shelterService.createShelter("Happy Paws", "h@p.org", Set.of("+48300000010"));
        assertThrows(IllegalArgumentException.class, () ->
                shelterService.createShelter("Other", "o@o.org", Set.of("+48300000010")));
    }

    @Test
    void searchSheltersByName() {
        shelterService.createShelter("Happy Paws", "h@p.org", Set.of("+48300000011"));
        assertEquals(1, shelterService.searchByName("Happy").size());
        assertEquals(0, shelterService.searchByName("Nothing").size());
    }

    // ========== PetService ==========
    @Test
    void createPetSucceeds() {
        Person owner = personRepository.save(new Person("John", "Doe", LocalDate.of(1990, 1, 1), "+48200000014", "j@d.com"));
        Species species = speciesRepository.save(new Species("Dog-pet"));
        Pet pet = petService.createPet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), null, species.getId(), owner.getId(), false);
        assertNotNull(pet.getId());
        assertNotNull(pet.getRegistrationNumber());
    }

    @Test
    void createPetDuplicateChipRejected() {
        Person owner = personRepository.save(new Person("John", "Doe", LocalDate.of(1990, 1, 1), "+48200000015", "j@d.com"));
        Species species = speciesRepository.save(new Species("Dog-chip"));
        petService.createPet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), "CHIP-1", species.getId(), owner.getId(), false);
        assertThrows(IllegalArgumentException.class, () ->
                petService.createPet("Max", PetGender.MALE, LocalDate.of(2020, 1, 1), "CHIP-1", species.getId(), owner.getId(), false));
    }

    @Test
    void createPetForNonClientThrows() {
        Veterinarian vet = persistVet("+48100000040", "VET-S40");
        Species species = speciesRepository.save(new Species("Dog-x"));
        assertThrows(IllegalArgumentException.class, () ->
                petService.createPet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), null, species.getId(), vet.getId(), false));
    }

    @Test
    void createPetUnknownSpeciesThrows() {
        Person owner = personRepository.save(new Person("John", "Doe", LocalDate.of(1990, 1, 1), "+48200000016", "j@d.com"));
        assertThrows(IllegalArgumentException.class, () ->
                petService.createPet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), null, 999999L, owner.getId(), false));
    }

    // ========== VeterinarianService / SpeciesService ==========
    @Test
    void veterinarianServiceFindsAndSearches() {
        Veterinarian vet = persistVet("+48100000030", "VET-S30");
        assertTrue(veterinarianService.findById(vet.getId()).isPresent());
        assertFalse(veterinarianService.getAllVeterinarians().isEmpty());
        assertEquals(1, veterinarianService.searchByName("Anna").size());
        assertEquals(1, veterinarianService.searchByName("Anna Vet").size());
    }

    @Test
    void speciesServiceReturnsAll() {
        speciesRepository.save(new Species("Dog"));
        speciesRepository.save(new Species("Cat"));
        assertEquals(2, speciesService.getAllSpecies().size());
    }
}