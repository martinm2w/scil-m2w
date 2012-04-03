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
//        DWLMap = new HashMap<String, Double>();
        TFMMap = new HashMap<String, Double>();
        POMMap = new HashMap<String, Double>();
        ConfMap = new HashMap<String, Double>();
        NCMMap = new HashMap<String, Double>();
        for(Utterance u : Utts){
            String tempSpk = u.getSpeaker();
            NameMap.put(tempSpk, 0.0);
        }
        PopMap.putAll(NameMap);
        ITCMMap.putAll(NameMap);
        CDMMap.putAll(NameMap);
//        DWLMap.putAll(NameMap);
        TFMMap.putAll(NameMap);
        POMMap.putAll(NameMap);
        ConfMap.putAll(NameMap);
        NCMMap.putAll(NameMap);
        language = Settings.getValue(Settings.LANGUAGE);
    }
    
    public void calPursuitOfPower(){
        this.calPOP();//calucaltions done this line.
        ArrayList<ArrayList> PopList = new ArrayList<ArrayList>();//converting hashmap to arraylist for sorting.
        PopList = this.sortAndConvertMapToArrayList(PopMap);
        if(doFinalPrintOut){
            System.out.println("@Pursuit of Power");
            for(ArrayList a : PopList){
                String tmpSpk = parts.get((String)(a.get(0))).getOriName();
                Double popScore = (Double)(a.get(1));
                if(!tmpSpk.equalsIgnoreCase("ilspersonnel")){
                    System.out.println(tmpSpk + " : " + popScore );
                }
            }
            System.out.println("@Pursuit of Power:");
            
            if(language.equalsIgnoreCase("english") && MODE.equals("simple")){
                this.decidePopAndPrintEnglish(PopList); // decide 1 or 2 for english.
            }else if(language.equalsIgnoreCase("chinese") && MODE.equals("simple")){
                this.decidePopAndPrintChinese(PopList); // decide 1 or 2 for english.
            }else if (MODE.equals("std")){
                this.decidePopAndPrintSTD(PopList);
            }else if (MODE.equals("cov")){
                
            }
            
            
            System.out.println("Confidence:");
            System.out.println("0.7" );
        }
        
    }
