package fr.univlr.info.AppointmentAPIV1.controller;

import fr.univlr.info.AppointmentAPIV1.model.Appointment;
import fr.univlr.info.AppointmentAPIV1.model.AppointmentModelAssembler;
import fr.univlr.info.AppointmentAPIV1.model.Doctor;
import fr.univlr.info.AppointmentAPIV1.store.AppointmentRepository;
import fr.univlr.info.AppointmentAPIV1.store.DoctorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Nicole PEARSON
 *
 * The appointment controller
 * Contains all the CRUD operations
 */
@RestController
@RequestMapping(path = "/api")
public class AppointmentController {

    // The port
    @Value("${local.server.port:8080}")
    private int port;

    // The appointment repository
    private final AppointmentRepository apptRepository;
    // The doctor repository
    private final DoctorRepository doctorRepository;
    // The appointment model assembler
    private final AppointmentModelAssembler assembler;

    /**
     * Constructor
     *
     * @param apptRepository   the appointment repository
     * @param doctorRepository the doctor repository
     */
    public AppointmentController(AppointmentRepository apptRepository, DoctorRepository doctorRepository) {
        this.apptRepository = apptRepository;
        this.doctorRepository = doctorRepository;
        // Creates the appointment model assembler
        this.assembler = new AppointmentModelAssembler();
    }

