package com.collabsphere.service;

import com.collabsphere.dto.AutoGenerateTeamsRequest;
import com.collabsphere.entity.Project;
import com.collabsphere.entity.Team;
import com.collabsphere.entity.User;
import com.collabsphere.entity.enums.UserRole;
import com.collabsphere.repository.ProjectRepository;
import com.collabsphere.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<Team> autoGenerateTeams(AutoGenerateTeamsRequest request, User user) {
        // Validate user role
        if (user.getRole() != UserRole.LECTURER && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only lecturers and admins can generate teams");
        }

        // Find project
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));

        // Validate lecturer owns the project (unless admin)
        if (user.getRole() == UserRole.LECTURER && 
            !project.getClassRoom().getLecturer().getId().equals(user.getId())) {
            throw new RuntimeException("You can only generate teams for your own projects");
        }

        // Check if teams already exist for this project
        List<Team> existingTeams = teamRepository.findByProjectId(request.getProjectId());
        if (!existingTeams.isEmpty()) {
            throw new RuntimeException("Teams already exist for this project. Please delete existing teams first.");
        }

        // Get students from the classroom
        Set<User> studentsSet = project.getClassRoom().getStudents();
        List<User> students = new ArrayList<>(studentsSet);

        if (students.isEmpty()) {
            throw new RuntimeException("No students found in the classroom");
        }

        if (students.size() < request.getGroupSize()) {
            throw new RuntimeException("Not enough students to form teams of size " + request.getGroupSize());
        }

        // Shuffle students randomly
        Collections.shuffle(students);

        // Generate teams
        List<Team> teams = new ArrayList<>();
        int teamNumber = 1;
        
        for (int i = 0; i < students.size(); i += request.getGroupSize()) {
            int endIndex = Math.min(i + request.getGroupSize(), students.size());
            List<User> teamMembers = students.subList(i, endIndex);

            // Create team
            Team team = new Team();
            team.setName("Team " + teamNumber);
            team.setProject(project);
            
            // Save team first to get ID
            team = teamRepository.save(team);
            
            // Add members to team
            for (User student : teamMembers) {
                team.getMembers().add(student);
                student.getTeams().add(team);
            }
            
            // Save team with members
            team = teamRepository.save(team);
            teams.add(team);
            teamNumber++;
        }

        // Handle remaining students (if any) - add them to existing teams
        int remainingStudents = students.size() % request.getGroupSize();
        if (remainingStudents > 0 && teams.size() > 0) {
            int startIndex = students.size() - remainingStudents;
            List<User> remainingStudentsList = students.subList(startIndex, students.size());
            
            // Distribute remaining students to existing teams
            for (int i = 0; i < remainingStudentsList.size(); i++) {
                Team targetTeam = teams.get(i % teams.size());
                User student = remainingStudentsList.get(i);
                
                targetTeam.getMembers().add(student);
                student.getTeams().add(targetTeam);
                teamRepository.save(targetTeam);
            }
        }

        return teams;
    }

    public List<Team> getTeamsByProject(Long projectId) {
        return teamRepository.findByProjectId(projectId);
    }

    public List<Team> getTeamsByUser(Long userId) {
        return teamRepository.findByMemberId(userId);
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Team addMemberToTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));

        // Find user in the same classroom
        Set<User> classroomStudents = team.getProject().getClassRoom().getStudents();
        User user = classroomStudents.stream()
            .filter(s -> s.getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User not found in this classroom"));

        if (team.getMembers().contains(user)) {
            throw new RuntimeException("User is already a member of this team");
        }

        team.getMembers().add(user);
        user.getTeams().add(team);

        return teamRepository.save(team);
    }

    public Team removeMemberFromTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));

        User user = team.getMembers().stream()
            .filter(m -> m.getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User is not a member of this team"));

        team.getMembers().remove(user);
        user.getTeams().remove(team);

        return teamRepository.save(team);
    }

    public void deleteTeamsByProject(Long projectId) {
        List<Team> teams = teamRepository.findByProjectId(projectId);
        for (Team team : teams) {
            // Remove team from all members
            for (User member : team.getMembers()) {
                member.getTeams().remove(team);
            }
            team.getMembers().clear();
        }
        teamRepository.deleteAll(teams);
    }
}