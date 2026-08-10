package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

    private String captureQuery() {
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(queryCaptor.capture());
        return queryCaptor.getValue();
    }

    private void assertSelectedEntitiesAreGrouped(String query) {
        assertTrue(query.contains("GROUP BY assignment, careTeam, v"));
        assertTrue(query.contains("LEFT JOIN org.openmrs.Encounter e on e.visit = v"));
    }
}
