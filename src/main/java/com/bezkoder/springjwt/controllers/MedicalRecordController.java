package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.models.MedicalRecord;
import com.bezkoder.springjwt.payload.request.MedicalRecordRequest;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.payload.response.MedicalRecordResponse;
import com.bezkoder.springjwt.services.MedicalRecordService;
import com.bezkoder.springjwt.services.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;
    
    @Autowired
    private AuditService auditService;

    /**
     * Get all medical records (Admin and clinical staff only)
     * GET /api/medical-records
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHYSICIAN') or hasRole('NURSE') or hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<List<MedicalRecordResponse>> getAllMedicalRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String recordType) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditService.logAccess(auth.getName(), "MEDICAL_RECORDS_LIST", "Accessed medical records list");
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MedicalRecord> records = medicalRecordService.getAllMedicalRecords(pageable, department, recordType);
        List<MedicalRecordResponse> responses = records.getContent().stream()
            .map(this::convertToResponse)
            .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get medical record by ID (Strict access control)
     * GET /api/medical-records/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE') or hasRole('PATIENT') or hasRole('ADMIN') or " +
                  "hasRole('EMERGENCY_ACCESS') or @medicalRecordService.hasAccessToRecord(#id)")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            Optional<MedicalRecord> record = medicalRecordService.getMedicalRecordById(id);
            
            if (record.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            MedicalRecord medicalRecord = record.get();
            
            // Additional authorization checks
            if (!hasRecordAccess(medicalRecord, auth)) {
                auditService.logUnauthorizedAccess(auth.getName(), "MEDICAL_RECORD", id.toString(), 
                    "Attempted unauthorized access to medical record");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied to this medical record"));
            }
            
            // Log successful access
            auditService.logAccess(auth.getName(), "MEDICAL_RECORD_VIEW", 
                "Accessed medical record: " + medicalRecord.getRecordNumber());
            
            // Update last accessed information
            medicalRecordService.updateLastAccessed(id, auth.getName());
            
            return ResponseEntity.ok(convertToResponse(medicalRecord));
            
        } catch (Exception e) {
            auditService.logError(auth.getName(), "MEDICAL_RECORD_ACCESS_ERROR", 
                "Error accessing medical record ID: " + id + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error accessing medical record"));
        }
    }

    /**
     * Get patient's own medical records
     * GET /api/medical-records/my-records
     */
    @GetMapping("/my-records")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecordResponse>> getMyMedicalRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
        Page<MedicalRecord> records = medicalRecordService.getPatientMedicalRecords(username, pageable);
        
        List<MedicalRecordResponse> responses = records.getContent().stream()
            .map(this::convertToResponse)
            .toList();
        
        auditService.logAccess(username, "PATIENT_RECORDS_VIEW", "Patient accessed own medical records");
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get records by patient ID (Clinical staff only)
     * GET /api/medical-records/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE') or hasRole('NURSE_PRACTITIONER') or " +
                  "hasRole('PHYSICIAN_ASSISTANT') or hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<List<MedicalRecordResponse>> getRecordsByPatientId(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
        Page<MedicalRecord> records = medicalRecordService.getRecordsByPatientId(patientId, pageable);
        
        List<MedicalRecordResponse> responses = records.getContent().stream()
            .map(this::convertToResponse)
            .toList();
        
        auditService.logAccess(auth.getName(), "PATIENT_RECORDS_ACCESS", 
            "Accessed records for patient ID: " + patientId);
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Create new medical record (Clinical staff only)
     * POST /api/medical-records
     */
    @PostMapping
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE') or hasRole('NURSE_PRACTITIONER') or " +
                  "hasRole('PHYSICIAN_ASSISTANT')")
    public ResponseEntity<?> createMedicalRecord(@Valid @RequestBody MedicalRecordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            MedicalRecord createdRecord = medicalRecordService.createMedicalRecord(request, auth.getName());
            
            auditService.logCreation(auth.getName(), "MEDICAL_RECORD", createdRecord.getId().toString(),
                "Created medical record: " + createdRecord.getRecordNumber());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(createdRecord));
                
        } catch (RuntimeException e) {
            auditService.logError(auth.getName(), "MEDICAL_RECORD_CREATE_ERROR", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update medical record (Attending physician, care team, or admin)
     * PUT /api/medical-records/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE_PRACTITIONER') or hasRole('ADMIN') or " +
                  "@medicalRecordService.canModifyRecord(#id)")
    public ResponseEntity<?> updateMedicalRecord(@PathVariable Long id, 
                                               @Valid @RequestBody MedicalRecordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(id, request, auth.getName());
            
            auditService.logModification(auth.getName(), "MEDICAL_RECORD", id.toString(),
                "Updated medical record: " + updatedRecord.getRecordNumber());
            
            return ResponseEntity.ok(convertToResponse(updatedRecord));
            
        } catch (RuntimeException e) {
            auditService.logError(auth.getName(), "MEDICAL_RECORD_UPDATE_ERROR", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Emergency access to medical record (Break-glass access)
     * POST /api/medical-records/{id}/emergency-access
     */
    @PostMapping("/{id}/emergency-access")
    @PreAuthorize("hasRole('EMERGENCY_ACCESS') or hasRole('EMERGENCY') or hasRole('PHYSICIAN')")
    public ResponseEntity<?> emergencyAccess(@PathVariable Long id, 
                                           @RequestBody EmergencyAccessRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            MedicalRecord record = medicalRecordService.grantEmergencyAccess(id, auth.getName(), request.getReason());
            
            auditService.logEmergencyAccess(auth.getName(), "MEDICAL_RECORD_EMERGENCY", id.toString(),
                "Emergency access granted. Reason: " + request.getReason());
            
            return ResponseEntity.ok(convertToResponse(record));
            
        } catch (RuntimeException e) {
            auditService.logError(auth.getName(), "EMERGENCY_ACCESS_ERROR", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Add user to care team (Attending physician or admin)
     * POST /api/medical-records/{id}/care-team/{userId}
     */
    @PostMapping("/{id}/care-team/{userId}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('ADMIN') or " +
                  "@medicalRecordService.isAttendingPhysician(#id)")
    public ResponseEntity<?> addToCareTeam(@PathVariable Long id, @PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            medicalRecordService.addToCareTeam(id, userId);
            
            auditService.logModification(auth.getName(), "CARE_TEAM", id.toString(),
                "Added user " + userId + " to care team for record " + id);
            
            return ResponseEntity.ok(new MessageResponse("User added to care team successfully"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get records by department (Department staff and admin)
     * GET /api/medical-records/department/{department}
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('RADIOLOGY') and #department == 'RADIOLOGY') or " +
                  "(hasRole('LABORATORY') and #department == 'LABORATORY') or " +
                  "(hasRole('PHARMACY') and #department == 'PHARMACY') or " +
                  "(hasRole('EMERGENCY') and #department == 'EMERGENCY') or " +
                  "(hasRole('ICU') and #department == 'ICU')")
    public ResponseEntity<List<MedicalRecordResponse>> getRecordsByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
        Page<MedicalRecord> records = medicalRecordService.getRecordsByDepartment(department, pageable);
        
        List<MedicalRecordResponse> responses = records.getContent().stream()
            .map(this::convertToResponse)
            .toList();
        
        auditService.logAccess(auth.getName(), "DEPARTMENT_RECORDS", 
            "Accessed records for department: " + department);
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Archive medical record (Admin only)
     * PUT /api/medical-records/{id}/archive
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<?> archiveRecord(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            medicalRecordService.archiveRecord(id, auth.getName());
            
            auditService.logModification(auth.getName(), "MEDICAL_RECORD_ARCHIVE", id.toString(),
                "Archived medical record");
            
            return ResponseEntity.ok(new MessageResponse("Medical record archived successfully"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Search medical records (Clinical staff only)
     * GET /api/medical-records/search
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE') or hasRole('MEDICAL_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<List<MedicalRecordResponse>> searchRecords(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicalRecord> records = medicalRecordService.searchRecords(query, pageable);
        
        List<MedicalRecordResponse> responses = records.getContent().stream()
            .map(this::convertToResponse)
            .toList();
        
        auditService.logAccess(auth.getName(), "MEDICAL_RECORDS_SEARCH", 
            "Searched medical records with query: " + query);
        
        return ResponseEntity.ok(responses);
    }

    // Helper Methods

    /**
     * Check if user has access to specific medical record
     */
    private boolean hasRecordAccess(MedicalRecord record, Authentication auth) {
        String username = auth.getName();
        
        // Admin and emergency access always allowed
        if (auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_ADMIN") || 
            a.getAuthority().equals("ROLE_EMERGENCY_ACCESS") ||
            a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        
        // Patient can access their own records
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            return record.getPatient().getUsername().equals(username);
        }
        
        // Clinical staff access based on care team membership or attending physician
        if (auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_PHYSICIAN") ||
            a.getAuthority().equals("ROLE_NURSE") ||
            a.getAuthority().equals("ROLE_NURSE_PRACTITIONER") ||
            a.getAuthority().equals("ROLE_PHYSICIAN_ASSISTANT"))) {
            
            // Check if user is attending physician
            if (record.getAttendingPhysician() != null && 
                record.getAttendingPhysician().getUsername().equals(username)) {
                return true;
            }
            
            // Check if user is in care team
            if (record.getCareTeam().stream().anyMatch(user -> user.getUsername().equals(username))) {
                return true;
            }
            
            // Check if user is specifically authorized
            if (record.getAuthorizedUsers().stream().anyMatch(user -> user.getUsername().equals(username))) {
                return true;
            }
        }
        
        // Department-specific access
        String department = record.getDepartment();
        if (department != null) {
            switch (department.toUpperCase()) {
                case "RADIOLOGY":
                    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RADIOLOGY"));
                case "LABORATORY":
                    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LABORATORY"));
                case "PHARMACY":
                    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PHARMACY"));
                case "EMERGENCY":
                    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMERGENCY"));
                case "ICU":
                    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ICU"));
            }
        }
        
        // Special access for sensitive records
        if (record.getIsSensitive() && !record.getBreakGlassAccess()) {
            return false;
        }
        
        return false;
    }

    /**
     * Convert MedicalRecord entity to response DTO
     */
    private MedicalRecordResponse convertToResponse(MedicalRecord record) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Create response with appropriate level of detail based on role
        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setRecordNumber(record.getRecordNumber());
        response.setRecordType(record.getRecordType().toString());
        response.setVisitDate(record.getVisitDate());
        response.setDepartment(record.getDepartment());
        response.setRecordStatus(record.getRecordStatus().toString());
        
        // Basic patient info (always included for authorized users)
        if (record.getPatient() != null) {
            response.setPatientId(record.getPatient().getId());
            response.setPatientName(record.getPatient().getFullName());
        }
        
        // Attending physician info
        if (record.getAttendingPhysician() != null) {
            response.setAttendingPhysicianName(record.getAttendingPhysician().getFullName());
        }
        
        // Clinical details (restricted based on role)
        if (hasDetailedAccess(auth)) {
            response.setChiefComplaint(record.getChiefComplaint());
            response.setAssessmentPlan(record.getAssessmentPlan());
            response.setDiagnosisCodes(record.getDiagnosisCodes());
            response.setMedications(record.getMedications());
            
            // Vital signs
            response.setBloodPressure(record.getBloodPressure());
            response.setHeartRate(record.getHeartRate());
            response.setTemperature(record.getTemperature());
            response.setRespiratoryRate(record.getRespiratoryRate());
            response.setOxygenSaturation(record.getOxygenSaturation());
        }
        
        // Highly sensitive information (physicians and authorized users only)
        if (hasFullAccess(auth)) {
            response.setHistoryPresentIllness(record.getHistoryPresentIllness());
            response.setPhysicalExamination(record.getPhysicalExamination());
            response.setProcedureCodes(record.getProcedureCodes());
            response.setLabOrders(record.getLabOrders());
            response.setImagingOrders(record.getImagingOrders());
        }
        
        // Administrative info (admin roles only)
        if (hasAdminAccess(auth)) {
            response.setCreatedBy(record.getCreatedBy());
            response.setCreatedAt(record.getCreatedAt());
            response.setLastAccessed(record.getLastAccessed());
            response.setLastAccessedBy(record.getLastAccessedBy());
            response.setConfidentialityLevel(record.getConfidentialityLevel().toString());
        }
        
        return response;
    }

    /**
     * Check if user has detailed access (nurses and above)
     */
    private boolean hasDetailedAccess(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_PHYSICIAN") ||
            a.getAuthority().equals("ROLE_NURSE") ||
            a.getAuthority().equals("ROLE_NURSE_PRACTITIONER") ||
            a.getAuthority().equals("ROLE_PHYSICIAN_ASSISTANT") ||
            a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if user has full clinical access (physicians and NPs)
     */
    private boolean hasFullAccess(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_PHYSICIAN") ||
            a.getAuthority().equals("ROLE_NURSE_PRACTITIONER") ||
            a.getAuthority().equals("ROLE_SPECIALIST") ||
            a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if user has administrative access
     */
    private boolean hasAdminAccess(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_ADMIN") ||
            a.getAuthority().equals("ROLE_MEDICAL_ADMIN") ||
            a.getAuthority().equals("ROLE_COMPLIANCE_OFFICER") ||
            a.getAuthority().equals("ROLE_AUDITOR"));
    }

    /**
     * Inner class for emergency access request
     */
    public static class EmergencyAccessRequest {
        private String reason;
        private String justification;
        private boolean acknowledged;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getJustification() { return justification; }
        public void setJustification(String justification) { this.justification = justification; }

        public boolean isAcknowledged() { return acknowledged; }
        public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    }
}