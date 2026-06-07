package mas.vetclinic.view;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.person.PetOwner;

import java.time.LocalDateTime;

public record AppointmentView(LocalDateTime startDateTime, LocalDateTime endDateTime, String petName, String ownerName) {
    public static AppointmentView of(Appointment appointment) {
        PetOwner owner = appointment.getPet().getOwner();
        String ownerName = owner.getName();
        return new AppointmentView(appointment.getStartDateTime(), appointment.getEndDateTime() == null ? appointment.getExpectedEndDateTime() : appointment.getEndDateTime(), appointment.getPet().getName(), ownerName);
    }
}
