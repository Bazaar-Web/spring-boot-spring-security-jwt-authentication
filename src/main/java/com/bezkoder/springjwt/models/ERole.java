package com.bezkoder.springjwt.models;

public enum ERole {
  // ==== Basic System Roles ====
  ROLE_USER,              // Basic authenticated user
  ROLE_MODERATOR,         // Content/community moderator
  ROLE_ADMIN,             // System administrator
  
  // ==== Patient/Consumer Roles ====
  ROLE_PATIENT,           // Patient accessing their own records
  ROLE_PATIENT_GUARDIAN,  // Parent/guardian of minor patient
  ROLE_PATIENT_PROXY,     // Legal proxy/power of attorney
  
  // ==== Clinical Staff Roles ====
  ROLE_PHYSICIAN,         // Licensed physician/doctor
  ROLE_NURSE,             // Registered nurse
  ROLE_NURSE_PRACTITIONER, // Advanced practice nurse
  ROLE_PHYSICIAN_ASSISTANT, // Physician assistant
  ROLE_THERAPIST,         // Physical/occupational/speech therapist
  ROLE_PHARMACIST,        // Licensed pharmacist
  ROLE_TECHNICIAN,        // Medical technician (lab, radiology, etc.)
  ROLE_SPECIALIST,        // Medical specialist (cardiologist, etc.)
  
  // ==== Administrative Healthcare Roles ====
  ROLE_MEDICAL_ADMIN,     // Medical office administrator
  ROLE_MEDICAL_ASSISTANT, // Medical assistant
  ROLE_RECEPTIONIST,      // Front desk/scheduling
  ROLE_BILLING_ADMIN,     // Billing and coding specialist
  ROLE_INSURANCE_COORDINATOR, // Insurance verification/authorization
  
  // ==== Compliance & Quality Roles ====
  ROLE_COMPLIANCE_OFFICER, // HIPAA/regulatory compliance
  ROLE_QUALITY_ASSURANCE, // Quality assurance specialist
  ROLE_RISK_MANAGER,      // Risk management
  ROLE_PRIVACY_OFFICER,   // Privacy officer
  ROLE_SECURITY_OFFICER,  // Information security officer
  
  // ==== IT & Technical Roles ====
  ROLE_IT_ADMIN,          // IT system administrator
  ROLE_DATABASE_ADMIN,    // Database administrator
  ROLE_SYSTEM_ANALYST,    // System analyst
  ROLE_HELPDESK,          // IT helpdesk support
  
  // ==== Research & Analytics Roles ====
  ROLE_RESEARCHER,        // Clinical researcher
  ROLE_DATA_ANALYST,      // Healthcare data analyst
  ROLE_BIOSTATISTICIAN,   // Biostatistics specialist
  ROLE_CLINICAL_COORDINATOR, // Clinical trial coordinator
  
  // ==== Emergency & Special Access ====
  ROLE_EMERGENCY_ACCESS,  // Emergency break-glass access
  ROLE_AUDITOR,          // System auditor
  ROLE_CONSULTANT,       // External consultant (limited access)
  
  // ==== Department-Specific Roles ====
  ROLE_RADIOLOGY,        // Radiology department staff
  ROLE_LABORATORY,       // Laboratory staff
  ROLE_PHARMACY,         // Pharmacy department
  ROLE_SURGERY,          // Surgical staff
  ROLE_EMERGENCY,        // Emergency department staff
  ROLE_ICU,              // Intensive care unit staff
  ROLE_PEDIATRIC,        // Pediatric department staff
  
  // ==== External Partner Roles ====
  ROLE_INSURANCE_PROVIDER, // Insurance company representative
  ROLE_VENDOR,           // Third-party vendor (limited access)
  ROLE_LEGAL,            // Legal counsel
  ROLE_EXTERNAL_AUDITOR, // External auditor
  
  // ==== Service Roles ====
  ROLE_CASE_MANAGER,     // Patient case manager
  ROLE_SOCIAL_WORKER,    // Medical social worker
  ROLE_CHAPLAIN,         // Hospital chaplain
  ROLE_INTERPRETER,      // Medical interpreter
  
  // ==== Training & Education ====
  ROLE_STUDENT,          // Medical student/intern
  ROLE_RESIDENT,         // Medical resident
  ROLE_FELLOW,           // Medical fellow
  ROLE_INSTRUCTOR,       // Clinical instructor
  
  // ==== Super Admin Roles ====
  ROLE_SUPER_ADMIN,      // Highest level system access
  ROLE_SYSTEM_OWNER      // System owner (break-glass)
}