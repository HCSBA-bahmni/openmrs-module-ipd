package org.openmrs.module.ipd.web.service.impl;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.entity.BedPatientAssignment;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.service.CareTeamService;
import org.openmrs.module.ipd.web.contract.CareTeamRequest;
import org.openmrs.module.ipd.web.factory.CareTeamFactory;
import org.openmrs.module.ipd.web.service.IPDCareTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class IPDCareTeamServiceImpl implements IPDCareTeamService {

    private final CareTeamService careTeamService;
    private final CareTeamFactory careTeamFactory;
    private final PatientService patientService;
    private final BedManagementService bedManagementService;

    @Autowired
    public IPDCareTeamServiceImpl(CareTeamService careTeamService, CareTeamFactory careTeamFactory,
                                  PatientService patientService,
                                  BedManagementService bedManagementService) {
        this.careTeamService = careTeamService;
        this.careTeamFactory = careTeamFactory;
        this.patientService = patientService;
        this.bedManagementService = bedManagementService;
    }

    @Override
    public CareTeam saveCareTeamParticipants(CareTeamRequest careTeamRequest) {
        Patient patient = patientService.getPatientByUuid(careTeamRequest.getPatientUuid());
        if (patient == null) {
            throw new APIException("No patient found for the requested care team");
        }
        Visit visit = getCareTeamVisit(careTeamRequest, patient);
        CareTeam careTeam = careTeamService.getCareTeamByVisit(visit);
        if (careTeam==null || careTeam.getCareTeamId()==null){
            careTeam = careTeamFactory.createCareTeamFromRequest(careTeamRequest,patient,visit);
        }
        else {
            careTeam = careTeamFactory.updateCareTeamFromRequest(careTeamRequest, careTeam);
        }
        return careTeamService.saveCareTeam(careTeam);
    }

    private Visit getCareTeamVisit(CareTeamRequest careTeamRequest, Patient patient) {
        BedDetails bedDetails = bedManagementService.getBedAssignmentDetailsByPatient(patient);
        if (bedDetails == null || bedDetails.getCurrentAssignments() == null || bedDetails.getCurrentAssignments().size() != 1) {
            throw new APIException("The patient must have exactly one current bed assignment");
        }

        BedPatientAssignment assignment = bedDetails.getCurrentAssignments().get(0);
        Visit bedVisit = assignment.getEncounter() == null ? null : assignment.getEncounter().getVisit();
        if (bedVisit == null) {
            throw new APIException("The current bed assignment is not linked to a visit");
        }
        if (bedVisit.getPatient() == null || !Objects.equals(bedVisit.getPatient().getPatientId(), patient.getPatientId())) {
            throw new APIException("The current bed visit does not belong to the patient");
        }
        if (bedVisit.getStopDatetime() != null) {
            throw new APIException("The current bed assignment is linked to a closed visit");
        }

        String requestedVisitUuid = careTeamRequest.getVisitUuid();
        if (requestedVisitUuid != null && !requestedVisitUuid.trim().isEmpty()
                && !Objects.equals(requestedVisitUuid, bedVisit.getUuid())) {
            throw new APIException("The requested visit does not match the current bed visit");
        }

        return bedVisit;
    }
}
