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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
//import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class PBytes extends Argument {
    
    public final static PBytes EMPTY = new PBytes(new byte[0], "");
    
    private final byte[] bytes;

    private String str;

    private PBytes(byte[] bytes, String str) {
        this.bytes = bytes;
        this.str = str;
    }

    @Override
    public String toString() {
        if (str == null) {
            if (bytes.length == 0) {
                str = "";
            } else {
                str = Base64.getMimeEncoder().encodeToString(bytes);
            }
        }
        return str;
    }

    public void read(byte[] dst) {
        System.arraycopy(bytes, 0, dst, 0, bytes.length);
    }

//    public ByteBuffer asByteBuffer() {
//        return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
//    }
    
    public InputStream asInputStream() {
        return new ByteArrayInputStream(bytes);
    }
    
    public int getSize() {
        return bytes.length;
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PBytes) {
            final PBytes other = (PBytes) obj;
            return Arrays.equals(this.bytes, other.bytes);
        }
        return false;
    }

    @Override
    public boolean isEquivalent(Argument arg) {
         try {
            if (arg == this) {
                return true;
            }
            PBytes other = PBytes.coerce(arg);
            return Arrays.equals(bytes, other.bytes);
        } catch (ArgumentFormatException ex) {
            return false;
        }
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    
    
    public static PBytes valueOf(byte[] bytes) {
        return new PBytes(bytes.clone(), null);
    }
    
    public static PBytes valueOf(String str) throws ArgumentFormatException {
        if (str.trim().isEmpty()) {
            return PBytes.EMPTY;
        }
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(str);
            return new PBytes(bytes, str);
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
    }
    
    public static PBytes coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PBytes) {
            return (PBytes) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static Optional<PBytes> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PBytes.class, PMap.EMPTY);
    }
    
    public static class OutputStream extends ByteArrayOutputStream {
        
        public OutputStream() {
        }
        
        public OutputStream(int size) {
            super(size);
        }
        
        public synchronized PBytes toBytes() {
            // @TODO zero copy if buf.length == count?
            return new PBytes(toByteArray(), null);
        }
        
    }
    
}
