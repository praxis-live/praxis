/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package org.jaudiolibs.audioservers.ext;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Identifier {
      
    private String identifier;
    
    public Identifier(String identifier) {
        if (identifier == null) {
            throw new NullPointerException();
        }
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Identifier) {
            return ((Identifier)obj).identifier.equals(identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
    
    
    
}