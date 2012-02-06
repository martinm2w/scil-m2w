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
        this.buildUttLists("/home/ruobo/develop/scil0200/data/testing_agreement");
        ArrayList results = this.calAgreements();
        System.out.println("processing: " + doc_names_.get(0) + " and "+ doc_names_.get(1));
        System.out.println("res_to precision: " + results.get(0));
        System.out.println("link_to precision: " + results.get(1));
        
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
    
    private ArrayList calAgreements(){
        ArrayList calResults = new ArrayList<Double>();
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
        
        double res_to_prec =  (double)res_to_hit / (double)(anno1_res_to_list.size());
        double link_to_prec = (double)link_to_hit / (double)(anno1_res_to_list.size());
        
        calResults.add(res_to_prec);
        calResults.add(link_to_prec);
        
        return calResults;
    }
    
}
