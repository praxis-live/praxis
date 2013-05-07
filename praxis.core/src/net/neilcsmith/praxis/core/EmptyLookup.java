
package net.neilcsmith.praxis.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Neil C Smith
 */
class EmptyLookup implements Lookup {
    private static final EmptyResult EMPTY_RESULT = new EmptyResult<Object>();

    EmptyLookup() {
    }

    public <T> T get(Class<T> type) {
        return null;
    }

    @SuppressWarnings(value = "unchecked")
    public <T> Result<T> getAll(Class<T> type) {
        return (Result<T>) EMPTY_RESULT;
    }

    private static class EmptyResult<T> implements Lookup.Result<T> {

        public Iterator<T> iterator() {
            List<T> list = Collections.emptyList();
            return list.iterator();
        }
    }
    
}
