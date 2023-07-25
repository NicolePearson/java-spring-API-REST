package fr.univlr.info.AppointmentAPIV1.controller;

import fr.univlr.info.AppointmentAPIV1.model.Appointment;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 * @author Nicole PEARSON
 *
 * The appointment date validator
 * Checks if the appointment date is valid
 */
public class AppointmentDateValidator implements ConstraintValidator<AppointmentDateConstraint, Appointment> {

    // Variable that will contain the error message
    private String message;

    /**
     * Intializes the appointment date constraint message.
     * @param dateCst the appointment date constraint
     */
    @Override
    public void initialize(AppointmentDateConstraint dateCst) {
        this.message = dateCst.message();
    }

    /**
     * Checks if the appointment date is valid
     * @param app the appointment
     * @param ctxt the constraint validator context
     * @return true if the appointment date is valid, false otherwise
     */
    @Override
    public boolean isValid(Appointment app, ConstraintValidatorContext ctxt) {
        Date start = app.getStartDate();    // Gets the start date of the appointment
        Date end = app.getEndDate();    // Gets the end date of the appointment
        // Checks if the start date and end date are not null, if the start date is not the same as the end date and if the start date is before the end date.
        if (start == null || end == null || start.equals(end) || start.after(end) || start.before(new Date()) || end.before(new Date())) {
            ctxt.buildConstraintViolationWithTemplate(this.message).addConstraintViolation();   // Adds a constraint violation
            return false;   // Returns false if the appointment date is not valid
        }else {
            return true;    // Returns true if the appointment date is valid
        }
    }

}
