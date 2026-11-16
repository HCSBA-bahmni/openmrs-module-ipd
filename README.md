# openmrs-module-ipd
Openmrs module for Bahmni IPD Care and Monitoring

OpenMRS Module Bahmni-events Backend
=================================
This repository handles Bahmni IPD Care and Monitoring.

## Packaging
```mvn clean package```

### Prerequisite
    JDK 1.8

## Deploy

Copy ```openmrs-module-ipd/omod/target/bahmni-ipd-1.0.0-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS

## QA compartido del HIS HCSBA

El paquete [his-qa-release](skills/his-qa-release/SKILL.md) incluye el flujo PRE/POST, referencias y plantillas para QA de desarrollos y entregas a `.225`. Consultar la [guía para contribuyentes](skills/his-qa-release/references/contributors.md) para instalar la skill completa y ejecutar el harness mantenido en `bahmni-nextjs-hcsba`. Conservar informes y datos de prueba en una carpeta privada fuera de Git.
