package com.ortto.messaging;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.junit.runner.RunWith;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;

import com.ortto.messaging.data.LinkUtm;
import com.ortto.messaging.retrofit.TrackingClickedResponse;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LinkTrackingTest extends OrttoTest {
    @Mock
    private OrttoClientService mockClient;
    @Mock
    private Call<TrackingClickedResponse> mockCall;

    @Before
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTrackLinkClick() throws Exception {
        String encodedUrl = "ortto-sdk://example.com/pathname?tracking_url=aHR0cHM6Ly90cmFja2luZy5leGFtcGxlLmNvbS8_cD1leGFtcGxlJnBsdD1pb3MmZT0xMTYxYzUzOThhYzMyY2JiNjI3ZmY1NzU3Y2U0ZWQyNzdjNjkwNTkwZWJhNzBhN2Q2Y2Q5ZDRhMWZkMTc1ZjJhJnNpZD02NjczYTRhZWZlNjllM2E3YWJkM2I4MTU";
        String decodedUrl = "https://tracking.example.com/?p=example&plt=ios&e=1161c5398ac32cbb627ff5757ce4ed277c690590eba70a7d6cd9d4a1fd175f2a&sid=6673a4aefe69e3a7abd3b815";

        ortto.client = mockClient;

        when(mockClient.trackLinkClick(anyString(), anyMap())).thenReturn(mockCall);

        // To capture the listener passed to the enqueue method
        ArgumentCaptor<Callback<TrackingClickedResponse>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        // To verify the completion
        Ortto.OnTrackedListener listener = mock(Ortto.OnTrackedListener.class);
        ortto.trackLinkClick(encodedUrl, listener);

        // Capture the callback passed to enqueue
        verify(mockCall).enqueue(callbackCaptor.capture());
        Callback<TrackingClickedResponse> capturedCallback = callbackCaptor.getValue();

        // Mock the response
        Response<TrackingClickedResponse> mockResponse = Response.success(new TrackingClickedResponse());
        capturedCallback.onResponse(mockCall, mockResponse);

        // Verify the listener is called
        verify(listener, times(1)).onComplete(any(LinkUtm.class));

        // Verify the URL decoding
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockClient).trackLinkClick(urlCaptor.capture(), anyMap());
        assertEquals(decodedUrl, urlCaptor.getValue());
    }
}