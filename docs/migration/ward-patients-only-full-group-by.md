# Ward patients and `ONLY_FULL_GROUP_BY`

The HCSBA OpenMRS database enables MySQL's `ONLY_FULL_GROUP_BY` mode. Queries
that build an `AdmittedPatient` projection must therefore group every
non-aggregated entity selected alongside the care-task count.

Both ward patient entry points use the same grouping rule:

- `getAdmittedPatients`
- `searchAdmittedPatients`

The projection selects the bed assignment, the care-task aggregate, the care
team participant and the visit. Its `GROUP BY` clause must consequently include
`assignment`, `careTeam` and `v`. Grouping only by patient and visit causes the
endpoint to catch a SQL grammar exception and return an empty patient list even
when the ward contains admitted patients.

Regression tests capture the generated HQL for the list and search paths so the
three selected entities cannot be omitted silently.

The encounter association is optional in these queries. A valid active bed
assignment and visit must remain visible before the first clinical encounter is
created, so both paths use a `LEFT JOIN` for `Encounter`. An inner join makes the
ward summary and patient list disagree for newly admitted patients.
