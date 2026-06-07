package mas.vetclinic.service;

import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.PersonRole;
import mas.vetclinic.repository.PersonRepository;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
//@Transactional
public class PersonService {
    public static final int SEARCH_LIMIT = 10;

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person createIndividualClient(String firstName, String lastName, LocalDate dateOfBirth,
                                         String phoneNumber, String emailAddress) throws IllegalArgumentException {
        if (personRepository.existsByPhoneNumberEquals(phoneNumber)) {
            throw new IllegalArgumentException("Phone number is already in use");
        }
        Person person = new Person(firstName, lastName, dateOfBirth, phoneNumber, emailAddress);
        return personRepository.save(person);
    }

    public List<Person> searchIndividualClientsByNameOrPhoneNumber(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String trimmed = query.trim();

        if (trimmed.matches("\\+?\\d+")) {
            if (!trimmed.startsWith("+")) {
                trimmed = "+" + trimmed;
            }
            return personRepository.findByPhoneNumber(trimmed)
                    .filter(Person::isIndividualClient)
                    .map(ic -> List.of(ic))
                    .orElse(List.of());
        }

        String[] parts = trimmed.split("\\s+");
        Limit limit = Limit.of(SEARCH_LIMIT);

        if (parts.length >= 2) {
            return personRepository.searchByFullNameAndRole(parts[0], parts[1], PersonRole.INDIVIDUAL_CLIENT, limit);
        } else {
            return personRepository.searchBySinglePartOfNameAndRole(parts[0], PersonRole.INDIVIDUAL_CLIENT, limit);
        }
    }

    public Optional<Person> findIndividualClientById(Long id) {
        return personRepository.findByIdAndRolesContaining(id, PersonRole.INDIVIDUAL_CLIENT);
    }
}
