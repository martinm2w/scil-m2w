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
    public PursuitOfPower(ArrayList<Utterance> utts_){
        //assigning list of the Utterances.
        this.Utts = utts_;
        //create pop-map for furture calculation.
        PopMap = new HashMap();
        for(Utterance u : Utts){
            String tempSpk = u.getSpeaker();
            PopMap.put(tempSpk, 0.0);
        }
        System.out.println(PopMap.keySet());
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
     */
    private void calCDM(){
        
    }
    
    /**
     * m2w: this method calculates pop using Disagreement with Leader Measure.
     */
    private void calDWL(){
        
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
    }
    
    
//    ======================================== sub level util methods =================================================
    /**
     * m2w: TFM 's Disagree-Reject Target Index (DRT)
     */
    private void calTFM_DRT(){
        int totalDis = 0;
        int totalConf = 0;
        HashMap<String, Double> localMapDis = new HashMap();
        HashMap<String, Double> localMapConf = new HashMap();
        HashMap<String, Double> localMapPercent = new HashMap();
        localMapDis.putAll(PopMap); // storing the count of DISAGREE_REJECT turns of 
        localMapConf.putAll(PopMap);
        localMapPercent.putAll(PopMap);
        
        //parse utt list, adding count to local map
        for(Utterance u : Utts){
            String tempDaTag = u.getTag();
            String tempSpk = u.getSpeaker();
            String tempCATag = u.getCommActType();
//            Double tempCountDis = (Double) localMapDis.get(tempSpk);
//            if(tempDaTag.contains(DISAGREE_REJECT) ){
//                totalDis++;
//                localMapDis.put(tempSpk, tempCountDis+1);
//            }
            if(tempDaTag.contains(CONFIRMATION_REQUEST) && tempCATag.contains(RESPONSE_TO)){
                totalConf++;
                String lkto_spk = u.getRespToSpk();
                Double tempCountConf = (Double) localMapConf.get(tempSpk);
                localMapConf.put(lkto_spk, tempCountConf+1);
            }
        }
        
        for(String spk : localMapPercent.keySet()){
           
            Double spkDisCount = localMapDis.get(spk);
            Double spkConfCount = localMapConf.get(spk);
            
            Double percentage = (spkCount)/ (double) totalDisAndConf;
            localMap.put(spk, percentage);
            System.out.println("local perc" + localMap.get(spk));
        }
            
        this.addingPercentageToPopMap(localMap);
    }
    
    /**
     * m2w: TFM 's Topical Disagreement Target Index (TDT)
     */
    private void calTFM_TDT(){
    
    }
    
    /**
     * m2w: this util method is for adding local percentage onto the pop-map total percentage.
     * @param localMap 
     */
    private void addingPercentageToPopMap(HashMap<String,Double> localMap){
        for(String spk : localMap.keySet()){
            Double popPerc = PopMap.get(spk);
            Double localPerc = localMap.get(spk);
            PopMap.put(spk, popPerc + localPerc);
        }
    }
    
//    =================================== vars and consts =============================================
    //variables:
    private HashMap<String, Double> PopMap; //where the precentage of each speaker are stored.
    private ArrayList<Utterance> Utts;
    
    //constants:
    private final String DISAGREE_REJECT = "disagree-reject";
    private final String CONFIRMATION_REQUEST = "confirmation-request";
    private final String RESPONSE_TO = "response-to";
}
