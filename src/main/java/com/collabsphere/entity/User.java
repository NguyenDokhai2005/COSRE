package com.collabsphere.entity;

import com.collabsphere.entity.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    
    @Column(nullable = false)
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Column(nullable = false)
    private Boolean active = true;

    // Constructors
    public User() {}

    public User(String email, String password, String fullName, UserRole role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    
    // Quan hệ với ClassRoom (as lecturer)
    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ClassRoom> classRoomsAsLecturer = new HashSet<>();
    
    // Quan hệ với ClassRoom (as student)
    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ClassRoom> classRoomsAsStudent = new HashSet<>();
    
    // Quan hệ với Team
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Team> teams = new HashSet<>();
    
    // Quan hệ với Task (as assignee)
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Task> assignedTasks = new HashSet<>();
    
    // Quan hệ với Message
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Message> messages = new HashSet<>();

    // Getters and Setters for relationships
    public Set<ClassRoom> getClassRoomsAsLecturer() {
        return classRoomsAsLecturer;
    }

    public void setClassRoomsAsLecturer(Set<ClassRoom> classRoomsAsLecturer) {
        this.classRoomsAsLecturer = classRoomsAsLecturer;
    }

    public Set<ClassRoom> getClassRoomsAsStudent() {
        return classRoomsAsStudent;
    }

    public void setClassRoomsAsStudent(Set<ClassRoom> classRoomsAsStudent) {
        this.classRoomsAsStudent = classRoomsAsStudent;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }

    public Set<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(Set<Task> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }
}