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

    public SubSentences(ArrayList<Utterance> subSentences) {
        this.subSentences = subSentences;
    }

    public Utterance getVotedUtterance(Utterance pre_utt) {

        // check only last two sentences for information-request
        int size = subSentences.size();
        //added by TL 04/11
        for (Utterance utt : subSentences) {
            if (utt.getTag().equals(DsarmdDATag.AD)
                    && (utt.getContent().toLowerCase().indexOf("please") != -1
                    || utt.getContent().toLowerCase().indexOf("suggest") != -1
                    || utt.getContent().toLowerCase().indexOf("recommend") != -1)) {
                return utt;
            }
        }
        Utterance tmp = null;
        ArrayList<Utterance> tmpar = new ArrayList<Utterance>();
        if (size > 2) {
            ArrayList<Integer> indexArray = new ArrayList<Integer>();
            for (int i = 0; i < size - 2; i++) {
                tmp = subSentences.get(i);
                if (tmp.getTag().toLowerCase().trim().equals(DsarmdDATag.IR)) {
                    indexArray.add(i);
                }
            }
            int i = 0;
            int j = 0;
            tmpar = new ArrayList<Utterance>();
            while (i < subSentences.size() && j < indexArray.size()) {
                if (i == indexArray.get(j)) {
                    i++;
                    j++;
                } else {
                    tmpar.add(subSentences.get(i));
                    i++;
                }
            }
            if (i < subSentences.size()) {
                for (; i < subSentences.size(); i++) {
                    tmpar.add(subSentences.get(i));
                }
            }
            subSentences = tmpar;
            size = subSentences.size();
//            System.out.println("tmpArrInloop=="+tmpar);
        }
        // get 1st, 2nd, 2nd last, and last
        Utterance first = subSentences.get(0);
        Utterance second = subSentences.get(1);
//        Utterance secondLast = subSentences.get(size-2);
        Utterance last = subSentences.get(size - 1);

        // action-directive has highest priority
       /*
         * System.out.println("Old Rules"); if (pre_utt != null &&
         * !pre_utt.getTag().equals(DsarmdDATag.IR) || pre_utt == null) {
         * if(first.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
         * return first; } else
         * if(last.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){ return
         * last; } else
         * if(second.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
         * return second; } // System.out.println(subSentences); //
         * System.out.println(subSentences.size()); //	else
         * if(secondLast.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){
         * //	return secondLast; //	} }
         *
         * // 1st > last > 2nd > 2ndLast // System.out.println("second loop");
         *
         * if(!first.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
         * pre_utt == null || pre_utt != null &&
         * !first.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
         * !pre_utt.getTag().toLowerCase().trim().equals(DsarmdDATag.IR) &&
         * !first.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){ //
         * System.out.println("1."+first.getTag()+"==="+subSentences); return
         * first; } else
         * if(!last.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
         * pre_utt == null || pre_utt != null &&
         * !last.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
         * !pre_utt.getTag().toLowerCase().trim().equals(DsarmdDATag.IR) &&
         * !last.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){ //
         * System.out.println("2."+last.getTag()+"==="+subSentences); return
         * last; }
         *
         * //	else
         * if(!second.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)){ //
         * return second; //	}
         *
         * else if(!second.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)
         * && pre_utt == null || pre_utt != null &&
         * !second.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) &&
         * !pre_utt.getTag().toLowerCase().trim().equals(DsarmdDATag.IR) &&
         * !second.getTag().toLowerCase().trim().equals(DsarmdDATag.AD)){ //
         * System.out.println("3."+second.getTag()+"==="+subSentences); return
         * second; } else{ //	System.out.println("All assertion-opinion ?? " +
         * first.getTag()+"==="+subSentences); return first; }
         *
         */
//           System.out.println("New Rules");
        //return AD,DR,IR and rest in the same order
        Utterance tmparr = null;
        for (int i = 0; i < subSentences.size(); i++) {
            tmparr = subSentences.get(i);
        }

        for (int i = 0; i < subSentences.size(); i++) {
            tmparr = subSentences.get(i);

            if (!(tmparr.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)) && (pre_utt != null | pre_utt == null)
                    && tmparr.getTag().toLowerCase().equals(DsarmdDATag.AD)) {
//               System.out.println("return 1:"+tmparr.getTag());
                return tmparr;

            }
            continue;
        }
        for (int i = 0; i < subSentences.size(); i++) {
            tmparr = subSentences.get(i);
            if (!tmparr.getTag().toLowerCase().trim().equals(DsarmdDATag.AS) && (pre_utt != null | pre_utt == null)
                    && (tmparr.getTag().toLowerCase().trim().equalsIgnoreCase("disagree-reject") | tmparr.getTag().toLowerCase().trim().equalsIgnoreCase("--disagree-reject"))) {
//               System.out.println("return 2:"+tmparr.getTag());
                return tmparr;
            }
            continue;
        }
        for (int i = 0; i < subSentences.size(); i++) {
            tmparr = subSentences.get(i);
            if (!(tmparr.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)) && (pre_utt != null | pre_utt == null)
                    && (tmparr.getTag().toLowerCase().equals(DsarmdDATag.IR))) {
//               System.out.println("return 3:"+tmparr.getTag());
                return tmparr;
            }
            continue;
        }
        for (int i = 0; i < subSentences.size(); i++) {
            tmparr = subSentences.get(i);
            if (!(tmparr.getTag().toLowerCase().trim().equals(DsarmdDATag.AS)) && (pre_utt != null | pre_utt == null)) {//return any other tag other than AS, if it exists
//               System.out.println("return 4:"+tmparr.getTag());
                return tmparr;
            }
            continue;
        }
//           System.out.println("return 5:"+first.getTag()+"\n");
//           all assertion opinions in the split utterances??return AS tag
        return first;


    }
}
