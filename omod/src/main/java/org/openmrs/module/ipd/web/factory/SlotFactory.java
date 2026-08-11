package org.openmrs.module.ipd.web.factory;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.module.ipd.api.util.ServiceTypeConceptResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlotFactory {

    private final BedManagementService bedManagementService;
    private final ServiceTypeConceptResolver serviceTypeConceptResolver;
    private final PatientService patientService;
    private final SlotService slotService;

    @Autowired
    public SlotFactory(BedManagementService bedManagementService, ServiceTypeConceptResolver serviceTypeConceptResolver, PatientService patientService,SlotService slotService) {
        this.bedManagementService = bedManagementService;
        this.serviceTypeConceptResolver = serviceTypeConceptResolver;
        this.patientService = patientService;
        this.slotService = slotService;
    }

    public List<Slot> createSlotsForMedicationFrom(Schedule savedSchedule, List<LocalDateTime> slotsStartTime,
                                                   Order drugOrder, MedicationAdministration medicationAdministration,
                                                   Slot.SlotStatus status, ServiceType serviceType, String comments) {

        return slotsStartTime.stream().map(slotStartTime -> {
            Slot slot = new Slot();

            String patientUuid = savedSchedule.getSubject().getTargetUuid();
            Patient patient = patientService.getPatientByUuid(patientUuid);
            BedDetails bedAssignmentDetailsByPatient = bedManagementService.getBedAssignmentDetailsByPatient(patient);
            if(bedAssignmentDetailsByPatient != null){
                slot.setLocation(bedAssignmentDetailsByPatient.getPhysicalLocation());
            }

            Concept medicationRequestServiceType = serviceTypeConceptResolver.resolve(serviceType);
            slot.setServiceType(medicationRequestServiceType);

            slot.setOrder(drugOrder);
            slot.setSchedule(savedSchedule);
            slot.setStartDateTime(slotStartTime);
            slot.setStatus(status);
            slot.setMedicationAdministration(medicationAdministration);
            slot.setNotes(comments);
            return slot;
        }).collect(Collectors.toList());
    }

    public Slot getSlotFromUUID(String uuid){
       return slotService.getSlotByUUID(uuid);
    }

    public Slot createAsNeededPlaceholderSlot(Schedule savedSchedule, Order drugOrder, String comments) {
        Slot slot = new Slot();

        String patientUuid = savedSchedule.getSubject().getTargetUuid();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        BedDetails bedAssignmentDetailsByPatient = bedManagementService.getBedAssignmentDetailsByPatient(patient);
        if(bedAssignmentDetailsByPatient != null){
            slot.setLocation(bedAssignmentDetailsByPatient.getPhysicalLocation());
        }

        Concept medicationRequestServiceType = serviceTypeConceptResolver.resolve(ServiceType.AS_NEEDED_PLACEHOLDER);
        slot.setServiceType(medicationRequestServiceType);

        slot.setOrder(drugOrder);
        slot.setSchedule(savedSchedule);
        slot.setStartDateTime(DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate()));
        slot.setEndDateTime(DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate()));
        slot.setStatus(Slot.SlotStatus.SCHEDULED);
        slot.setNotes(comments);
        return slot;
    }
}
