package com.collabsphere.service;

import com.collabsphere.dto.AddStudentRequest;
import com.collabsphere.dto.CreateClassRequest;
import com.collabsphere.entity.ClassRoom;
import com.collabsphere.entity.User;
import com.collabsphere.entity.enums.UserRole;
import com.collabsphere.repository.ClassRoomRepository;
import com.collabsphere.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClassRoomService {

    @Autowired
    private ClassRoomRepository classRoomRepository;

    @Autowired
    private UserRepository userRepository;

    public ClassRoom createClassRoom(CreateClassRequest request, User lecturer) {
        // Validate lecturer role
        if (lecturer.getRole() != UserRole.LECTURER && lecturer.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only lecturers and admins can create classrooms");
        }

        // Check if code already exists
        if (classRoomRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Classroom code already exists");
        }

        ClassRoom classRoom = new ClassRoom();
        classRoom.setName(request.getName());
        classRoom.setCode(request.getCode());
        classRoom.setLecturer(lecturer);

        return classRoomRepository.save(classRoom);
    }

    public ClassRoom addStudentToClass(Long classId, AddStudentRequest request) {
        // Find classroom
        ClassRoom classRoom = classRoomRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Find user by email
        User student = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User with email " + request.getEmail() + " not found"));

        // Validate student role
        if (student.getRole() != UserRole.STUDENT) {
            throw new RuntimeException("Only students can be added to classrooms");
        }

        // Check if student is already in the class
        if (classRoom.getStudents().contains(student)) {
            throw new RuntimeException("Student is already in this classroom");
        }

        // Add student to classroom
        classRoom.getStudents().add(student);
        student.getClassRoomsAsStudent().add(classRoom);

        return classRoomRepository.save(classRoom);
    }

    public List<ClassRoom> getClassRoomsByLecturer(User lecturer) {
        return classRoomRepository.findByLecturer(lecturer);
    }

    public List<ClassRoom> getClassRoomsByStudent(Long studentId) {
        return classRoomRepository.findByStudentId(studentId);
    }

    public Optional<ClassRoom> getClassRoomById(Long id) {
        return classRoomRepository.findById(id);
    }

    public Optional<ClassRoom> getClassRoomByCode(String code) {
        return classRoomRepository.findByCode(code);
    }

    public List<ClassRoom> getAllClassRooms() {
        return classRoomRepository.findAll();
    }

    public ClassRoom removeStudentFromClass(Long classId, String email) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));

        User student = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Student not found"));

        classRoom.getStudents().remove(student);
        student.getClassRoomsAsStudent().remove(classRoom);

        return classRoomRepository.save(classRoom);
    }
}