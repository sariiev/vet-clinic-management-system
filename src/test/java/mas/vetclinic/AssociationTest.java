package mas.vetclinic;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.person.Nurse;
import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;
import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.model.entity.procedure.Certification;
import mas.vetclinic.model.entity.procedure.Specialization;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssociationTest {

    // ---------- fixtures ----------
    private Person client(String phone) {
        return new Person("John", "Doe", LocalDate.of(1990, 1, 1), phone, "john@doe.com");
    }

    private Pet pet(Person owner) {
        return new Pet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), null, new Species("Dog"), owner);
    }

    private Veterinarian vet(String phone, String license, Specialization spec) {
        return new Veterinarian("Anna", "Vet", LocalDate.of(1985, 1, 1), phone,
                LocalDate.of(2015, 1, 1), "60",
                license, "Chamber", LocalDate.of(2015, 1, 1), LocalDate.of(2035, 1, 1), Set.of(spec));
    }

    private LocalDateTime futureSlot() {
        return LocalDate.now().plusDays(1).atTime(10, 0);
    }

    // ---------- Owner <-> Pet ----------
    @Test
    void petConstructorWiresBothSides() {
        Person owner = client("+48100000001");
        Pet pet = pet(owner);
        assertEquals(owner, pet.getOwner());
        assertTrue(owner.getPets().contains(pet));
    }

    @Test
    void addPetFromOwnerSide() {
        Person owner = client("+48100000002");
        Pet pet = pet(client("+48100000003"));      // изначально у другого владельца
        owner.addPet(pet);                            // добавляем со стороны владельца
        assertEquals(owner, pet.getOwner());
        assertTrue(owner.getPets().contains(pet));
    }

    @Test
    void reassignPetUpdatesBothOwners() {
        Person a = client("+48100000004");
        Person b = client("+48100000005");
        Pet pet = pet(a);

        pet.setOwner(b);                              // переассайн со стороны питомца

        assertEquals(b, pet.getOwner());
        assertTrue(b.getPets().contains(pet));
        assertFalse(a.getPets().contains(pet));       // ушёл от старого владельца
    }

    @Test
    void removePetClearsBothSides() {
        Person owner = client("+48100000006");
        Pet pet = pet(owner);
        owner.removePet(pet);
        assertNull(pet.getOwner());
        assertFalse(owner.getPets().contains(pet));
    }

    // ---------- Veterinarian <-> Specialization ----------
    @Test
    void addSpecializationFromBothSides() {
        Specialization surgery = new Specialization("Surgery");
        Veterinarian vet = vet("+48100000007", "VET-T1", surgery);

        Specialization derma = new Specialization("Dermatology");
        vet.addSpecialization(derma);                 // со стороны врача
        assertTrue(vet.getSpecializations().contains(derma));
        assertTrue(derma.getVeterinarians().contains(vet));

        Specialization cardio = new Specialization("Cardiology");
        cardio.addVeterinarian(vet);                  // со стороны специализации
        assertTrue(vet.getSpecializations().contains(cardio));
        assertTrue(cardio.getVeterinarians().contains(vet));
    }

    @Test
    void removeSpecializationUpdatesBothSides() {
        Specialization surgery = new Specialization("Surgery");
        Specialization derma = new Specialization("Dermatology");
        Veterinarian vet = vet("+48100000008", "VET-T2", surgery);
        vet.addSpecialization(derma);

        System.out.println(vet);

        vet.removeSpecialization(derma);
        assertFalse(vet.getSpecializations().contains(derma));
        assertFalse(derma.getVeterinarians().contains(vet));
    }

    @Test
    void cannotRemoveLastSpecialization() {
        Specialization surgery = new Specialization("Surgery");
        Veterinarian vet = vet("+48100000009", "VET-T3", surgery);
        assertThrows(IllegalStateException.class, () -> vet.removeSpecialization(surgery));
    }

    // ---------- Nurse <-> Certification ----------
    @Test
    void addCertificationFromBothSides() {
        Certification c1 = new Certification("Anesthesia");
        Nurse nurse = new Nurse("Kate", "Nurse", LocalDate.of(1990, 1, 1), "+48100000010",
                LocalDate.of(2018, 1, 1), "40", Set.of(c1));

        Certification c2 = new Certification("Radiology");
        nurse.addCertification(c2);
        assertTrue(nurse.getCertifications().contains(c2));
        assertTrue(c2.getNurses().contains(nurse));

        Certification c3 = new Certification("SurgeryAssist");
        c3.addNurse(nurse);
        assertTrue(nurse.getCertifications().contains(c3));
        assertTrue(c3.getNurses().contains(nurse));
    }

    @Test
    void cannotRemoveLastCertification() {
        Certification c1 = new Certification("Anesthesia");
        Nurse nurse = new Nurse("Kate", "Nurse", LocalDate.of(1990, 1, 1), "+48100000011",
                LocalDate.of(2018, 1, 1), "40", Set.of(c1));
        assertThrows(IllegalStateException.class, () -> nurse.removeCertification(c1));
    }

    // ---------- Appointment <-> Veterinarian / Pet ----------
    @Test
    void appointmentConstructorWiresVetAndPet() {
        Veterinarian vet = vet("+48100000012", "VET-T4", new Specialization("Surgery"));
        Pet pet = pet(client("+48100000013"));

        Appointment appt = new Appointment(vet, pet, futureSlot(), AppointmentDuration.ONE_HOUR);

        assertEquals(vet, appt.getVeterinarian());
        assertEquals(pet, appt.getPet());
        assertTrue(vet.getAppointments().contains(appt));
        assertTrue(pet.getAppointments().contains(appt));
    }

    @Test
    void overlapDetection() {
        Veterinarian vet = vet("+48100000014", "VET-T5", new Specialization("Surgery"));
        Pet pet = pet(client("+48100000015"));
        LocalDateTime slot = futureSlot();                                   // 10:00
        Appointment first = new Appointment(vet, pet, slot, AppointmentDuration.ONE_HOUR); // 10–11

        assertTrue(first.overlapsWith(slot.plusMinutes(30), AppointmentDuration.ONE_HOUR));  // 10:30–11:30 пересекается
        assertFalse(first.overlapsWith(slot.plusHours(1), AppointmentDuration.ONE_HOUR));    // 11–12 не пересекается
    }
}