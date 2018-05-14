

package org.praxislive.video.pgl.code;

import org.praxislive.code.CodeContext;
import org.praxislive.code.CodeFactory;


public class P3DCodeFactory extends CodeFactory<P3DCodeDelegate> {
    
    private final static P3DClassBodyContext CBC = new P3DClassBodyContext();
    
    private final boolean emptyDefault;
    
    public P3DCodeFactory(String type) {
        super(CBC, type, P3DClassBodyContext.TEMPLATE);
        emptyDefault = true;
    }
    
    public P3DCodeFactory(String type, String sourceTemplate) {
        super(CBC, type, sourceTemplate);
        emptyDefault = false;
    }

    @Override
    public Task<P3DCodeDelegate> task() {
        return new P3DContextCreator();
    }

    private class P3DContextCreator extends Task<P3DCodeDelegate> {

        private P3DContextCreator() {
            super(P3DCodeFactory.this);
        }

        @Override
        protected CodeContext<P3DCodeDelegate> createCodeContext(P3DCodeDelegate delegate) {
            return new P3DCodeContext(new P3DCodeConnector(this, delegate));
        }


    }
    
}
