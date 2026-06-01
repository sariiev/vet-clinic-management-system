package mas.vetclinic.model.entity.appointment;

import java.time.Duration;

public enum AppointmentDuration {
    FIFTEEN_MINUTES(Duration.ofMinutes(15), "15 minutes"),
    THIRTY_MINUTES(Duration.ofMinutes(30), "30 minutes"),
    ONE_HOUR(Duration.ofHours(1), "1 hour"),
    TWO_HOURS(Duration.ofHours(2), "2 hours"),
    FOUR_HOURS(Duration.ofHours(4), "4 hours");

    private final Duration duration;
    private final String name;

    AppointmentDuration(Duration duration, String name) {
        this.duration = duration;
        this.name = name;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }
}
