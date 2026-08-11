# IPD service type concepts use a metadata locale

IPD service types such as `MedicationRequest`, `AsNeededMedicationRequest`,
`EmergencyMedicationRequest`, and `AsNeededPlaceholder` are technical metadata
identifiers. The module metadata creates their concept names in English, so they
must not be resolved using the authenticated user's locale.

All IPD code must resolve these concepts through `ServiceTypeConceptResolver`.
The resolver performs an exact lookup in `Locale.ENGLISH`, excludes retired
concepts, and fails when the result is missing or ambiguous. Do not reintroduce
`ConceptService.getConceptByName(ServiceType.conceptName())`, because it depends
on `Context.getLocale()` and fails for Spanish sessions.

When adding a new `ServiceType`:

1. Create exactly one active concept name in the metadata locale.
2. Resolve it through `ServiceTypeConceptResolver` in factories, services, and
   queries.
3. Add resolver tests for the expected name and failure cases.
4. Verify medication scheduling with a non-English user locale.
