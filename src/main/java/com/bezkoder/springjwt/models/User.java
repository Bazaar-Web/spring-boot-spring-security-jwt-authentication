package com.bezkoder.springjwt.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users", 
    uniqueConstraints = { 
      @UniqueConstraint(columnNames = "username"),
      @UniqueConstraint(columnNames = "email"),
      @UniqueConstraint(columnNames = "ssn"),
      @UniqueConstraint(columnNames = "medicalRecordNumber")
    })
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(max = 120)
  private String password;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  // ==== PII (Personally Identifiable Information) ====
  
  @Size(max = 50)
  @Column(name = "first_name")
  private String firstName;

  @Size(max = 50)
  @Column(name = "middle_name")
  private String middleName;

  @Size(max = 50)
  @Column(name = "last_name")
  private String lastName;

  @Past
  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "SSN must be in format XXX-XX-XXXX")
  @Size(max = 11)
  @Column(name = "ssn")
  private String ssn; // Social Security Number

  @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
  @Size(max = 20)
  @Column(name = "phone_number")
  private String phoneNumber;

  @Size(max = 100)
  @Column(name = "address_line1")
  private String addressLine1;

  @Size(max = 100)
  @Column(name = "address_line2")
  private String addressLine2;

  @Size(max = 50)
  @Column(name = "city")
  private String city;

  @Size(max = 50)
  @Column(name = "state")
  private String state;

  @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "ZIP code must be 5 digits or 5+4 format")
  @Size(max = 10)
  @Column(name = "zip_code")
  private String zipCode;

  @Size(max = 50)
  @Column(name = "country")
  private String country;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(name = "marital_status")
  private MaritalStatus maritalStatus;

  // ==== PHI (Protected Health Information) ====
  
  @Size(max = 50)
  @Column(name = "medical_record_number")
  private String medicalRecordNumber;

  @Size(max = 100)
  @Column(name = "primary_care_physician")
  private String primaryCarePhysician;

  @Size(max = 100)
  @Column(name = "insurance_provider")
  private String insuranceProvider;

  @Size(max = 50)
  @Column(name = "insurance_policy_number")
  private String insurancePolicyNumber;

  @Size(max = 50)
  @Column(name = "insurance_group_number")
  private String insuranceGroupNumber;

  @Column(name = "height_inches")
  private Integer heightInches;

  @Column(name = "weight_pounds")
  private Double weightPounds;

  @Size(max = 10)
  @Column(name = "blood_type")
  private String bloodType;

  @Column(name = "allergies", columnDefinition = "TEXT")
  private String allergies;

  @Column(name = "medications", columnDefinition = "TEXT")
  private String medications;

  @Column(name = "medical_conditions", columnDefinition = "TEXT")
  private String medicalConditions;

  @Column(name = "emergency_contact_name")
  @Size(max = 100)
  private String emergencyContactName;

  @Column(name = "emergency_contact_phone")
  @Size(max = 20)
  private String emergencyContactPhone;

  @Column(name = "emergency_contact_relationship")
  @Size(max = 50)
  private String emergencyContactRelationship;

  // ==== Additional Security/Compliance Fields ====
  
  @Column(name = "consent_to_treatment")
  private Boolean consentToTreatment;

  @Column(name = "consent_to_marketing")
  private Boolean consentToMarketing;

  @Column(name = "hipaa_authorization_signed")
  private Boolean hipaaAuthorizationSigned;

  @Column(name = "hipaa_authorization_date")
  private LocalDateTime hipaaAuthorizationDate;

  @Column(name = "data_retention_expiry")
  private LocalDateTime dataRetentionExpiry;

  @Column(name = "account_locked")
  private Boolean accountLocked = false;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts = 0;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "password_last_changed")
  private LocalDateTime passwordLastChanged;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  // ==== Enums ====
  
  public enum Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
  }

  public enum MaritalStatus {
    SINGLE, MARRIED, DIVORCED, WIDOWED, SEPARATED, DOMESTIC_PARTNERSHIP
  }

  // ==== Constructors ====
  
  public User() {
  }

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  // ==== Getters and Setters ====
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  // PII Getters and Setters
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getSsn() {
    return ssn;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public MaritalStatus getMaritalStatus() {
    return maritalStatus;
  }

  public void setMaritalStatus(MaritalStatus maritalStatus) {
    this.maritalStatus = maritalStatus;
  }

  // PHI Getters and Setters
  public String getMedicalRecordNumber() {
    return medicalRecordNumber;
  }

  public void setMedicalRecordNumber(String medicalRecordNumber) {
    this.medicalRecordNumber = medicalRecordNumber;
  }

  public String getPrimaryCarePhysician() {
    return primaryCarePhysician;
  }

  public void setPrimaryCarePhysician(String primaryCarePhysician) {
    this.primaryCarePhysician = primaryCarePhysician;
  }

  public String getInsuranceProvider() {
    return insuranceProvider;
  }

  public void setInsuranceProvider(String insuranceProvider) {
    this.insuranceProvider = insuranceProvider;
  }

  public String getInsurancePolicyNumber() {
    return insurancePolicyNumber;
  }

  public void setInsurancePolicyNumber(String insurancePolicyNumber) {
    this.insurancePolicyNumber = insurancePolicyNumber;
  }

  public String getInsuranceGroupNumber() {
    return insuranceGroupNumber;
  }

  public void setInsuranceGroupNumber(String insuranceGroupNumber) {
    this.insuranceGroupNumber = insuranceGroupNumber;
  }

  public Integer getHeightInches() {
    return heightInches;
  }

  public void setHeightInches(Integer heightInches) {
    this.heightInches = heightInches;
  }

  public Double getWeightPounds() {
    return weightPounds;
  }

  public void setWeightPounds(Double weightPounds) {
    this.weightPounds = weightPounds;
  }

  public String getBloodType() {
    return bloodType;
  }

  public void setBloodType(String bloodType) {
    this.bloodType = bloodType;
  }

  public String getAllergies() {
    return allergies;
  }

  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }

  public String getMedications() {
    return medications;
  }

  public void setMedications(String medications) {
    this.medications = medications;
  }

  public String getMedicalConditions() {
    return medicalConditions;
  }

  public void setMedicalConditions(String medicalConditions) {
    this.medicalConditions = medicalConditions;
  }

  public String getEmergencyContactName() {
    return emergencyContactName;
  }

  public void setEmergencyContactName(String emergencyContactName) {
    this.emergencyContactName = emergencyContactName;
  }

  public String getEmergencyContactPhone() {
    return emergencyContactPhone;
  }

  public void setEmergencyContactPhone(String emergencyContactPhone) {
    this.emergencyContactPhone = emergencyContactPhone;
  }

  public String getEmergencyContactRelationship() {
    return emergencyContactRelationship;
  }

  public void setEmergencyContactRelationship(String emergencyContactRelationship) {
    this.emergencyContactRelationship = emergencyContactRelationship;
  }

  // Security/Compliance Getters and Setters
  public Boolean getConsentToTreatment() {
    return consentToTreatment;
  }

  public void setConsentToTreatment(Boolean consentToTreatment) {
    this.consentToTreatment = consentToTreatment;
  }

  public Boolean getConsentToMarketing() {
    return consentToMarketing;
  }

  public void setConsentToMarketing(Boolean consentToMarketing) {
    this.consentToMarketing = consentToMarketing;
  }

  public Boolean getHipaaAuthorizationSigned() {
    return hipaaAuthorizationSigned;
  }

  public void setHipaaAuthorizationSigned(Boolean hipaaAuthorizationSigned) {
    this.hipaaAuthorizationSigned = hipaaAuthorizationSigned;
  }

  public LocalDateTime getHipaaAuthorizationDate() {
    return hipaaAuthorizationDate;
  }

  public void setHipaaAuthorizationDate(LocalDateTime hipaaAuthorizationDate) {
    this.hipaaAuthorizationDate = hipaaAuthorizationDate;
  }

  public LocalDateTime getDataRetentionExpiry() {
    return dataRetentionExpiry;
  }

  public void setDataRetentionExpiry(LocalDateTime dataRetentionExpiry) {
    this.dataRetentionExpiry = dataRetentionExpiry;
  }

  public Boolean getAccountLocked() {
    return accountLocked;
  }

  public void setAccountLocked(Boolean accountLocked) {
    this.accountLocked = accountLocked;
  }

  public Integer getFailedLoginAttempts() {
    return failedLoginAttempts;
  }

  public void setFailedLoginAttempts(Integer failedLoginAttempts) {
    this.failedLoginAttempts = failedLoginAttempts;
  }

  public LocalDateTime getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(LocalDateTime lastLogin) {
    this.lastLogin = lastLogin;
  }

  public LocalDateTime getPasswordLastChanged() {
    return passwordLastChanged;
  }

  public void setPasswordLastChanged(LocalDateTime passwordLastChanged) {
    this.passwordLastChanged = passwordLastChanged;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  // ==== Utility Methods ====
  
  public String getFullName() {
    StringBuilder fullName = new StringBuilder();
    if (firstName != null) fullName.append(firstName);
    if (middleName != null && !middleName.isEmpty()) {
      if (fullName.length() > 0) fullName.append(" ");
      fullName.append(middleName);
    }
    if (lastName != null) {
      if (fullName.length() > 0) fullName.append(" ");
      fullName.append(lastName);
    }
    return fullName.toString();
  }

  public String getFullAddress() {
    StringBuilder address = new StringBuilder();
    if (addressLine1 != null) address.append(addressLine1);
    if (addressLine2 != null && !addressLine2.isEmpty()) {
      if (address.length() > 0) address.append(", ");
      address.append(addressLine2);
    }
    if (city != null) {
      if (address.length() > 0) address.append(", ");
      address.append(city);
    }
    if (state != null) {
      if (address.length() > 0) address.append(", ");
      address.append(state);
    }
    if (zipCode != null) {
      if (address.length() > 0) address.append(" ");
      address.append(zipCode);
    }
    if (country != null) {
      if (address.length() > 0) address.append(", ");
      address.append(country);
    }
    return address.toString();
  }

  public boolean isAccountExpired() {
    return dataRetentionExpiry != null && dataRetentionExpiry.isBefore(LocalDateTime.now());
  }

  public boolean isAccountLocked() {
    return accountLocked != null && accountLocked;
  }
}