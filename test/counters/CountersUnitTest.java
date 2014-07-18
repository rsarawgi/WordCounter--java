package counters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CountersUnitTest {

    @Mock
    private BufferedReader mockReader;

    @Mock
    private InputStream mockInputStream;

    @Spy
    private Counters spyCounters;

    @Before
    public void setUp() throws IOException{
        doReturn(this.mockReader).when(this.spyCounters).getReaderUsingInputStream(any(InputStream.class));
    }

    private void mockInputStream() throws IOException{
        when(this.mockReader.readLine()).thenReturn("cat:1").thenReturn("dog:2").thenReturn("cat:3").thenReturn(null);
    }

    @Test
    public void testCollectWithInputStream() throws IOException{
        this.mockInputStream();

        assertEquals(15, this.spyCounters.collect(this.mockInputStream));
    }

    @Test
    public void testCollect() throws IOException{
        this.mockInputStream();
        this.spyCounters.collect(this.mockInputStream);

        Map<String, Integer> actual = this.spyCounters.collect();

        assertEquals(new Integer(4), actual.get("cat"));
        assertEquals(new Integer(2), actual.get("dog"));
        assertNull(actual.get("bird"));

        verify(this.spyCounters, times(1)).closeReaderAndStream(any(InputStream.class),any(BufferedReader.class));
    }

    @Test
    public void testReset() throws IOException{
        this.mockInputStream();
        this.spyCounters.collect(this.mockInputStream);

        Map<String, Integer> actual = this.spyCounters.reset();

        assertEquals(new Integer(4), actual.get("cat"));
        assertEquals(new Integer(2), actual.get("dog"));

        Map<String,Integer> dataMap = this.spyCounters.collect();

        assertTrue(dataMap.isEmpty());

        verify(this.spyCounters, times(1)).closeReaderAndStream(any(InputStream.class),any(BufferedReader.class));
    }

    @Test
    public void testCollectWithMultipleCalls() throws IOException{
        when(this.mockReader.readLine()).thenReturn("cat:1").thenReturn("dog:2").thenReturn("cat:3").thenReturn(null)
        .thenReturn("cat:1").thenReturn("dog:2").thenReturn("cat:3").thenReturn(null)
        .thenReturn("cat:1").thenReturn("dog:2").thenReturn("cat:3").thenReturn(null)
        .thenReturn("cat:1").thenReturn("dog:2").thenReturn("cat:3").thenReturn(null);

        this.spyCounters.collect(this.mockInputStream);
        this.spyCounters.collect(this.mockInputStream);
        this.spyCounters.collect(this.mockInputStream);
        this.spyCounters.collect(this.mockInputStream);

        Map<String, Integer> actual = this.spyCounters.collect();

        assertEquals(new Integer(16), actual.get("cat"));
        assertEquals(new Integer(8), actual.get("dog"));
        assertNull(actual.get("bird"));

        verify(this.spyCounters, times(4)).closeReaderAndStream(any(InputStream.class),any(BufferedReader.class));
    }

    @Test(expected = IOException.class)
    public void testCollectForException() throws IOException{
        when(this.mockReader.readLine()).thenThrow(new IOException("IOException is thrown"));
        Integer inputStreamBytes = this.spyCounters.collect(mockInputStream);
        assertEquals(new Integer(0), inputStreamBytes);

        verify(this.spyCounters, times(1)).closeReaderAndStream(any(InputStream.class),any(BufferedReader.class));
    }

}
