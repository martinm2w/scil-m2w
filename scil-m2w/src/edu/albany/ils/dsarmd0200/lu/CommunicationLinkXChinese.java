package edu.albany.ils.dsarmd0200.lu;

/**
 * m2w: this file is modified to improve the performance. commented all old methods, modified ,created new methods, wrote all new comments. 
 * @file: CommunicationLinkX.java
 * @author: Xin Shi * 
 * @lastupdate: 5/13/11 2:10 PM version 13-02
 */

import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.AbstractCollection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommunicationLinkXChinese{
    
//  ==================================Attributes===================================================
//chinese char unicode representation : "[\\u4E00-\\u9FA5]"
    //changed all instance vars to private. 4/18/11 2:06 PM
//    private ArrayList<Utterance> utts = new ArrayList<Utterance>();	// all utterances
    private ArrayList<Utterance> utts ;
    private HashSet<String> speaker_names = new HashSet<String>();	// all speakers names
    private TreeMap<String,Integer> comm_total = new TreeMap<String,Integer>();	// saves all speaker's preference
//    private HashMap<String,String> comm_speaker = new HashMap<String,String>();	// saves preferences for a certain pair of speakers
    private static final String CONTINUATION_OF="continuation-of";
    private static final String ADDRESSED_TO="addressed-to";
    private static final String RESPONSE_TO="response-to";
    private static final int COMM_THRESHOLD = 10;
    private static final int SHORT_THRESHOLD = 6;          // threshold < this threshold is considered short, else long
    private static final int LONG_THRESHOLD = 4;            // for long turn lengths
    private static final double SIM_THRESHOLD = 0.3;        // for similarity
    private static final int SHORT_THRESHOLD_QUESTION = 3;
    private static final int LONG_THRESHOLD_QUESTION = 3;
    private static final int NOUN_SIM = 5; //m2w: the look back threshold , 5 turns now. 3/21/11 2:15 PM
    private int response_to_count;
    private int hit;	// correct count
    private Utterance name_utt;
    private TreeMap<Integer,String> map = new TreeMap<Integer,String>();
    private ArrayList<Integer> cl_arr = new ArrayList<Integer>();

    
//    private static final String EVAL_FILE = "/home/ruobo/NetBeansProjects/dsarmd0100/src/edu/albany/ils/dsarmd0100/lu/CommunicationLinkX.eval";
    private static final int CONF_SHORT_THRES = 3; // m2w : how many turns look back for confirmation_short utterances 3/19/11 8:15 PM
    private static final int CONF_LONG_THRES = 4; // m2w : how many turns look back for confirmation_short utterances 3/19/11 8:15 PM
    private static final int FIND_NAME_THRES = 11; //m2w : the look back threshold for findname() . 3/21/11 1:40 PM, 10 , 4/18/11 12:35 PM, 11 4/18/11 1:04 PM
    private static final int SHORT_LOOK_BACK = 3;
    private static final int LONG_LOOK_BACK = 3;
    private static final int SHORT_SIM_LOOK_BACK = 5;//m2w : for similarity 3/23/11 1:07 PM
    private static final int LONG_SIM_LOOK_BACK = 5;//m2w : for similarity 3/23/11 1:07 PM
    //how many utts to look back from current utt, when doing sim words calculation.
    private static final int SHORT_WORD_SIM_LOOK_BACK = 3; //m2w : ADDED short and long, short was 3. 4/20/11 1:10 PM
    private static final int LONG_WORD_SIM_LOOK_BACK = 5; //m2w : was 3 , changed to 5. 4/20/11 1:10 PM
    //do sim words calculation for words longer than SIM_LENGTH_CHECK
    private static final int LONG_WORD_SIM_LENGTH_CHECK = 6;//m2w : utt length for doing check words sim, (8), 4/20/11 10:23 AM
    private static final int SHORT_WORD_SIM_LENGTH_CHECK = 5;//M2W: added short and long for different checks. (5) 4/20/11 10:23 AM
    private static final double WORD_SIM_THRESHOLD = 0.7; // m2w : for word sim 4/16/11 3:10 PM
    private static int LONG_UTT_STATICS;// m2w: for statistics 4/10/11 12:53 PM
    private static int LONG_UTT_CND_STATICS;// m2w: for statistics 4/10/11 12:53 PM
    private static int SHORT_UTT_STATICS;// m2w: for statistics 4/10/11 12:53 PM
    private static int SHORT_UTT_CND_STATICS;// m2w: for statistics 4/10/11 12:53 PM
    private static final int LONG_UTT_SIM_LOOK_BACK = 4; // m2w : long utt sim. 4/20/11 3:09 PM
    private static final double SHORT_SYN_SIM_THRESH = 0.2; //for calsimbysyn method 4/10/11 12:54 PM
    private static final double LONG_SYN_SIM_THRESH = 0.3; //for calsimbysyn method 4/10/11 12:55 PM
    private static final int LONG_SET_AS_DEFAULT_LOOK_BACK = 4;
    private static Wordnet wn;
//    private static Wordnet wn = new Wordnet();
    private int short_hit;
    private int long_hit;
    private int short_cnd_hit;
    private int long_cnd_hit;
    private int yes_count;
    private int i_r_count;
    private int qm_count;

    //report controlling. 5/13/11 2:09 PM
    private boolean doHitReport = true;
    private boolean doMissReport = true;
    private boolean doFinalReport = true; // whether print out the final report at the end of each file or not. 4/27/11 12:40 PM
    private boolean doGenReport = true; // whether print out the evaluation(miss and hit) or not. 4/27/11 12:40 PM

    //added 7/27/11 12:47 PM
    private boolean isEnglish_ = false;
    private boolean isChinese_ = false;
    
    private int $prevIn4Count = 0;
    
    //statistics chinese:
    private int hits4 = 0;
    private int hits5 = 0;
    private int hits6 = 0;
    private int hits7 = 0;
    private int hits8 = 0;
    private int hits9 = 0;
    private int hits10 = 0;

    
    
        /**
         * m2w : constructor , get all utterances for one file, and remove the .1 and .0 turns in the file.
         * @param utts
         */
        public CommunicationLinkXChinese(ArrayList<Utterance> utts_, Wordnet wn_,boolean isEnglish,
			      boolean isChinese){
            wn = wn_;
            // get all utterances for one file
            isEnglish_ = isEnglish;
            isChinese_ = isChinese;
//            this.utts = (ArrayList<Utterance>)utts_.clone(); //3/22/11 10:08 PM
            this.utts = new ArrayList<Utterance>(utts_);
            StanfordPOSTagger.initialize();
            ParseTools.initialize();
            LONG_UTT_STATICS = 0;
            SHORT_UTT_STATICS = 0;
            LONG_UTT_CND_STATICS = 0;
            SHORT_UTT_CND_STATICS = 0;
                       
        }

//  ============================================main method==================================================
        /**
         * m2w : it is called in the Assertion class, this is the initiate calculation method
         */
        public void collectCLFtsX()
        {
//            System.out.println("in cmm_link");
//            CommunicationLinkX.checkUttTurnsInCMDLine(utts);
//    	1. loop through all utt, get speakernames(hashset)
    	for(int i=0; i<utts.size(); i++)
    	{
            Utterance u_speaker = utts.get(i);
            speaker_names.add(u_speaker.getSpeaker().toLowerCase());

//            if(utts.get(i).getContent().toLowerCase().equals("yes")){
//                yes_count++;
////                String tempResTo = utts.get(i).getRespTo();
////                if(!tempResTo.equals("") && tempResTo.indexOf(":") != -1){
////                    int toTurn = Integer.parseInt(tempResTo.split(":")[1]) - 1;
////                    String catype = utts.get(toTurn).getTag();
////                    if(catype.toLowerCase().contains("information-request")){
////                        i_r_count++;
////                    }
////                    if(utts.get(toTurn).getContent().contains("?")){
////                        qm_count++;
////                    }
//                }
                
//            }
    	}

//      2.loop through all utts, if commActType equals "response_to", do the calculation and get the turn no. of which turn is responsed to.
    	for(int index=0; index<utts.size(); index++)
    	{
            Utterance u_content = utts.get(index);
//            System.out.println("turn_no: " + u_content.getTurn() + " index: " + index);
            if(u_content.getCommActType().equals(RESPONSE_TO))
            {
                    response_to_count++;
                    String content = contentExtraction(u_content).toLowerCase();
                    int turn_length = ParseTools.wordCountChinese(content);
                    if(turn_length < SHORT_THRESHOLD){
//                            shortUtterance(index);
                            shortUttRank(index);
                            SHORT_UTT_STATICS++;
                            //testing 8/2/11 12:47 PM
//                            System.out.println( "short: "+ content + " - length: " + turn_length);
                    } else{
//                            longUtterance(index);
                            shortUttRank(index);
                            LONG_UTT_STATICS++;
                            //testing 8/2/11 12:47 PM
//                            System.out.println("long: "+content + " - length: " + turn_length);
                    }

            }
    	}
//            System.out.println("out of cmm_link");
    	// here print out the final result after done with each file.
        if(doFinalReport){
            System.out.println("Hit: " + hit);
            System.out.println("Number of response utt: " + response_to_count);
            System.out.println("Precision: " + (double)((double)hit)/((double)response_to_count));
    //        System.out.println("Number of short utts: " + SHORT_UTT_STATICS + ". CdN: " + SHORT_UTT_CND_STATICS);
    //        System.out.println("Number of long utts: " + LONG_UTT_STATICS + ". CdN: " + LONG_UTT_CND_STATICS);
            System.out.println("Number of short utts: " + SHORT_UTT_STATICS + ", short hit: "+ short_hit +". CdN: " + SHORT_UTT_CND_STATICS + ", cnd hit: "+ short_cnd_hit);
            System.out.println("Number of long utts: " + LONG_UTT_STATICS + ", long hit: "+ long_hit +". CdN: " + LONG_UTT_CND_STATICS + ", cnd hit: "+ long_cnd_hit);
//            System.out.println("4: " + hits4);
//            System.out.println("5: " + hits5);
//            System.out.println("6: " + hits6);
//            System.out.println("7: " + hits7);
//            System.out.println("8: " + hits8);
//            System.out.println("9: " + hits9);
//            System.out.println("10: " + hits10);
//            System.out.println($prevIn4Count);
    //        System.out.println("yes count: "+ yes_count +", ir count: "+ i_r_count + "qm: " +qm_count);
        }
    }
    
//    ==========================================calculation methods==========================================
        /**
             * m2w : modified xin's method, This function deals with utterance length under 5
             * added judging and doing confirmation & deny short type utils.
             * @param index
             * @last 3/21/11 1:44 PM
             */
        private void shortUtterance(int index){
                boolean found = false;
                Utterance utt = utts.get(index);
                String utt_content = utt.getContent().toLowerCase();
//                System.out.println("sht_content" + utt_content);
                String curr_speaker = utt.getSpeaker().toLowerCase();
                ArrayList<Integer> pre_utt_list = new ArrayList<Integer>();	// saves previous turn numbers

                //m2w: first judge the utterance, see if it's "confirmation & deny and short" 3/22/11 5:26 PM
                if(!found && judgingCnDSht(utt_content, utt)){
                    found = calCnDSht(index,found);
                    SHORT_UTT_CND_STATICS++;
                }

                //m2w : calculate if there are similar words in curr and prev utts.//4/10/11 8:37 AM
                if (!found){
//                    found = calSimilarity(index, SHORT_SIM_LOOK_BACK, found);
                    found = this.checkWordSimilarity(index, found, "Short: check word similarity in shortUtterance");//4/10/11 8:37 AM
                }

                //m2w: code 11 put find name after calCnDsht. 3/28/11 2:58 PM
                //m2w: check if there is same speaker which is appeared in curr utt. 4/12/11 8:48 AM
                if(!found){
                    found = this.findNamePart1(index, found, "Short: findNamePt1 in shortUtterance");
                }

                //m2w: no name matches, start looking for ? question mark 4/12/11 8:53 AM
                if(!found)
                {
                    for(int i=1; i<=SHORT_THRESHOLD_QUESTION; i++)
                    {
                        Utterance utterance = null;

                        if(index >= i)
                        {

                            utterance = utts.get(index-i);
                            if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
                            {
                                String pre_content = utterance.getContent();
                                pre_utt_list.add(Integer.parseInt(utterance.getTurn()));
                                if(pre_content.indexOf('?')!=-1)
                                {
                                        found = true;
                                        this.setRespTo(index, i, utt, "Short: Question Mark");
                                        break;
                                }
                            }
                        }
                    }
                }
                
                if(!found && index > 0){
                    //excluded res-to and prev utt length < 2 in long utts default to prev utt. 4/20/11 2:12 PM
                    int to = 1;
                    this.setRespTo(index, to, utt, "Short: Default Link To Prev _ new");
                }
        }


        /**
         * m2w : modified xin's method. This function deals with utterance length over 5
         * @param index
         */
        private void longUtterance(int index)  {
                boolean found = false;
                Utterance utt = utts.get(index);
//    		String cur_content = contentExtraction(utts.get(index));
                String cur_raw_content = utts.get(index).getContent();
                String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
    //		TreeMap<Double,Integer> sims = new TreeMap<Double,Integer>();
                ArrayList<Integer> pre_utt_turn = new ArrayList<Integer>();

//                m2w: 4/9/11 11:06 PM
                if(!found && judgingCnDLong(cur_raw_content, utt)){
                    found = calCnDLong(index,found);
                    LONG_UTT_CND_STATICS++;
                }
                if(!found){
//                    found = this.calUttSimilarity(index, LONG_UTT_SIM_LOOK_BACK, found, "Long: utt sim in longUtterance");
                }
                //m2w : find name maybe after the calsim 4/9/11 10:41 PM
                //m2w: check if there is same speaker which is appeared in curr utt. 4/12/11 9:03 AM
                if(!found){
                    found = this.findNamePart1(index, found, "Long: findNamePt1 in longUtterance");
                }
                //m2w: put the check word similarity in the long utts. 4/9/11 10:29 PM
                if (!found){
                    found = this.checkWordSimilarity(index, found, "Long: check word similarity in longUtterance");
                }
                //m2w : added calSim method 4/9/11 10:41 PM
//                if(!found){
//                    found = this.calUttSimBySynon(index, LONG_SIM_LOOK_BACK, LONG_SYN_SIM_THRESH, found, "Long: utt similarity by synon in longUtterance");
//                }

                // find question, code 22
                if(!found)
                {
                        for(int i=1; i<=LONG_THRESHOLD_QUESTION; i++)
                        {
                                if(index >= i)
                                {
                                        Utterance utterance = utts.get(index-i);
                                        if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
                                        {
                                                // calculate similarity
                                                // String pre_content = contentExtraction(utterance);
                                                // double sim = Util.compareUtts(pre_content, cur_content);
                                                int pre_turn_no = (Integer.parseInt(utts.get(index).getTurn()))-i;
//                                                pre_utt_turn.add(pre_turn_no);
                                                // sims.put(sim, pre_turn_no);

                                                // if ? found, just use this utterance
                                                String pre_raw_content = utterance.getContent();
                                                if(pre_raw_content.indexOf('?')!=-1)
                                                {
                                                        found = true;
//                                                        map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i));
//                                                        String SysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i);
//                                                        evaluate(index,Integer.parseInt(utts.get(index).getTurn())-i,"Long: Question Mark");
//                                                        utt.setRespTo(SysRespTo);
                                                        this.setRespTo(index, i, utt, "Long: Question Mark");
                                                        break;
                                                }
                                        }
                                }
                        }

//                        if(pre_utt_turn.size() == 0)
//                                pre_utt_turn.add(Integer.parseInt(utts.get(index).getTurn())-(LONG_THRESHOLD + 1));
                }

                // default to previous one, code 23
                // m2w : added index > 0 for index = 0 ; 4/21/11 9:20 AM
                if(!found && index > 0){
                    //excluded res-to and prev utt length < 2 in long utts default to prev utt. 4/20/11 2:12 PM
                    int to = 1;
                    //now is 4, changed while to for.
                    for(int i = 1; i < LONG_SET_AS_DEFAULT_LOOK_BACK; i ++){
                        if(index - i > 1){
                            Utterance to_utt = utts.get(index - i);
                            if(to_utt.getCommActType().equals("response-to") && this.lengthCal(to_utt) < 2 ){
                                continue;
                            }else{
                                to = i;
                                break;
                            }
                        }
                    }

                    this.setRespTo(index, to, utt, "Long: Default Link To Prev _ new");
                }


        }
//    ==========================================rank calculation methods==========================================

        /**
         * m2w: calculate and set res-to by calculating 10/9 previous utt's rank(how much is it likely to be the correct res-to).
         * @param index 
         */
        private void shortUttRank(int index){
            ArrayList<ArrayList> list = buildRankList(index);
            Utterance currUtt = utts.get(index);
//            System.out.println("testing: in short utt rank");
            //if curr utt is the first. thus list is empty.
            if(!list.isEmpty()){
//                System.out.println("testing: in short utt rank, list is not empty");
                list = calRankShort(list, index);
//                for(int i = 0; i < list.size(); i++){
//                    if((Integer)list.get(i).get(2) > 0){
//                        System.out.print(list.get(i).get(0) + " ");
//                        System.out.print(((Utterance)list.get(i).get(1)).getContent() + " " );
//                        System.out.println(list.get(i).get(2));
//                    }
//                }
//                System.out.println("testing: in short utt rank, after cal rank short");
                int res_to = 0;
                res_to = getHighestRankTN(list, index);
//                System.out.println("testing: in short utt rank, after get highest rank turn number");
//                Utterance currUtt = (Utterance)list.get(0).get(1);
//                System.out.println( " res_to: " + res_to + "rank : "+ list.get(0).get(2));
                int rank = (Integer)list.get(0).get(2);
                this.setResToTN(index, res_to, currUtt, "Short Rgetank",rank);
//                System.out.println("testing: in short utt rank, after set res-to");
                
            }//closes if is empty.
        }//closes method.
        
        /**
         * m2w: build the rank list for previous utts. ArrayList<ArrayList>
         * index 0 : the "i" , index - i = which previous utt.
         * index 1 : the i'th previous utt.
         * index 2 : the rank of this previous utt.
         * @param index
         * @return the rank list
         * @date 8/23/11 11:31 AM
         */
        private ArrayList<ArrayList> buildRankList(int index){
            ArrayList<ArrayList> list = new ArrayList<ArrayList>();
            //build list.
            for(int i = 1; (index - i > 0) && (i < 10); i++){
                Utterance prev_utt = utts.get(index - i);
                int init_rank = 0;
                ArrayList sub_list = new ArrayList();
                sub_list.add(i);
                sub_list.add(prev_utt);
                sub_list.add(init_rank);
                if(!sub_list.isEmpty()) list.add(sub_list);
            }
            return list;
        }
        
        
        /**
         * m2w: calculate the ranks of each previous utts and return the list.
         * @param list
         * @param index
         * @return 
         */
        private ArrayList<ArrayList> calRankShort(ArrayList<ArrayList> list, int index){
            Utterance curr_utt = utts.get(index);
            list = this.calRankUtilFindName(curr_utt, list);
            list = this.calRankUtilWordSim(curr_utt, list);
            list = this.calRankUtilCaseMatching(curr_utt, list);
            return list;
        }
        
        private ArrayList<ArrayList> calRankLong(ArrayList<ArrayList> list, int index){
            Utterance curr_utt = utts.get(index);
            return list;
        }
        
        /**
         * m2w: get the highest rank turn number form the list.
         * 1. sort the list in descending order according to the list's index 2 entry's value.
         * 2. get the highest ranked turn and its turn number.
         * @param list
         * @return 
         * @date 8/23/11 11:15 AM
         */
        private int getHighestRankTN(ArrayList<ArrayList> list, int index){
            int highestRankTN = 0;
            Collections.sort(list, new Comparator(){
                @Override
                public int compare(Object ob1, Object ob2){
                    int o1Rank = 0;
                    int o2Rank = 0;
                    o1Rank = (Integer)(((ArrayList)ob1).get(2));
                    o2Rank = (Integer)(((ArrayList)ob2).get(2));
                    //descending order
                    return o2Rank - o1Rank;
                }
            });
            if((Integer)(list.get(0).get(2)) < 2){
                return Integer.parseInt(utts.get(index - 1).getTurn());
            }
            Utterance highestUtt = (Utterance)list.get(0).get(1);
            highestRankTN = Integer.parseInt(highestUtt.getTurn());
            return highestRankTN;
        }
          
        /**
         * 
         * @param curr_utt
         * @param list
         * @return 
         */
        private ArrayList<ArrayList> calRankUtilFindName(Utterance curr_utt, ArrayList<ArrayList> list){
            int rank = 0;
            String curr_speaker = curr_utt.getSpeaker().toLowerCase();
            String cur_content = contentExtraction(curr_utt);
            String cur_raw_content = curr_utt.getContent().toLowerCase();
            
            
            String name = "";
            boolean name_found = false;
            boolean link_found = false;
            
            //look for if there's any speaker name in cur utterance;
            StringTokenizer tokenizer = new StringTokenizer(cur_content);
            while(tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    //arraylist for getting name in the list. 4/3/11 3:06 PM
                    ArrayList<String> nameList = new ArrayList<String>(speaker_names);
                    //nameList = (ArrayList)Arrays.asList(speaker_names.toArray());
                    for (int i = 0; i < nameList.size(); i ++ ){
                        if(token.length() > 2 && nameList.get(i).contains(token.toLowerCase())){ // added token length > 2 , 4/16/11 3:51 PM
                            name_found = true;
                            name = nameList.get(i).toLowerCase(); // this is the name in the map/arraylist, so no need to "contains" getting name matching. 4/3/11 10:09 PM
                            break;
                        }
                    }   
            }//closes while has more tokens
            
            //if there is a name in curr utt.
            if(name_found){
                for(int i = 1; i<list.size(); i++){
                Utterance prev_utt = (Utterance)(list.get(i).get(1));
                int iRank = (Integer)(list.get(i).get(0));
                String preSpkName = prev_utt.getSpeaker().toLowerCase();
                //if prev utt speaker's name matches the name we found in curr utt, rank + 5.
                    if(!preSpkName.equalsIgnoreCase(curr_speaker)){
                        
                        //added conventional-opening , hi some one case. 4/18/11 1:09 PM
                        if (curr_utt.getTag().toLowerCase().contains("opening") || cur_raw_content.contains("hi ")){
                            if((this.lengthCal(prev_utt) > 1) && preSpkName.equals(name) && this.lengthCal(prev_utt) > 1 && prev_utt.getTag().toLowerCase().contains("opening") ){
                                //rank + 6
                                iRank += 6;
                                list.get(i).set(0, iRank);
                            }
                        }else if(preSpkName.equals(name) && this.lengthCal(prev_utt) > 1){
                            //rank + 5
                            iRank += 6;
                            list.get(i).set(0, iRank);
                        }//ends if same name has found!
                    }//ends same name check
                }//ends outter for loop
            }//ends if name is found  
            
            return list;
        }
        
        private ArrayList<ArrayList> calRankUtilWordSim(Utterance curr_utt, ArrayList<ArrayList> list){

            String curr_speaker = curr_utt.getSpeaker().toLowerCase();
            String cur_content = contentExtraction(curr_utt);
            //m2w : look back ,loop through several utts, now set to 3, 3/28/11 1:19 PM
            outterFor:
            for(int i = 0; i<list.size(); i++){
                Utterance prev_utt = (Utterance)(list.get(i).get(1));
                String pre_content = contentExtraction(prev_utt);
                if(!prev_utt.getSpeaker().equalsIgnoreCase(curr_speaker)){
                    //convert the 2 utts' contents into arrays, check if they have the same word
                        
                        //changed to uttToWdsChinese 8/9/11 2:37 PM
                        //1. get the arraylist-of-arraylist of strings. (each word as an entry, chinese at index0, english at index1)
                        //2. if the curr list and prev list != empty.
                        //3. get list from map, check isempty before do.
                        //4. if English !isEmpty(), do chinese first. find identical words. 
                        //5. if Chinese !isEmpty(), do english, find similar words.
                        HashMap<String, ArrayList<String>> tempCurrUttMap = Util.uttToWdsChinese(cur_content);
                        HashMap<String, ArrayList<String>> tempPrevUttMap = Util.uttToWdsChinese(pre_content);
                        
                        //2.
                        if(!tempCurrUttMap.isEmpty() && !tempPrevUttMap.isEmpty()){
                            //3.
                            ArrayList<String> tempCurrCNList = tempCurrUttMap.get("CN");
                            ArrayList<String> tempCurrENList = tempCurrUttMap.get("EN");
                            ArrayList<String> tempPrevCNList = tempCurrUttMap.get("CN");
                            ArrayList<String> tempPrevENList = tempCurrUttMap.get("EN");
                            
                            //4.
                            if(tempCurrENList != null && !tempCurrENList.isEmpty()
                                    && tempPrevENList != null && !tempPrevENList.isEmpty()){
                                int swHit = 0;
                                for(int x = 0; x < tempCurrENList.size(); x++){//index utt loop
                                    for(int y = 0; y < tempPrevENList.size(); y++){//pre utt loop
                                        //both prev and curr longer than 6, will do the check or else do nothing.
                                        //threshold is now set to 5. 4/16/11 3:16 PM
                                        if(tempCurrENList.get(x).length() > 5 && tempPrevENList.get(y).length() > 5 - 2 ){//now set to 5 and 3 4/16/11 3:31 PM
                                            String tempCurrWord = tempCurrENList.get(x);
                                            String tempPrevWord = tempCurrENList.get(y);
                                            ArrayList<Character> tempCurrWordList = new ArrayList<Character>();
                                            ArrayList<Character> tempPrevWordList = new ArrayList<Character>();
                                            
                                            for(char value: tempCurrWord.toCharArray()){
                                                tempCurrWordList.add(value);
                                            }                                    
                                            for(char value: tempPrevWord.toCharArray()){
                                                tempPrevWordList.add(value);
                                            }
                                            
                                            int charHit = 0;
                                            //loop through the smaller one of the 2 wordlist
                                            for(int m = 0; m < Math.min(tempCurrWordList.size(), tempPrevWordList.size()); m++){
                                                    Character tempCurrChar = tempCurrWordList.get(m);
                                                    Character tempPrevChar = tempPrevWordList.get(m);
                                                    if(tempCurrChar.equals(tempPrevChar)){
                                                        charHit++;
                                                    }//ends if char same
                                            }//ends word list for loop
                                            //calculate the similarity of 2 words.
        //                                    Double wordSim = (double)charHit / (double)tempCurrWordList.size();
                                            //m2w: changed to min of the 2. 4/20/11 11:03 AM
                                            Double wordSim = (double)charHit / (double)Math.min(tempCurrWordList.size(), tempPrevWordList.size());
                                            if(wordSim >= WORD_SIM_THRESHOLD){
                                                //is now set to 0.7 . 4/16/11 3:16 PM
                                                swHit++;
                                            }
                                        }// ends if curr and prev word longer than 6 chars
                                    }//ends prev utt for loop
                                }//ends curr utt for loop

                                //excluded prev_utt com_act_type is response-to. 4/16/11 3:30 PM
                                if(swHit > 0 && !prev_utt.getCommActType().toLowerCase().contains("response-to")){
                                    //rank + 2
                                    this.increaseRank(list.get(i), 2);
                                }//ends if hit > 0;
                            }//closes 4. if english not empty
                            
                            //5.
                            if(tempCurrCNList != null && tempCurrCNList.size() > 1 //2 consecutive words, 8/10/11 2:46 PM
                                    && pre_content != null){
                                //using 2 consecutive words as searching criteria, parse through the current list and previous list.
                                for(int curIndex=1; curIndex<tempCurrCNList.size(); curIndex++){
                                    String tempCurSubStr = tempCurrCNList.get(curIndex) + tempCurrCNList.get(curIndex - 1);
                                    //if prev utt contains curr sub string.
                                    if(pre_content.contains(tempCurSubStr)){
                                        this.increaseRank(list.get(i), 2);
                                    }//close if conatins curr sub string
                                }//closes CN for loop.
                            }//closes 5. CN
                        }//closes check if CN & EN list is empty
                }//ends same name check
            }//ends look back for loop


            return list;
        }//ends method
        
        private ArrayList<ArrayList> calRankUtilCaseMatching(Utterance curr_utt, ArrayList<ArrayList> list){
            Utterance utt = curr_utt;
            String curr_speaker = curr_utt.getSpeaker().toLowerCase();
            String cur_content = contentExtraction(curr_utt).toLowerCase();
            String cur_raw_content = curr_utt.getContent().toLowerCase();
            
            //flow controling:
            boolean firstLol = true;
            boolean firstExactly = true;
            boolean firstGoodPoint = true;
            //m2w : look back ,loop through several utts, now set to 3
            for(int i = 0; i<list.size(); i++){
                Utterance prev_utt = (Utterance)(list.get(i).get(1));
                    //m2w: if prev_utt has the same speaker,skip， use the condition here, put the new condition in the skip method. 3/23/11 2:31 PM
                    if(!prev_utt.getSpeaker().equalsIgnoreCase(curr_speaker)){

                        String pre_raw_content = prev_utt.getContent().toLowerCase();
                        String pre_content = contentExtraction(prev_utt).toLowerCase();


//                      ===== m2w : current utt contains matching cases. 4/4/11 2:41 PM ======
                        //m2w: yes and D[agree-accept] && prev is C[addressed-to]D[Information-Request] and contains ? , then set.4/18/11 9:07 AM
                        if((cur_content.startsWith("yes")  || cur_content.startsWith("对")  || cur_content.startsWith("恩")  || cur_content.startsWith("好")  || cur_content.startsWith("是")  || cur_content.startsWith("没错"))
                                && this.lengthCal(utt) < 2 && utt.getTag().contains("agree-accept")
                                && pre_raw_content.contains("?") 
//                                && prev_utt.getTag().toLowerCase().contains("information-request")
                                ){
                            //rank + 2
                            this.increaseRank(list.get(i), 2);
                        }

                        //m2w: if curr and prev both contains "haha", should be laughing at the same utt.
                        //chinese : skip prev_utt if prev_utt also is "lol", then add 2 to the first prev_utt which is not lol, and only do it once.
                        //put haha to 2nd of case matching. 4/18/11 1:01 PM
                        if((firstLol) 
                                && (cur_content.contains("hah") || cur_raw_content.contains("lol") || cur_raw_content.contains("哈哈")) 
                                //changed the lengthcal to > 3. 4/18/11 10:01 AM
                                && !(pre_content.contains("hah")|| pre_content.contains("lol") || cur_raw_content.contains("哈哈"))
                                //added prev_utt length > 1. 4/18/11 11:21 AM
                                && this.lengthCal(prev_utt) > 1
                                ){
                            //rank + 2
                            this.increaseRank(list.get(i), 2);
                            firstLol = false; // do + 2 once.
                        }

                        //m2w: sure and can you ? wanna ? case 4/18/11 1:41 PM
                        if((cur_raw_content.equals("sure") || cur_raw_content.contains("当然")  || cur_raw_content.contains("没问题")  )
//                                && utt.getTag().equals("--Response-Answer")
//                                && prev_utt.getTag().equals("Information-Request")
                                && pre_raw_content.contains("?")
                                ){
                            //rank + 2
                            this.increaseRank(list.get(i), 2);
                        }
                        
                        //m2w: code 34, too, neither case 3/24/11 1:56 PM
                        //chinese: curr_utt contains "也", prev_utt is some statement starts with "我". 8/24/11 11:10 AM
                        if(((cur_content.contains(" too") || cur_content.contains(" neither")) && (
                                pre_content.contains("not ")
                                || pre_content.contains("nt ")
                                || pre_content.contains("id be")
                                || pre_content.contains("i would be")
                                ))
                                ||
                                (cur_content.contains("也") && (
                                pre_content.contains("没有")
                                || pre_content.startsWith("我")
                                ))
                                ){
                            //rank + 1
                            this.increaseRank(list.get(i), 1);
                            
                        }
                        
                        //m2w: code 32, rhetorical question case. if in previous 3 utts contains “?” && “ don't dont isn't wasn't hasn't weren't” , FOUND! 3/22/11 3:50 PM
                        //chinese： 反问句. 
                        if((pre_raw_content.contains("?")) &&
                                        !(pre_content.contains("know ")) &&
                                        ((pre_content.contains("dont ")
                                        || pre_content.contains("isnt ")
                                        || pre_content.contains("wasnt ")
                                        || pre_content.contains("hasnt ")
                                        || pre_content.contains("havnt ")
                                        || pre_content.contains("werent ")
                                        || pre_content.contains("didnt ")
                                        || pre_raw_content.contains("right?")
                                        ) || (
                                            pre_content.matches(".*不[\\u4E00-\\u9FA5]?吗.*")// 不是吗
                                            || pre_content.matches(".*没[\\u4E00-\\u9FA5]?吗.*")//
                                            || pre_content.matches(".*难道[\\u4E00-\\u9FA5]+吗.*")//
                                            || pre_content.matches(".*怎么[\\u4E00-\\u9FA5]+呢.*")
                                ))){
                            //rank + 3
                            this.increaseRank(list.get(i), 3);
                       }

//                        //m2w : code 35, wow case, wow about something, look for "i", 3/24/11 11:17 AM
////                        if((cur_raw_content.contains("wow ") || cur_raw_content.contains(" wow") || cur_raw_content.equals("wow")) && (
//                        if((cur_raw_content.contains("wow") && (
//                                //m2w : added exclaimation mark! 4/18/11 9:53 AM
//                                (pre_raw_content.contains("i ") || pre_raw_content.contains("!"))
//                                )) 
//                                || cur_raw_content.contains("哇")
//                                ){
//                                found = true;
//                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: wow");
//                                break;
//
//                        }
                        
                        //m2w : code 36, agree on some one's denying about something, look for don't. 3/24/11 11:17 AM
                        if(((cur_raw_content.contains("nt") || cur_raw_content.contains("n't")) && cur_raw_content.contains("either"))
//                                ||  ((cur_raw_content.contains("没") || cur_raw_content.contains("不")|| cur_raw_content.contains("赞")|| cur_raw_content.contains("反对")) && cur_raw_content.contains("也")) 
                                || (cur_content.contains("我也") || utt.getTag().toLowerCase().contains("agree"))
                                ){
                            if (pre_content.contains("dont") || 
//                                    (cur_raw_content.contains("没") || cur_raw_content.contains("不")|| cur_raw_content.contains("赞")|| cur_raw_content.contains("反对"))
                                    (pre_content.contains("我不赞")
                                    || pre_content.contains("我反对")
                                    || prev_utt.getTag().toLowerCase().contains("disagree")
                                    )){
                                //rank + 2
                                this.increaseRank(list.get(i), 2);
                            }
                        }

                        //m2w: code 39, it's ok case, look for "sorry" in previous utts 4/1/11 3:10 PM
                        if((cur_raw_content.contains("'s ok")
                                || cur_raw_content.contains("'s okay")
                                || cur_raw_content.contains("s ok")
                                || cur_raw_content.contains("s okay")
                                || cur_raw_content.contains("没事")
                                || cur_raw_content.contains("没关系")
                                
                                ) && (pre_content.contains("sorry") 
                                        || pre_content.contains("对不起") 
                                        || pre_content.contains("不好意思") 
                                        )){
                                //rank + 3
                            this.increaseRank(list.get(i), 3);
                        }

//                        //m2w: code 310, sorry check. if last utt has sorry too, link to last's link_to ,4/3/11 11:38 AM
//                        if((cur_raw_content.contains("sorry") && pre_raw_content.contains("sorry") && prev_utt.getSysRespTo() != null)){
//                            found = true;
//                            String pre_SysRespTo = prev_utt.getSysRespTo();
//                            this.setRespToAs(index, pre_SysRespTo, utt, "Short: cnd: CaseMatching_cur_utt: sorry same prev utt");
//                            break;
//                        }
//                        4/16/11 4:01 PM

                        //m2w: code 311, exactly case, check its last utt, then perform skip judge and link to prev utts 4/1/11 3:10 PM
                        //chinese: if curr and prev utt both contains exactly, then look for prev-utt's res-to see if that utt is in the list, 
                        //if is in the list ,then add 2 to its rank.
                        if((cur_raw_content.toLowerCase().contains("exactly")) 
                                || cur_raw_content.contains("没错") || cur_raw_content.contains("对") || cur_raw_content.contains("就是")
                                ){
                            //checking last utt,
                            if((pre_content.contains("exactly") 
                                    || cur_raw_content.contains("没错") || cur_raw_content.contains("对") || cur_raw_content.contains("就是")
                                    ) && prev_utt.getSysRespTo() != null){ // gai
                                String pre_SysRespTo = prev_utt.getSysRespTo();
                                String pre_SysRespToTN = pre_SysRespTo.split(":")[1];
                                for(int x=i; x<list.size(); x++){
                                    Utterance tempUtt = (Utterance)(list.get(x).get(1));
                                    if(tempUtt.getTurn().equals(pre_SysRespToTN)){
                                        //rank + 2
                                        this.increaseRank(list.get(x), 2);
                                    }
                                }//closes looking for prev utt's res-to and add 2 to that utt's rank.
                            }//closes prev utt contains exactly too.                        
                            else{
                                if(firstExactly){
                                    //if didn't find, increase last utt 1 once.
                                    this.increaseRank(list.get(i), 1);
                                    firstExactly = false;
                                }
                            }
                        }//closes if curr utt contains exactly

                        //m2w: code 312, good point case. look for "?" and/or "why",4/3/11 11:38 AM
                        if(cur_raw_content.toLowerCase().contains("good point") || cur_raw_content.toLowerCase().contains("good question")
                                || cur_raw_content.contains("说的好")|| cur_raw_content.contains("问的好")|| cur_raw_content.contains("有道理")
                                ){
                            //if previous utt also fits the case, then set to prev's link_to
                            if(( pre_raw_content.contains("good point") || pre_raw_content.contains("good question")
                                    || cur_raw_content.contains("说的好")|| cur_raw_content.contains("问的好")|| cur_raw_content.contains("有道理")
                                    ) && prev_utt.getSysRespTo() != null){
                                String pre_SysRespTo = prev_utt.getSysRespTo();
                                String pre_SysRespToTN = pre_SysRespTo.split(":")[1];
                                for(int x=i; x<list.size(); x++){
                                    Utterance tempUtt = (Utterance)(list.get(x).get(1));
                                    if(tempUtt.getTurn().equals(pre_SysRespToTN)){
                                        //rank + 2
                                        this.increaseRank(list.get(x), 2);
                                    }
                                }//closes looking for prev utt's res-to and add 2 to that utt's rank.                            
                                // if not , look for ? or why, then set.
                            }else if(pre_content.contains("?") || pre_content.contains("why")
                                        || pre_content.contains("为什么")|| pre_content.contains("怎么")
                                        ){
                                    this.increaseRank(list.get(i), 2);
                            }else{
                                if(firstGoodPoint){
                                    this.increaseRank(list.get(i), 1);
                                    firstGoodPoint = false;
                                }
                            }
                        }

                        

                         //m2w : i agree can't be responsing to a question. 3/24/11 11:17 AM
//                        if(cur_content.contains("agree") && pre_raw_content.contains("?")){
//                                    continue;
//                                }
                         //m2w : curr contains true, and previous shouldn't contains should or true, 3/24/11 11:17 AM
//                        if(cur_content.contains("true") && (pre_content.contains("should") || pre_content.contains("true"))){
//                                    continue;
//                                }




        
//                        ===== m2w : previous utt contains matching cases. 4/4/11 2:41 PM ======
                        //m2w : code 33 , certain types of conditions, if fits, set link_to, set to the utt; 4/1/11 1:52 PM
                        //m2w: split to several cases. since 4/4/11 2:48 PM.
                        //m2w: chinese: donesn't check curr utt anymore, if prev utt contains these, rank +;


                        if(lengthCal(prev_utt) >= 3){

                            //m2w: guessing case
                            if(pre_content.contains("might be") || pre_content.contains("可能")
                                    || pre_content.contains("might have") || pre_content.contains("也许")
                                    || pre_content.contains("could be") || pre_content.contains("应该")
                                    || pre_content.contains("could have")   || pre_content.contains("如果")
                                    || pre_content.contains("may be")   || pre_content.contains("要是")
                                    || pre_content.contains("maybe")    || pre_content.contains("像是")
                                    || pre_content.contains("may have") || pre_content.contains("好像")
                                    || pre_content.contains("perhaps")  || pre_content.contains("或许")
                                    || pre_content.contains("probably") || pre_content.contains("似乎")
                                    || pre_content.contains("seems")
                                    || pre_content.contains("seem")
                                    || (pre_content.contains("either") && pre_content.contains("or"))
                                    || (pre_content.matches(".*不是.*就是.*"))
                                    
                                    ){
                                //rank + 2.
                                this.increaseRank(list.get(i), 2);
                                

                                //m2w: degree case
                            }else if (pre_content.contains("really")            || pre_content.contains("真的")
                                    || pre_content.contains("definatly")        || pre_content.contains("肯定")
                                    || pre_content.contains("especially")       || pre_content.contains("特别是")
                                    || pre_content.contains("apparently")       || pre_content.contains("明显")
                                    || pre_content.contains("regardless")       || pre_content.contains("无论")
                                    || pre_content.contains("some how")         || pre_content.contains("最多")
                                    || pre_content.contains("somehow")          || pre_content.contains("全部")
                                    || pre_content.contains("most")             || pre_content.contains("最少")
                                    || pre_content.contains("at all")           || pre_content.contains("只要")
                                    || pre_content.contains("at least")         || pre_content.contains("只有")
                                    || pre_content.contains("as long as")       
                                    || pre_content.contains("only")             
                                    || pre_content.contains("best")             || pre_content.contains("最")
                                    || pre_content.contains("worst")            || (pre_content.contains("太") && !pre_content.contains("太阳") && !pre_content.contains("太极")&& !pre_content.contains("太平"))
                                    || pre_content.contains("est ") // 4/5/11 12:00 PM 
                                    || pre_raw_content.contains("it's so ") || pre_raw_content.contains("its so ") || pre_content.contains("那么的")
                                    || pre_raw_content.contains("that's so ") || pre_raw_content.contains("thats so ")
                                    || pre_raw_content.contains("are so ") || pre_raw_content.contains("r so ")                                    
                                    ){
                                //rank + 2
                                this.increaseRank(list.get(i), 2);
                                
                                //m2w: opinion case
                            }else if ((pre_content.contains("i think")          || pre_content.matches(".*我.*想.*")    
                                    || pre_raw_content.contains("i'm thinking") || pre_raw_content.contains("im thinking")
                                    || pre_content.contains("i was thinking")   
                                    || pre_content.contains("i believe")        || pre_content.matches(".*我.*相信.*")    
                                    || pre_content.contains("i guess")          || pre_content.contains("我猜")
                                    || pre_content.contains("because")          || pre_content.contains("因为")
                                    || pre_content.matches(".*对.*?.*")
                                    || pre_raw_content.endsWith("right?")) && (!pre_content.contains("why i think"))
                                    ){
                                    //rank + 2
                                this.increaseRank(list.get(i), 2);
                                
                                //m2w: order & propossal case
                            }else if (pre_content.contains("how about")         || pre_content.contains("要不")  || pre_content.contains("不如")|| pre_content.contains("不然")
                                    || pre_content.contains("need to")          || pre_content.contains("必须") 
                                    || pre_content.contains("should be")        || pre_content.contains("我得")  || pre_content.contains("他得") || pre_content.contains("她得") || pre_content.contains("你得") 
                                    || (pre_content.contains("should") && pre_raw_content.contains("?"))        || pre_content.matches(".*能.*?.*")
                                    || pre_content.contains("we can")           || pre_content.contains("们能")  || pre_content.contains("们可以")  
                                    || pre_content.contains("you may")          || pre_content.contains("你能")  || pre_content.contains("你可以") 
                                    // 4/5/11 12:02 PM
                                    || pre_content.contains("u may") 
                                    //4/5/11 12:02 PM
                                    || pre_content.contains("you should")       || pre_content.contains("应该")
                                    //4/5/11 12:02 PM
                                    || pre_content.contains("u should") 
                                    || pre_raw_content.contains("shouldn't")    || pre_content.contains("不应该")
                                    || pre_raw_content.contains("should've")
                                    || (pre_content.contains("would you") && pre_raw_content.contains("?"))     || pre_content.matches(".*可以.*?.*")
                                    || (pre_content.contains("could you") && pre_raw_content.contains("?"))     || pre_content.matches(".*可否.*?.*")
                                    || (pre_content.contains("can you") && pre_raw_content.contains("?"))
                                    ){
                                    //rank + 2
                                    this.increaseRank(list.get(i), 2);
                                
                                //m2w: other case
                            }else if (pre_content.contains("sometimes")         || pre_content.contains("有时候")
                                    || pre_content.contains("somewhere")        || pre_content.contains("有的地方")
                                    || pre_content.contains("everyone")         || pre_content.contains("所有人") || pre_content.contains("大家")
                                    || pre_content.contains("anything")         || pre_content.contains("任何")|| pre_content.contains("所有") || pre_content.contains("任意")
                                    || pre_content.contains("that's why")       || pre_content.contains("这就是为什么")
                                    || pre_content.contains("anything can")     || pre_content.contains("所以")
                                                                                || pre_content.contains("不一定")
                                    || pre_raw_content.contains("it ") && pre_raw_content.contains(" depend")
                                    ){
                                    //rank + 1
                                    this.increaseRank(list.get(i), 1);
                                    
                                //m2w: prev_utt contains cnd key words, if there isn't any thing to match, check this then pass down
                            }else if ((pre_content.contains("yes ") 
                                    || (pre_content.contains("yea ") && !pre_content.contains("year"))
                                    || (pre_content.contains("true ") && !pre_content.contains("be true"))
                                    || pre_content.contains("agree ")
                                    || pre_content.contains("exactly ")
                                    || pre_content.equals("是") || pre_content.equals("就是") || pre_content.matches(".*对") || pre_content.matches(".*好") || pre_content.contains("没错") || pre_content.contains("恩") 
                                    //excluded "because" put into opinion. 4/5/11 11:54 AM
                                    //edcluded prev_utt 's dialog act is agree-accept. 4/16/11 3:00 PM
                                    ) && !prev_utt.getTag().toLowerCase().contains("agree-accept")){

                                //rank + 1
                                this.increaseRank(list.get(i), 1);
                            }else{
                                //do nothing, continue the loop
                            }//ends all the else ifs
                        }//ends if length cal > 3
                    }//ends if speaker is the same                    
//                }//ends if index > 1
            }//ends for loop
            return list;
        }//ends method
        
        /**
         * m2w: chinese ver: increase the ith previous utt's rank by x.
         * @param subList
         * @param increasement x
         */
        private void increaseRank(ArrayList subList, int increasement){
            int rank = (Integer)(subList.get(2));
            rank += increasement;
            subList.set(2, rank);
        }
//     =====================================confirmation & deny short type util=====================================
        /**
         * m2w: judge the current utterance , see if it's confirmation & deny type
         * check if it contains agree, true, yes, yea,ok , (no sorry, because "it's ok" is a respound to "im sorry", it's not confirmation & deny)
         * @param cur_utterance
         * @return
         */
        private boolean judgingCnDSht(String cur_utterance, Utterance utt){//lowercased unfiltered
            if((cur_utterance.contains("agree")                     || cur_utterance.contains("赞成") || cur_utterance.contains("同意") || cur_utterance.contains("赞同") 
                            || cur_utterance.contains("true")       || cur_utterance.contains("真的") 
                            || cur_utterance.contains("yes")        || cur_utterance.contains("是啊")
                            || cur_utterance.contains("yea")        || cur_utterance.contains("是阿")
                            || cur_utterance.contains("ya ")        || cur_utterance.contains("对 ")    
                            || cur_utterance.equals("ok")           || cur_utterance.equals("好")
                            || cur_utterance.equals("okay")         || cur_utterance.equals("好的")
                            || cur_utterance.contains("ok,")        || cur_utterance.contains("好,")
                            || cur_utterance.contains("ok ")        || cur_utterance.equals("好 ")
                            || cur_utterance.contains("s cool")
                            || (cur_utterance.contains(" too") && !cur_utterance.contains("you too"))
                            || cur_utterance.contains("wow")        || cur_utterance.equals("哇")
                            || cur_utterance.contains("neither")    || cur_utterance.contains("也不")
                            || cur_utterance.contains("sure")       || cur_utterance.contains("当然")
                            || cur_utterance.contains("same ")      || cur_utterance.contains("也是")
                            || (cur_utterance.contains("i ") && (cur_utterance.contains("dont") ||cur_utterance.contains("don't")))
                            || cur_utterance.contains("我不")
                            || (cur_utterance.contains("hah") || cur_utterance.contains("lol") || cur_utterance.contains(":D"))
                            || cur_utterance.contains("哈哈")
                            || (cur_utterance.contains("right") && !cur_utterance.contains("the right") && cur_utterance.contains("?")) // maybe include not right 4/1/11 1:40 PM
                            || cur_utterance.contains("nope") //4/1/11 1:42 PM
                            || cur_utterance.contains("不")         || cur_utterance.contains("没有")
                            || cur_utterance.contains("yep") //4/1/11 1:42 PM
                            || cur_utterance.contains("恩")         
                            || cur_utterance.contains("s ok") //4/1/11 1:42 PM
                            || cur_utterance.contains("没事的")      || cur_utterance.contains("没关系")
                            || cur_utterance.contains("sorry") // 4/1/11 1:42 PM
                            || cur_utterance.contains("对不起")      || cur_utterance.contains("不好意思")
                            || cur_utterance.contains("definately") //4/1/11 1:42 PM
                            || cur_utterance.contains("绝对")      || cur_utterance.contains("肯定")
                            || cur_utterance.contains("disagree")// 4/1/11 1:42 PM
                            || cur_utterance.contains("不赞成")      || cur_utterance.contains("不赞同")      || cur_utterance.contains("不同意")
                            || cur_utterance.contains("make sense")// 4/1/11 1:42 PM
                            || cur_utterance.contains("makes sense")// 4/1/11 1:42 PM
                            || cur_utterance.contains("correct")// 4/1/11 1:42 PM
                            || cur_utterance.contains("没错")
                            || cur_utterance.contains("good point") || cur_utterance.contains("good question") || cur_utterance.contains("valid point")//4/16/11 3:57 PM
                            || cur_utterance.contains("说的好") || cur_utterance.contains("问的好") || cur_utterance.contains("有道理")
                            || cur_utterance.contains("exactly")//4/16/11 3:59 PM
                            || cur_utterance.contains("apparently")//4/16/11 4:36 PM
                            || cur_utterance.contains("明显")
                            || cur_utterance.contains("not ")//4/18/11 12:32 PM
//                            || utt.getTag().toLowerCase().contains("agree-accept")
                            )
//                            && !cur_utterance.contains("sorry")
                    ){
                    return true;
            }else{
                    return false;
            }
        }


        /**
         * m2w : called in the shortUtterance method, to calculate confirmation & deny type short utt
         * 4 steps now :1. findname 2. check the same word code 37 (synon update later) 2. case matching for code 32-36 and code 38 3. skip judge for code 31
         * @param index
         * @param found
         * @return
         */
        private boolean calCnDSht(int index, boolean found){
            //1.find name first, if found, return found, if not, do next
            //m2w: added the that's ok case check if does contains , passes it down. 4/5/11 11:49 AM
            String cur_raw_content = utts.get(index).getContent();
            if (!(cur_raw_content.contains("thats ok") || cur_raw_content.contains("that's ok") || cur_raw_content.contains("its ok") || cur_raw_content.contains("it's ok")
                    || cur_raw_content.contains("good question") || cur_raw_content.contains("good point")
                 )

                    && (found = this.findNamePart1(index, found, "Short: cnd: findNamePt1 in calCndSht"))){
                return found;
            }else if(found = this.checkWordSimilarity(index, found, "Short: cnd: check word similarity in calCndSht")){ //2. check same word, if found, return found, if not, do next
                return found;

                //adding the synonem checkhere
            }else if(found = this.CnDShtCaseMatching(index, found)){//3. case checking, if found return, if not, do next
                return found;
            }else{//4. skipping AFTER the case matching calculation
                found = this.CnDShtSkipJudge(index, found, "Short: cnd: Skip Judge in calCnDSht");
                return found;
            }//ends else
        }//ends method


        /**
         * m2w : this is extracted from the calCnDSht, setting up several certain cases for matching.
         * code 32://m2w: rhetorical question case. if in previous 3 utts contains “?” && “ don't dont isn't wasn't hasn't weren't” , FOUND! 3/22/11 3:50 PM
         * code 33://m2w: certain types of conditions, if fits, set link_to, set to the utt; 4/1/11 1:52 PM
         * code 34://m2w: too, neither case 3/24/11 1:56 PM
         * code 35://m2w: wow case, wow about something, look for "i", 3/24/11 11:17 AM
         * code 36://m2w: deny about something, look for don't. 3/24/11 11:17 AM
         * code 38://m2w: haha and lol case , 3/28/11 1:56 PM
         * modified the method, has been changed it into 2 parts, cur_case and pre_case 4/4/11 4:16 PM
         * @param index
         * @param found
         * @return found
         * @last 3/23/11 3:58 PM - 4/1/11 1:53 PM - 4/4/11 3:54 PM
         */
        private boolean CnDShtCaseMatching(int index, boolean found){
            Utterance utt = utts.get(index);
            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            String cur_content = contentExtraction(utt).toLowerCase();
            String cur_raw_content = utt.getContent().toLowerCase();
            //m2w : look back ,loop through several utts, now set to 3
            for(int i = 1; i<=CONF_SHORT_THRES; i++){
                Utterance prev_utt = null;
                if(index >= i){
                    prev_utt = utts.get(index-i);
                    Utterance utterance = utts.get(index-i);
                    //m2w: if prev_utt has the same speaker,skip， use the condition here, put the new condition in the skip method. 3/23/11 2:31 PM
                    if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker)){

//                        //pre judge
//                        if(this.CnDShtPreSkipJudge(index, i)){
//                           continue;
//                        }
                         
                        String pre_raw_content = prev_utt.getContent().toLowerCase();
                        String pre_content = contentExtraction(prev_utt).toLowerCase();


//                      ===== m2w : current utt contains matching cases. 4/4/11 2:41 PM ======
                        //m2w: yes and D[agree-accept] && prev is C[addressed-to]D[Information-Request] and contains ? , then set.4/18/11 9:07 AM
                        if((cur_content.startsWith("yes")  || cur_content.startsWith("对")  || cur_content.startsWith("恩")  || cur_content.startsWith("好")  || cur_content.startsWith("是")  || cur_content.startsWith("没错"))
                                && this.lengthCal(utt) < 2 && utt.getTag().contains("agree-accept")
                                && pre_raw_content.contains("?") && prev_utt.getTag().toLowerCase().contains("information-request")){
                            found = true;
                            this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: yes / I-R && ?");
                            break;
                        }

                        //m2w: code 38, haha and lol case , 3/28/11 1:56 PM
                        //put haha to 2nd of case matching. 4/18/11 1:01 PM
                        if((cur_content.contains("hah") || cur_raw_content.contains("lol") || cur_raw_content.contains("哈哈")) 
                                
                                && utt.getTag().toLowerCase().contains("acknowledge") && (
                                //changed the lengthcal to > 3. 4/18/11 10:01 AM
                                (pre_content.contains("hah")|| pre_content.contains("lol") || cur_raw_content.contains("哈哈")) 
                                
                                && this.lengthCal(prev_utt) >= 3)
                                //added prev_utt length > 1. 4/18/11 11:21 AM
                                && this.lengthCal(prev_utt) > 1
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: haha");
                                break;
                        }

                        //m2w: sure and can you ? wanna ? case 4/18/11 1:41 PM
                        if((cur_raw_content.equals("sure") || cur_raw_content.equals("当然")  || cur_raw_content.equals("没问题")  )
                                && utt.getTag().equals("--Response-Answer")
                                && prev_utt.getTag().equals("Information-Request") && pre_raw_content.contains("?")
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: sure - ir && ?");
                                break;
                        }
                        
                        //m2w: code 34, too, neither case 3/24/11 1:56 PM
                        if((cur_content.contains(" too") || cur_content.contains(" neither")) && (
                                pre_content.contains("not ")
                                || pre_content.contains("nt ")
                                || pre_content.contains("id be")
                                || pre_content.contains("i would be")
                                )
                                ||
                                (cur_content.contains("也没") || cur_content.contains("也不"))
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: too/neither");
                                break;
                            
                        }
                        
                        //m2w: code 32, rhetorical question case. if in previous 3 utts contains “?” && “ don't dont isn't wasn't hasn't weren't” , FOUND! 3/22/11 3:50 PM
                        if((pre_raw_content.contains("?")) &&
                                        !(pre_content.contains("know ")) &&
                                        ((pre_content.contains("dont ")
                                        || pre_content.contains("isnt ")
                                        || pre_content.contains("wasnt ")
                                        || pre_content.contains("hasnt ")
                                        || pre_content.contains("havnt ")
                                        || pre_content.contains("werent ")
                                        || pre_content.contains("didnt ")
                                        || pre_raw_content.contains("right?")
                                        ) || (
                                            (pre_content.contains("不是吗"))
                                            || (pre_content.contains("不") && pre_content.contains("吗"))
                                            || (pre_content.contains("没有吗"))
                                            || (pre_content.contains("没") && pre_content.contains("吗"))
                                            || (pre_content.contains("不对吗"))
                                ))){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: rhetorical question");
                                break;
                        }

                        //m2w : code 35, wow case, wow about something, look for "i", 3/24/11 11:17 AM
//                        if((cur_raw_content.contains("wow ") || cur_raw_content.contains(" wow") || cur_raw_content.equals("wow")) && (
                        if((cur_raw_content.contains("wow") && (
                                //m2w : added exclaimation mark! 4/18/11 9:53 AM
                                (pre_raw_content.contains("i ") || pre_raw_content.contains("!"))
                                )) 
                                || cur_raw_content.contains("哇")
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: wow");
                                break;

                        }
                        
                        //m2w : code 36, agree on some one's denying about something, look for don't. 3/24/11 11:17 AM
                        if(((cur_raw_content.contains("nt") || cur_raw_content.contains("n't")) && cur_raw_content.contains("either"))
                                ||  ((cur_raw_content.contains("没") || cur_raw_content.contains("不")|| cur_raw_content.contains("赞")|| cur_raw_content.contains("反对")
                                ) && cur_raw_content.contains("也")) ){
                            if (pre_content.contains("dont") || 
                                    (cur_raw_content.contains("没") || cur_raw_content.contains("不")|| cur_raw_content.contains("赞")|| cur_raw_content.contains("反对"))){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: i dont either");
                                break;
                            }else{
                                continue;
                            }

                        }

                        //m2w: code 39, it's ok case, look for "sorry" in previous utts 4/1/11 3:10 PM
                        if((cur_raw_content.contains("'s ok")
                                || cur_raw_content.contains("'s okay")
                                || cur_raw_content.contains("s ok")
                                || cur_raw_content.contains("s okay")
                                || cur_raw_content.contains("没事")
                                || cur_raw_content.contains("没关系")
                                
                                ) && (pre_content.contains("sorry") 
                                        || pre_content.contains("对不起") 
                                        || pre_content.contains("不好意思") 
                                        )){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: it's ok");
                                break;
                        }

//                        //m2w: code 310, sorry check. if last utt has sorry too, link to last's link_to ,4/3/11 11:38 AM
//                        if((cur_raw_content.contains("sorry") && pre_raw_content.contains("sorry") && prev_utt.getSysRespTo() != null)){
//                            found = true;
//                            String pre_SysRespTo = prev_utt.getSysRespTo();
//                            this.setRespToAs(index, pre_SysRespTo, utt, "Short: cnd: CaseMatching_cur_utt: sorry same prev utt");
//                            break;
//                        }
//                        4/16/11 4:01 PM

                        //m2w: code 311, exactly case, check its last utt, then perform skip judge and link to prev utts 4/1/11 3:10 PM
                        if((cur_raw_content.toLowerCase().contains("exactly")) 
                                || cur_raw_content.contains("没错") || cur_raw_content.contains("对") || cur_raw_content.contains("就是")
                                ){
                            //checking last utt,
                            if((pre_content.contains("exactly") 
                                    || cur_raw_content.contains("没错") || cur_raw_content.contains("对") || cur_raw_content.contains("就是")
                                    ) && prev_utt.getSysRespTo() != null){ // gai
                                found = true;
                                String pre_SysRespTo = prev_utt.getSysRespTo();
                                this.setRespToAs(index, pre_SysRespTo, utt, "Short: cnd: CaseMatching_cur_utt: exactly same prev utt");
                                break;
                            }else{
//                                this.CnDShtSkipJudge(index, found, "Short: cnd: CaseMatching_cur_utt: exactly");
                                //debugged in exactly case. 4/18/11 12:42 PM
                                break;
                            }                            
                        }

                        //m2w: code 312, good point case. look for "?" and/or "why",4/3/11 11:38 AM
                        if(cur_raw_content.toLowerCase().contains("good point") || cur_raw_content.toLowerCase().contains("good question")
                                || cur_raw_content.contains("说的好")|| cur_raw_content.contains("问的好")|| cur_raw_content.contains("有道理")
                                ){
                            //if previous utt also fits the case, then set to prev's link_to
                            if(( pre_raw_content.contains("good point") || pre_raw_content.contains("good question")
                                    || cur_raw_content.contains("说的好")|| cur_raw_content.contains("问的好")|| cur_raw_content.contains("有道理")
                                    ) && prev_utt.getSysRespTo() != null){
                                found = true;
                                String pre_SysRespTo = prev_utt.getSysRespTo();
                                this.setRespToAs(index, pre_SysRespTo, utt, "Short: cnd: CaseMatching_cur_utt: good point same prev utt");
                                break;                                
                            }else{
                                // if not , look for ? or why, then set.
                                if(pre_content.contains("?") || pre_content.contains("why")
                                        || pre_content.contains("为什么")|| pre_content.contains("怎么")
                                        ){
                                    this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_cur_utt: good point");
                                }
                            }
                        }

                        

                         //m2w : i agree can't be responsing to a question. 3/24/11 11:17 AM
//                        if(cur_content.contains("agree") && pre_raw_content.contains("?")){
//                                    continue;
//                                }
                         //m2w : curr contains true, and previous shouldn't contains should or true, 3/24/11 11:17 AM
//                        if(cur_content.contains("true") && (pre_content.contains("should") || pre_content.contains("true"))){
//                                    continue;
//                                }




        
//                        ===== m2w : previous utt contains matching cases. 4/4/11 2:41 PM ======
                        //m2w : code 33 , certain types of conditions, if fits, set link_to, set to the utt; 4/1/11 1:52 PM
                        //m2w: split to several cases. since 4/4/11 2:48 PM.


                        if(lengthCal(prev_utt) >= 3){

                            //m2w: guessing case
                            if(pre_content.contains("might be") || pre_content.contains("可能")
                                    || pre_content.contains("might have") || pre_content.contains("也许")
                                    || pre_content.contains("could be") || pre_content.contains("应该")
                                    || pre_content.contains("could have")   || pre_content.contains("如果")
                                    || pre_content.contains("may be")   || pre_content.contains("要是")
                                    || pre_content.contains("maybe")    || pre_content.contains("像是")
                                    || pre_content.contains("may have") || pre_content.contains("好像")
                                    || pre_content.contains("perhaps")  || pre_content.contains("或许")
                                    || pre_content.contains("probably")
                                    || pre_content.contains("seems")
                                    || pre_content.contains("seem")
                                    || (pre_content.contains("either") && pre_content.contains("or"))
                                    || (pre_content.contains("不是") && pre_content.contains("就是"))
                                    
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_prev_utt: guessing");
                                break;

                                //m2w: degree case
                            }else if (pre_content.contains("really")            || pre_content.contains("真的")
                                    || pre_content.contains("definatly")        || pre_content.contains("肯定")
                                    || pre_content.contains("especially")       || pre_content.contains("特别是")
                                    || pre_content.contains("apparently")       || pre_content.contains("明显")
                                    || pre_content.contains("regardless")       || pre_content.contains("无论")
                                    || pre_content.contains("some how")         || pre_content.contains("最多")
                                    || pre_content.contains("somehow")          || pre_content.contains("全部")
                                    || pre_content.contains("most")             || pre_content.contains("最少")
                                    || pre_content.contains("at all")           || pre_content.contains("只要")
                                    || pre_content.contains("at least")         || pre_content.contains("只有")
                                    || pre_content.contains("as long as")       
                                    || pre_content.contains("only")             
                                    || pre_content.contains("best")             || pre_content.contains("最")
                                    || pre_content.contains("worst")            
                                    || pre_content.contains("est ") // 4/5/11 12:00 PM 
                                    || pre_raw_content.contains("it's so ") || pre_raw_content.contains("its so ") || pre_content.contains("那么")
                                    || pre_raw_content.contains("that's so ") || pre_raw_content.contains("thats so ")
                                    || pre_raw_content.contains("are so ") || pre_raw_content.contains("r so ")                                    
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_prev_utt: degree");
                                break;

                                //m2w: opinion case
                            }else if ((pre_content.contains("i think")          || pre_content.contains("我想")    
                                    || pre_raw_content.contains("i'm thinking") || pre_raw_content.contains("im thinking")
                                    || pre_content.contains("i was thinking")   || pre_content.contains("我在想")
                                    || pre_content.contains("i believe")        || pre_content.contains("我相信")
                                    || pre_content.contains("i guess")          || pre_content.contains("我猜")
                                    || pre_content.contains("because")          || pre_content.contains("因为")
                                    || (pre_content.contains("对") && pre_content.contains("？"))
                                    || pre_raw_content.endsWith("right?")) && (!pre_content.contains("why i think"))
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_prev_utt: opinion");
                                break;

                                //m2w: order & propossal case
                            }else if (pre_content.contains("how about")         || pre_content.contains("要不")  || pre_content.contains("不如")|| pre_content.contains("不然")
                                    || pre_content.contains("need to")          || pre_content.contains("必须") 
                                    || pre_content.contains("should be")        || pre_content.contains("我得")  || pre_content.contains("他得") || pre_content.contains("她得") || pre_content.contains("你得") 
                                    || (pre_content.contains("should") && pre_raw_content.contains("?"))
                                    || pre_content.contains("we can")           || pre_content.contains("我们能")  || pre_content.contains("我们可以")  
                                    || pre_content.contains("you may")          || pre_content.contains("你能")  || pre_content.contains("你可以") 
                                    // 4/5/11 12:02 PM
                                    || pre_content.contains("u may") 
                                    //4/5/11 12:02 PM
                                    || pre_content.contains("you should")       || pre_content.contains("你应该")
                                    //4/5/11 12:02 PM
                                    || pre_content.contains("u should") 
                                    || pre_raw_content.contains("shouldn't")    || pre_content.contains("你不应该")
                                    || pre_raw_content.contains("should've")
                                    || (pre_content.contains("would you") && pre_raw_content.contains("?"))
                                    || (pre_content.contains("could you") && pre_raw_content.contains("?"))
                                    || (pre_content.contains("can you") && pre_raw_content.contains("?"))
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_prev_utt: order & propossal");
                                break;

                                //m2w: other case
                            }else if (pre_content.contains("sometimes")         || pre_content.contains("有时候")
                                    || pre_content.contains("somewhere")        || pre_content.contains("有的地方")
                                    || pre_content.contains("everyone")         || pre_content.contains("所有人") || pre_content.contains("大家")
                                    || pre_content.contains("anything")         || pre_content.contains("任何事")|| pre_content.contains("所有事")
                                    || pre_content.contains("that's why")       || pre_content.contains("这就是为什么")
                                    || pre_content.contains("anything can")     
                                                                                || pre_content.contains("不一定")
                                    || pre_raw_content.contains("it ") && pre_raw_content.contains(" depend")
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_prev_utt: other");
                                break;

                                //m2w: prev_utt contains cnd key words, if there isn't any thing to match, check this then pass down
                            }else if ((pre_content.contains("yes ") 
                                    || (pre_content.contains("yea ") && !pre_content.contains("year"))
                                    || (pre_content.contains("true ") && !pre_content.contains("be true"))
                                    || pre_content.contains("agree ")
                                    || pre_content.contains("exactly ")
                                    || pre_content.equals("是") || pre_content.equals("就是") || pre_content.matches("[\\u4E00-\\u9FA5]*对") || pre_content.matches("[\\u4E00-\\u9FA5]*好") || pre_content.contains("没错") || pre_content.contains("恩") 
                                    //excluded "because" put into opinion. 4/5/11 11:54 AM
                                    //edcluded prev_utt 's dialog act is agree-accept. 4/16/11 3:00 PM
                                    ) && !prev_utt.getTag().toLowerCase().contains("agree-accept")){
                                found = true;
                                this.setRespTo(index, i, utt, "Short: cnd: CaseMatching_prev_utt: prev_utt contains cnd");
                                break;
                            }else{
                                //do nothing, continue the loop
                            }//ends all the else ifs
                        }//ends if length cal > 3
                    }//ends if speaker is the same                    
                }//ends if index > 1
            }//ends for loop
            return found;
        }//ends method



        /**
         * m2w : we are going to set the res-to to the last utt, before that we are going to skip some cases. 
         * if it comes in this method, it always returns found as true, because we need to assign a response_to utt for the CnDSht type
         * includes certain types of skip conditions.
         * @param index
         * @param found
         * @return found
         * @last 3/23/11 2:28 PM
         */
        private boolean CnDShtSkipJudge(int index, boolean found, String which_case){

            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            Utterance utt = utts.get(index);
            
            for(int i = 1; i<=CONF_SHORT_THRES; i++){
                    if(index >= i){
                        Utterance utterance = utts.get(index-i);
                        //m2w: if prev_utt has the same speaker,skip， use the condition here, put the new condition in the skip method. 3/23/11 2:31 PM
                        if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker)
                                ){

                            //m2w : other conditions. 3/23/11 2:36 PM
                            Utterance prev_utt = utts.get(index-i);
                            String pre_content = contentExtraction(prev_utt);

//                            if(this.CnDShtPreSkipJudge(index, i)){
//                               continue;
//                            }

                            if(lengthCal(prev_utt) <= 2){//last utt shouldn't be a short utt like "yes" or "no"
                                continue;
                            }

//                            if((pre_content.contains("?")//last utt contains “?” AND “what who where when”
//                                                    &&(pre_content.contains("what ")
//                                                    || pre_content.contains("who ")
//                                                    || pre_content.contains("where ")
//                                                    || pre_content.contains("when ")
//                                                    || pre_content.contains("why ")
//                                                    || pre_content.contains("how "))
//                                                    )){
//                                continue;
//                            }

                            if((lengthCal(prev_utt) < 5)//prev utts contains “true yea yes agree” and its length < 5
                                                    && (pre_content.contains("yea")
                                                    || pre_content.contains("yes ")
                                                    || pre_content.contains("true ")
                                                    || pre_content.contains("agree ")
                                                    || pre_content.contains("exactly ")
                                                    || pre_content.contains("ok ")
                                                    || pre_content.contains("是") || pre_content.contains("就是") || pre_content.contains("对") || pre_content.contains("好") || pre_content.contains("没错") || pre_content.contains("恩") 
                                                    )){
                                continue;
                            }

                            //m2w : commented additional skipping. 4/18/11 11:10 AM
//                        }else{//same names
//                            continue;
                        }
                    //the setting should be put here because after skipping what you can skip, you can just set the response_to,it's in the for loop.3/23/11 10:24 AM
                        found = true;
//                        map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i));
//                        String sysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i);
//                        evaluate(index,Integer.parseInt(utts.get(index).getTurn())-i,31);
//                        utt.setRespTo(sysRespTo);
                        this.setRespTo(index, i, utt, which_case);
                        break;
                }//ends if index > 1 
            }//ends for loop
            
            return found;
        }

        

        /**
         * m2w : judge if we need to parse the utt, check the past two consecutive utts have the same speaker, and length comparing
         * was put in the starting of casematching and skipJudging.
         * @param index
         * @param i
         * @return
         * @last 3/23/11 2:28 PM
         */
        private boolean CnDShtPreSkipJudge(int index, int i){
            //m2w : check the utt we are looping now, has the same speaker with the last one or not
            String currSpeakerInLoop = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker();
            String prevSpeakerInLoop = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+2))).getSpeaker();
            //if speaker is the same 
            if (currSpeakerInLoop.equals(prevSpeakerInLoop)){

                Utterance currUttInLoop = utts.get(index-i);
                Utterance prevUttInLoop = utts.get(index-(i+1));
                String currUttContentInLoop = contentExtraction(currUttInLoop);
                String prevUttContentInLoop = contentExtraction(prevUttInLoop);
                int curr_words_length = countLetters(currUttContentInLoop);
                int prev_words_length = countLetters(prevUttContentInLoop);
                int curr_length = ParseTools.wordCountChinese(currUttContentInLoop);
                int prev_length = ParseTools.wordCountChinese(prevUttContentInLoop);
                if ((((prev_length == curr_length) && (prev_words_length > curr_words_length))
                            || (prev_length > curr_length))
                            && !currUttContentInLoop.contains("i think")
                            ){
                    return true;
                }
            }
            return false;
        }

