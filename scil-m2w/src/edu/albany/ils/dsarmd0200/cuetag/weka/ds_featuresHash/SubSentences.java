/*
 * This class determines the combine stategie of sub sentences' tags
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.lu.Assertions;
import edu.albany.ils.dsarmd0200.cuetag.DocDialogueType;
import java.util.ArrayList;

/**
 *
 * @author gjiao
 */
public class SubSentences {

    private ArrayList<Utterance> subSentences;

    public SubSentences(ArrayList<Utterance> subSentences){
        this.subSentences = subSentences;
    }

    public Utterance getVotedUtterance(Utterance pre_utt){

        // check only last two sentences for information-request
        int size = subSentences.size();
        if(size > 2){
            ArrayList<Integer> indexArray = new ArrayList<Integer>();
            for(int i = 0; i < size-2; i++){
                Utterance tmp = subSentences.get(i);
                if(tmp.getTag().toLowerCase().trim().equals(DsarmdDATag.IR)){
                    indexArray.add(i);
                }
            }
            int i = 0; int j = 0;
            ArrayList<Utterance> tmp = new ArrayList<Utterance>();
            while(i < subSentences.size() && j < indexArray.size()){
                if(i == indexArray.get(j)){
                    i++; j++;
                }
                else{
                    tmp.add(subSentences.get(i));
                    i++;
                }
            }
            if(i < subSentences.size()){
                for(; i < subSentences.size(); i++){
                    tmp.add(subSentences.get(i));
                }
            }
            subSentences = tmp;
            size = subSentences.size();
        }

        // get 1st, 2nd, 2nd last, and last
        Utterance first = subSentences.get(0);
        Utterance second = subSentences.get(1);
        Utterance secondLast = subSentences.get(size-2);
        Utterance last = subSentences.get(size-1);

        // action-directive has highest priority

	    if (pre_utt != null &&
		!pre_utt.getTag().equals(DsarmdDATag.IR) ||
		pre_utt == null) {
		if(first.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		    return first;
		}
		else if(last.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		    return last;
		}
		else if(second.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		    return second;
		}
		/*else if(secondLast.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		  return secondLast;
		  }*/
	    }
	    
	    // 1st > last > 2nd > 2ndLast
	    if(!first.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
		    pre_utt == null ||
		    pre_utt != null &&
		    !first.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
		    !pre_utt.getTag().toLowerCase().trim().equals(DsarmdDATag.IR) &&
		    !first.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		return first;
	    }
	    else if(!last.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
		    pre_utt == null ||
		    pre_utt != null &&
		    !last.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
		    !pre_utt.getTag().toLowerCase().trim().equals(DsarmdDATag.IR) &&
		    !last.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		return last;
	    }
	    /*
	      else if(!second.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)){
	      return second;
	      }
	    */
	    else if(!second.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
		    pre_utt == null ||
		    pre_utt != null &&
		    !second.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
		    !pre_utt.getTag().toLowerCase().trim().equals(DsarmdDATag.IR) &&
		    !second.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
		return second;
	    }
	    else{
		//            System.out.println("All assertion-opinion ?? " + subSentences);
		return first;
	    }
    }
}
