package edu.albany.ils.dsarmd0200.lu;

import java.util.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.lu.*;
import edu.albany.ils.dsarmd0200.evaltag.*;
/**
 *
 * @author [Ruobo ☺ m2w]
 * @date [Feb 7, 2012 - 1:32:57 PM]
 */
public class PursuitOfPower {
    
    
//    ========================================== constructor and public method =============================================
    /**
     * m2w: constructor, assign instance variables.
     * @param utts_ 
     */
    public PursuitOfPower(ArrayList<Utterance> utts_, HashMap<String, Speaker> parts_, Speaker leader_){
        //assigning list of the Utterances.
        this.Utts = utts_;
        this.parts = parts_;
        this.leader = leader_;
        //create pop-map for furture calculation.
        PopMap = new HashMap();
        NameMap = new HashMap();
        for(Utterance u : Utts){
            String tempSpk = u.getSpeaker();
            NameMap.put(tempSpk, 0.0);
        }
        PopMap.putAll(NameMap);
//        System.out.println(PopMap.keySet());
    }
    
    public void calPursuitOfPower(){
        this.calPOP();
        //print out results.
        System.out.println("========= Pursuit Of Power=========");
        for(String spk : PopMap.keySet()){
            System.out.println(spk + ": " + PopMap.get(spk));
        }
    }
//    ============================================ top level cal methods =================================================
    private void calPOP(){
        //calculate pop
        //1.Involved Topic COntrol Measure (ITCM)
        this.calITCM();
        //2.Cumulative Disagreement Measure (CDM)
        this.calCDM();
        //3.Disagreement with Leader (DWL)
        this.calDWL();
        //4.Tension Focus Measure (TFM)
        this.calTFM();
        
    }
    
    /**
     * m2w: this method calculates pop using Involved Topic COntrol Measure.
     */
    private void calITCM(){
        
    }
    
    /**
     * m2w: this method calculates pop using Cumulative Disagreement Measure.
     * 1. get and build topic control map
     * 2. get and build task control map
     * 3. get disagreement map
     * 4. compare 1 and 2 for each speaker. build local map and assign the higher score to each speaker. can't guarentee it's 100%.  cuz higher scores are from 2 percentages.
     * 5. add disagreement to local map. average
     * 6. add local map to pop map.
     * 7. leader should be 0.0
     */
    private void calCDM(){
        
        for(String spk : parts.keySet()){
            Double tpctrl = parts.get(spk).getTC().getPower();// topic control 
            Double tkctrl = parts.get(spk).getDI();
            Double dis = parts.get(spk).getDisagreement();
            System.out.println(spk + "tpctrl" + tpctrl);
            System.out.println(spk + "tkctrl" + tkctrl);
            System.out.println(spk + "dis" + dis);
        }
        
    }
    
    /**
     * m2w: this method calculates pop using Disagreement with Leader Measure.
     *   //1. get the leader of the dialogue
     *   //2. build local map of count.
     *   //3. get count of dis towards the leader, assign to each key in the local map.
     *   //4. build and add the local map of percent to the pop map.
     */
    private void calDWL(){
        
        Double totalDis = 0.0;
        //1. get the leader of the dialogue
        String leaderName = leader.getName();
        //2. build local map of count.
        HashMap<String, Double> localMapCount = new HashMap();
        localMapCount.putAll(NameMap);
        //3. get count of dis towards the leader, assign to each key in the local map.
        for(Utterance u : Utts){
            String DaTag = u.getTag();
            String tempSpk = u.getSpeaker();
            String tempCATag = u.getCommActType();
            if(DaTag.toLowerCase().contains(DISAGREE_REJECT) && tempCATag.equalsIgnoreCase(RESPONSE_TO)){
                totalDis++;
                String lkto_spk = u.getRespToSpk();
                //if it links to the leader and current speaker is not the leader
                if(lkto_spk.equalsIgnoreCase(leaderName) && !tempSpk.equalsIgnoreCase(leaderName)){
                    Double tempCount = localMapCount.get(tempSpk);
                    localMapCount.put(tempSpk, tempCount + 1); // count + 1
                }
            }
        }
        //4. build and add the local map of percent to the pop map.
        HashMap<String, Double> localMapPerc = new HashMap();
        localMapPerc.putAll(NameMap);
        for(String spk : localMapCount.keySet()){
            Double tempDisCount = localMapCount.get(spk);
            Double tempPerc = tempDisCount / totalDis;
            localMapPerc.put(spk, tempPerc);
        }

        //adding
        this.addingPercentageToPopMap(localMapPerc);
        
        if(doAnalysisPrintOut){
            System.out.println("---DWL---");
            System.out.println("total dis: " + totalDis);
            System.out.println("count map dis: " + localMapCount.toString());
            System.out.println("perc map dis: " + localMapPerc);
            System.out.println(" pop map: " + PopMap.toString());
            System.out.println();
        }
    }
    
