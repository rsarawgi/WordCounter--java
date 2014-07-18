package counters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Counters implements ICounters {

    private static final String DELIMITER = ":";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private Map<String, Integer> aggregatedDataMap;

    Counters() {
        // Note the use of synchronized map here , we could be using ConcurrentHashMap here
        // but it will fail the following scenario. Consider 3 threads running in parallel
        // collect(stream), reset(), collect()
        // Depending on which of the first two finish first, the third thread can have different values
        // since ConcurrentHashMap only synchronizes parts of the map
        // If we are in a single thread world, we can use a normal Hashmap
        // If we are in a multiple thread world where clear() is only called at the end, we can optimize using a ConcurrentHashMap
        aggregatedDataMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    }

    @Override
    public int collect(InputStream iStream) throws IOException{

        String inputLine;
        int totalBytes = 0;
        BufferedReader reader = getReaderUsingInputStream(iStream);
        try {
            while ((inputLine = reader.readLine()) != null) {
                totalBytes = totalBytes + inputLine.getBytes(CHARACTER_ENCODING).length;
                String[] words = inputLine.split(DELIMITER);
                if (aggregatedDataMap.containsKey(words[0])) {
                    Integer value = aggregatedDataMap.get(words[0]);
                    aggregatedDataMap.put(words[0], value + new Integer(words[1]));
                } else
                    aggregatedDataMap.put(words[0], new Integer(words[1]));
            }
        } catch (IOException e) {
            System.out.println("Error reading from the input stream");
            throw e;
        } finally {
            //this functionality can be achieved using IOUtils.closeQuietly()
            closeReaderAndStream(iStream, reader);
        }
        return totalBytes;
    }

    @Override
    public Map<String, Integer> collect() {
        return aggregatedDataMap;
    }

    @Override
    public Map<String, Integer> reset() {
        Map<String, Integer> clonedMap = new HashMap<String, Integer>(aggregatedDataMap);
        aggregatedDataMap.clear();
        return clonedMap;
    }

    protected BufferedReader getReaderUsingInputStream(InputStream iStream) {
        return new BufferedReader(new InputStreamReader(iStream));
    }

    protected void closeReaderAndStream(InputStream iStream, BufferedReader reader) {
        try {
            reader.close(); //reader != null will always be true in our use case

            if(iStream != null) iStream.close();

        } catch(IOException ex) {
            System.out.println("Input stream is not closed");
        }
    }
}
