# Ward patients use the bed assignment visit

The ward patient queries must use the visit referenced by the active
`BedPatientAssignment.encounter`. Joining every active visit for the patient can
duplicate a bed occupant and associate treatments or care teams from an
unrelated encounter.

An active bed assignment is the ward admission source of truth. The query must
therefore:

- include each active assignment once, even when its visit has been stopped;
- count treatments from encounters belonging to the assignment visit;
- obtain the care team from that same visit; and
- calculate ward totals from distinct active assignments.

This keeps Care View consistent with the operational bed map and with the visit
already exposed by `AdmittedPatientResponse`.
