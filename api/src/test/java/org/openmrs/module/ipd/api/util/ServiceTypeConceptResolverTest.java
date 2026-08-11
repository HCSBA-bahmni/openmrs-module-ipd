package org.openmrs.module.ipd.api.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ipd.api.model.ServiceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class ServiceTypeConceptResolverTest {

    private ServiceTypeConceptResolver resolver;

    @Mock
    private ConceptService conceptService;

    @Before
    public void setUp() {
        resolver = new ServiceTypeConceptResolver();
        resolver.setConceptService(conceptService);
    }

    @Test
    public void shouldResolveTechnicalConceptUsingEnglishMetadataLocale() {
        Concept medicationRequest = new Concept(149);
        Mockito.when(conceptService.getConceptsByName("MedicationRequest", Locale.ENGLISH, false))
                .thenReturn(Collections.singletonList(medicationRequest));

        Concept resolved = resolver.resolve(ServiceType.MEDICATION_REQUEST);

        assertSame(medicationRequest, resolved);
        Mockito.verify(conceptService).getConceptsByName("MedicationRequest", Locale.ENGLISH, false);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenTechnicalConceptIsMissing() {
        Mockito.when(conceptService.getConceptsByName("MedicationRequest", Locale.ENGLISH, false))
                .thenReturn(Collections.emptyList());

        resolver.resolve(ServiceType.MEDICATION_REQUEST);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenTechnicalConceptIsAmbiguous() {
        Mockito.when(conceptService.getConceptsByName("MedicationRequest", Locale.ENGLISH, false))
                .thenReturn(Arrays.asList(new Concept(149), new Concept(150)));

        resolver.resolve(ServiceType.MEDICATION_REQUEST);
    }
}
