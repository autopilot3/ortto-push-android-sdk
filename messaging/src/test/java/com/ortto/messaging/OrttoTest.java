package com.ortto.messaging;

import org.mockito.Mockito;
import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.app.Application;
import android.content.SharedPreferences;

@Config(sdk = 33)
abstract public class OrttoTest {
    protected Ortto ortto;

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
    }
}