//      -------------------------------------------------------------------------------------------------------------

         /**
         * m2w: judge the current utterance , see if it's confirmation & deny type
         * check if it contains agree, true, yes, yea,ok , (no sorry, because "it's ok" is a respound to "im sorry", it's not confirmation & deny)
         * @param cur_utterance
         * @return
         */
        private boolean judgingCnDLong(String cur_utterance, Utterance utt){//lowercased unfiltered
            if((cur_utterance.contains("agree")                     || cur_utterance.contains("赞成") || cur_utterance.contains("同意") || cur_utterance.contains("赞同")
                            || cur_utterance.contains("true")       || cur_utterance.contains("真的") 
                            || cur_utterance.contains("yes")        || cur_utterance.contains("是啊")
                            || cur_utterance.contains("yea")        || cur_utterance.contains("是阿")
                            || cur_utterance.contains("ya ")        || cur_utterance.contains("对 ")    
                            || cur_utterance.equals("ok")           || cur_utterance.equals("好")
                            || cur_utterance.equals("okay")         || cur_utterance.equals("好的")
                            || cur_utterance.contains("ok,")        || cur_utterance.contains("好,")
                            || cur_utterance.contains("ok ")        || cur_utterance.equals("好 ")
                            || cur_utterance.contains("s cool")
                            || (cur_utterance.contains(" too") && !cur_utterance.contains("you too"))
                            || cur_utterance.contains("wow")        || cur_utterance.equals("哇")
                            || cur_utterance.contains("neither")    || cur_utterance.contains("也不")
                            || cur_utterance.contains("sure")       || cur_utterance.contains("当然")
                            || cur_utterance.contains("same ")      || cur_utterance.contains("也是")
                            || (cur_utterance.contains("i ") && (cur_utterance.contains("dont") ||cur_utterance.contains("don't")))
                            || cur_utterance.contains("我不")
                            || (cur_utterance.contains("hah") || cur_utterance.contains("lol") || cur_utterance.contains(":D"))
                            || cur_utterance.contains("哈哈")
                            || (cur_utterance.contains("right") && !cur_utterance.contains("the right") && cur_utterance.contains("?")) // maybe include not right 4/1/11 1:40 PM
                            || cur_utterance.contains("nope") //4/1/11 1:42 PM
                            || cur_utterance.contains("不")         || cur_utterance.contains("没有")
                            || cur_utterance.contains("yep") //4/1/11 1:42 PM
                            || cur_utterance.contains("恩")         
                            || cur_utterance.contains("s ok") //4/1/11 1:42 PM
                            || cur_utterance.contains("没事的")      || cur_utterance.contains("没关系")
                            || cur_utterance.contains("sorry") // 4/1/11 1:42 PM
                            || cur_utterance.contains("对不起")      || cur_utterance.contains("不好意思")
                            || cur_utterance.contains("definately") //4/1/11 1:42 PM
                            || cur_utterance.contains("绝对")      || cur_utterance.contains("肯定")
                            || cur_utterance.contains("disagree")// 4/1/11 1:42 PM
                            || cur_utterance.contains("不赞成")      || cur_utterance.contains("不赞同")      || cur_utterance.contains("不同意")
                            || cur_utterance.contains("make sense")// 4/1/11 1:42 PM
                            || cur_utterance.contains("makes sense")// 4/1/11 1:42 PM
                            || cur_utterance.contains("correct")// 4/1/11 1:42 PM
                            || cur_utterance.contains("没错")
                            || cur_utterance.contains("good point") || cur_utterance.contains("good question") || cur_utterance.contains("valid point")//4/16/11 3:57 PM
                            || cur_utterance.contains("说的好") || cur_utterance.contains("问的好") || cur_utterance.contains("有道理")
                            || cur_utterance.contains("exactly")//4/16/11 3:59 PM
                            || cur_utterance.contains("apparently")//4/16/11 4:36 PM
                            || cur_utterance.contains("明显")
                            || cur_utterance.contains("not ")//4/18/11 12:32 PM
                            || utt.getTag().toLowerCase().contains("agree-accept")
                    
                            )
                            && !cur_utterance.contains("sorry")){
                    return true;
            }else{
                    return false;
            }
        }


        /**
         * m2w : called in the shortUtterance method, to calculate confirmation & deny type short utt
         * 4 steps now :1. findname 2. check the same word code 37 (synon update later) 2. case matching for code 32-36 and code 38 3. skip judge for code 31
         * @param index
         * @param found
         * @return
         */
        private boolean calCnDLong(int index, boolean found){
            //1.find name first, if found, return found, if not, do next
            //m2w: added the that's ok case check if does contains , passes it down. 4/5/11 11:49 AM
            String cur_raw_content = utts.get(index).getContent();
            if(found = this.calUttSimilarity(index, LONG_UTT_SIM_LOOK_BACK, found, "Long: cnd: utt sim")){
                return found;
            }else
            //included good point and that's ok in long utts. findname . 4/20/11 1:37 PM
            if (found = this.findNamePart1(index, found, "Long: cnd: findNamePt1 in calCndLong")){
                return found;
            }else if(found = this.checkWordSimilarity(index, found, "Long: cnd: check word similarity in calCndLong")){ //2. check same word, if found, return found, if not, do next
                return found;

                //adding the synonem checkhere
            }else if(found = this.CnDLongCaseMatching(index, found)){//3. case checking, if found return, if not, do next
                return found;
            }else{//4. skipping AFTER the case matching calculation
                found = this.CnDLongSkipJudge(index, found, "Long: cnd: Skip Judge in calCnDLong");
                return found;
            }//ends else
        }//ends method


        /**
         * m2w : this is extracted from the calCnDSht, setting up several certain cases for matching.
         * code 32://m2w: rhetorical question case. if in previous 3 utts contains “?” && “ don't dont isn't wasn't hasn't weren't” , FOUND! 3/22/11 3:50 PM
         * code 33://m2w: certain types of conditions, if fits, set link_to, set to the utt; 4/1/11 1:52 PM
         * code 34://m2w: too, neither case 3/24/11 1:56 PM
         * code 35://m2w: wow case, wow about something, look for "i", 3/24/11 11:17 AM
         * code 36://m2w: deny about something, look for don't. 3/24/11 11:17 AM
         * code 38://m2w: haha and lol case , 3/28/11 1:56 PM
         * modified the method, has been changed it into 2 parts, cur_case and pre_case 4/4/11 4:16 PM
         * @param index
         * @param found
         * @return found
         * @last 3/23/11 3:58 PM - 4/1/11 1:53 PM - 4/4/11 3:54 PM
         */
        private boolean CnDLongCaseMatching(int index, boolean found){
            Utterance utt = utts.get(index);
            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            String cur_content = contentExtraction(utt).toLowerCase();
            String cur_raw_content = utt.getContent().toLowerCase();
            //m2w : look back ,loop through several utts, now set to 3
            for(int i = 1; i<=CONF_SHORT_THRES; i++){
                Utterance prev_utt = null;
                if(index >= i){
                    prev_utt = utts.get(index-i);
                    Utterance utterance = utts.get(index-i);
                    //m2w: if prev_utt has the same speaker,skip， use the condition here, put the new condition in the skip method. 3/23/11 2:31 PM
                    if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker)){

//                        //pre judge
//                        if(this.CnDShtPreSkipJudge(index, i)){
//                           continue;
//                        }

                        String pre_raw_content = prev_utt.getContent().toLowerCase();
                        String pre_content = contentExtraction(prev_utt).toLowerCase();


//                      ===== m2w : current utt contains matching cases. 4/4/11 2:41 PM ======
                        //m2w: yes and D[agree-accept] && prev is C[addressed-to]D[Information-Request] and contains ? , then set.4/18/11 9:07 AM
                        if((cur_content.startsWith("yes")  || cur_content.startsWith("对")  || cur_content.startsWith("恩")  || cur_content.startsWith("好")  || cur_content.startsWith("是")  || cur_content.startsWith("没错"))
                                && this.lengthCal(utt) < 2 && utt.getTag().contains("agree-accept")
                                && pre_raw_content.contains("?") && prev_utt.getTag().toLowerCase().contains("information-request")){
                            found = true;
                            this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: yes / I-R && ?");
                            break;
                        }
                        
                        //m2w: code 38, haha and lol case , 3/28/11 1:56 PM
                        //put haha to 2nd of case matching. 4/18/11 1:01 PM
                        if((cur_content.contains("hah") || cur_raw_content.contains("lol") || cur_raw_content.contains("哈哈")) 
                                
                                && utt.getTag().toLowerCase().contains("acknowledge") && (
                                //changed the lengthcal to > 3. 4/18/11 10:01 AM
                                (pre_content.contains("hah")|| pre_content.contains("lol") || cur_raw_content.contains("哈哈")) 
                                
                                && this.lengthCal(prev_utt) >= 3)
                                //added prev_utt length > 1. 4/18/11 11:21 AM
                                && this.lengthCal(prev_utt) > 1
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: haha");
                                break;
                        }

                        //m2w: sure and can you ? wanna ? case 4/18/11 1:41 PM
                        if((cur_raw_content.equals("sure") || cur_raw_content.equals("当然")  || cur_raw_content.equals("没问题")  )
                                && utt.getTag().equals("--Response-Answer")
                                && prev_utt.getTag().equals("Information-Request") && pre_raw_content.contains("?")
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: sure - ir && ?");
                                break;
                        }

                        //m2w: code 34, too, neither case 3/24/11 1:56 PM
                        if((cur_content.contains(" too") || cur_content.contains(" neither")) && (
                                pre_content.contains("not ")
                                || pre_content.contains("nt ")
                                || pre_content.contains("id be")
                                || pre_content.contains("i would be")
                                )
                                ||
                                (cur_content.contains("也没") || cur_content.contains("也不"))
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: too/neither");
                                break;

                        }

                        //m2w: code 32, rhetorical question case. if in previous 3 utts contains “?” && “ don't dont isn't wasn't hasn't weren't” , FOUND! 3/22/11 3:50 PM
                        if((pre_raw_content.contains("?")) &&
                                        !(pre_content.contains("know ")) &&
                                        ((pre_content.contains("dont ")
                                        || pre_content.contains("isnt ")
                                        || pre_content.contains("wasnt ")
                                        || pre_content.contains("hasnt ")
                                        || pre_content.contains("havnt ")
                                        || pre_content.contains("werent ")
                                        || pre_content.contains("didnt ")
                                        || pre_raw_content.contains("right?")
                                        ) || (
                                            (pre_content.contains("不是吗"))
                                            || (pre_content.contains("不") && pre_content.contains("吗"))
                                            || (pre_content.contains("没有吗"))
                                            || (pre_content.contains("没") && pre_content.contains("吗"))
                                            || (pre_content.contains("不对吗"))
                                ))){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: rhetorical question");
                                break;
                        }

                        //m2w : code 35, wow case, wow about something, look for "i", 3/24/11 11:17 AM
//                        if((cur_raw_content.contains("wow ") || cur_raw_content.contains(" wow") || cur_raw_content.equals("wow")) && (
                        if((cur_raw_content.contains("wow") && (
                                //m2w : added exclaimation mark! 4/18/11 9:53 AM
                                (pre_raw_content.contains("i ") || pre_raw_content.contains("!"))
                                )) 
                                || cur_raw_content.contains("哇")
                                ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: wow");
                                break;

                        }

                        //m2w : code 36, deny about something, look for don't. 3/24/11 11:17 AM
                        if(((cur_raw_content.contains("nt") || cur_raw_content.contains("n't")) && cur_raw_content.contains("either"))
                                ||  ((cur_raw_content.contains("没") || cur_raw_content.contains("不")|| cur_raw_content.contains("赞")|| cur_raw_content.contains("反对")
                                ) && cur_raw_content.contains("也")) ){
                            if (pre_content.contains("dont") || 
                                    (cur_raw_content.contains("没") || cur_raw_content.contains("不")|| cur_raw_content.contains("赞")|| cur_raw_content.contains("反对"))){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: i dont either");
                                break;
                            }else{
                                continue;
                            }

                        }

                        //m2w: code 39, it's ok case, look for "sorry" in previous utts 4/1/11 3:10 PM
                        if((cur_raw_content.contains("'s ok")
                                || cur_raw_content.contains("'s okay")
                                || cur_raw_content.contains("s ok")
                                || cur_raw_content.contains("s okay")
                                || cur_raw_content.contains("没事")
                                || cur_raw_content.contains("没关系")
                                
                                ) && (pre_content.contains("sorry") 
                                        || pre_content.contains("对不起") 
                                        || pre_content.contains("不好意思") 
                                        )){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: it's ok");
                                break;
                        }

//                        //m2w: code 310, sorry check. if last utt has sorry too, link to last's link_to ,4/3/11 11:38 AM
//                        if((cur_raw_content.contains("sorry") && pre_raw_content.contains("sorry") && prev_utt.getSysRespTo() != null)){
//                            found = true;
//                            String pre_SysRespTo = prev_utt.getSysRespTo();
//                            this.setRespToAs(index, pre_SysRespTo, utt, "Long: cnd: CaseMatching_cur_utt: sorry same prev utt");
//                            break;
//                        }
//                        4/16/11 4:01 PM

                        //m2w: code 311, exactly case, check its last utt, then perform skip judge and link to prev utts 4/1/11 3:10 PM
                        if((cur_raw_content.toLowerCase().contains("exactly")) 
                                || cur_raw_content.contains("没错") || cur_raw_content.contains("对") || cur_raw_content.contains("就是")
                                ){
                            //checking last utt,
                            if((pre_content.contains("exactly") 
                                    || cur_raw_content.contains("没错") || cur_raw_content.contains("对") || cur_raw_content.contains("就是")
                                    ) && prev_utt.getSysRespTo() != null){ // gai
                                found = true;
                                String pre_SysRespTo = prev_utt.getSysRespTo();
                                this.setRespToAs(index, pre_SysRespTo, utt, "Long: cnd: CaseMatching_cur_utt: exactly same prev utt");
                                break;
                            }else{
//                                this.CnDShtSkipJudge(index, found, "Long: cnd: CaseMatching_cur_utt: exactly");
                                //debugged in exactly case. 4/18/11 12:42 PM
                                break;
                            }
                        }

                        //m2w: code 312, good point case. look for "?" and/or "why",4/3/11 11:38 AM
                        if(cur_raw_content.toLowerCase().contains("good point") || cur_raw_content.toLowerCase().contains("good question")
                                || cur_raw_content.contains("说的好")|| cur_raw_content.contains("问的好")|| cur_raw_content.contains("有道理")
                                ){
                            //if previous utt also fits the case, then set to prev's link_to
                            if(( pre_raw_content.contains("good point") || pre_raw_content.contains("good question")
                                    || cur_raw_content.contains("说的好")|| cur_raw_content.contains("问的好")|| cur_raw_content.contains("有道理")
                                    ) && prev_utt.getSysRespTo() != null){
                                found = true;
                                String pre_SysRespTo = prev_utt.getSysRespTo();
                                this.setRespToAs(index, pre_SysRespTo, utt, "Long: cnd: CaseMatching_cur_utt: good point same prev utt");
                                break;
                            }else{
                                // if not , look for ? or why, then set.
                                if(pre_content.contains("?") || pre_content.contains("why")
                                        || pre_content.contains("为什么")|| pre_content.contains("怎么")
                                        ){
                                    this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_cur_utt: good point");
                                }
                            }
                        }



                         //m2w : i agree can't be responsing to a question. 3/24/11 11:17 AM
//                        if(cur_content.contains("agree") && pre_raw_content.contains("?")){
//                                    continue;
//                                }
//                         //m2w : curr contains true, and previous shouldn't contains should or true, 3/24/11 11:17 AM
//                        if(cur_content.contains("true") && (pre_content.contains("should") || pre_content.contains("true"))){
//                                    continue;
//                                }





//                        ===== m2w : previous utt contains matching cases. 4/4/11 2:41 PM ======
                        //m2w : code 33 , certain types of conditions, if fits, set link_to, set to the utt; 4/1/11 1:52 PM
                        //m2w: split to several cases. since 4/4/11 2:48 PM.


                        if(lengthCal(prev_utt) >= 3){

                            //m2w: guessing case
                            if(pre_content.contains("might be") || pre_content.contains("可能")
                                    || pre_content.contains("might have") || pre_content.contains("也许")
                                    || pre_content.contains("could be") || pre_content.contains("应该")
                                    || pre_content.contains("could have")   || pre_content.contains("如果")
                                    || pre_content.contains("may be")   || pre_content.contains("要是")
                                    || pre_content.contains("maybe")    || pre_content.contains("像是")
                                    || pre_content.contains("may have") || pre_content.contains("好像")
                                    || pre_content.contains("perhaps")  || pre_content.contains("或许")
                                    || pre_content.contains("probably")
                                    || pre_content.contains("seems")
                                    || pre_content.contains("seem")
                                    || (pre_content.contains("either") && pre_content.contains("or"))
                                    || (pre_content.contains("不是") && pre_content.contains("就是"))
                                    
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_prev_utt: guessing");
                                break;

                                //m2w: degree case
                            }else if (pre_content.contains("really")            || pre_content.contains("真的")
                                    || pre_content.contains("definatly")        || pre_content.contains("肯定")
                                    || pre_content.contains("especially")       || pre_content.contains("特别是")
                                    || pre_content.contains("apparently")       || pre_content.contains("明显")
                                    || pre_content.contains("regardless")       || pre_content.contains("无论")
                                    || pre_content.contains("some how")         || pre_content.contains("最多")
                                    || pre_content.contains("somehow")          || pre_content.contains("全部")
                                    || pre_content.contains("most")             || pre_content.contains("最少")
                                    || pre_content.contains("at all")           || pre_content.contains("只要")
                                    || pre_content.contains("at least")         || pre_content.contains("只有")
                                    || pre_content.contains("as long as")       
                                    || pre_content.contains("only")             
                                    || pre_content.contains("best")             || pre_content.contains("最")
                                    || pre_content.contains("worst")            
                                    || pre_content.contains("est ") // 4/5/11 12:00 PM 
                                    || pre_raw_content.contains("it's so ") || pre_raw_content.contains("its so ") || pre_content.contains("那么")
                                    || pre_raw_content.contains("that's so ") || pre_raw_content.contains("thats so ")
                                    || pre_raw_content.contains("are so ") || pre_raw_content.contains("r so ")                                    
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_prev_utt: degree");
                                break;

                                //m2w: opinion case
                            }else if ((pre_content.contains("i think")          || pre_content.contains("我想")    
                                    || pre_raw_content.contains("i'm thinking") || pre_raw_content.contains("im thinking")
                                    || pre_content.contains("i was thinking")   || pre_content.contains("我在想")
                                    || pre_content.contains("i believe")        || pre_content.contains("我相信")
                                    || pre_content.contains("i guess")          || pre_content.contains("我猜")
                                    || pre_content.contains("because")          || pre_content.contains("因为")
                                    || (pre_content.contains("对") && pre_content.contains("？"))
                                    || pre_raw_content.endsWith("right?")) && (!pre_content.contains("why i think"))
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_prev_utt: opinion");
                                break;

                                //m2w: order & propossal case
                            }else if (pre_content.contains("how about")         || pre_content.contains("要不")  || pre_content.contains("不如")|| pre_content.contains("不然")
                                    || pre_content.contains("need to")          || pre_content.contains("必须") 
                                    || pre_content.contains("should be")        || pre_content.contains("我得")  || pre_content.contains("他得") || pre_content.contains("她得") || pre_content.contains("你得") 
                                    || (pre_content.contains("should") && pre_raw_content.contains("?"))
                                    || pre_content.contains("we can")           || pre_content.contains("我们能")  || pre_content.contains("我们可以")  
                                    || pre_content.contains("you may")          || pre_content.contains("你能")  || pre_content.contains("你可以") 
                                    // 4/5/11 12:02 PM
                                    || pre_content.contains("u may") 
                                    //4/5/11 12:02 PM
                                    || pre_content.contains("you should")       || pre_content.contains("你应该")
                                    //4/5/11 12:02 PM
                                    || pre_content.contains("u should") 
                                    || pre_raw_content.contains("shouldn't")    || pre_content.contains("你不应该")
                                    || pre_raw_content.contains("should've")
                                    || (pre_content.contains("would you") && pre_raw_content.contains("?"))
                                    || (pre_content.contains("could you") && pre_raw_content.contains("?"))
                                    || (pre_content.contains("can you") && pre_raw_content.contains("?"))
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_prev_utt: order & propossal");
                                break;

                                //m2w: other case
                            }else if (pre_content.contains("sometimes")         || pre_content.contains("有时候")
                                    || pre_content.contains("somewhere")        || pre_content.contains("有的地方")
                                    || pre_content.contains("everyone")         || pre_content.contains("所有人") || pre_content.contains("大家")
                                    || pre_content.contains("anything")         || pre_content.contains("任何事")|| pre_content.contains("所有事")
                                    || pre_content.contains("that's why")       || pre_content.contains("这就是为什么")
                                    || pre_content.contains("anything can")     
                                                                                || pre_content.contains("不一定")
                                    || pre_raw_content.contains("it ") && pre_raw_content.contains(" depend")
                                    ){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_prev_utt: other");
                                break;

                                //m2w: prev_utt contains cnd key words, if there isn't any thing to match, check this then pass down
                           }else if ((pre_content.contains("yes ") 
                                    || (pre_content.contains("yea ") && !pre_content.contains("year"))
                                    || (pre_content.contains("true ") && !pre_content.contains("be true"))
                                    || pre_content.contains("agree ")
                                    || pre_content.contains("exactly ")
                                    || pre_content.contains("是") || pre_content.contains("就是") || pre_content.contains("对") || pre_content.contains("好") || pre_content.contains("没错") || pre_content.contains("恩") 
                                    //excluded "because" put into opinion. 4/5/11 11:54 AM
                                    //edcluded prev_utt 's dialog act is agree-accept. 4/16/11 3:00 PM
                                    ) && !prev_utt.getTag().toLowerCase().contains("agree-accept")){
                                found = true;
                                this.setRespTo(index, i, utt, "Long: cnd: CaseMatching_prev_utt: prev_utt contains cnd");
                                break;
                            }else{
                                //do nothing, continue the loop
                            }//ends all the else ifs
                        }//ends if length cal > 3
                    }//ends if speaker is the same
                }//ends if index > 1
            }//ends for loop
            return found;
        }//ends method



        /**
         * m2w : extracted from the calCnDSht method, make it easy to read . later on skipping conditions should be put here
         * if it comes in this method, it always returns found as true, because we need to assign a response_to utt for the CnDSht type
         * includes certain types of skip conditions         *
         * @param index
         * @param found
         * @return found
         * @last 3/23/11 2:28 PM
         */
        private boolean CnDLongSkipJudge(int index, boolean found, String which_case){

            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            Utterance utt = utts.get(index);

            for(int i = 1; i<=CONF_SHORT_THRES; i++){
                    if(index >= i){
                        Utterance utterance = utts.get(index-i);
                        //m2w: if prev_utt has the same speaker,skip， use the condition here, put the new condition in the skip method. 3/23/11 2:31 PM
                        if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker)
                                ){

                            //m2w : other conditions. 3/23/11 2:36 PM
                            Utterance prev_utt = utts.get(index-i);
                            String pre_content = contentExtraction(prev_utt);

//                            if(this.CnDShtPreSkipJudge(index, i)){
//                               continue;
//                            }

                            if(lengthCal(prev_utt) <= 2){//last utt shouldn't be a short utt like "yes" or "no"
                                continue;
                            }

//                            if((pre_content.contains("?")//last utt contains “?” AND “what who where when”
//                                                    &&(pre_content.contains("what ")
//                                                    || pre_content.contains("who ")
//                                                    || pre_content.contains("where ")
//                                                    || pre_content.contains("when ")
//                                                    || pre_content.contains("why ")
//                                                    || pre_content.contains("how "))
//                                                    )){
//                                continue;
//                            }

                            if((lengthCal(prev_utt) < 5)//prev utts contains “true yea yes agree” and its length < 5
                                                    && (pre_content.contains("yea")
                                                    || pre_content.contains("yes ")
                                                    || pre_content.contains("true ")
                                                    || pre_content.contains("agree ")
                                                    || pre_content.contains("exactly ")
                                                    || pre_content.contains("ok ")
                                                    || pre_content.contains("是") || pre_content.contains("就是") || pre_content.contains("对") || pre_content.contains("好") || pre_content.contains("没错") || pre_content.contains("恩") 
                                                    )){
                                continue;
                            }

                            //m2w : commented additional skipping. 4/18/11 11:10 AM
//                        }else{//same names
//                            continue;
                        }
                    //the setting should be put here because after skipping what you can skip, you can just set the response_to,it's in the for loop.3/23/11 10:24 AM
                        found = true;
//                        map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i));
//                        String sysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i);
//                        evaluate(index,Integer.parseInt(utts.get(index).getTurn())-i,31);
//                        utt.setRespTo(sysRespTo);
                        this.setRespTo(index, i, utt, which_case);
                        break;
                }//ends if index > 1
            }//ends for loop

            return found;
        }



