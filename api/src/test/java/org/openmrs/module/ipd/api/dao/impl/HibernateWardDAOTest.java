package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HibernateWardDAOTest {

    private HibernateWardDAO wardDAO;
    private Session session;

    @Before
    public void setUp() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        session = mock(Session.class);
        Query query = mock(Query.class);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        wardDAO = new HibernateWardDAO();
        wardDAO.setSessionFactory(sessionFactory);
    }

    @Test
    public void shouldGroupSelectedEntitiesWhenGettingAdmittedPatients() {
        wardDAO.getAdmittedPatients(null, null, null, null);

        assertSelectedEntitiesAreGrouped(captureQuery());
    }

    @Test
    public void shouldGroupSelectedEntitiesWhenSearchingAdmittedPatients() {
        wardDAO.searchAdmittedPatients(null, Arrays.asList("patientName"), "patient", null);

        assertSelectedEntitiesAreGrouped(captureQuery());
    }

    @Test
    public void shouldExcludeClosedVisitsFromWardSummaries() {
        wardDAO.getWardPatientSummary(mock(Location.class), mock(Provider.class), new Date());

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session, times(2)).createQuery(queryCaptor.capture());
        List<String> queries = queryCaptor.getAllValues();
        assertTrue(queries.get(0).contains("assignment.encounter.visit.stopDatetime IS NULL"));
        assertTrue(queries.get(1).contains("assignment.encounter.visit.stopDatetime IS NULL"));
    }

    private String captureQuery() {
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(queryCaptor.capture());
        return queryCaptor.getValue();
    }

    private void assertSelectedEntitiesAreGrouped(String query) {
        assertTrue(query.contains("GROUP BY assignment, careTeam"));
        assertTrue(query.contains("e.visit = assignment.encounter.visit"));
        assertTrue(query.contains("careTeam.visit = assignment.encounter.visit"));
        assertTrue(query.contains("assignment.encounter.visit.stopDatetime is null"));
    }
}
