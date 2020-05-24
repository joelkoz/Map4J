package org.map4j.utils.mbtiles;

import java.util.Objects;

/**
 * Exceptions generated from the reading and writing of MBTiles files.
 * 
 * @author Joel Kozikowski
 */
public class MBTilesException extends RuntimeException {

    public MBTilesException(String msg) {
        super(msg);
    }
    
    public MBTilesException(Throwable e) {
        super(e);
    }

    public MBTilesException(String msg, Throwable e) {
        super(msg, e);
    }
    
    @Override
    public String getMessage() {
        Throwable root = this.getRootCause();
        if (!this.equals(root)) {
            return super.getMessage() + ": " + root.getMessage();
        }
        else {
            return super.getMessage();
        }
    }
    
    
    public Throwable getRootCause() {
        return findRootCause(this);
    }
    
    
    public static Throwable findRootCause(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }    
}