    /**
     * Get all appointments
     * @return a list of appointments
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> all(@RequestParam(name = "date", required = false) String date, @RequestHeader(value="Accept", required=false) String halContent) {
        List<Appointment> appts = apptRepository.findAll(); // Finds all appointments
        // Check if the date parameter is set
        if(date != null) {
            // Finds all appointments after the date specified
            try{
                appts = apptRepository.findByAfterDate(Date.from(LocalDateTime.parse(date).atZone(java.time.ZoneId.systemDefault()).toInstant()));
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Error: invalid date format, returns status code
            }

        }
        // Check if the Accept header is set to HAL
        if (MediaTypes.HAL_JSON_VALUE.equals(halContent) && halContent != null) {
            // Converts the list of appointments to a list of entity models
            List<EntityModel<Appointment>> halAppts = appts.stream().map(assembler::toModel).collect(Collectors.toList());
            // Returns the list of entity models and status code
            return new ResponseEntity<>(CollectionModel.of(halAppts,
                    linkTo(methodOn(AppointmentController.class).all(null ,null)).withSelfRel()),
                    HttpStatus.OK);
        }

        return new ResponseEntity<>(appts, HttpStatus.OK);  // Returns the list of appointments and status code
    }

    /**
     * Get an appointment by its id
     * @param id the appointment id
     * @return the appointment
     */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<?> one(@PathVariable Long id, @RequestHeader(value="Accept", required=false) String halContent) {
        // Finds the appointment by its id and throws an exception if it doesn't exist
        Appointment appt = apptRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));
        // Check if the Accept header is set to HAL
        if (halContent != null && MediaTypes.HAL_JSON_VALUE.equals(halContent)){
            // Returns the entity model and status code
            return new ResponseEntity<>(assembler.toModel(appt), HttpStatus.OK);
        }
        return new ResponseEntity<>(appt, HttpStatus.OK);   // Returns the appointment and status code
    }

    /**
     * Create a new appointment
     * @param appt the appointment
     * @return the appointment just created
     */
    @PostMapping("/appointments")
    ResponseEntity<?> newAppointment(@Valid @RequestBody Appointment appt, @RequestHeader(value="Accept", required=false) String halContent) {
        HttpHeaders headers = new HttpHeaders();    // Creates an instance of HttpHeaders
        Doctor doctor = doctorRepository.findByName(appt.getDoctor()); // Finds the doctor by its name
        // Check if the doctor exists
        if(doctor == null) {
            doctor = new Doctor(appt.getDoctor());  // Creates a new doctor if it doesn't exist
            doctorRepository.save(doctor);  // Saves the doctor
        }

        // Loop through the doctor's appointments
        for(Appointment appointment : doctor.getAppointments()) {
            // Check that the new appointment doesn't overlap with an existing appointment
            if(appointment.getStartDate().equals(appt.getStartDate()) ||
                    appointment.getEndDate().equals(appt.getEndDate()) ||
                    (appointment.getStartDate().after(appt.getStartDate()) && appointment.getStartDate().before(appt.getEndDate())) ||
                    (appointment.getEndDate().after(appt.getStartDate()) && appointment.getEndDate().before(appt.getEndDate()))) {
                return new ResponseEntity<>(HttpStatus.CONFLICT); // Error: appointment already booked, returns status code
            }
        }

        appt.setDoctorObj(doctor);   // Sets the doctor name
        Appointment newAppt = apptRepository.save(appt); // modify the code to save the appointment
        doctor.addAppointment(newAppt); // Adds the appointment to the doctor
        doctorRepository.save(doctor);  // Saves the doctor
        // Set the location header for the newly created resource
        headers.setLocation(
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(newAppt.getId())
                        .toUri());

        if (halContent != null && MediaTypes.HAL_JSON_VALUE.equals(halContent)) {
            return new ResponseEntity<>(assembler.toModel(newAppt), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(appt, headers, HttpStatus.CREATED); // Returns the appointment, headers and status code
    }

    /**
     * Replace or modify an existing appointment
     * @param newAppt the new appointment
     * @param id the id of the existing appointment
     * @return the modified appointment
     */
    @PutMapping("/appointments/{id}")
    ResponseEntity<?> replaceAppointment(@Valid @RequestBody Appointment newAppt, @PathVariable Long id, @RequestHeader(value="Accept", required=false) String halContent) {

        Appointment ap = apptRepository.findById(id)
                .map(appt -> {
                    appt.setDoctor(newAppt.getDoctor());
                    appt.setEndDate(newAppt.getEndDate());
                    appt.setPatient(newAppt.getPatient());
                    appt.setStartDate(newAppt.getStartDate());
                    return apptRepository.save(appt);
                }).orElseGet(() -> {
                    newAppt.setId(id);
                    return apptRepository.save(newAppt);
                });

        if (halContent != null && MediaTypes.HAL_JSON_VALUE.equals(halContent)){
            return new ResponseEntity<>(assembler.toModel(ap), HttpStatus.OK);
        }
        return new ResponseEntity<>(ap, HttpStatus.OK);

    }

    /**
     * Delete an appointment
     * @param id the id of the appointment to delete
     * @return the deleted appointment
     */
    @DeleteMapping("/appointments/{id}")
    ResponseEntity<Appointment> deleteAppointment(@PathVariable Long id) {
        Optional<Appointment> appt = apptRepository.findById(id);   // Finds the appointment by its id
        // Check if the appointment exists
        if (appt.isPresent()) {
            apptRepository.deleteById(id);  // Delete the appointment
            return new ResponseEntity<>(appt.get(), HttpStatus.OK); // Returns the deleted appointment and status code
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Error: appointment not found, returns status code
        }
    }

    /**
     * Delete all appointments
     * @return the status code
     */
    @DeleteMapping("/appointments")
    ResponseEntity<Appointment> deleteAllAppointments() {
        apptRepository.deleteAll(); // Delete all appointments
        return new ResponseEntity<>(HttpStatus.OK); // Returns the status code
    }

    /**
     * Cancel an appointment if it hasn't started yet
     * @param id the id of the appointment to cancel
     * @param halContent the Accept header
     * @return the deleted appointment
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id,
                                    @RequestHeader(value="Accept", required=false) String halContent) {
        Appointment appt = (Appointment) one(id, null).getBody();
        Date now = new Date();

        if(appt != null && now.after(appt.getStartDate())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Error: appointment already started, returns status code
        }

        return this.deleteAppointment(id);
    }

}