//    ===============================================common util=============================================
         /**
         * m2w : this method is used in short utts, to check to see if previous utt has same 2 consecutive words as the current one. 
         *          english part is done by calculating the similarity of words(same long word or misspelling), 
         * @param index
         * @param found
         * @return found
         * @date 8/10/11 2:43 PM 
         */
        private boolean checkWordSimilarity(int index, boolean found, String which_case){

            int sim_thresh = 0;
            int sim_look_back = 0;
            if(which_case.toLowerCase().contains("short")){
                sim_thresh = SHORT_WORD_SIM_LENGTH_CHECK;
                sim_look_back = SHORT_WORD_SIM_LOOK_BACK;
            }else if(which_case.toLowerCase().contains("long")){
                sim_thresh = LONG_WORD_SIM_LENGTH_CHECK;
                sim_look_back = LONG_WORD_SIM_LOOK_BACK;
            }
            Utterance utt = utts.get(index);
            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            String cur_content = contentExtraction(utt);
//            System.out.println("curr content: " + cur_content);
            int where_to = 0;
            boolean isFound = false;
            //m2w : look back ,loop through several utts, now set to 3, 3/28/11 1:19 PM
            outterFor:
            for(int i = 1; i<=sim_look_back; i++){
                Utterance prev_utt = null;
                if(index >= i){
                    prev_utt = utts.get(index-i);
                    String pre_content = contentExtraction(prev_utt);
                    if(!prev_utt.getSpeaker().toLowerCase().equals(curr_speaker)){
                    //convert the 2 utts' contents into arrays, check if they have the same word
                        
                        //changed to uttToWdsChinese 8/9/11 2:37 PM
                        //1. get the arraylist-of-arraylist of strings. (each word as an entry, chinese at index0, english at index1)
                        //2. if the curr list and prev list != empty.
                        //3. get list from map, check isempty before do.
                        //4. if English !isEmpty(), do chinese first. find identical words. 
                        //5. if Chinese !isEmpty(), do english, find similar words.
                        HashMap<String, ArrayList<String>> tempCurrUttMap = Util.uttToWdsChinese(cur_content);
                        HashMap<String, ArrayList<String>> tempPrevUttMap = Util.uttToWdsChinese(pre_content);
                        
                        //2.
                        if(!tempCurrUttMap.isEmpty() && !tempPrevUttMap.isEmpty()){
                            //3.
//                            System.out.println("in----------------------------------------------------");
                            ArrayList<String> tempCurrCNList = tempCurrUttMap.get("CN");
                            ArrayList<String> tempCurrENList = tempCurrUttMap.get("EN");
                            ArrayList<String> tempPrevCNList = tempCurrUttMap.get("CN");
                            ArrayList<String> tempPrevENList = tempCurrUttMap.get("EN");
                            
                            //4.
                            if(tempCurrENList != null && !tempCurrENList.isEmpty()
                                    && tempPrevENList != null && !tempPrevENList.isEmpty()){
                                int swHit = 0;
                                for(int x = 0; x < tempCurrENList.size(); x++){//index utt loop
                                    for(int y = 0; y < tempPrevENList.size(); y++){//pre utt loop
                                        //both prev and curr longer than 6, will do the check or else do nothing.
                                        //threshold is now set to 5. 4/16/11 3:16 PM
                                        if(tempCurrENList.get(x).length() > sim_thresh && tempPrevENList.get(y).length() > sim_thresh - 2 ){//now set to 5 and 3 4/16/11 3:31 PM
                                            String tempCurrWord = tempCurrENList.get(x);
                                            String tempPrevWord = tempCurrENList.get(y);
                                            ArrayList<Character> tempCurrWordList = new ArrayList<Character>();
                                            ArrayList<Character> tempPrevWordList = new ArrayList<Character>();
                                            
                                            for(char value: tempCurrWord.toCharArray()){
                                                tempCurrWordList.add(value);
                                            }                                    
                                            for(char value: tempPrevWord.toCharArray()){
                                                tempPrevWordList.add(value);
                                            }
                                            
                                            int charHit = 0;
                                            //loop through the smaller one of the 2 wordlist
                                            for(int m = 0; m < Math.min(tempCurrWordList.size(), tempPrevWordList.size()); m++){
                                                    Character tempCurrChar = tempCurrWordList.get(m);
                                                    Character tempPrevChar = tempPrevWordList.get(m);
                                                    if(tempCurrChar.equals(tempPrevChar)){
                                                        charHit++;
                                                    }//ends if char same
                                            }//ends word list for loop
                                            //calculate the similarity of 2 words.
        //                                    Double wordSim = (double)charHit / (double)tempCurrWordList.size();
                                            //m2w: changed to min of the 2. 4/20/11 11:03 AM
                                            Double wordSim = (double)charHit / (double)Math.min(tempCurrWordList.size(), tempPrevWordList.size());
                                            if(wordSim >= WORD_SIM_THRESHOLD){
                                                //is now set to 0.7 . 4/16/11 3:16 PM
                                                swHit++;
                                            }
                                        }// ends if curr and prev word longer than 6 chars
                                    }//ends prev utt for loop
                                }//ends curr utt for loop

                                //excluded prev_utt com_act_type is response-to. 4/16/11 3:30 PM
                                if(swHit > 0 && !prev_utt.getCommActType().toLowerCase().contains("response-to")){
                                    isFound = true;
                                    where_to = i;
                                }//ends if hit > 0;
                                
                            }//closes 4. if english not empty
                            
                            //5.
                            if(isFound != true && tempCurrCNList != null && tempCurrCNList.size() > 1 //2 consecutive words, 8/10/11 2:46 PM
                                    && pre_content != null){
                                //using 2 consecutive words as searching criteria, parse through the current list and previous list.
                                for(int curIndex=1; curIndex<tempCurrCNList.size(); curIndex++){
                                    String tempCurSubStr = tempCurrCNList.get(curIndex) + tempCurrCNList.get(curIndex - 1);
//                                    System.out.println("tempCurrSubStr: " + tempCurSubStr);
                                    
                                    //if prev utt contains curr sub string.
                                    if(pre_content.contains(tempCurSubStr)){
                                        isFound = true;
                                        where_to = i;
                                        //want to find the most previous one so don't break.
    //                                    break outterFor;
                                    }//close if conatins curr sub string
                                }//closes CN for loop.
                            }//closes 5. CN
                            
                        }//closes check if CN & EN list is empty
                    }//ends same name check
                }//ends if index > i
            }//ends look back for loop

            //m2w : changed to loop through all utts then set to the last contains sim. 4/18/11 10:50 AM
            if(isFound){
                found = true;
                this.setRespTo(index, where_to, utt, which_case);
            }

            return found;
        }//ends method


        /**
         * m2w: code 41. this method is calculating the similarity between prev_utts, if it's above the sim_threshold, then set response_to to the utt.
         * should be called in the short and long utt method, after counldn't find any.
         * @param index
         * @param curThreshold
         * @param found
         * @return
         * @last 3/23/11 10:45 AM - 4/1/11 1:57 PM
         */
        private boolean calUttSimilarity(int index, int curThreshold,  boolean found, String which_case){
            TreeMap<Double,Integer> sims = new TreeMap<Double,Integer>(); //m2w : for similarity calculation 3/23/11 10:04 AM
            int res_to = 0;
            String cur_content = contentExtraction(utts.get(index));
            Utterance utt = utts.get(index);
            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            
            for (int i = 0; i < curThreshold; i++){
                if(index - i > 0){
                    Utterance utterance = utts.get(index-i);
                    
//                    if(!this.sameSpkerJudge(index, i)){
                    if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker)){
                            String pre_content = contentExtraction(utterance);
                            double sim = Util.compareUttsChinese(pre_content, cur_content);
                            int pre_turn_no = (Integer.parseInt(utts.get(index).getTurn()))-i;
//                            System.out.println("pre: " + pre_content + " : " + pre_turn_no + " : " + sim);
                            sims.put(sim, pre_turn_no);
                        }else{ // if same speaker
                            continue;
                    }//ends if same speaker
                }//ends if index >= i 
            }//ends for loop
            
            if(sims.size() > 0){
                // get the highest similarity value
//                sims.
                double best_sim = sims.lastKey();
//                System.out.println("best_sim: " + best_sim);
//              set the target to the utterance of its sim value over threshold
                if(best_sim > SIM_THRESHOLD){
                    found = true;
                    res_to = sims.get(best_sim);
//                    System.out.println("to?"+res_to);
                    res_to = index - res_to +1;
//                    System.out.println("before set: " + index + " | " + res_to + " " );
                    this.setRespTo(index, res_to, utt, which_case);
                }
            }
            return found;
        }//ends method



        /**
         * m2w: compare 2 utt contents by checking how many synonyms there are in the 2 utts.
         * @param content1
         * @param content2
         * @return double similarity
         * @last 4/10/11 12:49 PM
         */
        public static double compareUttsBySynon(String content1, String content2){
            double sim = 0.0;
            ArrayList<String> content1List = Util.uttToWds(content1);
            ArrayList<String> content2List = Util.uttToWds(content2);
            int hit = 0;
            
            for(int i = 0; i < content1List.size(); i++){
                for(int j = 0; j < content2List.size(); j++){
                    String iStr = content1List.get(i);
                    String jStr = content2List.get(j);                    
                    if(wn.isSynonym(iStr, jStr)){
                        hit++;
                    }
                    
                }//ends content 2 loop
            }//ends content 1 loop

            if(!content1List.isEmpty() && !content2List.isEmpty()){

                sim = ( ((double)hit / (double)content1List.size()) + ((double)hit / (double)content2List.size()) ) / 2;
                
            }


            return sim;
            
        }



        /**
         * m2w : code 11 or 21 modified xin's method, look back several turns, looking for current speaker name in the previous utts.
         * @param utt
         * @return
         * @last 4/1/11 1:57 PM
         */
        public boolean findName(Utterance utt){
            int index = Integer.parseInt(utt.getTurn()) - 1;
            String name = "";
            boolean name_found = false;
            boolean link_found = false;
            // get current utterance content
            String cur_content = contentExtraction(utt);
            String curr_speaker = utt.getSpeaker().toLowerCase();
            StringTokenizer tokenizer = new StringTokenizer(cur_content);
            while(tokenizer.hasMoreTokens())
            {
                    String token = tokenizer.nextToken();
                    if(speaker_names.contains(token.toLowerCase()))
                    {
                            // a speaker name found in current utterance
                            name_found = true;
                            name = token.toLowerCase();
                            break;
                    }
            }

            ArrayList<Integer> pre_utt_list = new ArrayList<Integer>();	// saves previous turn numbers
            ArrayList<String> pre_speaker_list = new ArrayList<String>();	// saves previous speaker names

            for(int i=1; i<=FIND_NAME_THRES; i++)
            {
                    // get one previous utterance
                    if(index >= i)
                    {
                            if (utts.get(index-i).getTurn().contains(".")){
                                    Utterance utterance = utts.get(index-(i+1));
                    }else{

                            Utterance utterance = utts.get(index-i);
                            // cannot response self
                            if(!utterance.getSpeaker().toLowerCase().equals(curr_speaker))
                            {
                                    String pre_content = contentExtraction(utterance);
                                    // add turn number and speaker name of previous utterance one by one
                                    pre_utt_list.add(Integer.parseInt(utterance.getTurn()));
                                    pre_speaker_list.add(utterance.getSpeaker().toLowerCase());
                                    // name in current utterance find in previous speakers
                                    if(name_found)
                                    {//m2w: added || if name in current utterance is part of the name of one of the previous speaker 3/24/11 2:00 PM
                                            if(name.equals(utterance.getSpeaker().toLowerCase()) || utterance.getSpeaker().toLowerCase().contains(name))
                                            {
                                                    link_found = true;
                                                    setNameUtt(utterance);
                                                    break;
                                            }
                                    }
                                    if(!link_found && !name_found)
                                    {
                                            StringTokenizer pre_tokenizer = new StringTokenizer(pre_content);
                                            // name equals to current speaker found in a specific previous utterance
                                            while(pre_tokenizer.hasMoreTokens())
                                            {
                                                    String pre_token = pre_tokenizer.nextToken();
                                                    if(curr_speaker.equals(pre_token.toLowerCase()))
                                                    {
                                                            link_found = true;
                                                            setNameUtt(utterance);
                                                            break;
                                                    }
                                            }
                                    }
                            }
                    }
                    }
            }
            return link_found;

        }

        /**
         * m2w : extracted and modified from the original findName method that xin wrote, only look for speaker names that has been mentioned
         * in the current utt.  loop back 5 utts
         * @param index
         * @param found
         * @return found
         * @FIND_NAME_THRES now is 5
         * @last 4/4/11 1:58 PM
         */
        public boolean findNamePart1(int index, boolean found, String which_case){
            Utterance utt = utts.get(index);
            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
            String cur_content = contentExtraction(utt);
            String cur_raw_content = utt.getContent().toLowerCase();

            String name = "";
            boolean name_found = false;
            boolean link_found = false;
            //look for if there's any speaker name in cur utterance;
            StringTokenizer tokenizer = new StringTokenizer(cur_content);
            while(tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    //arraylist for getting name in the list. 4/3/11 3:06 PM
                    ArrayList<String> nameList = new ArrayList<String>(speaker_names);
                    //nameList = (ArrayList)Arrays.asList(speaker_names.toArray());
                    for (int i = 0; i < nameList.size(); i ++ ){
                        if(token.length() > 2 && nameList.get(i).contains(token.toLowerCase())){ // added token length > 2 , 4/16/11 3:51 PM
                            name_found = true;
                            name = nameList.get(i).toLowerCase(); // this is the name in the map/arraylist, so no need to "contains" getting name matching. 4/3/11 10:09 PM
                            break;
                        }
                    }   
            }

            //m2w : if name_found = true, look back ,loop through several utts, now set to 5, 3/28/11 1:19 PM
            if(name_found){
                for(int i = 1; i<=FIND_NAME_THRES; i++){
                Utterance prev_utt = null;
                    if(index >= i){
                        prev_utt = utts.get(index-i);
    //                    String prev_content = contentExtraction(prev_utt);
                        String preSpkName = prev_utt.getSpeaker().toLowerCase();
                        if(!preSpkName.equals(curr_speaker)){
                        //if prev utt has the same name as the name we found, set link_to to it
                            //added conventional-opening , hi some one case. 4/18/11 1:09 PM
                            if (utt.getTag().toLowerCase().contains("opening") || cur_raw_content.contains("hi ")){
                                if((this.lengthCal(prev_utt) > 1) && preSpkName.equals(name) && this.lengthCal(prev_utt) > 1 && prev_utt.getTag().toLowerCase().contains("opening") ){
                                found = true;
                                this.setRespTo(index, i, utt, which_case);
                                break;
                                }else{
                                    continue;
                                }
                            }else//added length cal > 1. 4/18/11 12:05 PM
                                if(preSpkName.equals(name) && this.lengthCal(prev_utt) > 1){
                                found = true;
                                this.setRespTo(index, i, utt, which_case);
                                break;
                            }//ends if same name has found!
                        }//ends same name check
                    }//ends if index > i
                }//ends outter for loop
            }//ends if name is found            
            return found;
        }//ends method

        
        
        /**
         * m2w : returns the speaker's name if it appears in the current utt.
         * @param index
         * @return String speaker's name 
         */
        public String hasNameInCurUtt(int index){
            Utterance utt = utts.get(index);
            String cur_content = contentExtraction(utt);
//            boolean name_found = false;
            String name = "";
            //look for if there's any speaker name in cur utterance;
            StringTokenizer tokenizer = new StringTokenizer(cur_content);
            while(tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    //arraylist for getting name in the list. 4/3/11 3:06 PM
                    ArrayList<String> nameList = new ArrayList<String>(speaker_names); // convert hashmap to arrayList.
                    //nameList = (ArrayList)Arrays.asList(speaker_names.toArray());
                    for (int i = 0; i < nameList.size(); i ++ ){
                        if(nameList.get(i).contains(token.toLowerCase())){
//                            name_found = true;
                            name = nameList.get(i).toLowerCase();
                            break;
                        }
                    }
            }
            return name;
        }


        /**
         * m2w : This method counts the number of letters in a String
         * @param the string needs to count
         * @return counter
         */
        private int countLetters(String str) {

            if (str == null)
                return 0;

            int counter = 0;

            for (int i = 0; i < str.length(); i++) {

                if (Character.isLetter(str.charAt(i)))
                    counter++;
            }
            return counter;

        }

         
        /**
         * m2w : this calculates the length(word count) of the utt.
         * @param utt
         * @return length
         */
        private int lengthCal(Utterance utt){

            String content = contentExtraction(utt);
            int turn_length = ParseTools.wordCountChinese(content);

            return turn_length;
        }

        
        /**
         * m2w : xin's method, extract content from an utterance and filter it, removing emo and punctuations.
         * @param utterance
         * @return
         */
        private static String contentExtraction(Utterance utterance)
        {
            String content = utterance.getContent().toLowerCase();
            content = ParseTools.removeEmoticons(content);
//            content = ParseTools.removePunctuation(content);
            return content;
        }

        /**
         * m2w : xin's method, calculate the hit count. and save to the static var "hit".
         * @param curr_index
         * @param sysTurnNo
         * @param code
         */
        private void evaluate(int curr_index, int sys_turn, String which_case){
//                System.out.println("in eval" + curr_index + "|" + sys_turn);
		String curr_turn_no = utts.get(curr_index).getTurn();
		String link_to = utts.get(curr_index).getRespTo();
		if(link_to.indexOf(":")!=-1){
			String[] lkto = link_to.split(":");
			int anno_turn =0;
			if(lkto[1].contains(".")){
				anno_turn = (int) Double.parseDouble(lkto[1]);
			}else{
				anno_turn = Integer.parseInt(lkto[1]);
			}
//                        if(anno_turn - sysTurnNo > 50) System.out.println("error is here"); 
			if(sys_turn == anno_turn){
				hit++;
                                if(doHitReport){
                                    this.genReport(curr_turn_no, sys_turn, anno_turn, which_case, "HIT");
                                }
                                //added long and short statistics
                                if(which_case.startsWith("Long:")){
                                    long_hit++;
                                }
                                if(which_case.startsWith("Short:")){
                                    short_hit++;
                                }
                                if(which_case.startsWith("Short: cnd:")){
                                    short_cnd_hit++;
                                }
                                if(which_case.startsWith("Long: cnd:")){
                                    long_cnd_hit++;
                                }
                        }else{
                            if(doMissReport){
                                this.genReport(curr_turn_no, sys_turn, anno_turn, which_case, "MISSED");
                            }
                        }
                }
        }
	
         private void evaluate(int curr_index, int sys_turn, String which_case, int rank){
//                System.out.println("in eval: " + curr_index + "|" + sys_turn);
		String curr_turn_no = utts.get(curr_index).getTurn();
		String link_to = utts.get(curr_index).getRespTo();
//                System.out.println("link to: " + link_to);
		if(link_to.indexOf(":")!=-1){
			String[] lkto = link_to.split(":");
			int anno_turn =0;
			if(lkto[1].contains(".")){
				anno_turn = (int) Double.parseDouble(lkto[1]);
			}else{
				anno_turn = Integer.parseInt(lkto[1]);
			}
//                        if(anno_turn - sysTurnNo > 50) System.out.println("error is here"); 
			if(sys_turn == anno_turn){
				hit++;
                                if(doHitReport){
                                    this.genReport(curr_turn_no, sys_turn, anno_turn, which_case, "HIT");
                                    switch (rank) {
                                        case 4: {hits4++; break;}
                                        case 5: {hits5++; break;}
                                        case 6: {hits6++; break;}
                                        case 7: {hits7++; break;}
                                        case 8: {hits8++; break;}
                                        case 9: {hits9++; break;}
                                        case 10: {hits10++; break;}
                                            
                                    }
                                }
                                //added long and short statistics
                                if(which_case.startsWith("Long:")){
                                    long_hit++;
                                }
                                if(which_case.startsWith("Short:")){
                                    short_hit++;
                                }
                                if(which_case.startsWith("Short: cnd:")){
                                    short_cnd_hit++;
                                }
                                if(which_case.startsWith("Long: cnd:")){
                                    long_cnd_hit++;
                                }
                        }else{
                            if(doMissReport){
                                this.genReport(curr_turn_no, sys_turn, anno_turn, which_case, "MISSED");
                            }
                        }
                }
        }
	

        /**
         * m2w : xin's method. Looking for identical nouns in previous utts(for NOUN_SIM turns, which is now set to 5)
         * @param srcArr
         * @param destArr
         * @return
         */
        private boolean nounMatch(ArrayList<String> srcArr, ArrayList<String> destArr)
        {
            if(srcArr.size() == 0 || destArr.size() == 0)
                    return false;
            else
            {
                    for(int i=0; i<srcArr.size(); i++)
                    {
                            String srcWord = srcArr.get(i);
                            for(int j=0; j<destArr.size(); j++)
                            {
                                    String destWord = destArr.get(j);
                                    if(srcWord.equalsIgnoreCase(destWord))
                                            return true;
                            }
                    }
                    return false;
            }
        }

        /**
         * m2w : this method does the error analyze, it print's the code, automated result and annotated result for comparation.
         * @param curr_turn_no
         * @param auto_turn_no
         * @param anno_turn_no
         * @param code
         */
        public void errorAnalysis(String curr_turn_no, int auto_turn_no, int anno_turn_no, int code){
		System.out.println("Turn " + curr_turn_no + "(" + utts.get(Integer.parseInt(curr_turn_no)-1).getSpeaker() + "): " +
		utts.get(Integer.parseInt(curr_turn_no)-1).getContent());
		System.out.println("Code: " + code);
		System.out.println("System thought it linked to " + auto_turn_no + "(" + utts.get(auto_turn_no - 1).getSpeaker() + "): " + utts.get(auto_turn_no - 1).getContent());
		System.out.println("Annotated linked to " + anno_turn_no + "(" + utts.get(anno_turn_no - 1).getSpeaker() + "): " + utts.get(anno_turn_no - 1).getContent());
		System.out.println();
	}

        /**
         * m2w : analyze the repsonse_to . 3/28/11 3:25 PM
         * @param curr_turn_no
         * @param auto_turn_no
         * @param anno_turn_no
         * @param code
         */
        public void genReport(String curr_turn_no, int auto_turn_no, int anno_turn_no, String which_case, String hitOrNot){
            if(doGenReport){
//                System.out.println("in gen");
                Utterance cUtt = utts.get(Integer.parseInt(curr_turn_no) - 1);
                Utterance sUtt = utts.get(auto_turn_no - 1);
                Utterance aUtt = utts.get(anno_turn_no - 1);
                System.out.println(hitOrNot + "!!!   " + " -- " + which_case);
		System.out.println("turn: "+ curr_turn_no + "(" + cUtt.getSpeaker() + "): " + cUtt.getContent() + "  |  C["+ cUtt.getCommActType() +"]D["+ cUtt.getTag() +"]");
		System.out.println("Syst: "+ auto_turn_no + "(" + sUtt.getSpeaker() + "): " + sUtt.getContent() + "  |  C["+ sUtt.getCommActType() +"]D["+ sUtt.getTag() +"]");
		System.out.println("Anno: "+ anno_turn_no + "(" + aUtt.getSpeaker() + "): " + aUtt.getContent() + "  |  C["+ aUtt.getCommActType() +"]D["+ aUtt.getTag() +"]");
		System.out.println();
                
            }
	}

        private boolean isChineseStr(String s){
            boolean is = true;
            if(s != null && s.length() != 0){
                for(int i=0; i<s.length(); i++){
                    Character c = s.charAt(i);
                    String temp = c.toString();
                    if (!temp.matches("[\\u4E00-\\u9FA5]")){
                        is = false;
                    }
                }
            }
            
            return is;
        }
        
