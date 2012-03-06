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
        //init all instance var maps
        PopMap = new HashMap<String, Double>(); //pop final map
        NameMap = new HashMap<String, Double>(); // for init
        ITCMMap = new HashMap<String, Double>(); //4 sub-method maps
        CDMMap = new HashMap<String, Double>();
        DWLMap = new HashMap<String, Double>();
        TFMMap = new HashMap<String, Double>();
        POMMap = new HashMap<String, Double>();
        for(Utterance u : Utts){
            String tempSpk = u.getSpeaker();
            NameMap.put(tempSpk, 0.0);
        }
        PopMap.putAll(NameMap);
        ITCMMap.putAll(NameMap);
        CDMMap.putAll(NameMap);
        DWLMap.putAll(NameMap);
        TFMMap.putAll(NameMap);
        POMMap.putAll(NameMap);
    }
    
    public void calPursuitOfPower(){
        this.calPOP();
        ArrayList<ArrayList> PopList = new ArrayList();//converting hashmap to arraylist for sorting.
        PopList = this.sortAndConvertMapToArrayList(PopMap);
        if(doFinalPrintOut){
            System.out.println("@Pursuit of Power");
            for(ArrayList a : PopList){
                String tmpSpk = (String)a.get(0);
                Double popScore = (Double)(a.get(1));
                if(!tmpSpk.equalsIgnoreCase("ilspersonnel")){
                    System.out.println(tmpSpk + " : " + popScore );
                }
            }
            System.out.println("person pursuing power: 1." + (String)PopList.get(0).get(0) + " and 2." + (String)PopList.get(1).get(0));
        }
        
    }
