package mas.vetclinic.view;

import mas.vetclinic.model.entity.appointment.Appointment;

import java.time.LocalDate;
import java.util.List;

public record DaySchedule(LocalDate date, List<AppointmentView> appointments) {}