/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.praxislive.base;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.Clock;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.Packet;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Root;
import org.praxislive.core.RootHub;
import org.praxislive.core.types.PString;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith - https://www.neilcsmith.net
 */
public class AbstractRootIT {

    public AbstractRootIT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testProcessCall() {
        RootImpl root = new RootImpl();
        LinkedBlockingQueue<Packet> responseQueue = new LinkedBlockingQueue<>();
        RootHubImpl hub = new RootHubImpl(root, responseQueue);
        hub.ctrl.start(Thread::new);
        hub.ctrl.submitPacket(Call.create(ControlAddress.create("/test.hello"),
                ControlAddress.create("/hub.world"),
                hub.getClock().getTime() + TimeUnit.SECONDS.toNanos(1)));
        try {
            Call reply = (Call) responseQueue.poll(2, TimeUnit.SECONDS);
            assertSame("OK", reply.args().get(0).toString());
        } catch (Exception ex) {
            fail();
        }
        hub.ctrl.shutdown();
    }
    
    @Test
    public void testDelegateProcessCall() {
        DelegatingRootImpl root = new DelegatingRootImpl();
        LinkedBlockingQueue<Packet> responseQueue = new LinkedBlockingQueue<>();
        RootHubImpl hub = new RootHubImpl(root, responseQueue);
        hub.ctrl.start(Thread::new);
        hub.ctrl.submitPacket(Call.create(ControlAddress.create("/test.hello"),
                ControlAddress.create("/hub.world"),
                hub.getClock().getTime() + TimeUnit.SECONDS.toNanos(1)));
        try {
            Call reply = (Call) responseQueue.poll(2, TimeUnit.SECONDS);
            assertSame("OK", reply.args().get(0).toString());
        } catch (Exception ex) {
            fail();
        }
        hub.ctrl.shutdown();
    }

    public class RootImpl extends AbstractRoot {

        @Override
        protected void activating() {
            setRunning();
        }

        public void processCall(Call call, PacketRouter router) {
            router.route(call.reply(PString.of("OK")));
        }
    }
    
     public class DelegatingRootImpl extends AbstractRoot {

        Delegate del;
         
        @Override
        protected void activating() {
            setRunning();
            del = new Delegate(getRootHub());
            attachDelegate(del);
            del.start();
        }

        @Override
        protected void terminating() {
            System.out.println("Terminate called");
            assertNotEquals(del.active, Thread.currentThread());
            assertTrue(!del.active.isAlive());
            
        }
        
        

        public void processCall(Call call, PacketRouter router) {
            assertEquals(del.active, Thread.currentThread());
            router.route(call.reply(PString.of("OK")));
        }
        
        class Delegate extends AbstractRoot.Delegate {
            
            Thread active;
            RootHub hub;
            
            Delegate(RootHub hub) {
                this.hub = hub;
            }
            
            void start() {
                active = getThreadFactory().newThread(() -> {
                    while (doUpdate(hub.getClock().getTime(), true)) {
                        try {
                            doTimedPoll(1, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AbstractRootIT.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    detachDelegate(this);
                });
                active.start();
            }
            
        }
    }
    

    public class RootHubImpl implements RootHub {

        private final Root root;
        private final Root.Controller ctrl;
        private final Queue<Packet> queue;

        private RootHubImpl(Root root, Queue<Packet> queue) {
            this.root = root;
            this.queue = queue;
            this.ctrl = root.initialize("test", this);
        }

        @Override
        public boolean dispatch(Packet packet) {
            if ("test".equals(packet.rootID())) {
                return ctrl.submitPacket(packet);
            } else {
                System.out.println(packet);
                queue.add(packet);
            }
            return true;
        }

        @Override
        public Clock getClock() {
            return System::nanoTime;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

    }

}
