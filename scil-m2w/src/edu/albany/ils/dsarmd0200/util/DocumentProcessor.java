package edu.albany.ils.dsarmd0200.util;

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;

/**
 * this file is used to parse document in text format.
 * @author Ting Liu
 */
public class DocumentProcessor {

    public DocumentProcessor() {
    }

    public static ArrayList parseUtts(File doc) {
        String[] items = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(doc), "UTF-8"));
            String a_line = null;
            ArrayList utts = new ArrayList();
            int count = 1;
            while ((a_line = br.readLine()) != null) {
                if (a_line.trim().length() == 0) {
                    continue;
                }
                items = a_line.split("\\: ");
                if (items.length < 2) { /*System.out.println("dialogue format is wrong: " + a_line);*/ continue;
                }
                Utterance utt = new Utterance();
                String[] time_spk = items[0].split("\\)[ |\\t]+");
                if (time_spk.length > 1) {
                    utt.setTime(time_spk[0].replace("\\(", ""));
                    //System.out.println("(in Docu) speaker: " + time_spk[1]);
                    utt.setSpeaker(time_spk[1]);
                    utt.setTurn((new Integer(count)).toString());
                } else {
                    time_spk = items[0].split("\\(");
                    utt.setTime(time_spk[1].split(" ")[0]);
                    //System.out.println("(in Docu) speaker: " + time_spk[0].split(" ")[1]);
                    utt.setSpeaker(time_spk[0].split(" ")[1]);
                    utt.setTurn((new Integer(count)).toString());
                }
                //System.out.println("a_line: " + a_line);
                if (items.length > 2) {
                    utt.setContent(items[1] + ": " + items[2]);
                } else {
                    utt.setContent(items[1]);
                }
                //System.out.println("utt content: " + utt.getContent());
                //System.out.println("utt speaker: " + utt.getSpeaker());
                count++;
                utts.add(utt);
            }
            return utts;
        } catch (Exception ioe) {
            System.out.println("item[0]: " + items[0]);
            ioe.printStackTrace();
        }
        return null;
    }

    public static ArrayList parseWikiUtts(File doc) {
        String[] items = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(doc), "UTF-8"));
            //BufferedReader br = new BufferedReader(new FileReader(doc));
            String a_line = null;
            ArrayList utts = new ArrayList();
            int count = 1;
            while ((a_line = br.readLine()) != null) {
                if (a_line.trim().length() == 0) {
                    continue;
                }
                items = a_line.split("# ");
                if (items.length < 2) { /*System.out.println("dialogue format is wrong: " + a_line);*/ continue;
                }
                Utterance utt = new Utterance();
                /**new for wiki data*/
                String[] lnk_tm_spk = items[0].split("#");
                //System.out.println("lnk_tm_spk: " + lnk_tm_spk[0] + " --- " + lnk_tm_spk[1] + " --- " + lnk_tm_spk[2]);
                if (lnk_tm_spk.length != 3) {System.out.print("format error: " + items[0]);}
                items[0] = lnk_tm_spk[2];
                utt.setTurn(lnk_tm_spk[0]);
                /**done for wiki data*/
                String[] time_spk = items[0].split("\\)[ |\\t]+");
                if (time_spk.length > 1) {
                    utt.setTime(time_spk[0].replace("\\(", ""));
                    utt.setSpeaker(time_spk[1]);
                    //utt.setTurn((new Integer(count)).toString());
                } else {
                    time_spk = items[0].split("\\(");
                    utt.setTime(time_spk[1].split(" ")[0]);
                    utt.setSpeaker(time_spk[0].split(" ")[1]);
                    //utt.setTurn((new Integer(count)).toString());
                }
                /** new for wiki data*/
                if (lnk_tm_spk[1].equals("-1") ||
                        lnk_tm_spk[1].equals(lnk_tm_spk[0])) {
                    utt.setCommActType("addressed-to");
                    utt.setRespTo("all-users");
                } else {
                    //System.out.println("preturn no: " + lnk_tm_spk[1]);
                    Utterance pre_turn = getUtt(utts, lnk_tm_spk[1]);
                    //if (pre_turn == null) System.out.println("preturn is null!!!");
                    String pre_turn_spk = pre_turn.getSpeaker();
                    String cur_spk = utt.getSpeaker();
                    if (pre_turn_spk.equals(cur_spk)) {
                        utt.setCommActType("continuation-of");
                    } else {
                        utt.setCommActType("response-to");
                    }
                    utt.setRespTo(pre_turn_spk + ":" + pre_turn.getTurn());
                }
                /*done*/
                //System.out.println("a_line: " + a_line);
                if (items.length > 2) {
                    utt.setContent(items[1] + ": " + items[2]);
                } else {
                    utt.setContent(items[1]);
                }
                //System.out.println("utt content: " + utt.getContent());
                //System.out.println("utt speaker: " + utt.getSpeaker());
                //System.out.println("utt comActType: " + utt.getCommActType());
                //System.out.println("utt link to: " + utt.getRespTo());
                count++;
                utts.add(utt);
            }
            return utts;
        } catch (Exception ioe) {
            System.out.println("item[0]: " + items[0]);
            ioe.printStackTrace();
        }
        return null;
    }
    
    public static Utterance getUtt(ArrayList<Utterance> utts, 
                String turn_no) {
        for (Utterance utt: utts) {
            //System.out.println("utt no: " + utt.getTurn());
            if (utt.getTurn().equals(turn_no)) return utt;
        }
        return null;
    }
}
