package mas.vetclinic.model.entity.payment;

import jakarta.persistence.Entity;
import mas.vetclinic.model.entity.appointment.Appointment;

import java.time.LocalDateTime;

@Entity
public class CashPayment extends Payment {
    protected CashPayment() {}

    public CashPayment(Appointment appointment, LocalDateTime dateTime) throws IllegalArgumentException {
        super(appointment, dateTime);
        appointment.setPayment(this);
    }

    @Override
    public String toString() {
        return "CashPayment{" +
                "id=" + getId() +
                ", dateTime=" + getDateTime() +
                ", amount=" + getAmount() +
                ", appointment=" + getAppointment().getId() +
                '}';
    }
}