//      ========================================testing methods===========================================

        /**
         * m2w : this is for checking the passed-in utts. see if have punctuations
         * @param utts
         * @param where
         */
        public static void checkUttsInCMDLine(ArrayList<Utterance> utts, String where){

            for(int i = 0; i < utts.size(); i ++){
                System.out.println("utts in " + where + " : " + utts.get(i).getContent());
            }
            
        }

        /**
         * m2w: testing , checking the turn numbers passed in , 4/5/11 12:12 PM, added 1 turn in lauren_cheney file, cuz it's started with turn 2.
         * @param utts
         */
        public static void checkUttTurnsInCMDLine(ArrayList<Utterance> utts){
            
            for(int i = 0; i < utts.size(); i ++){
                System.out.println("turn No: " + utts.get(i).getTurn() + " = content: " + utts.get(i).getContent());
            }
        }

        /**
         * m2w: testing , checking the taged comm_act_type
         * @param utts
         */
        public static void checkCAtypeInCMDLine(ArrayList<Utterance> utts){

            for(int i = 0; i < utts.size(); i ++){
                System.out.println("turn No: " + utts.get(i).getTurn() + "C["+ utts.get(i).getCommActType() +"] = content: " + utts.get(i).getContent());
            }
        }

        /**
         * m2w: testing , checking the taged comm_act_type
         * @param utts
         */
        public static void checkResInCMDLine(ArrayList<Utterance> utts){

            for(int i = 0; i < utts.size(); i ++){
                System.out.println("turn No: " + utts.get(i).getTurn() + "R["+ utts.get(i).getTag() +"] = content: " + utts.get(i).getContent());
            }
        }

