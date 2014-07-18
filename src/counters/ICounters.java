package counters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

interface ICounters {

    /**read the stream line by line, input will be in the format "key:value\n",
    * close the stream when finished or on exception. return the number of bytes read.
    */
    public int collect(InputStream iStream) throws IOException;


    /**   - return a copy of of the value map*/
    public Map<String,Integer> collect();

    /** return a copy of the state data and reset the map data to empty */
    public Map<String,Integer> reset();

}
