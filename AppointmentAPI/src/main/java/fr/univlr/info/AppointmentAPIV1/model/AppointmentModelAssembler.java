package fr.univlr.info.AppointmentAPIV1.model;

import fr.univlr.info.AppointmentAPIV1.controller.AppointmentController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Date;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AppointmentModelAssembler implements RepresentationModelAssembler<Appointment, EntityModel<Appointment>> {

    /**
     * Constructor
     */
    public AppointmentModelAssembler() {
        super();
    }

    /**
     * Converts an appointment to an entity model
     * @param entity    the appointment
     * @return         the entity model
     */
    @Override
    public EntityModel<Appointment> toModel(Appointment entity) {

        Date date = new Date();

        if(date.after(entity.getStartDate())) {

            return EntityModel.of(entity,
                    linkTo(methodOn(AppointmentController.class).one(entity.getId(), null)).withSelfRel(),
                    linkTo(methodOn(AppointmentController.class).all(null, null)).withRel("appointments"));
        } else {

            return EntityModel.of(entity,
                    linkTo(methodOn(AppointmentController.class).one(entity.getId(), null)).withSelfRel(),
                    linkTo(methodOn(AppointmentController.class).all(null,null)).withRel("appointments"),
                    linkTo(methodOn(AppointmentController.class).cancel(entity.getId(),null)).withRel("cancel"));
        }
    }

}