//    ============================================ top level cal methods =================================================
    private void calPOP(){
        //calculate pop
        //1.Tension Focus Measure (TFM) //moved to 1st because itcm uses tf score.
        TFMMap = this.calTFM();
        //2.Involved Topic COntrol Measure (ITCM)
        ITCMMap = this.calITCM();
        //3.Cumulative Disagreement Measure (CDM)
        CDMMap = this.calCDM();
        //4.Disagreement with Leader (DWL)
//        DWLMap = this.calDWL();
        //5.Positioning Measure (POM)
        POMMap = this.calPOM();
        //6.calculate confirmation request.(Conf)
        ConfMap = this.calConfRequestScore();
        //7.get network centrality map (NCM)
        NCMMap = this.calNCM();
        
        //8.average.
        ArrayList<HashMap<String, Double>> averageList = new  ArrayList<HashMap<String, Double>>();
        
        if(language.equalsIgnoreCase("english")){
            averageList.add(this.weightingMap(TFMMap, TFMWGT_EN));
            averageList.add(this.weightingMap(ITCMMap, ITCMWGT_EN));
            averageList.add(this.weightingMap(CDMMap, CDMWGT_EN));
            averageList.add(this.weightingMap(POMMap, POMWGT_EN));
            averageList.add(this.weightingMap(NCMMap, NCMWGT_EN));
        }else if(language.equalsIgnoreCase("chinese")){
            averageList.add(this.weightingMap(TFMMap, TFMWGT_CN));
            averageList.add(this.weightingMap(ITCMMap, ITCMWGT_CN));
            averageList.add(this.weightingMap(CDMMap, CDMWGT_CN));
            averageList.add(this.weightingMap(POMMap, POMWGT_CN));
            averageList.add(this.weightingMap(NCMMap, NCMWGT_CN));
        }
        
        PopMap = this.addingMaps(averageList);
        
//        PopMap = this.addingMapsAndAverageIt(averageList);
        
        //normalizing 3/26/12 1:10 PM
//        PopMap = this.normalizingMap(PopMap);
        
        //deleting leader, 3/28/12 2:23 PM
        PopMap = this.deleteLeader(PopMap);
    }
    
    /**
     * m2w: this method calculates pop using Involved Topic COntrol Measure.
       * //1. init all maps,  involvement, localitcm;
     *  //2. get and set these maps.
     *  //3. add the 2 maps and average
     *  //4. add to pop.
     *  @date 3/22/12 1:07 PM
     */
    private HashMap<String, Double> calITCM(){
       //1. init all maps, topic ctrl, involvement, local itcm, 
//        HashMap<String, Double> localMapTPctrl = new HashMap(); // commented on 3/6/12 2:33 PM
        HashMap<String, Double> localMapInv = new HashMap<String, Double>();
        HashMap<String, Double> localMapITCM = new HashMap<String, Double>();
//        localMapTPctrl.putAll(NameMap);
        localMapInv.putAll(NameMap);
        localMapITCM.putAll(NameMap);
        
       //2. get and set these maps.
        for(String spk : parts.keySet()){
            Speaker tmpSpk = parts.get(spk);
//            Double tpctrl = tmpSpk.getTC().getPower();// topic control 
            Double inv = tmpSpk.getInv().getPower();
//            localMapTPctrl.put(spk, tpctrl);
            localMapInv.put(spk, inv);
        }
        
        
       //3. add the maps and average
//        localMapITCM = this.adding2MapsAndAverageIt(localMapTPctrl, localMapInv) ;
        localMapITCM = localMapInv;
        
        //4: normalizing 3/26/12 12:27 PM
        localMapITCM = this.normalizingMap(localMapITCM) ;
        
        //5.  print.
        ArrayList<ArrayList> ITCMList = new ArrayList();
        ITCMList = this.sortAndConvertMapToArrayList(localMapITCM);
        if(doFinalPrintOut){
            System.out.println("@ITCM");
            for(ArrayList a : ITCMList){
                System.out.println( parts.get((String)(a.get(0))).getOriName() + " : " + (Double)(a.get(1)) );
            }
        }
        if(doAnalysisPrintOut){
            System.out.println("@ITCM");
//            System.out.println("topic ctrl map: " + localMapTPctrl.toString());
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
        
        //5. normalizing 3/26/12 12:28 PM
        localMapCDM = this.normalizingMap(localMapCDM) ;
        
        //6.testing print.
        ArrayList<ArrayList> CDMList = new ArrayList();
        CDMList = this.sortAndConvertMapToArrayList(localMapCDM);
        if(doFinalPrintOut){
            System.out.println("@CDM");
            for(ArrayList a : CDMList){
                System.out.println( parts.get((String)(a.get(0))).getOriName() + " : " + (Double)(a.get(1)) );
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
     * m2w: added network centrality into pop. 
     * @return  ncm map
     * @date 3/22/12 1:08 PM
     */
    private HashMap<String, Double> calNCM(){
        //1. init
        HashMap<String, Double> localMapNCM = new HashMap<String, Double>();
        localMapNCM.putAll(NameMap);//added network centrality for testing.
        
        //2. get and set these maps.
        for(String spk : parts.keySet()){
            Speaker tmpSpk = parts.get(spk);
            Double ncm = tmpSpk.getNetCentr();//added network centrality for testing.
            localMapNCM.put(spk, ncm);//added network centrality for testing.
        }
        
        //3. normalizing 3/26/12 12:33 PM
        localMapNCM = this.normalizingMap(localMapNCM); 
        
        
        //4.  print.
        ArrayList<ArrayList> NCMList = new ArrayList();
        NCMList = this.sortAndConvertMapToArrayList(localMapNCM);
        if(doFinalPrintOut){
            System.out.println("@NCM");
            for(ArrayList a : NCMList){
                System.out.println( parts.get((String)(a.get(0))).getOriName() + " : " + (Double)(a.get(1)) );
            }
        }
        
        return localMapNCM;
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
        //4. normalizing; 3/26/12 12:34 PM
        localTFMMap = this.normalizingMap(localTFMMap); 
        
        //output
        ArrayList<ArrayList> TFMList = new ArrayList();
        TFMList = this.sortAndConvertMapToArrayList(localTFMMap);
        if(doFinalPrintOut){
            System.out.println("@TFM");
            for(ArrayList a : TFMList){
                System.out.println( parts.get((String)(a.get(0))).getOriName() + " : " + (Double)(a.get(1)) );
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
        int totalComm = 0;
        HashMap<String, Double> localMapPomComm = new HashMap<String, Double>();
        HashMap<String, Double> localMapPom = new HashMap<String, Double>();
        HashMap<String, Double> localMapPomCommCount = new HashMap<String, Double>();
        localMapPomComm.putAll(NameMap);
        localMapPom.putAll(NameMap);
        localMapPomCommCount.putAll(NameMap);
        //1. parse utt list, adding count to local map
        for(Utterance u : Utts){
            String tempDaTag = u.getTag();
            String tempCATag = u.getCommActType();
            String tempSpk = u.getSpeaker();
            
            // adding  confirmation request count & offe-comit. 
            if(tempDaTag.toLowerCase().contains(OFFER_COMMIT) && tempCATag.equalsIgnoreCase(RESPONSE_TO)) {
                totalComm++;
                String lkto_spk = u.getRespToSpk();
                Double tempCountComm = (Double) localMapPomCommCount.get(tempSpk);
                localMapPomCommCount.put(lkto_spk, tempCountComm+1);
            }
        }
        
        //2. transform conf & comm list from count to percentage.
        if(totalComm > 0.0){ //2/14/12 1:52 PM
            for(String spk : localMapPomComm.keySet()){
                Double spkConfCount = localMapPomCommCount.get(spk);
                Double percentage = (spkConfCount)/ (double) totalComm;
                localMapPomComm.put(spk, percentage);
            }
        }
        
        //3. adding
        localMapPom = this.adding2MapsAndAverageIt(ConfMap, localMapPomComm);
        
        //4. normalizing 
        localMapPom = this.normalizingMap(PopMap);
        
        ArrayList<ArrayList> POMList = new ArrayList();
        POMList = this.sortAndConvertMapToArrayList(localMapPom);
        if(doFinalPrintOut){
            System.out.println("@POM");
            for(ArrayList a : POMList){
                System.out.println( parts.get((String)(a.get(0))).getOriName() + " : " + (Double)(a.get(1)) );
            }
        }
        if(doAnalysisPrintOut){
            System.out.println("@POM");
            System.out.println("total comm: " + totalComm);
            System.out.println("count map Comm: " + localMapPomCommCount.toString());
            System.out.println("local POM conf map: " + ConfMap);
            System.out.println("local POM conf map: " + localMapPomComm);
            System.out.println("local POM map before pop: " + localMapPom);
            System.out.println();
        }
        
        return localMapPom;
    }
    
    /**
     * m2w: TFM 's Disagree-Reject Target Index (DRT)
     */
    private HashMap<String, Double> calTFM_DRT(){
        HashMap<String, Double> localMapDis = new HashMap();
        HashMap<String, Double> localMapPercent = new HashMap();
        localMapDis.putAll(NameMap); // storing the count of DISAGREE_REJECT turns of 
        localMapPercent.putAll(NameMap); //stroing the count of conf perc
        //1.2. removed conf, and also isolated the method for later use. if need conf just build hashmap = this.calConfRequestScore();
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
            System.out.println("@TFM_DRT");
            System.out.println("conf perc map: " + localMapPercent.toString());
            System.out.println("dis perc map: " +localMapDis.toString());
            System.out.println("local TFM_DRT Map before pop: " + localTFM_DRTMap.toString());
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
    
        /**
//     * m2w: this method calculates pop using Disagreement with Leader Measure.
//     *   //1. get the leader of the dialogue
//     *   //2. build local map of count.
//     *   //3. get count of dis towards the leader, assign to each key in the local map.
//     *   //4. build and add the local map of percent to the pop map.
//     */
//    private HashMap<String, Double> calDWL(){
//        
//        Double totalDis = 0.0;
//        //1. get the leader of the dialogue
//        String leaderName = leader.getName();
//        //2. build local map of count.
//        HashMap<String, Double> localMapCount = new HashMap();
//        localMapCount.putAll(NameMap);
//        //3. get count of dis towards the leader, assign to each key in the local map.
//        for(Utterance u : Utts){
//            String DaTag = u.getTag();
//            String tempSpk = u.getSpeaker();
//            String tempCATag = u.getCommActType();
//            if(DaTag.toLowerCase().contains(DISAGREE_REJECT) && tempCATag.equalsIgnoreCase(RESPONSE_TO)){
//                totalDis++;
//                String lkto_spk = u.getRespToSpk();
////                System.out.println(lkto_spk);
//                //if it links to the leader and current speaker is not the leader
//                if((lkto_spk != null) && (tempSpk != null) && lkto_spk.equalsIgnoreCase(leaderName) && !tempSpk.equalsIgnoreCase(leaderName)){
//                    Double tempCount = localMapCount.get(tempSpk);
//                    localMapCount.put(tempSpk, tempCount + 1); // count + 1
//                }
//            }
//        }
//        //4. build and add the local map of percent to the pop map.
//        HashMap<String, Double> localMapDWL = new HashMap();
//        localMapDWL.putAll(NameMap);
//        if(totalDis > 0){
//            for(String spk : localMapCount.keySet()){
//                Double tempDisCount = localMapCount.get(spk);
//                Double tempPerc = tempDisCount / totalDis;
//                localMapDWL.put(spk, tempPerc);
//            }
//        }
//
//        //output
//        ArrayList<ArrayList> DWLList = new ArrayList();
//        DWLList = this.sortAndConvertMapToArrayList(localMapDWL);
//        if(doFinalPrintOut){
//            System.out.println("@DWL");
//            for(ArrayList a : DWLList){
//                System.out.println( (String)(a.get(0)) + " : " + (Double)(a.get(1)) );
//            }
//        }
//        if(doAnalysisPrintOut){
//            System.out.println("@DWL");
//            System.out.println("total dis: " + totalDis);
//            System.out.println("count map dis: " + localMapCount.toString());
//            System.out.println("local DWL map before pop: " + localMapDWL);
//            System.out.println(" pop map: " + PopMap.toString());
//            System.out.println();
//        }
//        return localMapDWL;
//    }
    
    
    //    ======================================== sub level util methods =================================================
    
    /**
     * m2w: isolating this method because multiple calling coming from different top level methods.
     * calculating confirmation request score.
     * @return 
     */
    private HashMap<String, Double> calConfRequestScore(){
        int totalConf = 0;
        HashMap<String, Double> localMapConf = new HashMap<String, Double>();
        HashMap<String, Double> localMapConfCount = new HashMap<String, Double>();
        localMapConf.putAll(NameMap);
        localMapConfCount.putAll(NameMap);
        
        //1. parse utt list, adding count to local map
        for(Utterance u : Utts){
            String tempDaTag = u.getTag();
            String tempCATag = u.getCommActType();
            String tempSpk = u.getSpeaker();
            
            // adding  confirmation request count & offe-comit. 
            if(tempDaTag.toLowerCase().contains(CONFIRMATION_REQUEST) && tempCATag.equalsIgnoreCase(RESPONSE_TO)){
                totalConf++;
                String lkto_spk = u.getRespToSpk();
                Double tempCountConf = (Double) localMapConfCount.get(tempSpk);
                localMapConfCount.put(lkto_spk, tempCountConf+1);
            }
        }
        
        //2. transform conf  list from count to percentage.
        if(totalConf > 0.0){ //2/14/12 1:52 PM
            for(String spk : localMapConf.keySet()){
                Double spkConfCount = localMapConfCount.get(spk);
                Double percentage = (spkConfCount)/ (double) totalConf;
                localMapConf.put(spk, percentage);
            }
        }
        
        //3. print.
        if(doAnalysisPrintOut){
            System.out.println("@Confirmation - request Map");
            System.out.println("total count: "+ totalConf);
            System.out.println("localMapConfCount: " + localMapConfCount.toString());
            System.out.println("localMapConf: " + localMapConf.toString());
        }
        return localMapConf;
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
    
    /**
     * m2w: calculating average from several maps. 
     * @param list
     * @return 
     */
    private HashMap<String, Double> addingMapsAndAverageIt(ArrayList<HashMap<String, Double>> list){
        HashMap<String, Double> averageMap = new HashMap<String, Double>();
        averageMap.putAll(NameMap);
        int size = list.size();
        if(size > 0){
            for(String spk : averageMap.keySet()){
                Double tempTotal = 0.0;
                for(HashMap<String, Double> tempMap : list){
                    if(tempMap != null){
                        tempTotal = tempTotal + tempMap.get(spk);
                    }
                }
                Double tempAverage = tempTotal / (double) size;
                averageMap.put(spk, tempAverage);
            }
        }
        return averageMap;
    }
    
    
    /**
     * m2w: adding maps without averaging
     * @param list
     * @return 
     */
    private HashMap<String, Double> addingMaps(ArrayList<HashMap<String, Double>> list){
        HashMap<String, Double> totalMap = new HashMap<String, Double>();
        totalMap.putAll(NameMap);
        int size = list.size();
        if(size > 0){
            for(String spk : totalMap.keySet()){
                Double tempTotal = 0.0;
                for(HashMap<String, Double> tempMap : list){
                    if(tempMap != null){
                        tempTotal = tempTotal + tempMap.get(spk);
                    }
                }
                totalMap.put(spk, tempTotal);
            }
        }
        return totalMap;
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
            subList.add(spk); //0th index is speaker string
            subList.add(map.get(spk)); //1th index is speaker pop score.
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
    
    /**
     * m2w: normalize sum != 1 maps to sum = 1, i.e, make the input map's result equals to real percentage.
     * @param map
     * @return 
     */
    private HashMap<String, Double> normalizingMap(HashMap<String, Double> map){
        Double sum = 0.0;
        for(String spk : map.keySet()){
            Double score = map.get(spk);
            sum = sum + score;
        }
        
        if(sum != 0.0){
            for(String spk : map.keySet()){
                Double score = map.get(spk);
                score = score / sum;
                map.put(spk, score);
            }
        }
        
        return map;
    }
    
    /**
     * m2w: adding weight to input map.
     * @param map
     * @return 
     */
    private HashMap<String, Double> weightingMap(HashMap<String, Double> map, Double weight){
        for(String spk : map.keySet()){
            Double score = map.get(spk);
            score = score * weight;
            map.put(spk, score);
        }
        return map;
    }
    
    private HashMap<String, Double> deleteLeader(HashMap<String, Double> map){
        if(leader != null){
            map.remove(leader.getName());
            NameMap.remove(leader.getName());
        }
        
        if(map.containsKey("ilspersonnel")){
            map.remove("ilspersonnel");
            NameMap.remove("ilspersonnel");
        }
        return map;
    }
    
    private void decidePopAndPrintEnglish(ArrayList<ArrayList> PopList){
        ArrayList<Double> gaps = new ArrayList<Double>();
        if(PopList.size() > 1){
            Double gapsum = 0.0;
            Double howMany = 0.0;
            for(int i = 1; i <PopList.size(); i++){            
                Double gap = (Double)PopList.get(i-1).get(1) - (Double)PopList.get(i).get(1);
//                System.out.println((Double)PopList.get(i).get(1));
//                System.out.println((Double)PopList.get(i-1).get(1));
                gaps.add(gap);
                if(i < 6){
                    gapsum = gapsum + gap;
                    howMany = (double)i;
            }
            }
            
            Double gapMean = gapsum / howMany;
            
            if(gapMean < 0.3){
                System.out.println("no pop");
            }else if(gaps.get(0) > ONE_OR_TWO_POP_GAP){
                System.out.println((String)PopList.get(0).get(0));
            }else{
                System.out.println((String)PopList.get(0).get(0) + " , "  + (String)PopList.get(1).get(0));
            }
        }
        
    }
    
    private void decidePopAndPrintChinese(ArrayList<ArrayList> PopList){
        ArrayList<Double> gaps = new ArrayList<Double>();
        if(PopList.size() > 1){
            for(int i = 1; i <PopList.size(); i++){            
                Double gap = (Double)PopList.get(i-1).get(1) - (Double)PopList.get(i).get(1);
//                System.out.println((Double)PopList.get(i).get(1));
//                System.out.println((Double)PopList.get(i-1).get(1));
                gaps.add(gap);
//                System.out.println(gap);
            }
            
            if(gaps.get(0) > ONE_OR_TWO_POP_GAP){
                System.out.println((String)PopList.get(0).get(0));
            }else{
                System.out.println((String)PopList.get(0).get(0) + " , "  + (String)PopList.get(1).get(0));
            }
        }
        
    }
    
    /**
     * m2w: standard deviation version of decide and print.
     * @param PopList 
     */
    private void decidePopAndPrintSTD(ArrayList<ArrayList> PopList){
        Double totalWeight = 0.0;
        Double avgWeight = 0.0;
        Double sum = 0.0;
        Double sumSTD1 = 0.0;
        Double sumSTD2 = 0.0;
        Double num = (double)PopMap.size();
        ArrayList<String> outList = new ArrayList<String>();
        boolean hasPop = false;
        //getting average weight
        for(String spk : PopMap.keySet()){
            Double tmpWeight = PopMap.get(spk);
            totalWeight = totalWeight + tmpWeight;
        }
        avgWeight = totalWeight / num;
        
        //getting standard deviation
        for(String spk : PopMap.keySet()){
            Double subtract = PopMap.get(spk) - avgWeight;
            Double square = subtract*subtract;
            sum = sum + square;
        }
        
        Double tmpSTD1 = (double)Math.sqrt(sum/(num-1.0));
        Double tmpSTD2 = 2* tmpSTD1;
        
        //these 2 are for comparing.
        sumSTD1 = avgWeight + tmpSTD1;
        sumSTD2 = avgWeight + tmpSTD2;
        
//        Double p1score = (Double)PopList.get(0).get(1);
//        Double p2score = (Double)PopList.get(1).get(1);
        
//        if(p1score > sumSTD1) hasPop = true; // judging.
        
//        if(hasPop){
        for(int i = 0; i < PopList.size(); i++){
            Double tempScore = (Double)PopList.get(i).get(1);
            String tempSpk = (String)PopList.get(i).get(0);
            if(tempScore > sumSTD2){
                outList.add(tempSpk);
                continue;
            }
            if(tempScore > sumSTD1){
                outList.add(tempSpk);
            }
        }
        
        int outsize = outList.size();
        if(outsize > 0 && outsize < 3){
            for(String spk : outList){
                System.out.print(parts.get(spk).getOriName() + " ");
            }
            System.out.println();
        }else{
            System.out.println("No Pursuit of Power");
        }

        
        if(doSTDdebuggingPrintOut){
            System.out.println("total" + totalWeight);
            System.out.println("avg" + avgWeight);
            System.out.println("sum" + sum);
            System.out.println("sumstd1: " + sumSTD1);
            System.out.println("sumstd2: " + sumSTD2);
        }
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
//    private HashMap<String, Double> DWLMap; // stroing dwl score, for average pop.
    private HashMap<String, Double> TFMMap; // stroing tfm score, for average pop.
    private HashMap<String, Double> POMMap; // stroing pom score, for average pop.
    private HashMap<String, Double> ConfMap; // stroing Confirmation request score.
    private HashMap<String, Double> NCMMap; // stroing net work centrality score.
    private String language;
    
    //constants:
    private final String DISAGREE_REJECT = "disagree-reject";
    private final String CONFIRMATION_REQUEST = "confirmation-request";
    private final String RESPONSE_TO = "response-to";
    private final String OFFER_COMMIT = "offer-commit";
    
    private final Double ITCMWGT_EN = 0.80;
    private final Double CDMWGT_EN = 0.09;
    private final Double TFMWGT_EN = 0.01;
    private final Double POMWGT_EN = 0.01;
    private final Double NCMWGT_EN = 0.09;
    
    private final Double ITCMWGT_CN = 0.2;
    private final Double CDMWGT_CN = 0.2;
    private final Double TFMWGT_CN = 0.45;
    private final Double POMWGT_CN = 0.00;
    private final Double NCMWGT_CN = 0.15;
    
    private final Double ONE_OR_TWO_POP_GAP = 0.20;
    
    //mode control
//    private final String MODE = "simple";//standard deviation
    private final String MODE = "std";//standard deviation
//    private final String MODE = "cov"; // Coefficient_of_variation
    
    //print out control:
    private boolean doSTDdebuggingPrintOut = false;
    private boolean doAnalysisPrintOut = false;
    private boolean doFinalPrintOut = true;
}
