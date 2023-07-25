package fr.univlr.info.AppointmentAPIV1.store;

import fr.univlr.info.AppointmentAPIV1.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    /**
     * Finds all appointments after the date parameter
     * @param date the date
     * @return a list of appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.startDate > :date")
    List<Appointment> findByAfterDate(@Param("date") Date date);
}
