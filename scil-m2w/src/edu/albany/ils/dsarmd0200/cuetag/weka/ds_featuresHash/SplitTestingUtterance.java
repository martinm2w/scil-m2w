/*
 * This class is to split long utterances into several sub-sentences in the
 * testing corpus
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.util.SentenceSplitter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author gjiao
 */
public class SplitTestingUtterance {

    private ArrayList<Utterance> utts = new ArrayList<Utterance>();
    SentenceSplitter sp = new SentenceSplitter();
    private TreeMap<Integer, Integer> turnNoSplitNo = new TreeMap<Integer, Integer>();//key: turn no; value: number of sub-sentences
    private ArrayList<Utterance> splittedUtts = new ArrayList<Utterance>();

    public SplitTestingUtterance(ArrayList<Utterance> utts){
        this.utts = utts;
    }

    /**
     * This method goes through all utterance utts(testing corpus) and
     * split a complex sentence(contains 2-3 sub-sentences) into several
     * sub sentence, everything else stays the same.
     * @return an enriched utts
     */
    public void startSplitting(){
        
        for(Utterance utt : utts){
            //String content = utt.getContent().toLowerCase().trim();
            String content = utt.getContent().trim().replaceAll("[ ]+", " ");
	    //System.out.println("content: " + content);
            ArrayList<String> subSentences = sp.split(content);
            //if(subSentences.size() > 3){
            if(subSentences.size() < 3){
                splittedUtts.add(utt);
            }
            else if(subSentences.size() > 2){ // sub sentences num : 2 - 3
                
		//System.out.println("Laura debug: turn no = " + utt.getTurn());
                //System.out.println("(in split) speaker: " + utt.getOriSpeaker());
                int subSentenceNum = subSentences.size();
                int turnNo = Integer.parseInt(utt.getTurn());
                turnNoSplitNo.put(turnNo, subSentenceNum);
                for(String s : subSentences){
                    Utterance tmp = new Utterance();
                    tmp.setCommActType(utt.getCommActType());
                    tmp.setSpeaker(utt.getOriSpeaker());
                    tmp.setPOS(utt.getPOS());
                    tmp.setPOSCOUNT(utt.getPOSCOUNT());
                    tmp.setPOSORIGIN(utt.getPOSORIGIN());
                    tmp.setPolarity(utt.getPolarity());
                    tmp.setRespTo(utt.getRespTo());
                    tmp.setTopic(utt.getTopic());
                    tmp.setTag(utt.getTag());
                    tmp.setContent(s);
		    tmp.setTaggedContent(utt.getTaggedContent());
                    tmp.setTurn(utt.getTurn());
                    splittedUtts.add(tmp); // seperate them
                }
            }
            else{
                splittedUtts.add(utt);
            }
        }
    }

    public TreeMap<Integer, Integer> getTurnNoSplitNo(){
        return turnNoSplitNo;
    }

    public ArrayList<Utterance> getSplittedUtts(){
        return splittedUtts;
    }

    
    
}
