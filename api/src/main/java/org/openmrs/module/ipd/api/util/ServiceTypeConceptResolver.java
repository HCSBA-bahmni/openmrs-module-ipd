package org.openmrs.module.ipd.api.util;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ipd.api.model.ServiceType;

import java.util.List;
import java.util.Locale;

/**
 * Resolves IPD technical concepts in the locale in which module metadata creates them.
 * These names are identifiers, not translated clinical labels, and therefore must not
 * depend on the locale of the authenticated user's request.
 */
public class ServiceTypeConceptResolver {

    private static final Locale METADATA_LOCALE = Locale.ENGLISH;

    private ConceptService conceptService;

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public Concept resolve(ServiceType serviceType) {
        List<Concept> concepts = conceptService.getConceptsByName(serviceType.conceptName(), METADATA_LOCALE, false);
        int matchCount = concepts == null ? 0 : concepts.size();
        if (matchCount != 1) {
            throw new IllegalStateException("Expected exactly one active IPD service type concept named '"
                    + serviceType.conceptName() + "' in locale 'en', found " + matchCount);
        }
        return concepts.get(0);
    }
}
