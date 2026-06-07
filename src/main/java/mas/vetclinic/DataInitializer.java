package mas.vetclinic;

import mas.vetclinic.model.entity.appointment.Appointment;
import mas.vetclinic.model.entity.appointment.AppointmentDuration;
import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.Shelter;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.model.entity.pet.Pet;
import mas.vetclinic.model.entity.pet.PetGender;
import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.model.entity.procedure.Specialization;
import mas.vetclinic.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final VeterinarianRepository veterinarianRepository;
    private final SpecializationRepository specializationRepository;
    private final SpeciesRepository speciesRepository;
    private final AppointmentRepository appointmentRepository;
    private final PersonRepository personRepository;
    private final PetRepository petRepository;
    private final ShelterRepository shelterRepository;

    public DataInitializer(VeterinarianRepository veterinarianRepository, SpecializationRepository specializationRepository,
                           SpeciesRepository speciesRepository, AppointmentRepository appointmentRepository,
                           PersonRepository personRepository, PetRepository petRepository,
                           ShelterRepository shelterRepository) {
        this.veterinarianRepository = veterinarianRepository;
        this.specializationRepository = specializationRepository;
        this.speciesRepository = speciesRepository;
        this.appointmentRepository = appointmentRepository;
        this.personRepository = personRepository;
        this.petRepository = petRepository;
        this.shelterRepository = shelterRepository;
    }

    @Transactional
    @Override
    public void run(String... args) {
        if (veterinarianRepository.count() > 0) {
            return;
        }

        Specialization surgery = specializationRepository.save(new Specialization("Surgery"));
        Specialization dermatology = specializationRepository.save(new Specialization("Dermatology"));
        Specialization cardiology = specializationRepository.save(new Specialization("Cardiology"));
        List<Specialization> specs = List.of(surgery, dermatology, cardiology);

        String[][] vetNames = {
                {"John", "Smith"}, {"Jay", "Rogers"}, {"Daisy", "Day"},
                {"Tyler", "Hill"}, {"Ruby", "Walsh"}, {"Tommy", "Young"}
        };
        List<Veterinarian> vets = new ArrayList<>();
        for (int i = 0; i < vetNames.length; i++) {
            Veterinarian vet = new Veterinarian(
                    vetNames[i][0], vetNames[i][1],
                    LocalDate.of(1980, 1, 1).plusDays(i * 120L),
                    String.format("+48100%06d", i),
                    LocalDate.of(2015, 1, 1), "60",
                    String.format("VET-%03d", i + 1), "Polish Veterinary Chamber",
                    LocalDate.of(2015, 1, 1), LocalDate.of(2030, 1, 1),
                    Set.of(specs.get(i % specs.size()))
            );
            vets.add(veterinarianRepository.save(vet));
        }

        Species dog = speciesRepository.save(new Species("Dog"));
        Species cat = speciesRepository.save(new Species("Cat"));
        Species rabbit = speciesRepository.save(new Species("Rabbit"));
        List<Species> allSpecies = List.of(dog, cat, rabbit);

        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
                "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White", "Harris"};
        String[] firstNames = {"Mary", "James", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
                "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph"};

        List<Person> clients = new ArrayList<>();
        int phone = 0;

        for (String last : lastNames) {
            clients.add(personRepository.save(new Person(
                    "John", last,
                    LocalDate.of(1990, 1, 1).plusDays(phone),
                    String.format("+48200%06d", phone++),
                    String.format("john.%s@example.com", last.toLowerCase())
            )));
        }

        for (int i = 0; i < firstNames.length; i++) {
            for (int j = 0; j < 5; j++) {
                String last = lastNames[(i + j) % lastNames.length];
                clients.add(personRepository.save(new Person(
                        firstNames[i], last,
                        LocalDate.of(1985, 6, 15).plusDays(i * 7L + j),
                        String.format("+48200%06d", phone++),
                        String.format("%s.%s.%d@example.com", firstNames[i].toLowerCase(), last.toLowerCase(), j)
                )));
            }
        }

        String[] shelterNames = {"Happy Paws", "Safe Haven", "Furry Friends", "Second Chance", "Animal Rescue"};
        List<Shelter> shelters = new ArrayList<>();
        for (int i = 0; i < shelterNames.length; i++) {
            shelters.add(shelterRepository.save(new Shelter(
                    shelterNames[i],
                    String.format("contact@%s.org", shelterNames[i].toLowerCase().replace(" ", "")),
                    Set.of(String.format("+48300%06d", i))
            )));
        }

        String[] petNames = {"Reks", "Bella", "Max", "Luna", "Charlie", "Lucy", "Cooper", "Daisy", "Rocky", "Milo",
                "Buddy", "Molly", "Bear", "Coco", "Toby", "Ruby", "Oscar", "Lola", "Teddy", "Zoe"};
        PetGender[] genders = PetGender.values();
        List<Pet> pets = new ArrayList<>();
        int chip = 0;

        for (int i = 0; i < 30 && i < clients.size(); i++) {
            int c = chip++;
            pets.add(petRepository.save(new Pet(
                    petNames[c % petNames.length],
                    genders[c % genders.length],
                    LocalDate.of(2021, 1, 1).plusDays(c * 5L),
                    String.format("CHIP-%05d", c),
                    allSpecies.get(c % allSpecies.size()),
                    clients.get(i)
            )));
        }

        int[] shelterPetCounts = {25, 30};
        for (int s = 0; s < shelterPetCounts.length; s++) {
            Shelter shelter = shelters.get(s);
            for (int k = 0; k < shelterPetCounts[s]; k++) {
                int c = chip++;
                pets.add(petRepository.save(new Pet(
                        petNames[c % petNames.length],
                        genders[c % genders.length],
                        LocalDate.of(2020, 1, 1).plusDays(c * 3L),
                        String.format("CHIP-%05d", c),
                        allSpecies.get(c % allSpecies.size()),
                        shelter
                )));
            }
        }

        LocalDate today = LocalDate.now();
        int[] dayOffsets = {1, 2, 3, 4, 5};
        int[] hours = {10, 12, 14, 16, 10};
        for (int v = 0; v < vets.size(); v++) {
            for (int k = 0; k < dayOffsets.length; k++) {
                Pet pet = pets.get((v * dayOffsets.length + k) % pets.size());
                LocalDateTime start = today.plusDays(dayOffsets[k]).atTime(hours[k], 0);
                appointmentRepository.save(new Appointment(
                        vets.get(v), pet, start, AppointmentDuration.THIRTY_MINUTES));
            }
        }
    }
}