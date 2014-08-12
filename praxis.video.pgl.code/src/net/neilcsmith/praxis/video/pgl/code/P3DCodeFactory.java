

package net.neilcsmith.praxis.video.pgl.code;

import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.CodeFactory;


public class P3DCodeFactory extends CodeFactory<P3DCodeDelegate> {
    
    private final static P3DClassBodyContext CBC = new P3DClassBodyContext();
    
    public P3DCodeFactory(String type, String sourceTemplate) {
        super(CBC, type, sourceTemplate);
    }

    @Override
    protected CodeContext<P3DCodeDelegate> createCodeContext(P3DCodeDelegate delegate) {
        return new P3DCodeContext(new P3DCodeConnector(this, delegate));
    }
    
    public static class Custom extends P3DCodeFactory {
        
        public Custom(String type) {
            super(type, P3DClassBodyContext.TEMPLATE);
        }

        @Override
        protected P3DCodeDelegate createDefaultDelegate() throws Exception {
            return new P3DCodeDelegate(){};
        }

    }
    
}