//    ============================================ top level cal methods =================================================
    private void calPOP(){
        //calculate pop
        //1.Involved Topic COntrol Measure (ITCM)
        ITCMMap = this.calITCM();
        //2.Cumulative Disagreement Measure (CDM)
        CDMMap = this.calCDM();
        //3.Disagreement with Leader (DWL)
        DWLMap = this.calDWL();
        //4.Tension Focus Measure (TFM)
        TFMMap = this.calTFM();
        //5.Positioning Measure (POM)
        POMMap = this.calPOM();
        //5.average.
        for(String spk : PopMap.keySet()){
            Double tmpITCM = ITCMMap.get(spk);
            Double tmpCDM = CDMMap.get(spk);
            Double tmpDWL = DWLMap.get(spk);
            Double tmpTFM = TFMMap.get(spk);
            Double tmpPOM = POMMap.get(spk);
            Double avgPop = 0.0;
            if(tmpITCM!=null && tmpCDM!=null && tmpDWL!=null && tmpTFM!=null && tmpPOM!=null){    
                avgPop = (tmpITCM + tmpCDM + tmpDWL + tmpTFM + tmpPOM) / 5.0 ;
            }
            PopMap.put(spk, avgPop);
        }
    }
    
    /**
     * m2w: this method calculates pop using Involved Topic COntrol Measure.
     * //1. init all maps, topic ctrl, involvement, local itcm, 
     *  //2. get and set these maps.
     *  //3. add the 2 maps and average
     *  //4. add to pop.
     */
    private HashMap<String, Double> calITCM(){
       //1. init all maps, topic ctrl, involvement, local itcm, 
        HashMap<String, Double> localMapTPctrl = new HashMap();
        HashMap<String, Double> localMapInv = new HashMap();
        HashMap<String, Double> localMapITCM = new HashMap();
        localMapTPctrl.putAll(NameMap);
        localMapInv.putAll(NameMap);
        localMapITCM.putAll(NameMap);
       //2. get and set these maps.
        for(String spk : parts.keySet()){
            Double tpctrl = parts.get(spk).getTC().getPower();// topic control 
            Double inv = parts.get(spk).getInv().getPower();
            localMapTPctrl.put(spk, tpctrl);
            localMapInv.put(spk, inv);
        }
       //3. add the 2 maps and average
        localMapITCM = this.adding2MapsAndAverageIt(localMapTPctrl, localMapInv) ;
        //5. testing print.
        ArrayList<ArrayList> ITCMList = new ArrayList();
        ITCMList = this.sortAndConvertMapToArrayList(localMapITCM);
        if(doFinalPrintOut){
            System.out.println("@ITCM");
            for(ArrayList a : ITCMList){
                System.out.println( (String)(a.get(0)) + " : " + (Double)(a.get(1)) );
            }
        }
        if(doAnalysisPrintOut){
            System.out.println("@ITCM");
            System.out.println("topic ctrl map: " + localMapTPctrl.toString());
            System.out.println("involvement map: " + localMapInv.toString());
            System.out.println("itcm map before pop: " + localMapITCM.toString());
            System.out.println("pop map:" + PopMap.toString());
        }
        return localMapITCM;
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
    private HashMap<String, Double> calCDM(){
//       1. initialize all maps
        HashMap<String, Double> localMapTPctrl = new HashMap();
        HashMap<String, Double> localMapTKctrl = new HashMap();
        HashMap<String, Double> localMapDis = new HashMap();
        HashMap<String, Double> localMapTK_TPctrl = new HashMap();//storing higher score.
        HashMap<String, Double> localMapCDM = new HashMap();//map before adding to pop map
        localMapTPctrl.putAll(NameMap);
        localMapTKctrl.putAll(NameMap);
        localMapDis.putAll(NameMap);
        localMapTK_TPctrl.putAll(NameMap);
        localMapCDM.putAll(NameMap);
        //2. set all maps
        for(String spk : parts.keySet()){
            Double tpctrl = parts.get(spk).getTC().getPower();// topic control 
            Double tkctrl = parts.get(spk).getDI();             //task control
            Double dis = parts.get(spk).getDisagreement();
            if(dis < 0){
                dis = 0.0;
            }
            localMapTPctrl.put(spk, tpctrl);
            localMapTKctrl.put(spk, tkctrl);
            localMapDis.put(spk, dis);
        }
        //3. compare 1 and 2 for each speaker. build local map and assign the higher score to each speaker. can't guarentee it's 100%.  cuz higher scores are from 2 percentages.
        for(String spk : localMapTPctrl.keySet()){
            Double tpctrl = localMapTPctrl.get(spk);
            Double tkctrl = localMapTKctrl.get(spk);
            Double maxCtrl = Math.max(tpctrl, tkctrl);
            localMapTK_TPctrl.put(spk, maxCtrl);
        }
        //4. add disagreement to local map. average
        localMapCDM = this.adding2MapsAndAverageIt(localMapTK_TPctrl, localMapDis);
        
        //6.testing print.
        ArrayList<ArrayList> CDMList = new ArrayList();
        CDMList = this.sortAndConvertMapToArrayList(localMapCDM);
        if(doFinalPrintOut){
            System.out.println("@CDM");
            for(ArrayList a : CDMList){
                System.out.println( (String)(a.get(0)) + " : " + (Double)(a.get(1)) );
            }
        }
        if(doAnalysisPrintOut){
            System.out.println("@CDM");
            System.out.println("topic ctrl map: " + localMapTPctrl.toString());
            System.out.println("task ctrl map: " + localMapTKctrl.toString());
            System.out.println("task and topic map: " + localMapTK_TPctrl.toString());
            System.out.println("disagreement map: " + localMapDis.toString());
            System.out.println("cmd map before pop: " + localMapCDM.toString());
            System.out.println("pop map:" + PopMap.toString());
        }
        return localMapCDM;
    }
    
    /**
     * m2w: this method calculates pop using Disagreement with Leader Measure.
     *   //1. get the leader of the dialogue
     *   //2. build local map of count.
     *   //3. get count of dis towards the leader, assign to each key in the local map.
     *   //4. build and add the local map of percent to the pop map.
     */
    private HashMap<String, Double> calDWL(){
        
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
//                System.out.println(lkto_spk);
                //if it links to the leader and current speaker is not the leader
                if((lkto_spk != null) && (tempSpk != null) && lkto_spk.equalsIgnoreCase(leaderName) && !tempSpk.equalsIgnoreCase(leaderName)){
                    Double tempCount = localMapCount.get(tempSpk);
                    localMapCount.put(tempSpk, tempCount + 1); // count + 1
                }
            }
        }
        //4. build and add the local map of percent to the pop map.
        HashMap<String, Double> localMapDWL = new HashMap();
        localMapDWL.putAll(NameMap);
        if(totalDis > 0){
            for(String spk : localMapCount.keySet()){
                Double tempDisCount = localMapCount.get(spk);
                Double tempPerc = tempDisCount / totalDis;
                localMapDWL.put(spk, tempPerc);
            }
        }

        //output
        ArrayList<ArrayList> DWLList = new ArrayList();
        DWLList = this.sortAndConvertMapToArrayList(localMapDWL);
        if(doFinalPrintOut){
            System.out.println("@DWL");
            for(ArrayList a : DWLList){
                System.out.println( (String)(a.get(0)) + " : " + (Double)(a.get(1)) );
            }
        }
        if(doAnalysisPrintOut){
            System.out.println("@DWL");
            System.out.println("total dis: " + totalDis);
            System.out.println("count map dis: " + localMapCount.toString());
            System.out.println("local DWL map before pop: " + localMapDWL);
            System.out.println(" pop map: " + PopMap.toString());
            System.out.println();
        }
        return localMapDWL;
    }
    
    /**
     * m2w: this method calculates pop using Tension Focus Measure.
     * //1.Disagree-Reject Target Index (DRT)
     * //2.Topical Disagreement Target Index (TDT)
     */
    private HashMap<String, Double> calTFM(){
        HashMap<String, Double> TFM_DRTMap = new HashMap<String, Double>();
        HashMap<String, Double> TFM_TDTMap = new HashMap<String, Double>();
        HashMap<String, Double> localTFMMap = new HashMap<String, Double>();
        //1.Disagree-Reject Target Index (DRT)
        TFM_DRTMap = this.calTFM_DRT();
        //2.Topical Disagreement Target Index (TDT)
        TFM_TDTMap = this.calTFM_TDT();
        //3. calculate TFM from 2 sub-maps.
        localTFMMap = TFM_DRTMap;
        //output
        ArrayList<ArrayList> TFMList = new ArrayList();
        TFMList = this.sortAndConvertMapToArrayList(localTFMMap);
        if(doFinalPrintOut){
            System.out.println("@TFM");
            for(ArrayList a : TFMList){
                System.out.println( (String)(a.get(0)) + " : " + (Double)(a.get(1)) );
            }
        }       
        return localTFMMap;
    }
    
    /**
     * m2w: this method calculates pop using Positioning Measure.
     * //1.calcualte conf-re
     * //2.calcualte offer-commit.
     */
    private HashMap<String, Double> calPOM(){
        int totalConf = 0;
        int totalComm = 0;
        HashMap<String, Double> localMapPomConf = new HashMap<String, Double>();
        HashMap<String, Double> localMapPomComm = new HashMap<String, Double>();
        HashMap<String, Double> localMapPom = new HashMap<String, Double>();
        HashMap<String, Double> localMapPomConfCount = new HashMap<String, Double>();
        HashMap<String, Double> localMapPomCommCount = new HashMap<String, Double>();
        localMapPomConf.putAll(NameMap);
        localMapPomComm.putAll(NameMap);
        localMapPom.putAll(NameMap);
        localMapPomConfCount.putAll(NameMap);
        localMapPomCommCount.putAll(NameMap);
        //1. parse utt list, adding count to local map
        for(Utterance u : Utts){
            String tempDaTag = u.getTag();
            String tempCATag = u.getCommActType();
            String tempSpk = u.getSpeaker();
            
            // adding  confirmation request count & offe-comit. 
            if(tempDaTag.toLowerCase().contains(CONFIRMATION_REQUEST) && tempCATag.equalsIgnoreCase(RESPONSE_TO)){
                totalConf++;
                String lkto_spk = u.getRespToSpk();
                Double tempCountConf = (Double) localMapPomConfCount.get(tempSpk);
                localMapPomConfCount.put(lkto_spk, tempCountConf+1);
            }
            
            if(tempDaTag.toLowerCase().contains(OFFER_COMMIT) && tempCATag.equalsIgnoreCase(RESPONSE_TO)) {
                totalComm++;
                String lkto_spk = u.getRespToSpk();
                Double tempCountComm = (Double) localMapPomCommCount.get(tempSpk);
                localMapPomCommCount.put(lkto_spk, tempCountComm+1);
            }
        }
        
        //2. transform conf & comm list from count to percentage.
        if(totalConf > 0.0){ //2/14/12 1:52 PM
            for(String spk : localMapPomConf.keySet()){
                Double spkConfCount = localMapPomConfCount.get(spk);
                Double percentage = (spkConfCount)/ (double) totalConf;
                localMapPomConf.put(spk, percentage);
            }
        }
        
        if(totalComm > 0.0){ //2/14/12 1:52 PM
            for(String spk : localMapPomComm.keySet()){
                Double spkConfCount = localMapPomCommCount.get(spk);
                Double percentage = (spkConfCount)/ (double) totalComm;
                localMapPomComm.put(spk, percentage);
            }
        }
        
        //3. adding
        localMapPom = this.adding2MapsAndAverageIt(localMapPomConf, localMapPomComm);
        
        ArrayList<ArrayList> POMList = new ArrayList();
        POMList = this.sortAndConvertMapToArrayList(localMapPom);
        if(doFinalPrintOut){
            System.out.println("@POM");
            for(ArrayList a : POMList){
                System.out.println( (String)(a.get(0)) + " : " + (Double)(a.get(1)) );
            }
        }
        if(doAnalysisPrintOut){
            System.out.println("@POM");
            System.out.println("total conf: " + totalConf);
            System.out.println("total comm: " + totalComm);
            System.out.println("count map Conf: " + localMapPomConfCount.toString());
            System.out.println("count map Comm: " + localMapPomCommCount.toString());
            System.out.println("local POM conf map: " + localMapPomConf);
            System.out.println("local POM conf map: " + localMapPomComm);
            System.out.println("local POM map before pop: " + localMapPom);
            System.out.println(" pop map: " + PopMap.toString());
            System.out.println();
        }
        
        return localMapPom;
    }
    
    /**
     * m2w: TFM 's Disagree-Reject Target Index (DRT)
     */
    private HashMap<String, Double> calTFM_DRT(){
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
        HashMap<String,Double> localTFM_DRTMap = new HashMap<String,Double> ();
//        localTFM_DRTMap = this.adding2MapsAndAverageIt(localMapPercent, localMapDis); 
        //4.1 testing only dis, no conf-re and re-to.
        localTFM_DRTMap = localMapDis;

        //output
        if(doAnalysisPrintOut){
            System.out.println("--- TFM_DRT ---");
            System.out.println("conf count map: " + localMapConf.toString());
            System.out.println("total conf count: " + totalConf);
            System.out.println("conf perc map: " + localMapPercent.toString());
            System.out.println("dis perc map: " +localMapDis.toString());
            System.out.println("local TFM_DRT Map before pop: " + localTFM_DRTMap.toString());
            System.out.println("pop map : " + PopMap.toString());
            System.out.println();
        }
        return localTFM_DRTMap;
    }
    
    /**
     * m2w: TFM 's Topical Disagreement Target Index (TDT)
     */
    private HashMap<String, Double> calTFM_TDT(){
        HashMap<String, Double> localTFM_TDTMap = new HashMap<String, Double>();
        return localTFM_TDTMap;
    }
    
    //    ======================================== sub level util methods =================================================
    
