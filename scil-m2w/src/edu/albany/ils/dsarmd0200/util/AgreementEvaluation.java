package edu.albany.ils.dsarmd0200.util;


import edu.albany.ils.dsarmd0200.lu.*;
import edu.albany.ils.dsarmd0200.util.*;
import edu.albany.ils.dsarmd0200.util.xml.*;
import edu.albany.ils.dsarmd0200.cuetag.*;
import edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash.SplitTestingUtterance;
import edu.albany.ils.dsarmd0200.evaltag.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import de.fau.cs.jstk.agmt.*;



/**
 * m2w: this class is for the evaluation of agreement on the response to and link to of 2 different annotators
 * how to use:    1. assign the correct data path for the 2 files. 
 *                2. put the 2 different files of 2 annotators in the data path. (can only do 2 file at once.)
 *                3. run the file. get results.
 * @author [Ruobo + m2w]
 * @date [Oct 31, 2011 - 1:24:02 PM]
 */
public class AgreementEvaluation {
    
    private DsarmdDP dsarmdDP_ = new DsarmdDP();
    private ArrayList tr_utts_ = new ArrayList();
    private ArrayList doc_names_ = new ArrayList();  
    private ArrayList<XMLParse> xmlps_ = new ArrayList<XMLParse>();
    private ArrayList phr_checks_ = new ArrayList();
    private ArrayList docs_ = new ArrayList();
    private ArrayList docs_utts_ = new ArrayList();
    private ArrayList fls_ = new ArrayList();
    
    public static void main(String[] args){
        AgreementEvaluation ae = new AgreementEvaluation();
        ae.agEval();
    }
    
    private void agEval(){
        this.buildUttLists("/home/ruobo/scil0200/data/testing_agreement");
//        ArrayList results = this.calAgreements();
//        System.out.println("processing: " + doc_names_.get(0) + " and "+ doc_names_.get(1));
//        System.out.println("res_to precision: " + results.get(0));
//        System.out.println("link_to precision: " + results.get(1));
//        System.out.println("anno1 diff to anno2: ");
//        for(Object anno1_diff_utt : (ArrayList)results.get(2)){
//            Utterance tempUtt = (Utterance)anno1_diff_utt;
//            System.out.println("turn[" + tempUtt.getTurn() + "]\tspk[" + tempUtt.getSpeaker() + "]\tcmtype[" + tempUtt.getCommActType() + "]\tcontent: " + tempUtt.getContent());
//        }
//        System.out.println("anno2 diff to anno1: ");
//        for(Object anno2_diff_utt : (ArrayList)results.get(3)){
//            Utterance tempUtt = (Utterance)anno2_diff_utt;
//            System.out.println("turn[" + tempUtt.getTurn() + "]\tspk[" + tempUtt.getSpeaker() + "]\tcmtype[" + tempUtt.getCommActType() + "]\tcontent: " + tempUtt.getContent());
//        }

        this.calAgreementKAlpha();

    }
        
    
    
    
    
    
//    ==================================   util methods =========================================
    /**
     * m2w: this method is for building the 2 utts lists, of 2 different annotators.
     * @param datapath1
     * @param datapath2 
     */
    private void buildUttLists(String datapath1){
        Settings.initialize();
	ParseTools.initialize();
	PronounFormMatching.initialize();
	GenderCheck.initialize();
        
        loadAndParseTraining();
        loadAndParse(datapath1);
        
        //testing load and parse result
//        System.out.println("docs_utts: " + docs_utts_.size());
//        System.out.println("docs1: ");
//        for(Object uttList : docs_utts_){
//            System.out.println("current filelist size: " + ((ArrayList)uttList).size());
//            for(Object utts : (ArrayList)uttList){
//                Utterance tempUtt = (Utterance)utts;
//                System.out.println("turn=" + tempUtt.getTurn() + " : " + tempUtt.getContent());
//            }
//        }
    }
    