//      ========================================getters & setters===========================================

        // extract all nouns in each utterance content and save in arraylist
        private ArrayList<String> getNouns(String content){
            String noEmotes = ParseTools.removeEmoticons(content);
                    String tagged = StanfordPOSTagger.tagString(noEmotes).trim();
                    String [] tagsplit = tagged.split("\\s+");
                    ArrayList <String> tokens = getTokens(tagsplit);
            return tokens;
        }

        private ArrayList<String> getTokens(String[] tokens){
            ArrayList<String> retval = new ArrayList<String>();
            for(int i=0; i<tokens.length; i++){
                    String tag = ParseTools.getTag(tokens[i]);
                    String word = ParseTools.getWord(tokens[i]);
                    boolean is_noun = ParseTools.isNoun(tag);
                    if(is_noun)
                            retval.add(word);
            }
            return retval;
        }

        public TreeMap<Integer,String> getMap(){
            return map;
        }

        public ArrayList<Utterance> getUtts(){
            return utts;
        }
        // getters and setters
        private void setNameUtt(Utterance name_utt){
            this.name_utt = name_utt;
        }


        private Utterance getNameUtt(){
            return name_utt;
        }

        /**
         * m2w: the commen set resp_to method for : looking back several utts for resp_to, i is the iteration number, this case use this method.
         * @param index curr utt index
         * @param where_to which prev utt index, link_to's turn number = index - i - 1
         * @param utt current utt object
         * @param code which code is the case?
         * @last 4/15/11 3:00 PM
         */
        private void setRespTo(int index, int where_to, Utterance utt, String which_case){

            //check before set. 4/16/11 3:47 PM\
            //excluded similarity case doing checkResTo before set. 4/18/11 10:50 AM
            //included long sim case. 4/20/11 1:27 PM
//            int a = where_to;
//            if(which_case.contains("similarity") && which_case.toLowerCase().contains("short")){
//            }else{
//                where_to = this.checkRespToBeforeSet(index, where_to);
//                if(a != where_to){
//                    which_case = which_case + " check before set";
//                }
                
//            }
//            System.out.println("in setResto" + index + "|" + where_to);
                

//                map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(where_to+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-where_to));
                String sysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(where_to+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-where_to);
                evaluate(index,Integer.parseInt(utts.get(index).getTurn())-where_to,which_case);
                utt.setRespTo(sysRespTo);
    }//ends method

        private void setResToTN(int index, int res_to_TN, Utterance curr_utt, String which_case, int rank){
//            System.out.println("in set res-to");
            String sysRespTo = utts.get(res_to_TN -1).getSpeaker() + ":" + res_to_TN;
//            System.out.println(sysRespTo);
            evaluate(index, res_to_TN, which_case, rank);
            curr_utt.setRespTo(sysRespTo);
//            System.out.println("res_to after set: " + utts.get(index).getRespTo());
//            System.out.println();
            
        }
        /**
         * m2w : this method parse through several utts from the utt is going to set-to to index utt, see if there is any matching cases that needs to skip or modify.
         * @param index
         * @param where_to_
         * @return where to
         * @last 4/16/11 3:45 PM
         */
        private int checkRespToBeforeSet(int index, int where_to_){
            //            if(index-(where_to+1) > 0){
//                Utterance nowToUtt = utts.get(index-(where_to));
//                String nowToName = nowToUtt.getSpeaker();
//                for (int i = where_to; i > 0; --i){
//                    Utterance prevNextUtt = utts.get(index - i);
//                    String prevNextToName = prevNextUtt.getSpeaker();
                    ///but case---------------------------checking but in 5 chars. 4/15/11 2:05 PM
                    //check if the next utt has the same speaker and start's with but. then should be link to it, extract this out later 4/3/11 11:52 AM
//                    String prevNextUtt_raw_content = prevNextUtt.getContent().toLowerCase();
//                    char[] a = prevNextUtt_raw_content.toCharArray();
//                    char[] b = new char[5];
//                    if(a.length > 5){//a's length must be over 5 to proceed the judge.
//                        for(int j = 0; j < 5; j++){
//                            b[j] = a[j];
//                        }
//                        String butCheck = new String(b);
//                        if(prevNextToName.equals(nowToName) && butCheck.contains("but")){
//                            where_to = i;
//                        }//ends but case
//                    }//ends if a.length > 5
            int where_to = where_to_;
            Utterance indexUtt = utts.get(index);
            //m2w: added lengthcal < 5 , not doing for long utts.4/18/11 9:42 AM
            if(index-(where_to+1) > 0 
//                    && this.lengthCal(indexUtt) < SHORT_THRESHOLD //4/20/11 10:52 AM
                    ){
                Utterance nowToUtt = utts.get(index-(where_to));
                String nowToName = nowToUtt.getSpeaker();
                String nowToTurn = nowToUtt.getTurn();
                
//                System.out.println("nowTo: " + nowToTurn + ", spk: " + nowToName);
                for (int i = where_to-1; i > 0; i--){
                    Utterance prevNextUtt = utts.get(index - i);
                    String prevNextName = prevNextUtt.getSpeaker();
                    String prevNextTurn = prevNextUtt.getTurn();
//                    System.out.println(i + "prevTo: " + prevNextTurn + ", spk: " + prevNextName);

                    //m2w: added lencal > 1 .4/18/11 9:36 AM
                    if(prevNextName.equals(nowToName) && prevNextUtt.getCommActType().equals("continuation-of") && this.lengthCal(prevNextUtt) > 1){
                        where_to = i;
                    }                    
                }//ends looping forward
            }//ends if > 0
            return where_to;            
        }//ends method

        /**
         * m2w : this set method is used for the case that have got the resp_to we want to set before setting. like code 310, 311, 312 in casematching. 4/3/11 12:04 PM
         * @param index current utt's index
         * @param asRespTo set the resp_to as the resp_to passed in 
         * @param utt current utt
         * @param code which code is it
         */
        private void setRespToAs(int index, String asRespTo, Utterance utt, String which_case){
            
//            map.put(Integer.parseInt(utt.getTurn()), (asRespTo));
            int sysRespTurn = Integer.parseInt(asRespTo.split(":")[1]);
            evaluate(index,sysRespTurn+1,which_case);
            utt.setRespTo(asRespTo);
//            break;
        }



}

