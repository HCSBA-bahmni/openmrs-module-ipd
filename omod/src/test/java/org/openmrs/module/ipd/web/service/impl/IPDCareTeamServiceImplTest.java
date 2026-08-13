package org.openmrs.module.ipd.web.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
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

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IPDCareTeamServiceImplTest {

    private CareTeamService careTeamService;
    private CareTeamFactory careTeamFactory;
    private PatientService patientService;
    private BedManagementService bedManagementService;
    private IPDCareTeamServiceImpl service;

    @Before
    public void setUp() {
        careTeamService = mock(CareTeamService.class);
        careTeamFactory = mock(CareTeamFactory.class);
        patientService = mock(PatientService.class);
        bedManagementService = mock(BedManagementService.class);
        service = new IPDCareTeamServiceImpl(careTeamService, careTeamFactory, patientService, bedManagementService);
    }

    @Test
    public void shouldUseTheCurrentBedVisitRequestedByTheMigratedClient() {
        Patient patient = patient(11, "patient");
        Visit bedVisit = visit(patient, "bed-visit");
        CareTeam careTeam = new CareTeam();
        CareTeamRequest request = new CareTeamRequest("patient", "bed-visit", Collections.emptyList());
        stubCurrentAssignment(patient, bedVisit);
        when(patientService.getPatientByUuid("patient")).thenReturn(patient);
        when(careTeamFactory.createCareTeamFromRequest(request, patient, bedVisit)).thenReturn(careTeam);
        when(careTeamService.saveCareTeam(careTeam)).thenReturn(careTeam);

        assertSame(careTeam, service.saveCareTeamParticipants(request));

        verify(careTeamFactory).createCareTeamFromRequest(request, patient, bedVisit);
    }

    @Test
    public void shouldResolveTheCurrentBedVisitForLegacyRequestsWithoutVisitUuid() {
        Patient patient = patient(12, "patient");
        Visit bedVisit = visit(patient, "bed-visit");
        CareTeam careTeam = new CareTeam();
        CareTeamRequest request = new CareTeamRequest("patient", null, Collections.emptyList());
        stubCurrentAssignment(patient, bedVisit);
        when(patientService.getPatientByUuid("patient")).thenReturn(patient);
        when(careTeamFactory.createCareTeamFromRequest(request, patient, bedVisit)).thenReturn(careTeam);
        when(careTeamService.saveCareTeam(careTeam)).thenReturn(careTeam);

        assertSame(careTeam, service.saveCareTeamParticipants(request));
    }

    @Test(expected = APIException.class)
    public void shouldRejectARequestedVisitThatDoesNotMatchTheCurrentBedVisit() {
        Patient patient = patient(13, "patient");
        Visit bedVisit = visit(patient, "bed-visit");
        CareTeamRequest request = new CareTeamRequest("patient", "other-visit", Collections.emptyList());
        stubCurrentAssignment(patient, bedVisit);
        when(patientService.getPatientByUuid("patient")).thenReturn(patient);

        service.saveCareTeamParticipants(request);
    }

    @Test(expected = APIException.class)
    public void shouldRejectACurrentBedAssignmentLinkedToAClosedVisit() {
        Patient patient = patient(14, "patient");
        Visit bedVisit = visit(patient, "bed-visit");
        bedVisit.setStopDatetime(new Date());
        CareTeamRequest request = new CareTeamRequest("patient", "bed-visit", Collections.emptyList());
        stubCurrentAssignment(patient, bedVisit);
        when(patientService.getPatientByUuid("patient")).thenReturn(patient);

        service.saveCareTeamParticipants(request);
    }

    @Test(expected = APIException.class)
    public void shouldRejectAPatientWithoutACurrentBedAssignment() {
        Patient patient = patient(15, "patient");
        CareTeamRequest request = new CareTeamRequest("patient", null, Collections.emptyList());
        when(patientService.getPatientByUuid("patient")).thenReturn(patient);
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(null);

        service.saveCareTeamParticipants(request);
    }

    private void stubCurrentAssignment(Patient patient, Visit visit) {
        Encounter encounter = new Encounter();
        encounter.setVisit(visit);
        BedPatientAssignment assignment = new BedPatientAssignment();
        assignment.setPatient(patient);
        assignment.setEncounter(encounter);
        BedDetails bedDetails = new BedDetails();
        bedDetails.setCurrentAssignments(Collections.singletonList(assignment));
        when(bedManagementService.getBedAssignmentDetailsByPatient(patient)).thenReturn(bedDetails);
    }

    private Patient patient(int id, String uuid) {
        Patient patient = new Patient(id);
        patient.setUuid(uuid);
        return patient;
    }

    private Visit visit(Patient patient, String uuid) {
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setUuid(uuid);
        return visit;
    }
}
