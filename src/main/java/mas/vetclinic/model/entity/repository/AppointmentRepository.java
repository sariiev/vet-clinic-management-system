package mas.vetclinic.model.entity.repository;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentStatus;
import mas.vetclinic.model.entity.person.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByVeterinarianAndStartDateTimeBetweenAndStatusNotIn(Veterinarian veterinarian, LocalDateTime startDateTimeAfter, LocalDateTime startDateTimeBefore, Collection<AppointmentStatus> statuses);
}
