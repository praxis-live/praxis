

package net.neilcsmith.praxis.video.pgl.code;

import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.code.CodeFactory;


public class P2DCodeFactory extends CodeFactory<P2DCodeDelegate> {
    
    private final static P2DClassBodyContext CBC = new P2DClassBodyContext();
    
    public P2DCodeFactory(String type, String sourceTemplate) {
        super(CBC, type, sourceTemplate);
    }

    @Override
    protected CodeContext<P2DCodeDelegate> createCodeContext(P2DCodeDelegate delegate) {
        return new P2DCodeContext(new P2DCodeConnector(this, delegate));
    }
    
    public static class Custom extends P2DCodeFactory {
        
        public Custom(String type) {
            super(type, P2DClassBodyContext.TEMPLATE);
        }

        @Override
        protected P2DCodeDelegate createDefaultDelegate() throws Exception {
            return new P2DCodeDelegate(){};
        }

    }
    
}
