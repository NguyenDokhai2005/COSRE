package com.collabsphere.repository;

import com.collabsphere.entity.ClassRoom;
import com.collabsphere.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByClassRoom(ClassRoom classRoom);
    
    List<Project> findByClassRoomId(Long classroomId);
    
    @Query("SELECT p FROM Project p WHERE p.deadline BETWEEN :startDate AND :endDate")
    List<Project> findByDeadlineBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Project p WHERE p.title LIKE %:title%")
    List<Project> findByTitleContaining(@Param("title") String title);
    
    @Query("SELECT p FROM Project p WHERE p.classRoom.id = :classroomId AND p.deadline > :currentDate")
    List<Project> findActiveProjectsByClassroom(@Param("classroomId") Long classroomId, 
                                               @Param("currentDate") LocalDateTime currentDate);
}