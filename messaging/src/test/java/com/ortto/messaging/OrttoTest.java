package com.ortto.messaging;

import org.mockito.Mockito;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNull;

import android.app.Application;
import android.content.SharedPreferences;

import com.ortto.messaging.data.IdentityRepository;
import com.ortto.messaging.identity.UserID;

public class OrttoTest {
    private Ortto ortto;
    private IdentityRepository identityRepository;

    @Before
    public void setUp() {
        // Create a mock application context
        Application mockApplicationContext = Mockito.mock(Application.class);

        SharedPreferences mockSharedPreferences = Mockito.mock(SharedPreferences.class);
        SharedPreferences.Editor mockEditor = Mockito.mock(SharedPreferences.Editor.class);
        Mockito.when(mockApplicationContext.getApplicationContext()).thenReturn(mockApplicationContext);
        Mockito.when(mockApplicationContext.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(mockSharedPreferences);
        Mockito.when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        Mockito.when(mockEditor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(mockEditor);
        Mockito.when(mockEditor.clear()).thenReturn(mockEditor);
        Mockito.when(mockEditor.remove(Mockito.anyString())).thenReturn(mockEditor);

        OrttoConfig config = new OrttoConfig("abc123", "http://localhost", false);
        Ortto.instance().init(config, mockApplicationContext);
        ortto = Ortto.instance();
        identityRepository = new IdentityRepository(ortto.appContext);
        ortto.identityRepository = identityRepository;
    }

    @Test
    public void testClearData() {
        // Set some data
        UserID testUser = new UserID();
        testUser.contactId = "i-am-a-contact";
        identityRepository.setIdentifier(testUser);

        ortto.clearData();
        assertNull(identityRepository.identifier);

        ortto.clearIdentity();
        assertNull(ortto.identity);
    }
}