//    
//    /**
//     * m2w: this util method is for adding local percentage onto the pop-map total percentage.
//     * @param localMap 
//     */
//    private void addingPercentageToPopMap(HashMap<String,Double> localMap){
////        System.out.println("times: " + timesAddedToPopMap);
//        if(!localMap.isEmpty()){
//            if(timesAddedToPopMap==0){//if first time adding , do not average.
//                    for(String spk : localMap.keySet()){
//                    Double popPerc = PopMap.get(spk);
//                    Double localPerc = localMap.get(spk);
//                    Double sum = (popPerc+localPerc);
//                    PopMap.put(spk, sum);
////                    System.out.println("pop:" + popPerc);
////                    System.out.println("local:" + localPerc);
////                    System.out.println("sum:" + sum);
//                }
//            }else{//if not, average.
//                for(String spk : localMap.keySet()){
//                    Double popPerc = PopMap.get(spk);
//                    Double localPerc = localMap.get(spk);
//                    Double Average = (popPerc+localPerc)/2;
////                    System.out.println("pop:" + popPerc);
////                    System.out.println("local:" + localPerc);
////                    System.out.println("Average:" + Average);
//                    PopMap.put(spk, Average);
//                }
//            }
//            timesAddedToPopMap ++;
//        }else{
//            System.err.println("error adding to pop map: localmap is empty");
//        }
//        
//    }
    
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
    
    /**
     * m2w: in order to make each 4 methods' output sorted, this methods helps.
     * @param map
     * @return 
     * @date 2/24/12 11:21 AM
     */
    private ArrayList<ArrayList> sortAndConvertMapToArrayList(HashMap<String,Double> map){
        ArrayList<ArrayList> list = new ArrayList();//converting hashmap to arraylist for sorting.
        for(String spk : map.keySet()){
            ArrayList subList = new ArrayList();
            subList.add(spk);
            subList.add(map.get(spk));
            list.add(subList);
        }
        Comparator c = new Comparator(){
            @Override
            public int compare(Object obj1, Object obj2){
                ArrayList sublist1 = (ArrayList)obj1;
                ArrayList sublist2 = (ArrayList)obj2;
                Double pop1 = (Double)sublist1.get(1);
                Double pop2 = (Double)sublist2.get(1);
                return pop2.compareTo(pop1);
            }
            
        };
        Collections.sort(list, c);
        return list;
    }
//    =================================== vars and consts =============================================
    //variables:
    private HashMap<String, Double> PopMap; //where the precentage of each speaker are stored.
    private ArrayList<Utterance> Utts;
    private HashMap<String, Speaker> parts;
    private Speaker leader;
    private HashMap<String, Double> NameMap; // an empty map, used for each local map initialization. 
    private int timesAddedToPopMap = 0;
    private HashMap<String, Double> ITCMMap; // stroing itcm score, for average pop.
    private HashMap<String, Double> CDMMap; // stroing cdm score, for average pop.
    private HashMap<String, Double> DWLMap; // stroing dwl score, for average pop.
    private HashMap<String, Double> TFMMap; // stroing tfm score, for average pop.
    private HashMap<String, Double> POMMap; // stroing pom score, for average pop.
    
    //constants:
    private final String DISAGREE_REJECT = "disagree-reject";
    private final String CONFIRMATION_REQUEST = "confirmation-request";
    private final String RESPONSE_TO = "response-to";
    private final String OFFER_COMMIT = "offer-commit";
    
    //print out control:
    private boolean doAnalysisPrintOut = false;
    private boolean doFinalPrintOut = true;
}