//==============================================not-in-use methods==================================



        /**
         * m2w : code 37, check the short utt if has same word(length > 6 ) as the previous utt, if does, link to it.
         * @param index
         * @param found
         * @return found
         * @last 3/28/11 1:12 PM
         */
//        private boolean checkShtSameWord(int index, boolean found){
//            Utterance utt = utts.get(index);
//            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
//            String cur_content = contentExtraction(utt);
//            //m2w : look back ,loop through several utts, now set to 3, 3/28/11 1:19 PM
//            for(int i = 1; i<=SHORT_LOOK_BACK; i++){
//                Utterance prev_utt = null;
//                if(index >= i){
//                    prev_utt = utts.get(index-i);
//                    String prev_content = contentExtraction(prev_utt);
//                    if(!prev_utt.getSpeaker().toLowerCase().equals(curr_speaker)){
////                        convert the 2 utts' contents into arrays, check if they have the same word
//                        ArrayList<String> tempIndexUtt = new ArrayList(Arrays.asList(cur_content.split(" ", -1)));;
//                        ArrayList<String> tempCurrUttInLoop = new ArrayList(Arrays.asList(prev_content.split(" ", -1)));;
//                        int swHit = 0;
//                        for(int x = 0; x < tempIndexUtt.size(); x++){//index utt loop
//                            if(tempIndexUtt.get(x).length() > WORD_SIM_LENGTH_CHECK){//now set to 6
//                                for(int y = 0; y < tempCurrUttInLoop.size(); y++){//curr utt in loop's loop
//                                    if (tempIndexUtt.get(x).equals(tempCurrUttInLoop.get(y))){
//                                        swHit ++;
//                                    }
//                                }
//                            }
//                        }//ends index utt for loop
//                        if(swHit > 0){
//                            found = true;
////                            map.put(Integer.parseInt(utt.getTurn()), (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i));
////                            String sysRespTo = (utts.get(Integer.parseInt(utts.get(index).getTurn())-(i+1))).getSpeaker() + ":" + (Integer.parseInt(utts.get(index).getTurn())-i);
////                            evaluate(index,Integer.parseInt(utts.get(index).getTurn())-i,31);
////                            utt.setRespTo(sysRespTo);
//                            this.setRespTo(index, i, utt, "Same Word Check");
//                            break;
//                        }
//                    }//ends same name check
//                }//ends if index > i
//            }//ends outter for loop
//            return found;
//        }//ends method
    
        /**
         * m2w : changed the compare utt which was compare by exact word match to synonym.
         * @param index
         * @param curThreshold
         * @param found
         * @param which_case
         * @return found
         * @last 4/10/11 12:51 PM
         */