    /**
     * m2w: this method calculates pop using Tension Focus Measure.
     * //1.Disagree-Reject Target Index (DRT)
     * //2.Topical Disagreement Target Index (TDT)
     */
    private void calTFM(){
        //1.Disagree-Reject Target Index (DRT)
        this.calTFM_DRT();
        //2.Topical Disagreement Target Index (TDT)
        this.calTFM_TDT();
        
    }
    
    
    /**
     * m2w: TFM 's Disagree-Reject Target Index (DRT)
     */
    private void calTFM_DRT(){
        int totalConf = 0;
        HashMap<String, Double> localMapDis = new HashMap();
        HashMap<String, Double> localMapConf = new HashMap();
        HashMap<String, Double> localMapPercent = new HashMap();
        localMapDis.putAll(NameMap); // storing the count of DISAGREE_REJECT turns of 
        localMapConf.putAll(NameMap); //stroing the count of conf turns
        localMapPercent.putAll(NameMap); //stroing the count of conf perc
        
        //1. parse utt list, adding count to local map
        for(Utterance u : Utts){
            String tempDaTag = u.getTag();
            String tempCATag = u.getCommActType();
            String tempSpk = u.getSpeaker();
            
            // adding  confirmation request count. 
            if(tempDaTag.toLowerCase().contains(CONFIRMATION_REQUEST) && tempCATag.equalsIgnoreCase(RESPONSE_TO)){
                totalConf++;
                String lkto_spk = u.getRespToSpk();
                Double tempCountConf = (Double) localMapConf.get(tempSpk);
                localMapConf.put(lkto_spk, tempCountConf+1);
            }
        }
        
        //2. transform conf list from count to percentage.
        if(totalConf > 0.0){ //2/14/12 1:52 PM
            for(String spk : localMapPercent.keySet()){
                Double spkConfCount = localMapConf.get(spk);
                Double percentage = (spkConfCount)/ (double) totalConf;
                localMapPercent.put(spk, percentage);
            }
        }
            
        
        //3. getting dis percentage from the part object that has passed in.
        for(String spk : parts.keySet()){
            Double disPerc = parts.get(spk).getLnkto_disagreement_();
            if(disPerc < 0.0){//2/14/12 1:52 PM
                disPerc = 0.0;
            }
            localMapDis.put(spk, disPerc);
        }
        
        //4. adding up disagreement percentage to the local percentage list with confirmation-request percentage in it. seperated for furture improvements.
        HashMap<String,Double> tempMap = new HashMap<String,Double> ();
        tempMap = this.adding2MapsAndAverageIt(localMapPercent, localMapDis); 
        
        //adding to popmap
        this.addingPercentageToPopMap(tempMap);

        if(doAnalysisPrintOut){
            System.out.println("--- TFM_DRT ---");
            System.out.println("conf count map: " + localMapConf.toString());
            System.out.println("total conf count: " + totalConf);
            System.out.println("conf perc map: " + localMapPercent.toString());
            System.out.println("dis perc map: " +localMapDis.toString());
            System.out.println("temp perc map before adding: " + tempMap.toString());
            System.out.println("pop map : " + PopMap.toString());
            System.out.println();
        }
        
    }
    
    /**
     * m2w: TFM 's Topical Disagreement Target Index (TDT)
     */
    private void calTFM_TDT(){
    
    }
    
    //    ======================================== sub level util methods =================================================
    
    
    /**
     * m2w: this util method is for adding local percentage onto the pop-map total percentage.
     * @param localMap 
     */
    private void addingPercentageToPopMap(HashMap<String,Double> localMap){
        if(!localMap.isEmpty()){
            for(String spk : localMap.keySet()){
                Double popPerc = PopMap.get(spk);
                Double localPerc = localMap.get(spk);
                Double Average = (popPerc+localPerc)/2;
                PopMap.put(spk, popPerc + Average);
            }
        }else{
            System.err.println("error adding to pop map: localmap is empty");
        }
        
    }
    
    /**
     * m2w: for adding 2 maps together and calculate their average percentage.
     * using map1 's spk for iteration.
     */
    private HashMap<String,Double> adding2MapsAndAverageIt(HashMap<String,Double> map1, HashMap<String,Double> map2){
        HashMap<String,Double> map = new HashMap<String,Double>();
        if(!map1.isEmpty() && !map2.isEmpty()){            
            for(String spk : map1.keySet()){
                Double perc1 = map1.get(spk);
                Double perc2 = map2.get(spk);
                Double average = (perc1 + perc2 )/2;
                map.put(spk, average);
            }
        }else{
            System.err.println("error adding 2 maps: emtpy map found.");
        }
        return map;
    }
//    =================================== vars and consts =============================================
    //variables:
    private HashMap<String, Double> PopMap; //where the precentage of each speaker are stored.
    private ArrayList<Utterance> Utts;
    private HashMap<String, Speaker> parts;
    private Speaker leader;
    private HashMap<String, Double> NameMap; // an empty map, used for each local map initialization. 
    
    //constants:
    private final String DISAGREE_REJECT = "disagree-reject";
    private final String CONFIRMATION_REQUEST = "confirmation-request";
    private final String RESPONSE_TO = "response-to";
    
    //print out control:
    private boolean doAnalysisPrintOut = true;
    private boolean doFinalPrintOut = true;
}
