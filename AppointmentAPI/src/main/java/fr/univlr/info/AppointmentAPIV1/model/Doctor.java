package fr.univlr.info.AppointmentAPIV1.model;

import javax.persistence.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Nicole PEARSON
 *
 * Doctor entity
 * Contains the doctor's name and a list of appointments
 */
@Entity
public class Doctor {
    // Doctor's id, generated automatically
    @Id
    @GeneratedValue()
    private Long id;

    // Doctor's name
    private String name;

    // List of appointments
    @OneToMany(targetEntity = Appointment.class, mappedBy = "doctorObj", orphanRemoval = true)
    private List<Appointment> appointments;

    /**
     * Default constructor
     */
    public Doctor() {}

    /**
     * Constructor
     * @param name Doctor's name
     */
    public Doctor(String name) {
        this.name = name;
        this.appointments = new ArrayList<>();
    }

    /**
     * Getter
     * Gets the doctor's id
     * @return Doctor's id
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter
     * Sets the doctor's id
     * @param id Doctor's id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter
     * Gets the doctor's name
     * @return Doctor's name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter
     * Sets the doctor's name
     * @param name Doctor's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter
     * Gets the list of appointments
     * @return List of appointments
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Setter
     * Sets the list of appointments
     * @param appointments List of appointments
     */
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    /**
     * toString method
     * @return String representation of the doctor
     */
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", doctor name='" + this.name + '\'' +
                ", appointments='" + this.appointments + '\'' +
                '}';
    }

    /**
     * equals method
     * @param o Object to compare
     * @return True if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id.equals(doctor.id) &&
                name.equals(doctor.name) &&
                appointments.equals(doctor.appointments);
    }

    /**
     * hashCode method
     * @return Hash code of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, appointments);
    }

    /**
     * Adds an appointment to the list of appointments
     * @param newAppt Appointment to add
     */
    public void addAppointment(Appointment newAppt) {
        this.appointments.add(newAppt);
    }
}