    /**
     * m2w: this method is copied from Assertions.java
     */
    private void loadAndParseTraining() {
	try {
	    //System.out.println("load files...");
	    File tp = new File(Settings.getValue(Settings.TRAINING) + "_" + Settings.getValue(Settings.LANGUAGE));
	    ArrayList ps = new ArrayList();
	    while (tp != null) {
		File[] fls = tp.listFiles();
		ArrayList fls1 = new ArrayList(Arrays.asList(fls));
		Collections.sort(fls1);
		for (int i = 0; i < fls1.size(); i++) {
		    File fl = (File)fls1.get(i);
		    //System.out.println("+++++++++++++++++++++++++++++++++++++++++\nprocessing: " + fl.getName());
		    //prxmlp_.setDataUnit(fl.getName().split("\\.")[0], fl.getName().split("\\.")[0]);
		    if (fl.isDirectory()) {
			ps.add(fl);
		    }
		    else {
			//System.out.print("loading " + fl.getAbsolutePath() + "...");
			//doc_names_.add(fl.getName());
			Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
			ArrayList list = dsarmdDP_.parseDAList(doc,
							       "dsarmd",
							       false,
							       null);
			//all_utts_.addAll(list);
			tr_utts_.addAll(list);
		    }
		}
		if (ps.size() > 0) {
		    tp = (File)ps.get(0);
		    ps.remove(0);
		}
		else {
		    tp = null;
		}
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * m2w: this method is copied from Assertions.java
     */
    private void loadAndParse(String datapath1) {
	try {
            //dir 1 
	    File tp = new File(datapath1);
	    ArrayList ps = new ArrayList();
	    while (tp != null) {
		File[] fls = tp.listFiles();
		ArrayList fls1 = new ArrayList(Arrays.asList(fls));
		Collections.sort(fls1);
		for (int i = 0; i < fls1.size(); i++) {
		    File fl = (File)fls1.get(i);
		    //System.out.println("+++++++++++++++++++++++++++++++++++++++++\nprocessing: " + fl.getName());
		    if (fl.isDirectory()) {
			ps.add(fl);
		    }
		    else {
//			System.out.println("loading " + fl.getName() + "...");
			doc_names_.add(fl.getName());
			if (fl.getName().endsWith("xml")) {
			    Document doc = dsarmdDP_.getDocument(fl.getAbsolutePath());
			    ArrayList list = dsarmdDP_.parseDAList(doc,
								   "dsarmd",
								   false,
								   null);
                            //m2w: merge turns has .1 or .0 turns.
                            mergeTurnsWithSameTN(list);
			    XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
			    xmlps_.add(xp);
			    PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());
			    phr_checks_.add(phr_ch);
			    docs_.add(doc);
			    //utts_.addAll(list);
			    docs_utts_.add(list); //for test agreement between annotators
			    //all_utts_.addAll(list);
			    fls_.add(fl.getName());
			} else {
			    ArrayList list = DocumentProcessor.parseUtts(fl);
			    //System.out.println("list: \n" + list);
			    if (list != null) {
				XMLParse xp = new XMLParse(fl.getAbsolutePath(), list);
				xmlps_.add(xp);
				PhraseCheck phr_ch = new PhraseCheck(fl.getAbsolutePath(), xp.getUtterances());
				phr_checks_.add(phr_ch);
				//docs_.add(doc);
				//utts_.addAll(list);
				docs_utts_.add(list); //for test agreement between annotators
				//all_utts_.addAll(list);
				fls_.add(fl.getName());
			    }
			}
		    }
		}
		if (ps.size() > 0) {
		    tp = (File)ps.get(0);
		    ps.remove(0);
		}
		else {
		    tp = null;
		}
	    }
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * m2w: this method is copied from Assertions.java
     */
    private void mergeTurnsWithSameTN(ArrayList list){
        //test out the utts first
        for(int i = 0; i < list.size() -1 ; i++){
            Utterance tempUtt = (Utterance)list.get(i);
            String currTurn = tempUtt.getTurn();
            Utterance nextUtt = (Utterance)list.get(i+1);
            String nextTurn = nextUtt.getTurn();
            if(currTurn.equals(nextTurn)){
//                System.err.println("testing: " + tempUtt.getTurn() + " : " + tempUtt.getContent());
                tempUtt.getSub_turns().add(nextUtt);
                list.remove(i+1);
                i--;
            }
//                System.out.println("testing: " + tempUtt.getTurn() + " : " + tempUtt.getContent());
        }
    }

    /**
     * m2w: calculating agreement using krippendorf's alpha algorithm.
     */
    private void calAgreementKAlpha(){
        ArrayList calResults = new ArrayList();
        ArrayList<Utterance> anno1UttList = (ArrayList<Utterance>)docs_utts_.get(0);//all utts of anno1 file
        ArrayList<Utterance> anno2UttList = (ArrayList<Utterance>)docs_utts_.get(1);//all utts of anno2 file
        //add each res_to's utt to the list. calculate afterwards
//        System.out.println("1turn \t 2turn");
        for(Utterance tempUtt : anno1UttList)
            System.out.print(" " + tempUtt.getTurn()) ;
        System.out.println();
        for(Utterance tempUtt : anno2UttList)
            System.out.print(" " + tempUtt.getTurn() );
        System.out.println();
        //delete turns that only in 1 anno1 file
        for(int i = 0; i < anno1UttList.size(); i++){
            String tempTurnNo1 = anno1UttList.get(i).getTurn();
            boolean deleteCurrTurn = true;
            for(int j = 0; j < anno2UttList.size(); j++){
                String tempTurnNo2 = anno2UttList.get(i).getTurn();
                if(tempTurnNo1.equalsIgnoreCase(tempTurnNo2))
                    deleteCurrTurn = false;
            }
            if(deleteCurrTurn)
                anno1UttList.remove(i);
        }
        for(int i = 0; i < anno2UttList.size(); i++){
            String tempTurnNo2 = anno2UttList.get(i).getTurn();
            boolean deleteCurrTurn = true;
            for(int j = 0; j < anno1UttList.size(); j++){
                String tempTurnNo1 = anno1UttList.get(i).getTurn();
                if(tempTurnNo2.equalsIgnoreCase(tempTurnNo1))
                    deleteCurrTurn = false;
            }
            if(deleteCurrTurn)
                anno2UttList.remove(i);
        }
        System.out.println("after");
        for(Utterance tempUtt : anno1UttList)
            System.out.print(" " + tempUtt.getTurn()) ;
        System.out.println();
        for(Utterance tempUtt : anno2UttList)
            System.out.print(" " + tempUtt.getTurn() );
        System.out.println();


        //==========================================================\
        double[] anno2_data = new double[anno2UttList.size()];
        double[] anno1_data = new double[anno1UttList.size()];
        double[][] dataList = new double[2][anno1UttList.size()];

        System.out.println();
        //anno1 list building
        for(int i = 0; i < anno1UttList.size(); i++){
            String tempCommAct = "";
            Utterance tempUtt = anno1UttList.get(i);
            tempCommAct = tempUtt.getCommActType().toLowerCase();
            if(tempCommAct.equalsIgnoreCase("response-to")){
                anno1_data[i] = 1d;
            }else if(tempCommAct.equalsIgnoreCase("addressed-to")){
                anno1_data[i] = 0d;
            }else if(tempCommAct.equalsIgnoreCase("continuation-of")){
                anno1_data[i] = 0d;
            }else{
                anno1_data[i] = Double.MAX_VALUE;
            }
            System.out.print(" " + anno1_data[i]);
        }
        System.out.println();
        //anno2 list building
        for(int i = 0; i < anno2UttList.size(); i++){
            String tempCommAct = "";
            Utterance tempUtt = anno2UttList.get(i);
            tempCommAct = tempUtt.getCommActType().toLowerCase();
            if(tempCommAct.equalsIgnoreCase("response-to")){
                anno2_data[i] = 1d;
            }else if(tempCommAct.equalsIgnoreCase("addressed-to")){
                anno2_data[i] = 0d;
            }else if(tempCommAct.equalsIgnoreCase("continuation-of")){
                anno2_data[i] = 0d;
            }else{
                anno2_data[i] = Double.MAX_VALUE;
            }
            System.out.print(" " + anno2_data[i]);
        }
        System.out.println();

        //add to dataList. 
        dataList[0] = anno1_data;
        dataList[1] = anno2_data;

//        Alpha alpha4 = new Alpha(new NominalMetric(false));
//        System.out.println(kpalpha.agreement(dataList));
        Alpha alpha1 = new Alpha(new NominalMetric(true));
        Alpha alpha2 = new Alpha(new IntervalMetric());
        Alpha alpha3 = new Alpha(new RatioMetric());
        System.out.println("agreement on communication act");
        System.out.println("alpha nominal = " + alpha1.agreement(dataList));
        System.out.println("alpha interval = " + alpha2.agreement(dataList));
        System.out.println("alpha ratio = " + alpha3.agreement(dataList));
//        System.out.println(kpalpha.agreement(link_to_data));

        //res-to agreement cal
        ArrayList<Utterance> anno1_res_to_list = new ArrayList<Utterance>();
        ArrayList<Utterance> anno2_res_to_list = new ArrayList<Utterance>();

        //add each res_to's utt to the list. calculate afterwards
        for(Utterance tempUtt : anno1UttList){
            if(tempUtt.getCommActType().equalsIgnoreCase("response-to")){
                anno1_res_to_list.add(tempUtt);
            }
        }
        for(Utterance tempUtt : anno2UttList){
            if(tempUtt.getCommActType().equalsIgnoreCase("response-to")){
                anno2_res_to_list.add(tempUtt);
            }
        }

                //building the data array for alpha's algorithm.
        ArrayList<Double> alpha_res_to_anno1 = new ArrayList<Double>();
        ArrayList<Double> alpha_res_to_anno2 = new ArrayList<Double>();
        ArrayList<Double> alpha_link_to_anno1 = new ArrayList<Double>();
        ArrayList<Double> alpha_link_to_anno2 = new ArrayList<Double>();

//        for(Utterance tempUtt : anno1_res_to_list){
        for(int i = 0; i < anno1_res_to_list.size(); i++){
            Utterance tempUtt = anno1_res_to_list.get(i);
            String anno1_turn = tempUtt.getTurn();
            String anno1_spk_name = tempUtt.getSpeaker();
            String anno1_link_to = tempUtt.getRespTo();

            alpha_res_to_anno1.add(1d);
            alpha_link_to_anno1.add(1d);
            alpha_res_to_anno2.add(0d);
            alpha_link_to_anno2.add(0d);
//            for(Utterance anno2Utt : anno2_res_to_list){
            for(int j = 0; j < anno2_res_to_list.size(); j++){
                Utterance anno2Utt = anno2_res_to_list.get(j);
                String anno2_turn = anno2Utt.getTurn();
                String anno2_spk_name = anno2Utt.getSpeaker();
                String anno2_link_to = anno2Utt.getRespTo();
                if(anno1_turn.equals(anno2_turn) && anno1_spk_name.equalsIgnoreCase(anno2_spk_name)){
                    alpha_res_to_anno2.set(i, 1d);
                    //2. calculation of link_to precision.
                    if(anno1_link_to.equalsIgnoreCase(anno2_link_to)){
                        alpha_link_to_anno2.set(i, 1d);
                    }
                }
            }
        }
        //==========================================================
//
//        //building the data array for alpha's algorithm.
//        ArrayList<Double> alpha_res_to_anno1 = new ArrayList<Double>();
//        ArrayList<Double> alpha_res_to_anno2 = new ArrayList<Double>();
//        ArrayList<Double> alpha_link_to_anno1 = new ArrayList<Double>();
//        ArrayList<Double> alpha_link_to_anno2 = new ArrayList<Double>();
//
////        for(Utterance tempUtt : anno1_res_to_list){
//        for(int i = 0; i < anno1_res_to_list.size(); i++){
//            Utterance tempUtt = anno1_res_to_list.get(i);
//            String anno1_turn = tempUtt.getTurn();
//            String anno1_spk_name = tempUtt.getSpeaker();
//            String anno1_link_to = tempUtt.getRespTo();
//
//            alpha_res_to_anno1.add(1d);
//            alpha_link_to_anno1.add(1d);
//            alpha_res_to_anno2.add(0d);
//            alpha_link_to_anno2.add(0d);
////            for(Utterance anno2Utt : anno2_res_to_list){
//            for(int j = 0; j < anno2_res_to_list.size(); j++){
//                Utterance anno2Utt = anno2_res_to_list.get(j);
//                String anno2_turn = anno2Utt.getTurn();
//                String anno2_spk_name = anno2Utt.getSpeaker();
//                String anno2_link_to = anno2Utt.getRespTo();
//                if(anno1_turn.equals(anno2_turn) && anno1_spk_name.equalsIgnoreCase(anno2_spk_name)){
//                    alpha_res_to_anno2.set(i, 1d);
//                    //2. calculation of link_to precision.
//                    if(anno1_link_to.equalsIgnoreCase(anno2_link_to)){
//                        alpha_link_to_anno2.set(i, 1d);
//                    }
//                }
//            }
//        }
//
//        //testing lists
//        System.out.println(alpha_res_to_anno1.size());
//        System.out.println(alpha_res_to_anno1);
//        System.out.println(alpha_res_to_anno2.size());
//        System.out.println(alpha_res_to_anno2);
//        System.out.println(alpha_link_to_anno1.size());
//        System.out.println(alpha_link_to_anno1);
//        System.out.println(alpha_link_to_anno2.size());
//        System.out.println(alpha_link_to_anno2);
//
//        System.out.println("----"+anno1_res_to_list.size());
//
//        //build arrays
//        double [] res_to_anno1_array = new double[alpha_res_to_anno1.size()];
//        double [] res_to_anno2_array = new double[alpha_res_to_anno2.size()];
//        double [] link_to_anno1_array = new double[alpha_link_to_anno1.size()];
//        double [] link_to_anno2_array = new double[alpha_link_to_anno2.size()];
//        for(int i = 0; i < alpha_res_to_anno1.size(); i++){
//            res_to_anno1_array[i] = alpha_res_to_anno1.get(i);
//        }
//        for(int i = 0; i < alpha_res_to_anno2.size(); i++){
//            res_to_anno2_array[i] = alpha_res_to_anno2.get(i);
//        }
//        for(int i = 0; i < alpha_link_to_anno1.size(); i++){
//            link_to_anno1_array[i] = alpha_link_to_anno1.get(i);
//        }
//        for(int i = 0; i < alpha_link_to_anno2.size(); i++){
//            link_to_anno2_array[i] = alpha_link_to_anno2.get(i);
//        }
//        //testing arrays
//        System.out.println(res_to_anno1_array.length);
//        System.out.println(res_to_anno2_array.length);
//        System.out.println(link_to_anno1_array.length);
//        System.out.println(link_to_anno2_array.length);
//        ArrayList<ArrayList<Double>> res_to_data_list = new ArrayList<ArrayList<Double>>();
//        ArrayList<ArrayList<Double>> link_to_data_list = new ArrayList<ArrayList<Double>>();
//        res_to_data_list.add(alpha_res_to_anno1); res_to_data_list.add(alpha_res_to_anno2);
//        link_to_data_list.add(alpha_link_to_anno1); link_to_data_list.add(alpha_link_to_anno2);

//        double[][] res_to_data = new double[2][alpha_res_to_anno1.size()];
//        double[][] link_to_data = new double[2][alpha_link_to_anno1.size()];
//        res_to_data[0] = res_to_anno1_array;
//        res_to_data[1] = res_to_anno2_array;
//        link_to_data[0] = link_to_anno1_array;
//        link_to_data[1] = link_to_anno2_array;
//
//        res_to_data[0] = ;
//        res_to_data_list.toArray(res_to_data);
//        link_to_data_list.toArray(link_to_data);

//        Metric metric_res_to = new Metric();
        
//        System.out.println(alpha3.agreement(res_to_data));
//        kpalpha.main(args);



                //p
//        int hit = 0;
//        outter:
//        for(int i = 0; i < anno1_data.length; i++){
////                System.out.println(anno1_data[i]);
////                System.out.println(anno2_data[i]);
//            if(anno1_data[i] == anno2_data[i]){
//                hit++;
//            }
//        }
//        System.out.println(hit);
//        System.out.println((double)hit / (double)anno1_data.length);

    }

    private ArrayList calAgreements(){
        ArrayList calResults = new ArrayList();
        ArrayList<Utterance> anno1UttList = (ArrayList<Utterance>)docs_utts_.get(0);
        ArrayList<Utterance> anno2UttList = (ArrayList<Utterance>)docs_utts_.get(1);
        
        int res_to_hit = 0;
        int link_to_hit = 0;
        ArrayList<Utterance> anno1_res_to_list = new ArrayList<Utterance>();
        ArrayList<Utterance> anno2_res_to_list = new ArrayList<Utterance>();
        
        //add each res_to's utt to the list. calculate afterwards
        for(Utterance tempUtt : anno1UttList){
            if(tempUtt.getCommActType().equalsIgnoreCase("response-to")){
                anno1_res_to_list.add(tempUtt);
            }
        }
        for(Utterance tempUtt : anno2UttList){
            if(tempUtt.getCommActType().equalsIgnoreCase("response-to")){
                anno2_res_to_list.add(tempUtt);
            }
        }
        //precision calculation.
        //current algorithm :
        // hits = number of res_to , both in anno1 and anno2's list. 
        // size mean = mean of the anno1 and anno2 res_to_list size.
        // prec = hits / size mean.
        for(Utterance tempUtt : anno1_res_to_list){
            String anno1_turn = tempUtt.getTurn();
            String anno1_spk_name = tempUtt.getSpeaker();
            String anno1_link_to = tempUtt.getRespTo();
            for(Utterance anno2Utt : anno2_res_to_list){
                String anno2_turn = anno2Utt.getTurn();
                String anno2_spk_name = anno2Utt.getSpeaker();
                String anno2_link_to = anno2Utt.getRespTo();
                //1. calculation of res_to precision.
                if(anno1_turn.equals(anno2_turn) && anno1_spk_name.equalsIgnoreCase(anno2_spk_name)){
                    res_to_hit ++;
                    //2. calculation of link_to precision.
                    if(anno1_link_to.equalsIgnoreCase(anno2_link_to)){
                        link_to_hit++;
                    }
                }
            }
        }
        int sizeMean = (anno1_res_to_list.size() + anno2_res_to_list.size()) / 2;
        double res_to_prec =  (double)res_to_hit / (double)sizeMean;
        double link_to_prec = (double)link_to_hit / (double)sizeMean;
        
        calResults.add(res_to_prec);
        calResults.add(link_to_prec);
        
        
        //disagreement analysis.
        ArrayList anno1_diff_list = new ArrayList<Utterance>();
        ArrayList anno2_diff_list = new ArrayList<Utterance>();
        //difference in anno2 
        for(Utterance anno1_res_to_turn : anno1_res_to_list){
            String anno1_turn = anno1_res_to_turn.getTurn();
            String anno1_spk_name = anno1_res_to_turn.getSpeaker();
            for(Utterance anno2_turns : anno2UttList){
                String anno2_turn = anno2_turns.getTurn();
                String anno2_spk_name = anno2_turns.getSpeaker();
                if(anno1_turn.equalsIgnoreCase(anno2_turn) && anno1_spk_name.equalsIgnoreCase(anno2_spk_name)){
                    String anno2_cmtype = anno2_turns.getCommActType();
                   if(!anno2_cmtype.equalsIgnoreCase("response-to")){
                       anno2_diff_list.add(anno2_turns);
                   } 
                }
            }
        }
        //difference in anno1
        for(Utterance anno2_res_to_turn : anno2_res_to_list){
            String anno2_turn = anno2_res_to_turn.getTurn();
            String anno2_spk_name = anno2_res_to_turn.getSpeaker();
            for(Utterance anno1_turns : anno1UttList){
                String anno1_turn = anno1_turns.getTurn();
                String anno1_spk_name = anno1_turns.getSpeaker();
                if(anno2_turn.equalsIgnoreCase(anno1_turn) && anno2_spk_name.equalsIgnoreCase(anno1_spk_name)){
                    String anno1_cmtype = anno1_turns.getCommActType();
                   if(!anno1_cmtype.equalsIgnoreCase("response-to")){
                       anno1_diff_list.add(anno1_turns);
                   } 
                }
            }
        }
        
        calResults.add(anno1_diff_list);
        calResults.add(anno2_diff_list);
        
        
        return calResults;
    }
    
}

