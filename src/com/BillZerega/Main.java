package com.BillZerega;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;



public class Main {

    //declare fields
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    //all the names of instruments being used and their coresponding code
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
        "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
        "Open Hi Conga"};
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    //start the program
    public static void main(String[] args) {
	    new Main().buildGUI();
    }

    public void buildGUI(){

        //create the JFrame for everything to be laid out in, use BorderLayout manager
        theFrame = new JFrame("Cyber Beatbox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //create a panel (box) to insert into the Frame
        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        //create buttons + event listeners to add to box to be added to the Frame
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        //add a new box with all the instrument names listed vertically
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        //add boxes to the Jpanel, add Jpanel to JFrame
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        //create grid for checkboxes to call for instruments, add to the JPanel
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        //make the checkboxes, set to false(unclicked), add them to the ArrayList, add to the GUI panel
        for(int i = 0; i < 256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
    }//close method

    //set up the MIDI player in a try/catch statement because it can return an error
    public void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch(Exception e) {e.printStackTrace();}
    }

    public void buildTrackAndStart(){
        //looking for values of instrument across 16 beats, if instrument supposed to play will find key,
        //if not supposed to play it will put in a 0
        int[] trackList = null;

        //delete old track and create new track
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        //do this for each of the 16 rows(instruments)
        for(int i = 0; i < 16; i++){
            trackList = new int[16];

            //set the key that represents each instrument
            int key = instruments[i];

            //do this for the 16 beats for this row
            for(int j = 0; j < 16; j++){
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
                //adding instrument key if box is checked, removing if not
                if (jc.isSelected()){
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }//close inner loop

            //for each instrument and for each beat make events and add them to the track
            makeTracks(trackList);
            track.add(makeEvent(176,1,127,0,16));
        }// close outer loop


        //put the track together and play it
        track.add(makeEvent(192,9,1,0,15));
        try{

            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }catch(Exception e) {e.printStackTrace();}
    }//close buildTrackAndStart method


    //event listeners for all the buttons, all inner classes
    public class MyStartListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    } //close start button listener

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }//close stop button listener

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }//close TempoUp button listener

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .97));
        }
    }//close tempo down button listener

    public void makeTracks(int[] list){
        for(int i=0; i < 16; i++){
            int key = list[i];

            //if the instrument is supposed ot play create the turn on and off event for the note
            if (key != 0){
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100, i+1));
            }
        }
    }


    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch(Exception e){e.printStackTrace();}
        return event;
    }

}
