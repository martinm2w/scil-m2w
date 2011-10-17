/*
 * This class is to combine prediction tags from the splitted test set.
 *
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author gjiao
 */
public class CombineTestingUtterance {

//    private ArrayList<Utterance> utts; // original utts, for reference
    private ArrayList<Utterance> splitUtts; // expanded utts
    private TreeMap<Integer, Integer> turnNoSplitNo = new TreeMap<Integer, Integer>();//key: turn no; value: number of sub-sentences
    
    
    public CombineTestingUtterance(/*ArrayList<Utterance> utts,*/
            ArrayList<Utterance> splitUtts,
            TreeMap<Integer, Integer> turnNoSplitNo){
//        this.utts = utts;
        this.splitUtts = splitUtts;
        this.turnNoSplitNo = turnNoSplitNo;
    }

    public ArrayList<Utterance> getCombinedUtts(){
        SubSentences ss;
        Utterance sub;
        
        ArrayList<Utterance> result = new ArrayList<Utterance>();

        Set<Integer> splitTurnNos = turnNoSplitNo.keySet();
	Utterance pre_utt = null;
        for(int i = 0; i < splitUtts.size(); i++){
            Utterance utt = splitUtts.get(i);
            int turnNo = Integer.parseInt(utt.getTurn());
            if(!splitTurnNos.contains(turnNo)){ // no spliting for this turn
                result.add(utt);
            }
            else{ // combine
                int num = turnNoSplitNo.get(turnNo);
                String content = utt.getContent();
//                String tag = utt.getTag();
//                int k = i;
//                for(int j = 0; j < num-1; j++){
//                    Utterance nextUtt = splitUtts.get(++k);
//                    content = content + " " + nextUtt.getContent();
//                    if(!nextUtt.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)){
//                       tag = nextUtt.getTag().toLowerCase().trim();
//                    }
//                }
//                utt.setTag(tag);
//                utt.setContent(content);
//                result.add(utt);
//                i = i + (num-1);
                ArrayList<Utterance> uttArray = new ArrayList<Utterance>();
                uttArray.add(utt);
                int k = i;
                for(int j = 0; j < num-1; j++){
                    Utterance nextUtt = splitUtts.get(++k);
                    uttArray.add(nextUtt);
                    content = content + " " + nextUtt.getContent();
                }
                ss = new SubSentences(uttArray);
                sub = ss.getVotedUtterance(pre_utt);
                utt.setTag(sub.getTag());
                utt.setSubSentence(sub.getContent());
                utt.setContent(content);
		pre_utt = utt;
                result.add(utt);
                i = i + (num-1);
            }
        }

        return result;
    }

}
