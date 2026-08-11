# Medication schedules use the order visit

Patients can have more than one active visit. Medication scheduling must not
select `getActiveVisitsByPatient(patient).get(0)`, because the returned order is
not a clinical invariant and can attach slots to a different admission.

For prescribed medication, the owning visit is the visit of the drug order's
encounter. New schedules therefore use `order.encounter.visit`, with the former
active-visit lookup retained only for order types that have no encounter visit.

Time-frame reads accept either the schedule visit or the order encounter visit.
This compatibility condition makes schedules created before this correction
visible in the proper drug chart without directly updating clinical database
rows. Editing one of those schedules recreates its slots under the correct
visit through the normal audited application flow.

Verification must cover:

- a patient with multiple active visits;
- a drug order whose encounter visit is not the first active visit;
- current-shift reads with and without administered medication; and
- existing schedules whose stored visit differs from the order encounter visit.
