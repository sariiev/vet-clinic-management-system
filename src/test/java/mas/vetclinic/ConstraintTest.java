package mas.vetclinic;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;
import mas.vetclinic.model.entity.medication.*;
import mas.vetclinic.model.entity.payment.CashPayment;
import mas.vetclinic.model.entity.person.Nurse;
import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;
import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.model.entity.procedure.Certification;
import mas.vetclinic.model.entity.procedure.PerformedProcedure;
import mas.vetclinic.model.entity.procedure.Procedure;
import mas.vetclinic.model.entity.procedure.Specialization;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {

    // ---------- helpers ----------
    private Person client(String phone) {
        return new Person("John", "Doe", LocalDate.of(1990, 1, 1), phone, "john@doe.com");
    }

    private Veterinarian vet(String phone, String license, Specialization spec) {
        return new Veterinarian("Anna", "Vet", LocalDate.of(1985, 1, 1), phone,
                LocalDate.of(2015, 1, 1), "60",
                license, "Chamber", LocalDate.of(2015, 1, 1), LocalDate.of(2035, 1, 1), Set.of(spec));
    }

    private Pet pet(Person owner) {
        return new Pet("Rex", PetGender.MALE, LocalDate.of(2020, 1, 1), null, new Species("Dog"), owner);
    }

    private LocalDateTime futureSlot() {
        return LocalDate.now().plusDays(1).atTime(10, 0);
    }

    private Appointment futureAppointment(Veterinarian vet, Pet pet) {
        return new Appointment(vet, pet, futureSlot(), AppointmentDuration.ONE_HOUR);
    }

    // выставить статус через рефлексию (визит иначе не перевести из-за временных проверок)
    private void forceStatus(Appointment appointment, AppointmentStatus status) {
        try {
            Field field = Appointment.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(appointment, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- Person constraints ----------
    @Test
    void ageBelow18Throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Person("Kid", "Young", LocalDate.now().minusYears(17), "+48100000001", "k@y.com"));
    }

    @Test
    void clientEmailMandatory() {
        assertThrows(IllegalArgumentException.class,
                () -> new Person("John", "Doe", LocalDate.of(1990, 1, 1), "+48100000002", null));
    }

    @Test
    void veterinarianRequiresHireDateAndRate() {
        Specialization spec = new Specialization("Surgery");
        assertThrows(IllegalArgumentException.class,   // null hourlyRate
                () -> new Veterinarian("A", "V", LocalDate.of(1985, 1, 1), "+48100000003",
                        LocalDate.of(2015, 1, 1), null,
                        "VET-X1", "Ch", LocalDate.of(2015, 1, 1), LocalDate.of(2035, 1, 1), Set.of(spec)));
    }

    @Test
    void veterinarianRequiresSpecialization() {
        assertThrows(IllegalArgumentException.class,
                () -> new Veterinarian("A", "V", LocalDate.of(1985, 1, 1), "+48100000004",
                        LocalDate.of(2015, 1, 1), "60",
                        "VET-X2", "Ch", LocalDate.of(2015, 1, 1), LocalDate.of(2035, 1, 1), Set.of()));
    }

    @Test
    void nurseRequiresCertification() {
        assertThrows(IllegalArgumentException.class,
                () -> new Nurse("Kate", "Nurse", LocalDate.of(1990, 1, 1), "+48100000005",
                        LocalDate.of(2018, 1, 1), "40", Set.of()));
    }

    // ---------- Shelter constraints ----------
    @Test
    void shelterEmailMandatory() {
        assertThrows(IllegalArgumentException.class,
                () -> new mas.vetclinic.model.entity.person.Shelter("Shelter", null, Set.of("+48300000001")));
    }

    @Test
    void shelterRequiresAtLeastOnePhone() {
        assertThrows(IllegalArgumentException.class,
                () -> new mas.vetclinic.model.entity.person.Shelter("Shelter", "s@s.org", Set.of()));
    }

    // ---------- Pet constraints ----------
    @Test
    void petOwnerMustBeIndividualClient() {
        Veterinarian notAClient = vet("+48100000006", "VET-X3", new Specialization("Surgery")); // нет роли клиента
        assertThrows(IllegalArgumentException.class, () -> pet(notAClient));
    }

    @Test
    void petDateOfBirthCannotBeInFuture() {
        Person owner = client("+48100000007");
        assertThrows(IllegalArgumentException.class,
                () -> new Pet("Rex", PetGender.MALE, LocalDate.now().plusDays(1), null, new Species("Dog"), owner));
    }

    // ---------- Appointment status transitions ----------
    @Test
    void cancelOnlyWhenScheduled() {
        Appointment appt = futureAppointment(vet("+48100000008", "VET-X4", new Specialization("Surgery")),
                pet(client("+48100000009")));
        appt.cancel();
        assertEquals(AppointmentStatus.CANCELLED, appt.getStatus());
        assertThrows(IllegalStateException.class, appt::cancel);   // повторно нельзя
    }

    @Test
    void cannotStartBeforeStartTime() {
        Appointment appt = futureAppointment(vet("+48100000010", "VET-X5", new Specialization("Surgery")),
                pet(client("+48100000011")));
        assertThrows(IllegalStateException.class, appt::markAsInProcess);   // время ещё не наступило
    }

    @Test
    void noShowAndCompleteRequireInProcess() {
        Appointment appt = futureAppointment(vet("+48100000012", "VET-X6", new Specialization("Surgery")),
                pet(client("+48100000013")));
        assertThrows(IllegalStateException.class, appt::markAsNoShow);
        assertThrows(IllegalStateException.class, () -> appt.markAsCompleted(LocalDateTime.now()));
    }

    // ---------- Payment ----------
    @Test
    void paymentRequiresCompletedAppointment() {
        Appointment appt = futureAppointment(vet("+48100000014", "VET-X7", new Specialization("Surgery")),
                pet(client("+48100000015")));   // статус SCHEDULED
        assertThrows(IllegalArgumentException.class, () -> new CashPayment(appt, LocalDateTime.now()));
    }

    // ---------- PerformedProcedure ----------
    @Test
    void performedProcedureRequiresVetSpecialization() {
        Veterinarian vet = vet("+48100000016", "VET-X8", new Specialization("Surgery"));
        Appointment appt = futureAppointment(vet, pet(client("+48100000017")));
        forceStatus(appt, AppointmentStatus.IN_PROCESS);

        Procedure proc = new Procedure(Set.of(), Set.of(new Specialization("Cardiology")), "100", "desc", "Heart surgery");
        assertThrows(IllegalArgumentException.class, () -> new PerformedProcedure(appt, proc, Set.of()));
    }

    @Test
    void performedProcedureRequiresNurseCertification() {
        Veterinarian vet = vet("+48100000018", "VET-X9", new Specialization("Surgery"));
        Appointment appt = futureAppointment(vet, pet(client("+48100000019")));
        forceStatus(appt, AppointmentStatus.IN_PROCESS);

        Procedure proc = new Procedure(Set.of(new Certification("Anesthesia")), Set.of(), "100", "desc", "Sedation");
        Nurse nurse = new Nurse("Kate", "Nurse", LocalDate.of(1990, 1, 1), "+48100000020",
                LocalDate.of(2018, 1, 1), "40", Set.of(new Certification("Radiology"))); // нет нужного серта
        assertThrows(IllegalArgumentException.class, () -> new PerformedProcedure(appt, proc, Set.of(nurse)));
    }

    // ---------- Prescription ----------
    @Test
    void prescriptionRequiresDosageOrNotes() {
        Appointment appt = futureAppointment(vet("+48100000021", "VET-X10", new Specialization("Surgery")),
                pet(client("+48100000022")));
        forceStatus(appt, AppointmentStatus.IN_PROCESS);

        Medication med = medication("Amoxicillin");
        assertThrows(IllegalArgumentException.class,
                () -> new Prescription(appt, med, null, null, FrequencyType.ONCE_DAILY,
                        appt.getStartDateTime().toLocalDate(), 5, null));   // ни дозы, ни заметок
    }

    @Test
    void medicationCanBePrescribedOnlyOncePerAppointment() {
        Appointment appt = futureAppointment(vet("+48100000023", "VET-X11", new Specialization("Surgery")),
                pet(client("+48100000024")));
        forceStatus(appt, AppointmentStatus.IN_PROCESS);

        Medication med = medication("Ibuprofen");
        LocalDate start = appt.getStartDateTime().toLocalDate();
        new Prescription(appt, med, null, null, FrequencyType.ONCE_DAILY, start, 5, "take with food"); // ок (есть notes)
        assertThrows(IllegalArgumentException.class,
                () -> new Prescription(appt, med, null, null, FrequencyType.ONCE_DAILY, start, 5, "again")); // дубликат
    }

    private Medication medication(String name) {
        return new Medication(name, new MedicationCategory("Cat-" + name), MedicationForm.SOLID, "desc");
    }
}