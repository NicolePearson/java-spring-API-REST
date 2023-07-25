package fr.univlr.info.AppointmentAPIV1.model;

import fr.univlr.info.AppointmentAPIV1.controller.DoctorController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class DoctorModelAssembler implements RepresentationModelAssembler<Doctor, EntityModel<Doctor>> {
    /**
     * @param entity
     * @return
     */
    @Override
    public EntityModel<Doctor> toModel(Doctor entity) {
        return EntityModel.of(entity, //
                linkTo(methodOn(DoctorController.class).getDoctorByName(entity.getName(), null)).withSelfRel(),
                linkTo(methodOn(DoctorController.class).all(null)).withRel("doctors"),
                linkTo(methodOn(DoctorController.class).getDoctorAppointments(entity.getName(), null)).withRel("appointments"));
    }

}
