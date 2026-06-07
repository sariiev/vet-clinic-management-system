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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CascadeTest {
    @Autowired EntityManager em;
    @Autowired VeterinarianRepository vetRepo;
    @Autowired PetRepository petRepo;
    @Autowired PersonRepository personRepo;
    @Autowired AppointmentRepository appointmentRepo;
    @Autowired SpeciesRepository speciesRepo;
    @Autowired SpecializationRepository specRepo;

    private Appointment seed(String vetPhone, String vetLicense, String clientPhone) {
        Specialization spec = specRepo.save(new Specialization("Surgery-" + vetLicense));
        Veterinarian vet = vetRepo.save(new Veterinarian("Anna", "Vet", LocalDate.of(1985, 1, 1), vetPhone,
                LocalDate.of(2015, 1, 1), "60", vetLicense, "Chamber",
                LocalDate.of(2015, 1, 1), LocalDate.of(2035, 1, 1), Set.of(spec)));
        Person owner = personRepo.save(new Person("John", "Doe", LocalDate.of(1990, 1, 1), clientPhone, "j@d.com"));
        Species species = speciesRepo.save(new Species("Dog-" + vetLicense));
        Pet pet = petRepo.save(new Pet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), null, species, owner));
        return appointmentRepo.save(new Appointment(vet, pet, LocalDate.now().plusDays(1).atTime(10, 0),
                AppointmentDuration.ONE_HOUR));
    }

    @Test
    void deletingPetDeletesItsAppointments() {
        Appointment appt = seed("+48100000001", "VET-C1", "+48200000001");
        Long apptId = appt.getId();
        Pet pet = appt.getPet();

        petRepo.delete(pet);
        em.flush();

        assertTrue(appointmentRepo.findById(apptId).isEmpty());
    }

    @Test
    void deletingVeterinarianDeletesItsAppointments() {
        Appointment appt = seed("+48100000002", "VET-C2", "+48200000002");
        Long apptId = appt.getId();
        Veterinarian vet = appt.getVeterinarian();

        vetRepo.delete(vet);
        em.flush();

        assertTrue(appointmentRepo.findById(apptId).isEmpty());
    }

    @Test
    void deletingOwnerDeletesPetsAndAppointments() {
        Appointment appt = seed("+48100000003", "VET-C3", "+48200000003");
        Long apptId = appt.getId();
        Long petId = appt.getPet().getId();
        Person owner = (Person) appt.getPet().getOwner();

        personRepo.delete(owner);
        em.flush();

        assertTrue(petRepo.findById(petId).isEmpty());          // питомцы удалены
        assertTrue(appointmentRepo.findById(apptId).isEmpty()); // и их визиты
    }
}