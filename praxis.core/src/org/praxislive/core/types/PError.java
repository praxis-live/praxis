/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.core.types;

import java.util.Objects;
import java.util.Optional;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class PError extends Value {

    private final static String ERROR_PREFIX = "ERR:";

    private final Class<? extends Exception> type;
    private final String message;
    private final Exception ex;

    private volatile String string;

    private PError(Class<? extends Exception> type,
            String message, Exception ex, String str) {
        this.type = type;
        this.message = message;
        this.ex = ex;
        this.string = str;
    }
    
    public Class<? extends Exception> exceptionType() {
        return type;
    }

    @Deprecated
    public Class<? extends Exception> getType() {
        return type;
    }

    public String message() {
        return message;
    }
    
    @Deprecated
    public String getMessage() {
        return message;
    }

    @Deprecated
    public Exception getWrappedException() {
        return ex;
    }
    
    public Optional<Exception> exception() {
        return Optional.ofNullable(ex);
    }

    @Override
    public String toString() {
        String str = string;
        if (str == null) {
            StringBuilder sb = new StringBuilder(ERROR_PREFIX);
            sb.append(' ')
                    .append(type.getName())
                    .append(' ')
                    .append(Utils.escape(message));
            str = sb.toString();
            string = str;
        }
        return str;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.type);
        hash = 67 * hash + Objects.hashCode(this.message);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PError) {
            final PError other = (PError) obj;
            return Objects.equals(this.type, other.type)
                    && Objects.equals(this.message, other.message);
        }
        return false;
    }

    public static PError parse(String str) throws ValueFormatException {
        try {
            PArray arr = PArray.parse(str);
            if (arr.size() != 3 || !ERROR_PREFIX.equals(arr.get(0).toString())) {
                throw new ValueFormatException();
            }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<? extends Exception> type
                    = (Class<? extends Exception>) cl.loadClass(arr.get(1).toString());
            String msg = arr.get(2).toString();
            return new PError(type, msg, null, str);
        } catch (Exception ex) {
            throw new ValueFormatException(ex);
        }
    }
    
    
    static PError valueOf(String str) throws ValueFormatException {
        try {
            PArray arr = PArray.parse(str);
            if (arr.size() != 3 || !ERROR_PREFIX.equals(arr.get(0).toString())) {
                throw new ValueFormatException();
            }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<? extends Exception> type
                    = (Class<? extends Exception>) cl.loadClass(arr.get(1).toString());
            String msg = arr.get(2).toString();
            return new PError(type, msg, null, str);
        } catch (Exception ex) {
            throw new ValueFormatException(ex);
        }
    }

    @Deprecated
    public static PError coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PError) {
            return (PError) arg;
        } else if (arg instanceof PReference) {
            Object o = ((PReference) arg).getReference();
            if (o instanceof Exception) {
                return PError.of((Exception) o);
            } else {
                throw new ValueFormatException();
            }
        }
        return parse(arg.toString());
    }
    
    public static Optional<PError> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }

    public static PError of(Exception ex) {
        Class<? extends Exception> type = ex.getClass();
        String msg = ex.getMessage();
        if (msg == null) {
            msg = "";
        }
        return new PError(type, msg, ex, null);
    }
    
    @Deprecated
    public static PError create(Exception ex) {
        return PError.of(ex);
    }

    public static PError of(Exception ex, String msg) {
        return new PError(ex.getClass(),
                Objects.requireNonNull(msg),
                ex,
                null);
    }
    
    @Deprecated
    public static PError create(Exception ex, String msg) {
        return PError.of(ex, msg);
    }

    public static PError of(String msg) {
        return of(Exception.class, msg);
    }
    
    @Deprecated
    public static PError create(String msg) {
        return of(Exception.class, msg);
    }

    public static PError of(Class<? extends Exception> type, String msg) {
        return new PError(Objects.requireNonNull(type),
                Objects.requireNonNull(msg),
                null,
                null);
    }
    
    @Deprecated
    public static PError create(Class<? extends Exception> type, String msg) {
        return of(type, msg);
    }

}
