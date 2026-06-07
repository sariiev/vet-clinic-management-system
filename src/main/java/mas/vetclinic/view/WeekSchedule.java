package mas.vetclinic.view;

import java.time.LocalDate;
import java.util.List;

public record WeekSchedule(LocalDate monday, LocalDate sunday,
                           LocalDate previousWeek, LocalDate nextWeek, List<DaySchedule> days) {}