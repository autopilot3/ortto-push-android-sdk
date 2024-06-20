package com.ortto.messaging;

import static org.junit.Assert.assertNull;

import android.util.Log;

import com.ortto.messaging.data.IdentityRepository;
import com.ortto.messaging.identity.UserID;

import org.junit.Test;

public class IdentifyTest extends OrttoTest {
    private IdentityRepository identityRepository;

    @Override
    public void setUp() {
        super.setUp();
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

        ortto.clearIdentity(response -> {
            Log.d("test", "Response: " + response.sessionId);
            assertNull(identityRepository.identifier);
        });
        assertNull(ortto.identity);
    }
}
