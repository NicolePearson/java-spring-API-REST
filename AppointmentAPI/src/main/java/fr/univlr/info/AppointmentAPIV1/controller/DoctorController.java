package fr.univlr.info.AppointmentAPIV1.controller;

import fr.univlr.info.AppointmentAPIV1.model.Appointment;
import fr.univlr.info.AppointmentAPIV1.model.AppointmentModelAssembler;
import fr.univlr.info.AppointmentAPIV1.model.Doctor;
import fr.univlr.info.AppointmentAPIV1.model.DoctorModelAssembler;
import fr.univlr.info.AppointmentAPIV1.store.DoctorRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Nicole PEARSON
 *
 * The doctor controller
 * Contains all the CRUD operations
 */
@RestController
@RequestMapping(path = "/api")
public class DoctorController {

    // The doctor repository
    private final DoctorRepository doctorRepository;
    // The doctor model assembler
    private final DoctorModelAssembler assembler;
    // The appointment model assembler
    private final AppointmentModelAssembler apptAssembler;

    /**
     * Constructor
     * @param doctorRepository the doctor repository
     */
    public DoctorController(DoctorRepository doctorRepository) {

        this.doctorRepository = doctorRepository;
        // Creates the doctor model assembler
        this.assembler = new DoctorModelAssembler();
        // Creates the appointment model assembler
        this.apptAssembler = new AppointmentModelAssembler();
    }

    /**
     * Get all doctors
     * @return a list of doctors
     */
    @GetMapping("/doctors")
    public ResponseEntity<?> all(@RequestHeader(value = "Accept", required = false) String halContent) {
        List<Doctor> doctors = doctorRepository.findAll(); // Finds all doctors
        // Checks if the client accepts HAL
        if (halContent != null && MediaTypes.HAL_JSON_VALUE.equals(halContent)) {
            // Converts the list of doctors to a list of EntityModel<Doctor>
            List<EntityModel<Doctor>> halDoctors = doctors.stream().map(assembler::toModel).collect(Collectors.toList());
            // Returns the list of doctors and status code
            return new ResponseEntity<>(CollectionModel.of(halDoctors,
                    linkTo(methodOn(DoctorController.class).all(null)).withSelfRel()),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(doctors, HttpStatus.OK);  // Returns the list of doctors and status code
    }

    /**
     * Get a doctor by its name
     * @param name the doctor name
     * @return the doctor
     */
    @GetMapping("/doctors/{name}")
    public ResponseEntity<?> getDoctorByName(@PathVariable String name, @RequestHeader(value = "Accept", required = false) String halContent) {
        Doctor doctor = doctorRepository.findByName(name);  // Finds the doctor by its name
        //Checks if the doctor exists
        if(doctor == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Error, doctor not found
        }else {
            // Checks if the client accepts HAL
            if (halContent != null && MediaTypes.HAL_JSON_VALUE.equals(halContent)) {
                // Returns the doctor and status code with the assembler
                return new ResponseEntity<>(assembler.toModel(doctor), HttpStatus.OK);
            }
            return new ResponseEntity<>(doctor, HttpStatus.OK); // Returns the doctor and status code
        }
    }

    /**
     * Delete a doctor by its name
     * @param name the doctor name
     * @return the doctor deleted
     */
    @DeleteMapping("/doctors/{name}")
    ResponseEntity<Doctor> deleteDoctorByName(@PathVariable String name) {
        Doctor doctor = doctorRepository.findByName(name);  // Finds the doctor by its name
        //Checks if the doctor exists
        if(doctor == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Error, doctor not found
        }else {
            if(doctor.getAppointments().size() > 0) {  // Checks if the doctor has appointments
                return new ResponseEntity<>(HttpStatus.CONFLICT);  // Error, doctor has appointments (cannot be deleted)
            }
            doctorRepository.delete(doctor);    // Deletes the doctor
            return new ResponseEntity<>(doctor, HttpStatus.OK); // Returns the doctor deleted and status code
        }
    }

    /**
     * Get all appointments of a doctor
     * @param name the doctor name
     * @return  a list of appointments
     */
    @GetMapping("/doctors/{name}/appointments")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable String name, @RequestHeader(value = "Accept", required = false) String halContent) {
        Doctor doctor = doctorRepository.findByName(name);  // Finds the doctor by its name
        //Checks if the doctor exists
        if(doctor == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Error, doctor not found
        }else {
            // Checks if the client accepts HAL
            if (halContent != null && MediaTypes.HAL_JSON_VALUE.equals(halContent)){
                // Converts the list of appointments to a list of EntityModel<Appointment>
                List<EntityModel<Appointment>> halAppts = doctor.getAppointments().stream()
                        .map(apptAssembler::toModel).collect(Collectors.toList());
                // Returns the list of appointments and status code
                return new ResponseEntity<>(CollectionModel.of(halAppts,
                        linkTo(methodOn(DoctorController.class).getDoctorAppointments(doctor.getName() ,null)).withSelfRel()),
                        HttpStatus.OK);
            }
            return new ResponseEntity<>(doctor.getAppointments(), HttpStatus.OK); // Returns the doctor appointments and status code
        }
    }

}
