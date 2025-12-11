package com.collabsphere.service;

import com.collabsphere.dto.CreateMilestoneRequest;
import com.collabsphere.dto.CreateProjectRequest;
import com.collabsphere.entity.ClassRoom;
import com.collabsphere.entity.Milestone;
import com.collabsphere.entity.Project;
import com.collabsphere.entity.User;
import com.collabsphere.entity.enums.UserRole;
import com.collabsphere.repository.ClassRoomRepository;
import com.collabsphere.repository.MilestoneRepository;
import com.collabsphere.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ClassRoomRepository classRoomRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    public Project createProject(CreateProjectRequest request, User user) {
        // Validate user role
        if (user.getRole() != UserRole.LECTURER && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only lecturers and admins can create projects");
        }

        // Find classroom
        ClassRoom classRoom = classRoomRepository.findById(request.getClassroomId())
            .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Validate lecturer owns the classroom (unless admin)
        if (user.getRole() == UserRole.LECTURER && !classRoom.getLecturer().getId().equals(user.getId())) {
            throw new RuntimeException("You can only create projects for your own classrooms");
        }

        // Validate deadline is in the future
        if (request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Project deadline must be in the future");
        }

        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());
        project.setClassRoom(classRoom);

        return projectRepository.save(project);
    }

    public Milestone createMilestone(Long projectId, CreateMilestoneRequest request, User user) {
        // Find project
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        // Validate user can create milestones for this project
        if (user.getRole() == UserRole.LECTURER && 
            !project.getClassRoom().getLecturer().getId().equals(user.getId())) {
            throw new RuntimeException("You can only create milestones for your own projects");
        }

        // Validate due date is before project deadline
        if (request.getDueDate().isAfter(project.getDeadline())) {
            throw new RuntimeException("Milestone due date cannot be after project deadline");
        }

        // Validate due date is in the future
        if (request.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Milestone due date must be in the future");
        }

        Milestone milestone = new Milestone();
        milestone.setTitle(request.getTitle());
        milestone.setDueDate(request.getDueDate());
        milestone.setProject(project);

        return milestoneRepository.save(milestone);
    }

    public List<Project> getProjectsByClassroom(Long classroomId) {
        return projectRepository.findByClassRoomId(classroomId);
    }

    public List<Project> getProjectsByLecturer(User lecturer) {
        List<ClassRoom> classRooms = classRoomRepository.findByLecturer(lecturer);
        return classRooms.stream()
            .flatMap(classRoom -> projectRepository.findByClassRoom(classRoom).stream())
            .toList();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public List<Milestone> getMilestonesByProject(Long projectId) {
        return milestoneRepository.findByProjectIdOrderByDueDate(projectId);
    }

    public List<Project> getActiveProjects(Long classroomId) {
        return projectRepository.findActiveProjectsByClassroom(classroomId, LocalDateTime.now());
    }

    public List<Milestone> getUpcomingMilestones(Long projectId) {
        return milestoneRepository.findUpcomingMilestones(projectId, LocalDateTime.now());
    }
}