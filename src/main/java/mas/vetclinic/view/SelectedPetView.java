package mas.vetclinic.view;

import mas.vetclinic.model.entity.person.PetOwner;
import mas.vetclinic.model.entity.pet.Pet;

public record SelectedPetView(Long petId, String petName, String ownerName) {
    public static SelectedPetView of(Pet pet) {
        PetOwner owner = pet.getOwner();
        String ownerName = owner.getName();
        return new SelectedPetView(pet.getId(), pet.getName(), ownerName);
    }
}