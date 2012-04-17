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
        TFMMap = new HashMap<String, Double>();
        ConfMap = new HashMap<String, Double>();
        NCMMap = new HashMap<String, Double>();
        PopOutList = new ArrayList<String>();
        
        for(Utterance u : Utts){
            String tempSpk = u.getSpeaker();
            NameMap.put(tempSpk, 0.0);
        }
        PopMap.putAll(NameMap);
        ITCMMap.putAll(NameMap);
        CDMMap.putAll(NameMap);
        TFMMap.putAll(NameMap);
        ConfMap.putAll(NameMap);
        NCMMap.putAll(NameMap);
        language = Settings.getValue(Settings.LANGUAGE);
    }
    
    public void calPursuitOfPower(){
        if(language.equalsIgnoreCase("english")){
            this.calPopEng();//calucaltions done this line.
        }else if (language.equalsIgnoreCase("chinese")){
            this.calPopChn();//calucaltions done this line.
        }
        
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
            int confidence = 0;
            if (language.equalsIgnoreCase("english")){
                this.decidePopAndPrintSTDEnglish(PopList);
                confidence = this.calConfidenceEng(PopList);
            }else if (language.equalsIgnoreCase("chinese")){
                this.decidePopAndPrintSTDChinese(PopList);
                confidence = this.calConfidenceEng(PopList);
            }
            
            System.out.println("@Confidence:");
            System.out.println(confidence + "%" );
        }
        
    }