//        private boolean calUttSimBySynon(int index, int lookBackTresh, double simTresh,  boolean found, String which_case){
//
//            TreeMap<Double,Integer> sims = new TreeMap<Double,Integer>(); //m2w : for similarity calculation 3/23/11 10:04 AM
//            int res_to = 0;
////            boolean found_ = found;
//            String cur_content = contentExtraction(utts.get(index));
//            Utterance utt = utts.get(index);
//            String curr_speaker = utts.get(index).getSpeaker().toLowerCase();
////            Wordnet wn = new Wordnet();
//            
//            for (int i = 0; i < lookBackTresh; i++){
//                if(index >= i){
//                    Utterance prev_utt = utts.get(index-i);
//                    if(!prev_utt.getSpeaker().toLowerCase().equals(curr_speaker)){
//                        String pre_content = contentExtraction(prev_utt);
//                        double sim = CommunicationLinkX.compareUttsBySynon(cur_content, pre_content);// change this line 4/10/11 12:24 PM creating new comparebysynon method in this class
//                        System.err.println(">cur: " + cubefore setr_content);
//                        System.err.println(">pre: " + pre_content);
//                        System.err.println(sim);
//                        int pre_turn_no = (Integer.parseInt(utts.get(index).getTurn()))-i;
//                        sims.put(sim, pre_turn_no);
//                    }else{ // if same speaker
//                        continue;
//                    }//ends if same speaker
//                }//ends if index >= i
//            }//ends for loop
//
//            if(sims.size() > 0){
//                // get the highest similarity value
//                double best_sim = sims.keySet().iterator().next();
//                System.err.println(">best: " + best_sim);
//                //set the target to the utterance of its sim value over threshold
//                if(best_sim > simTresh){
//
//                    found = true;
//                    res_to = sims.get(best_sim);
//                    this.setRespTo(index, res_to, utt, which_case);
//                }
//
//            }
//            return found;
//        }//ends method







    /*
     * m2w : commented all old methods for backup, copied some useful ones for editing. 3/20/11 3:25 PM
     */
    






    // ===========================================old methods=======================================

