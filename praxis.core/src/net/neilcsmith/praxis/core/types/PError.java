/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.core.types;

import java.util.Objects;
import java.util.Optional;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class PError extends Argument {

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

    public Class<? extends Exception> getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Exception getWrappedException() {
        return ex;
    }

    @Override
    public String toString() {
        String str = string;
        if (str == null) {
            StringBuilder sb = new StringBuilder(ERROR_PREFIX);
            sb.append(' ')
                    .append(type.getName())
                    .append(' ')
                    .append(SyntaxUtils.escape(message));
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

    static PError valueOf(String str) throws ArgumentFormatException {
        try {
            PArray arr = PArray.valueOf(str);
            if (arr.getSize() != 3 || !ERROR_PREFIX.equals(arr.get(0).toString())) {
                throw new ArgumentFormatException();
            }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<? extends Exception> type
                    = (Class<? extends Exception>) cl.loadClass(arr.get(1).toString());
            String msg = arr.get(2).toString();
            return new PError(type, msg, null, str);
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
    }

    public static PError coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PError) {
            return (PError) arg;
        } else if (arg instanceof PReference) {
            Object o = ((PReference) arg).getReference();
            if (o instanceof Exception) {
                return create((Exception) o);
            } else {
                throw new ArgumentFormatException();
            }
        }
        return valueOf(arg.toString());
    }
    
    public static Optional<PError> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }

    public static PError create(Exception ex) {
        Class<? extends Exception> type = ex.getClass();
        String msg = ex.getMessage();
        if (msg == null) {
            msg = "";
        }
        return new PError(type, msg, ex, null);
    }

    public static PError create(Exception ex, String msg) {
        return new PError(ex.getClass(),
                Objects.requireNonNull(msg),
                ex,
                null);
    }

    public static PError create(String msg) {
        return create(Exception.class, msg);
    }
    
    public static PError create(Class<? extends Exception> type, String msg) {
        return new PError(Objects.requireNonNull(type),
                Objects.requireNonNull(msg),
                null,
                null);
    }

}