//    ============================================ top level cal methods =================================================
    /**
     * m2w: this is a wrapper method for pop score calculation. English
     * score is stored in PopMap
     */
    private void calPopEng(){
        //calculate pop
        TFMMap = this.calTFM();         //1.Tension Focus Measure (TFM) //moved to 1st because itcm uses tf score.
        ITCMMap = this.calITCM();       //2.Involved Topic COntrol Measure (ITCM)
        CDMMap = this.calCDM();         //3.Cumulative Disagreement Measure (CDM)
        ConfMap = this.calConfRequestScore();        //6.calculate confirmation request.(Conf)
        NCMMap = this.calNCM();         //7.get network centrality map (NCM)
        
        //8.average.
        ArrayList<HashMap<String, Double>> mapList = new  ArrayList<HashMap<String, Double>>();
        
        if(language.equalsIgnoreCase("english")){
            mapList.add(this.weightingMap(TFMMap, TFMWGT_EN));
            mapList.add(this.weightingMap(ITCMMap, ITCMWGT_EN));
            mapList.add(this.weightingMap(CDMMap, CDMWGT_EN));
            mapList.add(this.weightingMap(NCMMap, NCMWGT_EN));
        }
        
        PopMap = this.addingMaps(mapList);
        //deleting leader, 3/28/12 2:23 PM just for english. 
        PopMap = this.deleteLeader(PopMap);
    }
    
    /**
     * m2w: this is a wrapper method for pop score calculation. Chinese
     * score is stored in PopMap
     */
    private void calPopChn(){
        //calculate pop
        TFMMap = this.calTFM();         //1.Tension Focus Measure (TFM) //moved to 1st because itcm uses tf score.
        ITCMMap = this.calITCM();       //2.Involved Topic COntrol Measure (ITCM)
        CDMMap = this.calCDM();         //3.Cumulative Disagreement Measure (CDM)
        ConfMap = this.calConfRequestScore();        //6.calculate confirmation request.(Conf)
        NCMMap = this.calNCM();         //7.get network centrality map (NCM)
        
        //8.average.
        ArrayList<HashMap<String, Double>> mapList = new  ArrayList<HashMap<String, Double>>();
        
        if(language.equalsIgnoreCase("chinese")){
            mapList.add(this.weightingMap(TFMMap, TFMWGT_CN));
            mapList.add(this.weightingMap(ITCMMap, ITCMWGT_CN));
            mapList.add(this.weightingMap(CDMMap, CDMWGT_CN));
            mapList.add(this.weightingMap(NCMMap, NCMWGT_CN));
        }
        PopMap = this.addingMaps(mapList);
        //leader is deleted after pop
    }
    
    /**
     * m2w: this method calculates pop using Involved Topic COntrol Measure.
     *      //1. init all maps,  involvement, localitcm;
     *      //2. get and set these maps.
     *      //3. add the 2 maps and average
     *      //4. add to pop.
     *  @date 3/22/12 1:07 PM
     */
    private HashMap<String, Double> calITCM(){
       //1. init all maps, topic ctrl, involvement, local itcm, 
        HashMap<String, Double> localMapInv = new HashMap<String, Double>();
        HashMap<String, Double> localMapITCM = new HashMap<String, Double>();
        localMapInv.putAll(NameMap);
        localMapITCM.putAll(NameMap);
        
       //2. get and set these maps.
        for(String spk : parts.keySet()){
            Speaker tmpSpk = parts.get(spk);
            Double inv = tmpSpk.getInv().getPower();
            localMapInv.put(spk, inv);
        }
        
       //3. add the maps and average
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
    
    /**
     * m2w: English version, exclude leader from pop 
     * @param map
     * @return 
     */
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

    /**
     * m2w: standard deviation version of decide and print.
     * @param PopList 
     */
    private void decidePopAndPrintSTDEnglish(ArrayList<ArrayList> PopList){
        PopList = this.exclude2PplChating(PopList);
        PopList = this.excludePplisnotPop(PopList);
        PopList = this.increaseScoreSpk1(PopList);
        Double totalWeight = 0.0;
        Double avgWeight = 0.0;
        Double sum = 0.0;
        Double sumSTD1 = 0.0;
        Double sumSTD2 = 0.0;
        Double num = (double)PopList.size();
        ArrayList<String> outList = new ArrayList<String>();
        boolean hasPop = false;
        //getting average weight
        for(int i = 0; i < PopList.size(); i ++){
            Double tmpWeight = (Double)PopList.get(i).get(1);
            totalWeight = totalWeight + tmpWeight;
        }
        avgWeight = totalWeight / num;
        
        //getting standard deviation
        for(int i = 0; i < PopList.size(); i ++){
            Double subtract = (Double)PopList.get(i).get(1) - avgWeight;
            Double square = subtract*subtract;
            sum = sum + square;
        }
        
        Double tmpSTD1 = (double)Math.sqrt(sum/(num-1.0));
        Double tmpSTD2 = 2* tmpSTD1;
        
        //these 2 are for comparing.
        sumSTD1 = avgWeight + tmpSTD1;
        sumSTD2 = avgWeight + tmpSTD2;
        
        for(int i = 0; i < PopList.size(); i++){
            Double tempScore = (Double)PopList.get(i).get(1);
            String tempSpk = (String)PopList.get(i).get(0);
            if(tempScore > sumSTD2){
                outList.add(tempSpk);
                continue;
            }
            if(tempScore > sumSTD1 * 0.87){
                outList.add(tempSpk);
            }
        }
        
        int outsize = outList.size();
        if(outsize > 0 && outsize < 4){
            for(int i = 0; i < outList.size() && i < 2 ; i ++){
                String spk = outList.get(i);
                System.out.print(parts.get(spk).getOriName() + " ");
                PopOutList.add(spk);
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
    
    /**
     * m2w: standard deviation version of decide and print.
     * @param PopList 
     */
    private void decidePopAndPrintSTDChinese(ArrayList<ArrayList> PopList){
        
//        PopList = this.exclude2PplChating(PopList);
//        PopList = this.excludePplisnotPop(PopList);
//        PopList = this.increaseScoreSpk1(PopList);
        
        Double totalWeight = 0.0;
        Double avgWeight = 0.0;
        Double sum = 0.0;
        Double sumSTD1 = 0.0;
        Double sumSTD2 = 0.0;
                Double num = (double)PopList.size();
        ArrayList<String> outList = new ArrayList<String>();
        boolean hasPop = false;
        //getting average weight
        for(int i = 0; i < PopList.size(); i ++){
            Double tmpWeight = (Double)PopList.get(i).get(1);
            totalWeight = totalWeight + tmpWeight;
        }
        avgWeight = totalWeight / num;
        
        //getting standard deviation
        for(int i = 0; i < PopList.size(); i ++){
            Double subtract = (Double)PopList.get(i).get(1) - avgWeight;
            Double square = subtract*subtract;
            sum = sum + square;
        }
        
        Double tmpSTD1 = (double)Math.sqrt(sum/(num-1.0));
        Double tmpSTD2 = 2* tmpSTD1;
        
        //these 2 are for comparing.
        sumSTD1 = avgWeight + tmpSTD1;
        sumSTD2 = avgWeight + tmpSTD2;
        
        for(int i = 0; i < PopList.size(); i++){
            Double tempScore = (Double)PopList.get(i).get(1);
            String tempSpk = (String)PopList.get(i).get(0);
            if(tempScore < sumSTD2){
                outList.add(tempSpk);
                continue;
            }
            if(tempScore > sumSTD1){
                outList.add(tempSpk);
            }
        }
        
        int outsize = outList.size();
        if(outsize > 0 && outsize < 3){
            for(int i = 0; i < outList.size() && i < 2 ; i ++){
                String spk = outList.get(i);
                System.out.print(parts.get(spk).getOriName() + " ");
                PopOutList.add(spk);
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
    
    /**
     * m2w: this method is the english version of calculating the confidence score
     * @param PopList
     * @return 
     */
    private int calConfidenceEng(ArrayList<ArrayList> PopList){
        int confidence = 0;
        ArrayList<ArrayList> itcmList = this.sortAndConvertMapToArrayList(ITCMMap);
        ArrayList<ArrayList> cdmList = this.sortAndConvertMapToArrayList(CDMMap);
        ArrayList<ArrayList> ncmList = this.sortAndConvertMapToArrayList(NCMMap);
        ArrayList<ArrayList> tfmList = this.sortAndConvertMapToArrayList(TFMMap);
        
        if(PopOutList.isEmpty()){ confidence = 70;}
        else if(PopOutList.size() == 1){
            String pop = PopOutList.get(0);
            if(pop.equalsIgnoreCase((String)itcmList.get(0).get(0))){confidence += 50;}
            if(pop.equalsIgnoreCase((String)cdmList.get(0).get(0))){confidence += 20;}
            if(pop.equalsIgnoreCase((String)ncmList.get(0).get(0))){confidence += 20;}
            if(pop.equalsIgnoreCase((String)tfmList.get(0).get(0))){confidence += 10;}
        }else if (PopOutList.size() > 1){
            for(int i = 0; i < PopOutList.size(); i++){
                String pop = PopOutList.get(i);
                if(pop.equals((String)itcmList.get(i).get(0))){confidence += 25;}
                if(pop.equals((String)cdmList.get(i).get(0))){confidence += 10;}
                if(pop.equals((String)ncmList.get(i).get(0))){confidence += 10;}
                if(pop.equals((String)tfmList.get(i).get(0))){confidence += 5;}
            }
        }
        
        if(confidence == 0){
            confidence = 50;
        }
        
        return confidence;
    }
    
    /**
     * m2w: this method is the chinese version of claculating the confidence score.
     * @param PopList
     * @return 
     */
    private int calConfidenceChn(ArrayList<ArrayList> PopList){
        int confidence = 0;
        ArrayList<ArrayList> itcmList = this.sortAndConvertMapToArrayList(ITCMMap);
        ArrayList<ArrayList> cdmList = this.sortAndConvertMapToArrayList(CDMMap);
        ArrayList<ArrayList> ncmList = this.sortAndConvertMapToArrayList(NCMMap);
        ArrayList<ArrayList> tfmList = this.sortAndConvertMapToArrayList(TFMMap);
        
        if(PopOutList.isEmpty()){ confidence = 70;}
        else if(PopOutList.size() == 1){
            String pop = PopOutList.get(0);
            if(pop.equalsIgnoreCase((String)itcmList.get(0).get(0))){confidence += 12;}
            if(pop.equalsIgnoreCase((String)cdmList.get(0).get(0))){confidence += 12;}
            if(pop.equalsIgnoreCase((String)ncmList.get(0).get(0))){confidence += 6;}
            if(pop.equalsIgnoreCase((String)tfmList.get(0).get(0))){confidence += 70;}
        }else if (PopOutList.size() > 1){
            for(int i = 0; i < PopOutList.size(); i++){
                String pop = PopOutList.get(i);
                if(pop.equals((String)itcmList.get(i).get(0))){confidence += 6;}
                if(pop.equals((String)cdmList.get(i).get(0))){confidence += 6;}
                if(pop.equals((String)ncmList.get(i).get(0))){confidence += 3;}
                if(pop.equals((String)tfmList.get(i).get(0))){confidence += 35;}
            }
        }
        
         if(confidence == 0){
            confidence = 50;
        }
        
        return confidence;
    }
    
    /**
     * 
     */
    private ArrayList<ArrayList> exclude2PplChating(ArrayList<ArrayList> PopList){
        //loop through pop list's speakers
        if(Utts.size() > 40){//if file is big
            HashSet<String> deleteSet = new HashSet<String>();
            for(int i = 0; i < PopList.size(); i++){
                String spk = (String)PopList.get(i).get(0);
                ArrayList<Utterance> spkUttList = parts.get(spk).getUtts_();
                
                //calculate gap & sub speaker turns
                if(spkUttList.size() > 10){
                    HashMap<String, Integer> subSpeakerMap = new HashMap<String , Integer>();
                    Integer start = Integer.parseInt(spkUttList.get(0).getTurn());
                    Integer end = Integer.parseInt(spkUttList.get(spkUttList.size() - 1).getTurn());
                    //within the gap
                    //build sub map
                    for(int j = start; j < end; j++){
                        Utterance u = Utts.get(j);
                        String subSpk = u.getSpeaker();
                        if(subSpk.equals(spk)){continue;}
                        if(subSpeakerMap.containsKey(subSpk)){
                            subSpeakerMap.put(subSpk, subSpeakerMap.get(subSpk) + 1);
                        }else{
                            subSpeakerMap.put(subSpk, 1);
                        }
                    }

                    //parse sub map
                    for(String subSpk : subSpeakerMap.keySet()){
                        int subSpkTurns = subSpeakerMap.get(subSpk);
                        Double subSpkPercent = (double)subSpkTurns / (double)(end - start);
                        if(subSpkPercent > 0.35){
                            deleteSet.add(spk);
                            deleteSet.add(subSpk);
                        }
                    }
                }
            }//closes pop list loop
            for(int i = 0; i < PopList.size(); i++){
                String spk = (String)PopList.get(i).get(0);
                if(deleteSet.contains(spk)){
                    PopList.remove(i);
                    PopMap.remove(spk);
                }
            }
        }//closes file size > 40 utts
        return PopList;
    }
    
    /**
     * 
     */
    private ArrayList<ArrayList> excludePplisnotPop(ArrayList<ArrayList> PopList){
        if(Utts.size() > 15){
            HashSet<String> deleteSet = new HashSet<String>();
            for(int i = 0; i < PopList.size(); i++){
                String spk = (String)PopList.get(i).get(0);
                ArrayList<Utterance> spkUttList = parts.get(spk).getUtts_();
                Integer start = Integer.parseInt(spkUttList.get(0).getTurn());
                Integer end = Integer.parseInt(spkUttList.get(spkUttList.size() - 1).getTurn());
                Double sizePerc = (double)spkUttList.size() / (double)Utts.size();
                Double gapPerc = (double)(end-start) / (double)Utts.size();
                if(sizePerc > 0 && sizePerc < 0.07 && gapPerc < 0.15 && gapPerc > 0){
                    //if involved ppl is few then delete this ppl
                    HashSet<String> subSpkSet = new HashSet<String>();
                    
                    for(int j = start; j < end; j++){
                        Utterance u = Utts.get(j);
                        String subSpk = u.getSpeaker();
                        if(!subSpk.equalsIgnoreCase(spk) && PopMap.containsKey(subSpk)){
                            subSpkSet.add(subSpk);
                        }
                    }
                    
                    Double subSpkPercent = (double)subSpkSet.size() / (double)PopList.size();
                    if(subSpkPercent > 0 && subSpkPercent < 0.2){
                        deleteSet.add(spk);
                    }
                }
            }
            
            for(int i = 0; i < PopList.size(); i++){
                String spk = (String)PopList.get(i).get(0);
                if(deleteSet.contains(spk)){
                    PopList.remove(i);
                }
            }
        }//closes if
        return PopList;
    }
        
    /**
     * m2w : this is for increasing scores while speaker in a large group of ppl, and has consecutive speaks.
     * @param PopList
     * @return 
     */
    private ArrayList<ArrayList> increaseScoreSpk1(ArrayList<ArrayList> PopList){
        if(Utts.size() > 10){
            HashSet<String>  increaseSet= new HashSet<String>();
            for(int i = 0; i < PopList.size(); i++){
                String spk = (String)PopList.get(i).get(0);
                boolean increaseOrNot = false;
                ArrayList<Utterance> spkUttList = parts.get(spk).getUtts_();
                Integer start = Integer.parseInt(spkUttList.get(0).getTurn());
                Integer end = Integer.parseInt(spkUttList.get(spkUttList.size() - 1).getTurn());
                Double sizePerc = (double)spkUttList.size() / (double)Utts.size();
                Double gapPerc = (double)(end-start) / (double)Utts.size();
                if(sizePerc > 0.07 && gapPerc > 0.15){
                    //if involved ppl is few then delete this ppl
                    HashSet<String> subSpkSet = new HashSet<String>();
                    TreeMap<Integer, Boolean> spkUttMap = new TreeMap<Integer, Boolean>();
                    
                    for(int j = start; j < end; j++){
                        Utterance u = Utts.get(j);
                        String subSpk = u.getSpeaker();
                        if(!subSpk.equalsIgnoreCase(spk) && PopMap.containsKey(subSpk)){
                            subSpkSet.add(subSpk);
                        }
                        if(subSpk.equalsIgnoreCase(spk) && (j - 1) >start && (j + 1) < end){
                            String prevSpk = Utts.get(j-1).getSpeaker();
                            String nextSpk = Utts.get(j+1).getSpeaker();
                            if(prevSpk.equals(spk) && nextSpk.equals(spk)){
                                increaseOrNot = true;
                            }
                        }
                        
                    }
                    
                    Double subSpkPercent = (double)subSpkSet.size() / (double)PopList.size();
                    if(subSpkPercent > 0.2 && increaseOrNot){
                        //creat treemap, check for 3 consecutive utts.
                        increaseSet.add(spk);
                    }
                }
            }
            
            for(int i = 0; i < PopList.size(); i++){
                String spk = (String)PopList.get(i).get(0);
                if(increaseSet.contains(spk)){
                    Double tempScore = (Double)PopList.get(i).get(1);
                    PopList.get(i).set(1, tempScore*1.1);
                }
            }
        }//closes if
        return PopList;
    }
    
    
//    =================================== vars and consts =============================================
    //variables:
    private HashMap<String, Double> PopMap; //where the precentage of each speaker are stored.
    private ArrayList<Utterance> Utts;
    private HashMap<String, Speaker> parts;
    private Speaker leader;
    private HashMap<String, Double> NameMap; // an empty map, used for each local map initialization. 
    private HashMap<String, Double> ITCMMap; // stroing itcm score, for average pop.
    private HashMap<String, Double> CDMMap; // stroing cdm score, for average pop.
    private HashMap<String, Double> TFMMap; // stroing tfm score, for average pop.
    private HashMap<String, Double> ConfMap; // stroing Confirmation request score.
    private HashMap<String, Double> NCMMap; // stroing net work centrality score.
    private ArrayList<String> PopOutList;
    private String language;
    
    //constants:
    private final String CONFIRMATION_REQUEST = "confirmation-request";
    private final String RESPONSE_TO = "response-to";
    //english weights
    private final Double ITCMWGT_EN = 0.80;
    private final Double CDMWGT_EN = 0.09;
    private final Double TFMWGT_EN = 0.02;
    private final Double NCMWGT_EN = 0.09;
    //chinese weights
    private final Double ITCMWGT_CN = 0.15;
    private final Double CDMWGT_CN = 0.15;
    private final Double TFMWGT_CN = 0.70;
    private final Double NCMWGT_CN = 0.0;
    
    
    //print out control:
    private boolean doSTDdebuggingPrintOut = false;
    private boolean doAnalysisPrintOut = false;
    private boolean doFinalPrintOut = true;
}