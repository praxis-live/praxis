///*
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// *
// * Copyright 2010 Neil C Smith.
// *
// * This code is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 3 only, as
// * published by the Free Software Foundation.
// *
// * This code is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// * version 3 for more details.
// *
// * You should have received a copy of the GNU General Public License version 3
// * along with this work; if not, see http://www.gnu.org/licenses/value
// * 
// *
// * Please visit http://neilcsmith.net if you need additional information or
// * have any questions.
// */
//package net.neilcsmith.praxis.hub;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import net.neilcsmith.praxis.audio.DefaultAudioRoot;
//import net.neilcsmith.praxis.audio.components.AudioInput;
//import net.neilcsmith.praxis.audio.components.AudioOutput;
//import net.neilcsmith.praxis.audio.components.Gain;
//import net.neilcsmith.praxis.audio.components.SamplePlayer;
//import net.neilcsmith.praxis.audio.components.distortion.SimpleOverdrive;
//import net.neilcsmith.praxis.audio.components.filter.CombFilter;
//import net.neilcsmith.praxis.audio.components.filter.IIRFilter;
//import net.neilcsmith.praxis.audio.components.test.Sine;
//import net.neilcsmith.praxis.audio.components.time.MonoDelay2s;
//import net.neilcsmith.praxis.components.ControlFrameTrigger;
//import net.neilcsmith.praxis.components.Property;
//import net.neilcsmith.praxis.components.RandomArg;
//import net.neilcsmith.praxis.components.StartTrigger;
//import net.neilcsmith.praxis.components.Variable;
//import net.neilcsmith.praxis.components.io.RandomFile;
//import net.neilcsmith.praxis.components.math.Add;
//import net.neilcsmith.praxis.components.math.Multiply;
//import net.neilcsmith.praxis.components.math.RandomFloat;
//import net.neilcsmith.praxis.components.math.Scale;
//import net.neilcsmith.praxis.components.math.Threshold;
////import net.neilcsmith.praxis.components.math.Variable;
//import net.neilcsmith.praxis.components.test.Log;
//import net.neilcsmith.praxis.components.timing.SimpleDelay;
//import net.neilcsmith.praxis.components.timing.Timer;
//import net.neilcsmith.praxis.core.Component;
//import net.neilcsmith.praxis.core.Root;
//import net.neilcsmith.praxis.gui.DefaultGuiRoot;
//import net.neilcsmith.praxis.gui.components.Button;
//import net.neilcsmith.praxis.gui.components.FileField;
//import net.neilcsmith.praxis.gui.components.HPanel;
//import net.neilcsmith.praxis.gui.components.HRangeSlider;
//import net.neilcsmith.praxis.gui.components.HSlider;
//import net.neilcsmith.praxis.gui.components.Tabs;
//import net.neilcsmith.praxis.gui.components.ToggleButton;
//import net.neilcsmith.praxis.gui.components.VPanel;
//import net.neilcsmith.praxis.gui.components.VRangeSlider;
//import net.neilcsmith.praxis.gui.components.VSlider;
//import net.neilcsmith.praxis.gui.components.XYController;
//import net.neilcsmith.praxis.midi.DefaultMidiRoot;
//import net.neilcsmith.praxis.video.DefaultVideoRoot;
//import net.neilcsmith.praxis.video.components.VideoOutput;
//import net.neilcsmith.praxis.video.components.Snapshot;
//import net.neilcsmith.praxis.video.components.Splitter;
//import net.neilcsmith.praxis.video.components.Still;
//import net.neilcsmith.praxis.video.components.VideoCapture;
//import net.neilcsmith.praxis.video.components.mix.Composite;
//import net.neilcsmith.praxis.video.components.mix.XFader;
//import net.neilcsmith.praxis.video.components.test.Difference;
//import net.neilcsmith.praxis.video.components.test.FrameTimer;
//import net.neilcsmith.praxis.video.components.test.Hyp;
//import net.neilcsmith.praxis.video.components.test.ImageSave;
//import net.neilcsmith.praxis.video.components.test.Ripple;
//import net.neilcsmith.praxis.video.components.VideoPlayer;
//import net.neilcsmith.praxis.video.components.test.BackgroundDifference;
//import net.neilcsmith.praxis.video.components.test.DifferenceCalc;
//import net.neilcsmith.praxis.video.components.test.Noise;
//
///**
// *
// * @author Neil C Smith
// * @TODO split into factories under each package (audio, control, video, etc)
// * @TODO this should be called ComponentRegistry and NOT implement ComponentFactory
// */
//class DefaultComponentFactory implements ComponentFactory {
//
//    private static Logger logger = Logger.getLogger(DefaultComponentFactory.class.getName());
//    private static DefaultComponentFactory instance = new DefaultComponentFactory();
//    private Map<String, Class<? extends Component>> componentMap;
//    private Map<String, Class<? extends Root>> rootMap;
//
//    private DefaultComponentFactory() {
//        buildComponentMap();
//        buildRootMap();
//    }
//
//    private void buildComponentMap() {
////        root:image          net.neilcsmith.praxis.image.DefaultVideoRoot
////root:gui            net.neilcsmith.praxis.gui.DefaultGuiRoot
////
////
////frame-trigger       net.neilcsmith.praxis.components.ControlFrameTrigger
////start-trigger       net.neilcsmith.praxis.components.StartTrigger
////random-arg          net.neilcsmith.praxis.components.RandomArg
////
////file:random         net.neilcsmith.praxis.components.io.RandomFile
////
////math:random         net.neilcsmith.praxis.components.math.RandomFloat
////math:threshold      net.neilcsmith.praxis.components.math.Threshold
////
////test:log            net.neilcsmith.praxis.components.test.Log
////
////timing:delay        net.neilcsmith.praxis.components.timing.SimpleDelay
////timing:timer        net.neilcsmith.praxis.components.timing.Timer
////
////video:output        net.neilcsmith.praxis.image.components.VideoOutput
////video:still         net.neilcsmith.praxis.image.components.Still
////video:snapshot      net.neilcsmith.praxis.image.components.Snapshot
////video:splitter      net.neilcsmith.praxis.image.components.Splitter
////video:test:hypnosis net.neilcsmith.praxis.image.components.test.Hyp
////video:test:time     net.neilcsmith.praxis.image.components.test.FrameTimer
////video:test:save     net.neilcsmith.praxis.image.components.test.ImageSave
////video:mix:xfader    net.neilcsmith.praxis.image.components.mix.XFader
////video:mix:composite net.neilcsmith.praxis.image.components.mix.Composite
////
////gui:hslider         net.neilcsmith.praxis.gui.components.HSlider
//
//        componentMap = new LinkedHashMap<String, Class<? extends Component>>();
//
//        componentMap.put("core:k-rate", ControlFrameTrigger.class);
//        componentMap.put("core:i-rate", StartTrigger.class);
//        componentMap.put("core:random-arg", RandomArg.class);
//        componentMap.put("core:property", Property.class);
//        componentMap.put("core:variable", Variable.class);
//
//        componentMap.put("core:files:random", RandomFile.class);
//
//        // MATH
//        componentMap.put("core:math:random", RandomFloat.class);
//        componentMap.put("core:math:threshold", Threshold.class);
//        componentMap.put("core:math:multiply", Multiply.class);
//        componentMap.put("core:math:add", Add.class);
//        componentMap.put("core:math:scale", Scale.class);
//
//        componentMap.put("core:test:log", Log.class);
//
//        componentMap.put("core:timing:delay", SimpleDelay.class);
//        componentMap.put("core:timing:timer", Timer.class);
//
//
//        // AUDIO
//        componentMap.put("audio:input", AudioInput.class);
//        componentMap.put("audio:output", AudioOutput.class);
//        componentMap.put("audio:sine", Sine.class);
//        componentMap.put("audio:gain", Gain.class);
//        componentMap.put("audio:sampleplayer", SamplePlayer.class);
//        componentMap.put("audio:filter:comb", CombFilter.class);
//        componentMap.put("audio:filter:iir", IIRFilter.class);
//        componentMap.put("audio:delay:mono-delay", MonoDelay2s.class);
//        componentMap.put("audio:distortion:simple-overdrive", SimpleOverdrive.class);
//        componentMap.put("audio:mix:xfader", net.neilcsmith.praxis.audio.components.mix.XFader.class);
//
//        // VIDEO
//        componentMap.put("video:output", VideoOutput.class);
//        componentMap.put("video:still", Still.class);
//        componentMap.put("video:snapshot", Snapshot.class);
//        componentMap.put("video:splitter", Splitter.class);
//        componentMap.put("video:test:hypnosis", Hyp.class);
//        componentMap.put("video:test:time", FrameTimer.class);
//        componentMap.put("video:test:save", ImageSave.class);
//        componentMap.put("video:test:difference-calc", DifferenceCalc.class);
//        componentMap.put("video:test:bgdiff", BackgroundDifference.class);
//        componentMap.put("video:test:noise", Noise.class);
//        componentMap.put("video:time-fx:ripple", Ripple.class);
//        componentMap.put("video:time-fx:difference", Difference.class);
//        componentMap.put("video:mix:xfader", XFader.class);
//        componentMap.put("video:mix:composite", Composite.class);
//
//        componentMap.put("video:player", VideoPlayer.class);
//        componentMap.put("video:capture", VideoCapture.class);
//
//
//        // GUI
//        componentMap.put("gui:h-slider", HSlider.class);
//        componentMap.put("gui:v-slider", VSlider.class);
//        componentMap.put("gui:h-rangeslider", HRangeSlider.class);
//        componentMap.put("gui:v-rangeslider", VRangeSlider.class);
//        componentMap.put("gui:button", Button.class);
//        componentMap.put("gui:togglebutton", ToggleButton.class);
//        componentMap.put("gui:xy-pad", XYController.class);
//        componentMap.put("gui:filefield", FileField.class);
//        // GUI containers
//        componentMap.put("gui:h-panel", HPanel.class);
//        componentMap.put("gui:v-panel", VPanel.class);
//        componentMap.put("gui:tabs", Tabs.class);
//
//
//
//    }
//
//    private void buildRootMap() {
//        rootMap = new LinkedHashMap<String, Class<? extends Root>>();
//
//        rootMap.put("root:video", DefaultVideoRoot.class);
//        rootMap.put("root:gui", DefaultGuiRoot.class);
//        rootMap.put("root:audio", DefaultAudioRoot.class);
//        rootMap.put("root:midi", DefaultMidiRoot.class);
//    }
//
//    public String[] getComponentIDs() {
//        return componentMap.keySet().toArray(new String[componentMap.size()]);
//    }
//
//    public String[] getRootComponentIDs() {
//        return rootMap.keySet().toArray(new String[rootMap.size()]);
//    }
//
//    public Component createComponent(String id) throws ComponentCreationException {
//        try {
//            Class<? extends Component> cl = componentMap.get(id);
//            if (cl != null) {
//                Component comp = cl.newInstance();
//                return comp;
//            }
//        } catch (Exception ex) {
//            String msg = "Instantiation of component type " + id + " failed.";
//            logger.log(Level.WARNING, msg, ex);
//            throw new ComponentCreationException(msg);
//        }
//        String msg = "Unknown component id " + id;
//        logger.log(Level.WARNING, msg);
//        throw new ComponentCreationException(msg);
//
//    }
//
//    public Root createRootComponent(String id) throws ComponentCreationException {
//        try {
//            Class<? extends Root> cl = rootMap.get(id);
//            if (cl != null) {
//                Root comp = cl.newInstance();
//                return comp;
//            }
//        } catch (Exception ex) {
//            String msg = "Instantiation of root type " + id + " failed.";
//            logger.log(Level.WARNING, msg, ex);
//            throw new ComponentCreationException(msg);
//        }
//        String msg = "Unknown root id " + id;
//        logger.log(Level.WARNING, msg);
//        throw new ComponentCreationException(msg);
//    }
//
//    public static DefaultComponentFactory getInstance() {
//        return instance;
//    }
//}
