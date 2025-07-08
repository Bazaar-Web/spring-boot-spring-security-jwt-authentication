package com.bezkoder.springjwt.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "record_number", unique = true, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String recordNumber;
    
    // Patient Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    // Provider Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attending_physician_id")
    private User attendingPhysician;
    
    @Column(name = "department")
    @Size(max = 50)
    private String department;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level")
    private PriorityLevel priorityLevel;
    
    // Visit Information
    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;
    
    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;
    
    @Column(name = "admission_type")
    @Size(max = 50)
    private String admissionType;
    
    // Clinical Information
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;
    
    @Column(name = "history_present_illness", columnDefinition = "TEXT")
    private String historyPresentIllness;
    
    @Column(name = "physical_examination", columnDefinition = "TEXT")
    private String physicalExamination;
    
    @Column(name = "assessment_plan", columnDefinition = "TEXT")
    private String assessmentPlan;
    
    @Column(name = "diagnosis_codes", columnDefinition = "TEXT")
    private String diagnosisCodes; // ICD-10 codes
    
    @Column(name = "procedure_codes", columnDefinition = "TEXT")
    private String procedureCodes; // CPT codes
    
    // Medications and Orders
    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications;
    
    @Column(name = "lab_orders", columnDefinition = "TEXT")
    private String labOrders;
    
    @Column(name = "imaging_orders", columnDefinition = "TEXT")
    private String imagingOrders;
    
    // Vital Signs
    @Column(name = "blood_pressure")
    @Size(max = 20)
    private String bloodPressure;
    
    @Column(name = "heart_rate")
    private Integer heartRate;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;
    
    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;
    
    // Status and Security
    @Enumerated(EnumType.STRING)
    @Column(name = "record_status", nullable = false)
    private RecordStatus recordStatus;
    
    @Column(name = "confidentiality_level")
    @Enumerated(EnumType.STRING)
    private ConfidentialityLevel confidentialityLevel;
    
    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;
    
    @Column(name = "break_glass_access")
    private Boolean breakGlassAccess = false;
    
    // Authorized Users for this specific record
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "medical_record_authorized_users",
            joinColumns = @JoinColumn(name = "medical_record_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> authorizedUsers = new HashSet<>();
    
    // Care Team
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "medical_record_care_team",
            joinColumns = @JoinColumn(name = "medical_record_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> careTeam = new HashSet<>();
    
    // Audit Trail
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "last_accessed_by")
    private String lastAccessedBy;
    
    // Enums
    public enum RecordType {
        INPATIENT, OUTPATIENT, EMERGENCY, SURGERY, CONSULTATION, 
        LAB_RESULT, RADIOLOGY, PHARMACY, DISCHARGE_SUMMARY
    }
    
    public enum PriorityLevel {
        ROUTINE, URGENT, STAT, EMERGENCY
    }
    
    public enum RecordStatus {
        DRAFT, ACTIVE, COMPLETED, AMENDED, ARCHIVED, DELETED
    }
    
    public enum ConfidentialityLevel {
        NORMAL, RESTRICTED, HIGHLY_RESTRICTED, VIP
    }
    
    // Constructors
    public MedicalRecord() {}
    
    public MedicalRecord(String recordNumber, User patient, RecordType recordType, LocalDateTime visitDate) {
        this.recordNumber = recordNumber;
        this.patient = patient;
        this.recordType = recordType;
        this.visitDate = visitDate;
        this.recordStatus = RecordStatus.DRAFT;
        this.confidentialityLevel = ConfidentialityLevel.NORMAL;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getRecordNumber() { return recordNumber; }
    public void setRecordNumber(String recordNumber) { this.recordNumber = recordNumber; }
    
    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }
    
    public User getAttendingPhysician() { return attendingPhysician; }
    public void setAttendingPhysician(User attendingPhysician) { this.attendingPhysician = attendingPhysician; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public RecordType getRecordType() { return recordType; }
    public void setRecordType(RecordType recordType) { this.recordType = recordType; }
    
    public PriorityLevel getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(PriorityLevel priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
    
    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }
    
    public String getAdmissionType() { return admissionType; }
    public void setAdmissionType(String admissionType) { this.admissionType = admissionType; }
    
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    
    public String getHistoryPresentIllness() { return historyPresentIllness; }
    public void setHistoryPresentIllness(String historyPresentIllness) { this.historyPresentIllness = historyPresentIllness; }
    
    public String getPhysicalExamination() { return physicalExamination; }
    public void setPhysicalExamination(String physicalExamination) { this.physicalExamination = physicalExamination; }
    
    public String getAssessmentPlan() { return assessmentPlan; }
    public void setAssessmentPlan(String assessmentPlan) { this.assessmentPlan = assessmentPlan; }
    
    public String getDiagnosisCodes() { return diagnosisCodes; }
    public void setDiagnosisCodes(String diagnosisCodes) { this.diagnosisCodes = diagnosisCodes; }
    
    public String getProcedureCodes() { return procedureCodes; }
    public void setProcedureCodes(String procedureCodes) { this.procedureCodes = procedureCodes; }
    
    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }
    
    public String getLabOrders() { return labOrders; }
    public void setLabOrders(String labOrders) { this.labOrders = labOrders; }
    
    public String getImagingOrders() { return imagingOrders; }
    public void setImagingOrders(String imagingOrders) { this.imagingOrders = imagingOrders; }
    
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Integer getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Integer respiratoryRate) { this.respiratoryRate = respiratoryRate; }
    
    public Integer getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(Integer oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }
    
    public RecordStatus getRecordStatus() { return recordStatus; }
    public void setRecordStatus(RecordStatus recordStatus) { this.recordStatus = recordStatus; }
    
    public ConfidentialityLevel getConfidentialityLevel() { return confidentialityLevel; }
    public void setConfidentialityLevel(ConfidentialityLevel confidentialityLevel) { this.confidentialityLevel = confidentialityLevel; }
    
    public Boolean getIsSensitive() { return isSensitive; }
    public void setIsSensitive(Boolean isSensitive) { this.isSensitive = isSensitive; }
    
    public Boolean getBreakGlassAccess() { return breakGlassAccess; }
    public void setBreakGlassAccess(Boolean breakGlassAccess) { this.breakGlassAccess = breakGlassAccess; }
    
    public Set<User> getAuthorizedUsers() { return authorizedUsers; }
    public void setAuthorizedUsers(Set<User> authorizedUsers) { this.authorizedUsers = authorizedUsers; }
    
    public Set<User> getCareTeam() { return careTeam; }
    public void setCareTeam(Set<User> careTeam) { this.careTeam = careTeam; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
    
    public String getLastAccessedBy() { return lastAccessedBy; }
    public void setLastAccessedBy(String lastAccessedBy) { this.lastAccessedBy = lastAccessedBy; }
    
    // Utility Methods
    public boolean isPatientRecord(Long userId) {
        return patient != null && patient.getId().equals(userId);
    }
    
    public boolean isCareTeamMember(Long userId) {
        return careTeam.stream().anyMatch(user -> user.getId().equals(userId));
    }
    
    public boolean isAuthorizedUser(Long userId) {
        return authorizedUsers.stream().anyMatch(user -> user.getId().equals(userId));
    }
    
    public boolean isAttendingPhysician(Long userId) {
        return attendingPhysician != null && attendingPhysician.getId().equals(userId);
    }
}