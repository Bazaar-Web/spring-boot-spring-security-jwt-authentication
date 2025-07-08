package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.services.ExternalHealthcareService;
import com.bezkoder.springjwt.services.AuditService;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/external")
public class ExternalIntegrationController {

    @Autowired
    private ExternalHealthcareService externalHealthcareService;
    
    @Autowired
    private AuditService auditService;

    /**
     * Fetch patient data from Epic FHIR system
     * EXIT POINT: This endpoint triggers external connectivity to Epic's servers
     * GET /api/external/epic/patient/{patientId}
     */
    @GetMapping("/epic/patient/{patientId}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE') or hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<?> fetchEpicPatientData(@PathVariable String patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            // Log external data access attempt
            auditService.logExternalAccess(auth.getName(), "EPIC_FHIR_ACCESS", 
                "Attempting to fetch patient data from Epic for patient: " + patientId);
            
            Map<String, Object> patientData = externalHealthcareService.fetchEpicPatientData(patientId);
            
            auditService.logExternalAccess(auth.getName(), "EPIC_FHIR_SUCCESS", 
                "Successfully retrieved patient data from Epic for patient: " + patientId);
            
            return ResponseEntity.ok(patientData);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "EPIC_FHIR_ERROR", 
                "Failed to fetch Epic patient data: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to Epic FHIR system: " + e.getMessage()));
        }
    }

    /**
     * Lookup drug information from FDA database
     * EXIT POINT: This endpoint connects to FDA's external drug API
     * GET /api/external/fda/drug/{drugName}
     */
    @GetMapping("/fda/drug/{drugName}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('PHARMACIST') or hasRole('NURSE_PRACTITIONER')")
    public ResponseEntity<?> lookupDrugInformation(@PathVariable String drugName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalAccess(auth.getName(), "FDA_API_ACCESS", 
                "Querying FDA drug database for: " + drugName);
            
            List<Map<String, Object>> drugInfo = externalHealthcareService.fetchDrugInformation(drugName);
            
            auditService.logExternalAccess(auth.getName(), "FDA_API_SUCCESS", 
                "Successfully retrieved drug information from FDA for: " + drugName);
            
            return ResponseEntity.ok(drugInfo);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "FDA_API_ERROR", 
                "Failed to fetch FDA drug information: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to FDA drug database: " + e.getMessage()));
        }
    }

    /**
     * Verify insurance eligibility with external provider
     * EXIT POINT: This endpoint transmits PHI to external insurance verification system
     * POST /api/external/insurance/verify
     */
    @PostMapping("/insurance/verify")
    @PreAuthorize("hasRole('MEDICAL_ADMIN') or hasRole('BILLING_ADMIN') or hasRole('INSURANCE_COORDINATOR')")
    public ResponseEntity<?> verifyInsuranceEligibility(@RequestBody InsuranceVerificationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalAccess(auth.getName(), "INSURANCE_VERIFICATION", 
                "Verifying insurance eligibility for member: " + request.getMemberId() + 
                " with provider: " + request.getInsuranceProvider());
            
            Map<String, Object> eligibilityResult = externalHealthcareService.verifyInsuranceEligibility(
                request.getMemberId(), request.getInsuranceProvider());
            
            auditService.logExternalAccess(auth.getName(), "INSURANCE_VERIFICATION_SUCCESS", 
                "Successfully verified insurance eligibility for member: " + request.getMemberId());
            
            return ResponseEntity.ok(eligibilityResult);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "INSURANCE_VERIFICATION_ERROR", 
                "Failed to verify insurance eligibility: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to insurance verification system: " + e.getMessage()));
        }
    }

    /**
     * Submit lab order to external laboratory
     * EXIT POINT: This endpoint transmits patient data to external lab system
     * POST /api/external/lab/submit-order
     */
    @PostMapping("/lab/submit-order")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE_PRACTITIONER') or hasRole('PHYSICIAN_ASSISTANT')")
    public ResponseEntity<?> submitLabOrder(@RequestBody LabOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalDataTransmission(auth.getName(), "LAB_ORDER_SUBMISSION", 
                "Submitting lab order to external laboratory for patient: " + request.getPatientId());
            
            Map<String, Object> labResult = externalHealthcareService.submitLabOrder(
                request.getPatientId(), request.getLabOrder());
            
            auditService.logExternalDataTransmission(auth.getName(), "LAB_ORDER_SUCCESS", 
                "Successfully submitted lab order for patient: " + request.getPatientId());
            
            return ResponseEntity.ok(labResult);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "LAB_ORDER_ERROR", 
                "Failed to submit lab order: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to laboratory system: " + e.getMessage()));
        }
    }

    /**
     * Upload imaging study to external PACS
     * EXIT POINT: This endpoint transmits medical imaging data to external radiology partner
     * POST /api/external/imaging/upload
     */
    @PostMapping("/imaging/upload")
    @PreAuthorize("hasRole('RADIOLOGY') or hasRole('PHYSICIAN') or hasRole('TECHNICIAN')")
    public ResponseEntity<?> uploadImagingStudy(@RequestBody ImagingUploadRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalDataTransmission(auth.getName(), "IMAGING_UPLOAD", 
                "Uploading imaging study to external PACS for patient: " + request.getPatientId() + 
                ", study type: " + request.getStudyType());
            
            Map<String, Object> uploadResult = externalHealthcareService.uploadImagingStudy(
                request.getPatientId(), request.getStudyType(), request.getImagingData());
            
            auditService.logExternalDataTransmission(auth.getName(), "IMAGING_UPLOAD_SUCCESS", 
                "Successfully uploaded imaging study for patient: " + request.getPatientId());
            
            return ResponseEntity.ok(uploadResult);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "IMAGING_UPLOAD_ERROR", 
                "Failed to upload imaging study: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to PACS system: " + e.getMessage()));
        }
    }

    /**
     * Search clinical trials from NIH database
     * EXIT POINT: This endpoint connects to external NIH clinical trials database
     * GET /api/external/clinical-trials/search
     */
    @GetMapping("/clinical-trials/search")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('RESEARCHER') or hasRole('CLINICAL_COORDINATOR')")
    public ResponseEntity<?> searchClinicalTrials(
            @RequestParam String condition,
            @RequestParam(required = false) String location) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalAccess(auth.getName(), "CLINICAL_TRIALS_SEARCH", 
                "Searching NIH clinical trials for condition: " + condition);
            
            List<Map<String, Object>> trials = externalHealthcareService.searchClinicalTrials(condition, location);
            
            auditService.logExternalAccess(auth.getName(), "CLINICAL_TRIALS_SUCCESS", 
                "Successfully retrieved clinical trials for condition: " + condition);
            
            return ResponseEntity.ok(trials);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "CLINICAL_TRIALS_ERROR", 
                "Failed to search clinical trials: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to NIH clinical trials database: " + e.getMessage()));
        }
    }

    /**
     * Submit prescription to external pharmacy benefit manager
     * EXIT POINT: This endpoint transmits prescription data to external PBM system
     * POST /api/external/prescription/submit
     */
    @PostMapping("/prescription/submit")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE_PRACTITIONER') or hasRole('PHYSICIAN_ASSISTANT')")
    public ResponseEntity<?> submitPrescription(@RequestBody PrescriptionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalDataTransmission(auth.getName(), "PRESCRIPTION_SUBMISSION", 
                "Submitting prescription to external PBM for patient: " + request.getPatientId());
            
            Map<String, Object> prescriptionResult = externalHealthcareService.submitPrescription(
                request.getPatientId(), request.getPrescription());
            
            auditService.logExternalDataTransmission(auth.getName(), "PRESCRIPTION_SUCCESS", 
                "Successfully submitted prescription for patient: " + request.getPatientId());
            
            return ResponseEntity.ok(prescriptionResult);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "PRESCRIPTION_ERROR", 
                "Failed to submit prescription: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to pharmacy benefit manager: " + e.getMessage()));
        }
    }

    /**
     * Lookup ICD codes from WHO API
     * EXIT POINT: This endpoint connects to World Health Organization's external API
     * GET /api/external/icd/lookup/{searchTerm}
     */
    @GetMapping("/icd/lookup/{searchTerm}")
    @PreAuthorize("hasRole('PHYSICIAN') or hasRole('NURSE_PRACTITIONER') or hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<?> lookupIcdCode(@PathVariable String searchTerm) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalAccess(auth.getName(), "WHO_ICD_LOOKUP", 
                "Looking up ICD code from WHO API for term: " + searchTerm);
            
            Map<String, Object> icdResult = externalHealthcareService.lookupIcdCode(searchTerm);
            
            auditService.logExternalAccess(auth.getName(), "WHO_ICD_SUCCESS", 
                "Successfully retrieved ICD code information for term: " + searchTerm);
            
            return ResponseEntity.ok(icdResult);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "WHO_ICD_ERROR", 
                "Failed to lookup ICD code: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to WHO ICD API: " + e.getMessage()));
        }
    }

    /**
     * Fetch provider quality metrics from CMS
     * EXIT POINT: This endpoint connects to Centers for Medicare & Medicaid Services API
     * GET /api/external/cms/provider-quality/{npiNumber}
     */
    @GetMapping("/cms/provider-quality/{npiNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICAL_ADMIN') or hasRole('QUALITY_ASSURANCE')")
    public ResponseEntity<?> fetchProviderQualityMetrics(@PathVariable String npiNumber) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            auditService.logExternalAccess(auth.getName(), "CMS_QUALITY_METRICS", 
                "Fetching provider quality metrics from CMS for NPI: " + npiNumber);
            
            Map<String, Object> qualityMetrics = externalHealthcareService.fetchProviderQualityMetrics(npiNumber);
            
            auditService.logExternalAccess(auth.getName(), "CMS_QUALITY_SUCCESS", 
                "Successfully retrieved quality metrics for NPI: " + npiNumber);
            
            return ResponseEntity.ok(qualityMetrics);
            
        } catch (Exception e) {
            auditService.logExternalError(auth.getName(), "CMS_QUALITY_ERROR", 
                "Failed to fetch provider quality metrics: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new MessageResponse("Failed to connect to CMS API: " + e.getMessage()));
        }
    }

    /**
     * Test external connectivity (Admin only - for troubleshooting)
     * EXIT POINT: This endpoint tests connections to multiple external systems
     * GET /api/external/connectivity-test
     */
    @GetMapping("/connectivity-test")
    @PreAuthorize("hasRole('ADMIN') or hasRole('IT_ADMIN') or hasRole('SYSTEM_ANALYST')")
    public ResponseEntity<?> testExternalConnectivity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        auditService.logExternalAccess(auth.getName(), "CONNECTIVITY_TEST", 
            "Testing external system connectivity");
        
        Map<String, String> connectivityResults = Map.of(
            "epic_fhir", testConnection("https://fhir.epic.com/interconnect-fhir-oauth/metadata"),
            "fda_api", testConnection("https://api.fda.gov/drug/label.json?limit=1"),
            "nih_trials", testConnection("https://clinicaltrials.gov/api/info"),
            "who_icd", testConnection("https://icd-api.who.int/icd/release/11/2019-04"),
            "cms_api", testConnection("https://data.cms.gov/provider-data/api/1/metastore"),
            "insurance_verification", testConnection("https://api.insuranceverify.com/v2/health"),
            "lab_system", testConnection("https://api.labcorp.com/v1/health"),
            "pacs_system", testConnection("https://pacs.radiology-partner.com/api/v2/health"),
            "pbm_system", testConnection("https://api.pharmacybenefitmanager.com/v1/health")
        );
        
        auditService.logExternalAccess(auth.getName(), "CONNECTIVITY_TEST_COMPLETE", 
            "External connectivity test completed");
        
        return ResponseEntity.ok(connectivityResults);
    }

    // Helper method to test individual connections
    private String testConnection(String url) {
        try {
            ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful() ? "CONNECTED" : "FAILED";
        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }

    // Request DTOs

    public static class InsuranceVerificationRequest {
        private String memberId;
        private String insuranceProvider;
        private String serviceDate;

        public String getMemberId() { return memberId; }
        public void setMemberId(String memberId) { this.memberId = memberId; }

        public String getInsuranceProvider() { return insuranceProvider; }
        public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

        public String getServiceDate() { return serviceDate; }
        public void setServiceDate(String serviceDate) { this.serviceDate = serviceDate; }
    }

    public static class LabOrderRequest {
        private String patientId;
        private Map<String, Object> labOrder;

        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }

        public Map<String, Object> getLabOrder() { return labOrder; }
        public void setLabOrder(Map<String, Object> labOrder) { this.labOrder = labOrder; }
    }

    public static class ImagingUploadRequest {
        private String patientId;
        private String studyType;
        private byte[] imagingData;

        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }

        public String getStudyType() { return studyType; }
        public void setStudyType(String studyType) { this.studyType = studyType; }

        public byte[] getImagingData() { return imagingData; }
        public void setImagingData(byte[] imagingData) { this.imagingData = imagingData; }
    }

    public static class PrescriptionRequest {
        private String patientId;
        private Map<String, Object> prescription;

        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }

        public Map<String, Object> getPrescription() { return prescription; }
        public void setPrescription(Map<String, Object> prescription) { this.prescription = prescription; }
    }
}