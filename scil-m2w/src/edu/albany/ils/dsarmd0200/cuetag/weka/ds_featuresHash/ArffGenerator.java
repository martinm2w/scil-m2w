/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;
import edu.albany.ils.dsarmd0200.lu.Settings;
import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.cuetag.svm.TagNumber;
import edu.albany.ils.dsarmd0200.util.GenderCheck;
import edu.albany.ils.dsarmd0200.util.ParseTools;
import edu.albany.ils.dsarmd0200.util.Util;

/**
 *
 * @author Laura G.H. Jiao
 */
public class ArffGenerator {

    private String arffTrainingFileName = "";
    private String arffTrainingFileLocation = "";
    private String arffTestingFileName = "";
    private String arffTestingFileLocation = "";
    private ArrayList tr_utts = new ArrayList();
    private ArrayList utts = new ArrayList();
    private ArrayList tags = new ArrayList(); // tags of the corpus
    private HashMap<String, ArrayList> featuresMap = new HashMap<String, ArrayList>(); // key: tag; value: {features}
    private ArrayList<String> allFeatures = new ArrayList<String>(); // all features in the training set
    private static String tagType = Settings.getValue("tagType");
    //Lin added
    private boolean bNegativeFeatures=false;

    private HashMap<String, Integer> term_frequency_ = new HashMap<String, Integer>(); // key: ngram term in training set; value: frequency
    private HashMap<String, int[]> tag_frequency_ = new HashMap<String, int[]>(); // key: ngram term in training set; value: [tag1_frequency][tag2_frequency]...
    static int term_frequency_threshold_ = 3;
    //static double fraction_threshold_ = 0.325; 01/19/2012
    static double fraction_threshold_ = 0.4   ;//0.325;


//    private static PNWords pnw = new PNWords();
    private TagRulesPredefined trp;
    private HashMap<String, double[]> ad_ngrams_ = null;
    private HashMap<String, double[]> dr_ngrams_ = null;

    
     //check for the greatest Ad or Dr score and return tha term
     private    HashMap<String,Double> ad_dr=new HashMap<String,Double>();
     private    String utnumber="";
     private    ArrayList<Double> drAndad=new ArrayList<Double>();
     private    ArrayList<String> adrterms=new ArrayList<String>();
     private    ArrayList<String> tno=new ArrayList<String>();
     
    public ArffGenerator(String tr_fileName, String tr_fileLocation, String te_fileName, String te_fileLocation,
            ArrayList tr_utts, ArrayList utts, ArrayList tags, HashMap featuresMap, ArrayList allFeatures) {
        arffTrainingFileName = tr_fileName;
        arffTrainingFileLocation = tr_fileLocation;
        arffTestingFileName = te_fileName;
        arffTestingFileLocation = te_fileLocation;
        this.tr_utts = tr_utts;
        this.utts = utts;
        this.tags = tags;
        this.featuresMap = featuresMap;
        this.allFeatures = allFeatures;

//         modified by laura, Jul 11, 2011
        if (Settings.getValue("language").equals("chinese")) {
//            System.out.println("Use Chinese Ngram rules ...");
            trp = new TagRulesPredefinedChinese();
            //trp = new TagRulesPredefined(); //comment 01/19/2012
        }
        else{
            trp = new TagRulesPredefined();
        }
        term_frequency_.clear();
        tag_frequency_.clear();
        //ad_ngrams_ = (HashMap<String, double[]>) Util.loadFile(Settings.getValue("ad_ngrams"));
    }

    public void writeTrainingArff() {
        calculateTrainingFrequency(tr_utts);
        calculateTrainingTagFrequency(tr_utts);
        writeArffHeading(arffTrainingFileLocation, arffTrainingFileName, tags);
        appendToTrainingArff(arffTrainingFileLocation, tr_utts, featuresMap);
    }

    public void writeTestingArff(Boolean nonBlank) {
        writeArffHeading(arffTestingFileLocation, arffTestingFileName, tags);
        appendToTestingArff(arffTestingFileLocation, utts, allFeatures, nonBlank);
    }

    /**
     * Prepare arff heading
     *
     * @param tags Arff heading: classes
     */
    private void writeArffHeading(String arffFileLocation, String arffFileName, ArrayList tags) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation));
            /*
             * write the arff heading
             */
            bw.write("@RELATION " + arffFileName + "\n");
            bw.write("@ATTRIBUTE tag { ");
            String unknown = "unknown";
            boolean has_ukn = false;
            for (int i = 0; i < tags.size(); i++) {
                bw.write(tags.get(i) + ", ");
                if (unknown.equals(tags.get(i))) {
                    has_ukn = true;
                }
            }
            if (!has_ukn) {
                bw.write(unknown + ",");
            }
            bw.write("}\n");
            bw.write("@ATTRIBUTE utterance string\n");
            bw.write("\n@DATA\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendToTrainingArff(String arffFileLocation, ArrayList tr_utts, HashMap featuresMap) {
        BufferedWriter bw = null;

        String adString = "";

         String outAD = "";
         String outDR = "";
         String strSep = System.getProperty("line.separator");
         HashMap<String, Integer> preDR = new HashMap<String, Integer>();
         HashMap<String, Integer> postDR = new HashMap<String, Integer>();
         HashMap<String, Integer> preNonDR = new HashMap<String, Integer>();
         HashMap<String, Integer> postNonDR = new HashMap<String, Integer>();
         HashMap<String, Integer> triDR = new HashMap<String, Integer>();
         HashMap<String, Integer> triNonDR = new HashMap<String, Integer>();

        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation, true));

            for (int i = 0; i < tr_utts.size(); i++) { // tr_utts
                Utterance utterance = (Utterance) tr_utts.get(i);
                String daTag = "";
                String str = "";
                daTag = TaggingType.getTag(utterance, tagType);
//                if(tagType.equals("da15")){
//                    daTag = utterance.getTag().toLowerCase().trim();
//                    daTag = daTag.replace("--", "");
//                }
//                else if(tagType.equals("da3")){
//                    daTag = utterance.getMTag().toLowerCase().trim();
//                }
//                else{
//                    System.err.println("unrecognized tag type");
//                    return;
//                }
                ArrayList featuresOfTheTag = (ArrayList) featuresMap.get(daTag);


                // by Laura Nov 30, 2010
                String commActType = utterance.getCommActType().toLowerCase();

                String content = utterance.getContent();

                // by Laura Nov 30, 2010
//LinCommented                content = Ngram.urlNormalize(content);
//LinCommented                content = Ngram.filterUtterance(content);

//                // by Laura Dec 07, 2010
//                content = pnw.replaceSentence(content);

//Lin Added
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")) {
                    content = utterance.getSpaceTagContent();
                }
                content = Ngram.urlNormalize(content);

                content = Ngram.filterUtterance(content);

//end of
                ArrayList<String> ngrams = Ngram.generateNgramList(content);

                // by Laura Jan 04, 2011
                if (daTag.toLowerCase().contains(DsarmdDATag.AD)) { // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                }
                // by Laura Jan 25, 2011
                //Commented out by cslin
                if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.DR);
                }

                //Lin modified
                TagNumber tn = new TagNumber();
                String tag = utterance.getTag(); // dialog_act tag
                tag = tn.DialogAct(tag.toLowerCase(),
                        Integer.parseInt(Settings.getValue("tagNum"))).trim();
                int tagNum = tn.tagNumberDialogAct(tag.toLowerCase(), Integer.parseInt(Settings.getValue("tagNum")));

                for (int j = 0; j < ngrams.size(); j++) {
                    String term = ngrams.get(j);
                    int total_freq = (Integer) term_frequency_.get(term);
                    int[] freq = tag_frequency_.get(term);
                    int tag_freq = freq[tagNum - 1];
                    double fraction = (double) tag_freq / (double) total_freq;
                    String[] wordsInTerm = term.split("\\s+");
                    // by Laura Jan 04, 2011
                    if (!term.equals(DsarmdDATag.AD)
                            && !term.equals(DsarmdDATag.DR)
                            && wordsInTerm.length < 2) {
                        continue;
                    }
                    if (featuresOfTheTag != null && featuresOfTheTag.contains(term)) {
                        // use else if to avoid repeatly include the "hastopic" and "notopic"

                      if(total_freq >= term_frequency_threshold_ &&
                             fraction >= fraction_threshold_)
                      {
                            if (daTag.equalsIgnoreCase("action-directive")
                                    && fraction >= 0.6
                                    && (Settings.getValue(Settings.LANGUAGE)).equals("chinese")
                                    && !commActType.equalsIgnoreCase("response-to")) {
                                str += "|" + "ADM";
                                ADMSet += "|" + term;
                                //outAD+="|" + term+","+tag_freq+"; "+total_freq +strSep;
                                //adString+="|"+term;
                                continue;
                            }





                         /*if (daTag.equalsIgnoreCase("disagree-reject") &&
                             fraction >= 0.5 &&
                             (Settings.getValue(Settings.LANGUAGE)).equals("chinese") ){
                             str += "|" + "DRM";
                             DRMSet += "|" + term;
                             continue;
                         }*/

                         AllGramsSet= "|" + term;


                         str += "|" + term;

                        }




                    }
                }
                // end of Lin


                if(!str.equals("")){
                // by Laura Nov 30, 2010
                   /*if ( str.contains(DsarmdDATag.DR) && commActType.contains("response-to")){
                       str=str.replace("|"+DsarmdDATag.DR, "");
                       bw.write(daTag + ", '" + str + "|" + "DRRT" + "'\n");
                   }else*/

                    bw.write(daTag + ", '" + str + "|" + commActType + "'\n");
//                    bw.write(tag + ", '" + str + "'\n");
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private String termMatching(ArrayList utts, Utterance utterance, int iCurrent){
        String retString="";

        String[] resps = utterance.getRespTo().split(":");
        Utterance preUtterance=null;

	if (resps.length > 1) {
            String preIndex=resps[1].toLowerCase();

           for(int kk = 0; kk < utts.size(); kk++){ // utts
              preUtterance =(Utterance)utts.get(kk);
              if (preUtterance.getTurn().equalsIgnoreCase(preIndex)){

                String[] preContent=(preUtterance.getSpaceTagContent()).split(" ");
                String[] Content=(utterance.getSpaceTagContent()).split(" ");
                for (int i=0; i<Content.length; i++){
                    for(int j=0; j<preContent.length; j++){

                       if (Content[i].equalsIgnoreCase(preContent[j])){
                         retString+=Content[i]+" ";
                       }

                    }
                }
                break;
              }
            }
	}
        String tag = TaggingType.getTag(utterance, tagType);
        //System.out.println(tag+"; Utt "+utterance.getTurn()+" Rep Term:"+retString);
        return retString;

    }

    private int pairComparisonEng(ArrayList utts, Utterance utterance){
        //return 1 for Dis-Rej and 2 for NonDis-Rej

        String[] negWords={"though", "tho", "not", "no", "but", "can\'t",
                           "disagree", "disagrees", "disagreed","do not",
                           "doesn\'t", "doesnt",
                           "don\'t", "dont",
                           "didn\'t", "didnt"};

        String[] questionMarks={"?"};

        String[] posWords={"i agree", "yeah", "yea", "yes", "not sure", "not bad"};

        String[] resps = utterance.getRespTo().split(":");
        Utterance preUtterance=null;

        String sRepeatedConcept="";
        boolean bRepeatedConcept=false;
        boolean bPosInPre=false;
        boolean bNegInPre=false;
        boolean bPos=false;
        boolean bNeg=false;
        boolean bStart=false;
        int iNegInPre=0;
        int iPosInPre=0;
        int iNeg=0;
        int iPos=0;

        if (resps.length > 1) {
            String preIndex=resps[1].toLowerCase();
           
           for(int kk = 0; kk < utts.size(); kk++){ // utts
              preUtterance =(Utterance)utts.get(kk);

              //the replied utterance is found
              if (preUtterance.getTurn().equalsIgnoreCase(preIndex)){

                //String[] preContent=(preUtterance.getSpaceTagContent()).split(" ");
                //String[] Content=(utterance.getSpaceTagContent()).split(" ");
                String[] preContent=(preUtterance.getTaggedContent()).split(" ");
                String[] Content=(utterance.getTaggedContent()).split(" ");

                //check if the preSentence or utterance is a question?
                //check if the preSentence or utterance is a question?
                for (int i=0; i<questionMarks.length; i++){
                    /*if ( preContent[preContent.length-1].contains(questionMarks[i]) ||
                         (preContent.length-2>=0 && preContent[preContent.length-2].contains(questionMarks[i]))
                       )*/
                    /*if ( preContent[preContent.length-1].contains(questionMarks[i]) )
                    return 2;*/
                    
                    /*if ( Content[Content.length-1].contains(questionMarks[i]) ||
                         (Content.length-2>=0 && Content[Content.length-2].contains(questionMarks[i]))
                       )*/
                    if ( Content[Content.length-1].contains(questionMarks[i]) ){
                    return 2;
                    }
                }

                //check the number of positive words
                /*for (int i=0; i<posWords.length; i++){

                     if (preUtterance.getContent().toLowerCase().contains(posWords[i])){

                         iPosInPre++;
                     }

                     if (utterance.getContent().toLowerCase().contains(posWords[i])){
                         iPos++;
                     }
                }*/

                //Check the Neg words in utterance
                if (iPos>0){
                    return 2;
                }
                //check the number of negative words
                for (int i=0; i<negWords.length; i++){

                     if (preUtterance.getContent().toLowerCase().contains(negWords[i])){

                         iNegInPre++;
                     }

                     if (utterance.getContent().toLowerCase().contains(negWords[i])){
                         iNeg++;
                     }
                }

                //Check the Neg words in utterance
                if (iNeg>0){
                    return 1;
                }
              }
           }
        }
        return 0;
    }
    private int pairComparison(ArrayList utts, Utterance utterance){
        //String[] posWords={"支持","看重","可以","有的","不错","有用","好","有","对"};
        String[] posWords={"支持","看重","可以","不错","没错","好","有","对"};
        //String[] negWords={"不是","但是","不过","并不","没用","不同","没有","没什么","但","不","难"};
        String[] negWords={"可是","恩","没","但","不","难"};

        String[] startWords={"可是","但是","不是"};
        String[] agreeWords={"嗯","恩","对","没错","不错"};

        String[] questionMarks={"？","嗎","?","啥","吗","么"};

        String[] resps = utterance.getRespTo().split(":");
        Utterance preUtterance=null;

        String sRepeatedConcept="";
        boolean bRepeatedConcept=false;
        boolean bPosInPre=false;
        boolean bNegInPre=false;
        boolean bPos=false;
        boolean bNeg=false;
        boolean bStart=false;
        int iNegInPre=0;
        int iPosInPre=0;
        int iNeg=0;
        int iPos=0;


        if (resps.length > 1) {
            String preIndex=resps[1].toLowerCase();

           for(int kk = 0; kk < utts.size(); kk++){ // utts
              preUtterance =(Utterance)utts.get(kk);

              //the replied utterance is found
              if (preUtterance.getTurn().equalsIgnoreCase(preIndex)){

                String[] preContent=(preUtterance.getSpaceTagContent()).split(" ");
                String[] Content=(utterance.getSpaceTagContent()).split(" ");

                //check if the preSentence or utterance is a question?
                for (int i=0; i<questionMarks.length; i++){
                    /*if ( preContent[preContent.length-1].contains(questionMarks[i]) ||
                         (preContent.length-2>=0 && preContent[preContent.length-2].contains(questionMarks[i]))
                       )*/
                    if ( preContent[preContent.length-1].contains(questionMarks[i]) ){
                    return 2;
                    }
                    /*if ( Content[Content.length-1].contains(questionMarks[i]) ||
                         (Content.length-2>=0 && Content[Content.length-2].contains(questionMarks[i]))
                       )*/
                    if ( Content[Content.length-1].contains(questionMarks[i]) ){
                    return 2;
                    }
                }

                //check if the utterance is a agreed sentence
                for (int i=0; i<agreeWords.length; i++){
                     if (Content[0].contains(agreeWords[i])){
                         return 2;
                     }
                }


                //check the repeated concept
                for (int i=0; i<Content.length; i++){
                    for(int j=0; j<preContent.length; j++){

                       if (Content[i].equalsIgnoreCase(preContent[j])){
                          if (Content[i].length()>=2){
                            bRepeatedConcept=true;
                          }
                       }

                    }
                }

                //check the number of positive words
                for (int i=0; i<posWords.length; i++){
                     if (preUtterance.getContent().contains(posWords[i])){
                         iPosInPre++;
                     }

                     if (utterance.getContent().contains(posWords[i])){
                         iPos++;
                     }
                }





                //check the number of negative words


                for (int i=0; i<negWords.length; i++){

                     if (preUtterance.getContent().contains(negWords[i])){
                         iNegInPre++;
                     }

                     if (utterance.getContent().contains(negWords[i])){
                         iNeg++;
                     }
                }

                //if content or precontent contains agreeWords, and the other one doesn't
                //have negative word, it is not Dis-Rej.
                for (int i=0; i<agreeWords.length; i++){
                     if (agreeWords[i].length()>1 && preUtterance.getContent().contains(agreeWords[i])){
                        if (iNeg==0){
                          return 2;
                        }
                     }
                     if (agreeWords[i].length()>1 && utterance.getContent().contains(agreeWords[i])){
                        if (iNegInPre==0){
                          return 2;
                        }
                     }
                }





                //check the begining word
                for (int i=0; i<startWords.length; i++){
                   if (Content[0].contains(startWords[i])){
                       return 1;
                   }
                }

                //Check all conditions of Dis-Rej
               if (Content.length>=3){
                if (bRepeatedConcept && iNeg>0){
                    return 1;
                }

                if (iNegInPre>0 && iPos>0){
                    return 1;
                }

                if (iPosInPre>0 && iNeg>0){
                    return 1;
                }

                if (iNegInPre>0 && iNeg>1){
                    return 1;
                }

                if (bRepeatedConcept && iNegInPre>0 && iNeg==0){
                    return 1;
                }
               }





                break;
              }
            }
	}
        return 0;
    }
  
    
       //filter Let/'s say/see from AD-3/29/12
        private void filterAD(ArrayList utts){
        ArrayList<String> filterAD=new ArrayList<String>();
         String tagcontent="",content="";
         
         String[] words=null;
               for (int i = 0; i < utts.size(); i++) { 
                Utterance utterance = (Utterance) utts.get(i);
               tagcontent=utterance.getTaggedContent();
               content=utterance.getContent();
               words=tagcontent.split(" ");
              
               String see="";

              for(int j=0;j<words.length;j++){
                   String required="",tmp="";
//                   System.out.println("words:"+words[j]);
               if(words[j].contains("/VB")){
                if(j>1 && words.length>0){
                    words[j-2]=words[j-2].replaceAll("/+([A-Z]+)*"," ");
                    words[j-1]=words[j-1].replaceAll("/+([A-Z]+)*"," ");
                    words[j]=words[j].replaceAll("/+([A-Z]+)*"," ");
                    required=(words[j-1]+words[j]);   
                    tmp=   words[j-2]+words[j-1]+words[j];
                    //filter out let+somebody+/VB
                if((words[j-2].trim().equalsIgnoreCase("let"))&& (ParseTools.ignorePronoun((words[j-1].trim()))) &&(content.toLowerCase().startsWith("let")) ||
                   ((words[j-2].trim().equalsIgnoreCase("let"))&& (GenderCheck.isPronounHuman((words[j-1].trim()))) &&(content.toLowerCase().startsWith("let")))){

                    required=Ngram.filterUtterance(tmp);
                    see=required;
                    filterAD.add(required);
//                    System.out.println("TurnNo:"+utterance.getTurn()+":\tLet me /Verb "+tmp);

                }
                                              
                }

                }
                //filter out only let's say/let's see 
                if(j>0 && words.length>0){
               
                    words[j-1]=words[j-1].replaceAll("/+([A-Z]+)*"," ");
                    words[j]=words[j].replaceAll("/+([A-Z]+)*"," ");
                    required=(words[j-1]+words[j]);   
               if((j>0 && words[j-1].equalsIgnoreCase("Let's ") &&(content.toLowerCase().contains("let\'s"))  && (words[j].contains("see") |words[j].contains("say"))  )){
                    required=Ngram.filterUtterance(required);
                    see=required;
//                    System.out.println("TurnNo:"+utterance.getTurn()+":\tLet's+see/say "+required);
                    filterAD.add(required);
                }
                }
                
             }
              for(String s:filterAD){
                  trp.setfilter(s);
              }
    }
        
}
      //add new AD features-3/13/12
        private void addADFeatures(ArrayList utts){
        
         String tagcontent="",content="";
         String[] words=null;
               for (int i = 0; i < utts.size(); i++) { 
                Utterance utterance = (Utterance) utts.get(i);
               tagcontent=utterance.getTaggedContent();
               content=utterance.getContent();
               words=tagcontent.split(" ");
//               System.out.println(utterance.getTurn()+":tagcontent:\t"+tagcontent);
              for(int j=0;j<words.length;j++){
                   String required="";
               if(words[j].contains("/VB")){
                if(j>0 && words.length>0){
                    words[j-1]=words[j-1].replaceAll("/+([A-Z]+)*"," ");
                    words[j]=words[j].replaceAll("/+([A-Z]+)*"," ");
                    required=(words[j-1]+words[j]);   
                

                if((j>0 && words[j-1].equalsIgnoreCase("So "))&&(tagcontent.toLowerCase().startsWith("so") )){
                   required=Ngram.filterUtterance(required);
//                    System.out.println("TurnNo:"+utterance.getTurn()+":\tSO+Verb "+required);
                }
            
                else if(j>0 && words[j-1].equalsIgnoreCase("please ")){


                        required=Ngram.filterUtterance(required);
//                        System.out.println("TurnNo:"+utterance.getTurn()+":\tplease+Verb==="+required);
                            
                    }
                    else if(j>0 && words[j-1].equalsIgnoreCase("feel free to ") && (content.toLowerCase().startsWith("feel free to ") | content.toLowerCase().contains(". feel free to "))){
                        required=Ngram.filterUtterance(required);
                    }
                    
                else{
                        required="";
                    }
                }

                }
                if(required!= null && !(required.equals(""))){
                trp.addmoreADfeature(required);
                }
        
            }
    }  
        //end adding AD features-3/13/12
    }
    
    
    private void appendToTestingArff(String arffFileLocation, ArrayList utts, Collection allFeatures, Boolean nonBlank){
         String testoutAD="";
         String testoutDR="";
         String tagGTCAT="";
         String tagcont="";
         String strSep=System.getProperty("line.separator"); 
         ArrayList featuresOfTheTag = (ArrayList)featuresMap.get(DsarmdDATag.DR);
         BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation, true));

            for (int i = 0; i < utts.size(); i++) { // utts
                Utterance utterance = (Utterance) utts.get(i);
                String tag = "";
                String str = "";
                tag = TaggingType.getTag(utterance, tagType);
                //String com_act_type = utterance.getCommActType();
//                if(tagTypeTaggingType.equals("da15")){
//                    tag = utterance.getTag().toLowerCase().trim();
//                    tag = tag.replace("--", "");
//                }
//                else if(tagType.equals("da3")){
//                    tag = utterance.getMTag().toLowerCase().trim();
//                }
//                else{
//                    System.err.println("unrecognized tag type");
//                    return;
//                }
//                if(tag == null || tag.equals("")) // testing file may not have tag attribute
//                    tag = "unknown";

                // by Laura Nov 30, 2010
                String commActType = utterance.getCommActType().toLowerCase();

                String content = utterance.getContent();
                
                // by Laura Nov 30, 2010
//LinCommented                content = Ngram.urlNormalize(content);
//LinCommented                content = Ngram.filterUtterance(content);


                // by Laura Jan 04, 2011
                // Add pre-defined features
//LinCommented                content = trp.rules_filtered(content);


//                // by Laura Dec 07, 2010
//                content = pnw.replaceSentence(content);
//Lin Added
                int iUttLen=content.length();
                String strUtterance=content;
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese"))
		{
                    content=utterance.getSpaceTagContent();

                }
                
                 if (content == null) {
                    content = "";
                }
                content = Ngram.urlNormalize(content);
                content = Ngram.filterUtterance(content);
                //trp.allGramsAndDRMSet(AllGramsSet, DRMSet);
                //content = "<start> " + content.toLowerCase().trim() + " <finish>";

                //CSLin added--retrieve the ground truth for Comm Act type
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")){
                   //tagGTCAT=utterance.getGroundTruthCommActType();
                   tagGTCAT=utterance.getCommActType();
                   trp.setApproved(false);
                   trp.setTag(tagGTCAT);
                }
                else if ((Settings.getValue(Settings.LANGUAGE)).equals("english")){
                   //tagGTCAT=utterance.getGroundTruthCommActType();
                    tagGTCAT=utterance.getCommActType();
                }

                //end
                //filter AD and add new AD features
                filterAD(utts);
                addADFeatures(utts);
                
               content = trp.rules_filtered(content);
//   System.out.println(utterance.getTurn()+":::AFter TRPDef==="+content);              
                ArrayList<String> ngrams = Ngram.generateNgramList(content);
//                System.out.println(utterance.getTurn()+":"+ngrams);
                //if it contains WikiFeatures
                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    
                if(term.equals("wiki_ad")){
//                    System.out.println("here!");
                    str = DsarmdDATag.AD;
                    ngrams.clear();
                     ngrams.add(DsarmdDATag.AD);
                            break;
                }
                else{
                ;
                }  
                }
                
            
                
     
                
                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    String[] wordsInTerm = term.split("\\s+");
                    //comment it out for testing 01/18/2012
                    
//select only AD feature-repeated on English
                    if ((Settings.getValue(Settings.LANGUAGE)).equals("english") ){
                                          
                        
 
                    if (ad_ngrams_.containsKey(term)) {
                          double[] ad_ngram = ad_ngrams_.get(term);
                     if (ad_ngram != null && ad_ngram[0] >= 3) {
                            str = DsarmdDATag.AD;
                             ngrams.clear();
                             ngrams.add(DsarmdDATag.AD);
                            break;
                        } 
                    }

                   }
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese") &&
                        tagGTCAT.equalsIgnoreCase("addressed-to"))
		{
                    if (ad_ngrams_.containsKey(term)) {
                         double[] ad_ngram = ad_ngrams_.get(term);
                       ad_ngram = ad_ngrams_.get(term);
                        if (ad_ngram != null && ad_ngram[0] >= 3) {
                            str = DsarmdDATag.AD;
                            break;
                        } //only keep AD feature 01/11/2012 by TL
                    }
                }
                }
//                term = ngrams.get(j);

               //CSLin added--check the Dis-Rej conditions
                int bDisRej=0;
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese") &&
                        tagGTCAT.equalsIgnoreCase("response-to")){
                    
                   bDisRej=pairComparison(utts, utterance);

                   if (bDisRej==1){
                     ngrams.clear();
                     ngrams.add(DsarmdDATag.DR);
                   }
                   else if (bDisRej==2){
                     for(int k=0; k<ngrams.size(); k++){
                       if (ngrams.get(k).equalsIgnoreCase(DsarmdDATag.DR))
                           ngrams.set(k, "")  ;
   
                     }
                   }
                }
                else if ((Settings.getValue(Settings.LANGUAGE)).equals("english") &&
                        !tagGTCAT.equalsIgnoreCase("addressed-to")){
                //else if ((Settings.getValue(Settings.LANGUAGE)).equals("english") &&
                //        tagGTCAT.equalsIgnoreCase("response-to")){

                   bDisRej=pairComparisonEng(utts, utterance);

                   if (bDisRej==1){
//                     ngrams.clear();
                     ngrams.add(DsarmdDATag.DR);
                   }
                   else if (bDisRej==2){
                     for(int k=0; k<ngrams.size(); k++){
                       if (ngrams.get(k).equalsIgnoreCase(DsarmdDATag.DR))
                           ngrams.set(k, "")  ;
                       
                     }
                   }
                }
                
                //check for the highest score of DR/AD in an utterance
       
                for(int j = 0; j < ngrams.size(); j++){
                String term = ngrams.get(j);
                utnumber=utterance.getTurn();
                
                if ((Settings.getValue(Settings.LANGUAGE)).equals("english") ){
                       if( (dr_ngrams_.containsKey(term)) | term.equals("disagree-reject")){
//                             System.out.println(utterance.getTurn()+"ngrams:"+term);
                            double[] dr_ngram = dr_ngrams_.get(term);
                            if(dr_ngram!=null ){
//                                System.out.println(utnumber);
                               
                                int one=term_frequency_.get(term);
//                                System.out.println("tf==="+one);
                            
                                dr_ngram = dr_ngrams_.get(term);
//                            System.out.println(utterance.getTurn()+":term:"+term+":In LoopDR:"+dr_ngram[0]);
//                            tmp.add(ngrams);
                            if(tno.contains(utnumber)){
                                adrterms.add(term);
                                drAndad.add(dr_ngram[0]);
                                ad_dr.put(term,dr_ngram[0]);
                            
                            }
                            else if(!tno.contains(utnumber)){
                                tno.add(utnumber);
                                
                                adrterms.clear();
                                adrterms.add(term);
                                
                                ad_dr.clear();
                                ad_dr.put(term,dr_ngram[0]);
                                
                                drAndad.clear();
                                drAndad.add(dr_ngram[0]);
                            }
                            
                            }

                        }
                        else if( ad_ngrams_.containsKey(term) |term.equals("action-directive")){
//                             System.out.println(utterance.getTurn()+"ngrams"+term);
                             double[] ad_ngram = ad_ngrams_.get(term);
                            if(ad_ngram!=null ){
                                int one=term_frequency_.get(term);
//                                System.out.println("tf==="+one);
                                adrterms.add(term);
//                                System.out.println(utnumber);
//                                System.out.println(utterance.getTurn()+":term:"+term+":In LoopAD:"+ad_ngram[0]);
                                if(tno.contains(utnumber)){
                                     adrterms.add(term);
                                     ad_dr.put(term,ad_ngram[0]);
                                     drAndad.add(ad_ngram[0]);
                                }
                                else if(!tno.contains(utnumber)){
                                    tno.add(utnumber);

                                    adrterms.clear();
                                    adrterms.add(term);

                                    ad_dr.clear();
                                    ad_dr.put(term,ad_ngram[0]);

                                    drAndad.clear();
                                    drAndad.add(ad_ngram[0]);
                                    
                            }
                            }
                        }
                }
               
                        
                
                }
 //check if it has disagree-reject/action-directive/any other DR term
                boolean dr=false;
                Comparator c=Collections.reverseOrder();
                        Collections.sort(drAndad,c);
//                        for(Integer s:drAndad)
//                        System.out.println("drAndad="+s);
                        if(!ad_dr.isEmpty())
                        for(String s:ad_dr.keySet()){
                        double tmpadr=0.0;
                        tmpadr=ad_dr.get(s);

                        if(drAndad.get(0)==tmpadr){
//                            System.out.println(utnumber+"\tHighest valueandterm=::"+s+tmpadr+"\n");
                        String tmp=s;                        
                        
                        if(tmp.trim().equals("disagree-reject")){
                        str=DsarmdDATag.DR;
                        dr=true;
                        break;
                        }
                        else if(dr!=true && tmp.trim().equals("action-directive")){
                        str=DsarmdDATag.AD;
                        break;
                        }
                        
                        else
                            str=DsarmdDATag.DR;
                        }
                        }
                        
             /*           
                for(String ss:values){
                if( ss.contains("disagree-reject") ){
                    str=DsarmdDATag.DR;
                  dr=true;
                            
                }
                }
                           
                
                for(String ss:values){ 
                
                    if(dr!=true && ss.contains("action-directive"))
                        str=DsarmdDATag.AD;
                else
                            str=DsarmdDATag.DR;
                }
                */        
             
                for(int j = 0; j < ngrams.size(); j++){
                    
                    
                    String term = ngrams.get(j);
                       String[] wordsInTerm = term.split("\\s+");
                    if (!term.equals(DsarmdDATag.AD)
                            && !term.equals(DsarmdDATag.DR)
                            && wordsInTerm.length < 2) {
                        continue;
                    }

                    if (allFeatures.contains(term)) {

//                        if (tag.equalsIgnoreCase("action-directive"))
//                             testoutAD+="|" + term+","+strSep;
                        //if (tag.equalsIgnoreCase("disagree-reject"))
                        //     testoutDR+="|" + term+","+strSep;

                        // if term is included in ADMSet, replace it by "ADM" (term.equal(ADMSet[i]))

                        //CSLin-- D-R-specific testing


                      //Dis-Rej conditions
                      if((Settings.getValue(Settings.LANGUAGE)).equals("chinese")){

                        if (bDisRej==1)
                        {
                            ;
                        }
                        else if (iUttLen<5 && tagGTCAT.equalsIgnoreCase("response-to") && featuresOfTheTag.contains(term))
                        {
                            continue;
                        }
                        else if ( !tagGTCAT.equalsIgnoreCase("response-to") && featuresOfTheTag.contains(term))
                        {
                            continue;
                        }
                        
                      }
                      else if((Settings.getValue(Settings.LANGUAGE)).equals("english")){

                          if ( tagGTCAT.equalsIgnoreCase("addressed-to") && featuresOfTheTag.contains(term))
                          {
                            continue;
                          }

                      }

                        String admOriginal="";
                        if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese") && !ADMSet.equals("")){
                          String[] ADMgram = ADMSet.split("\\|");
                          for (int k=0; k<ADMgram.length; k++)
                          {
                              if (term.equalsIgnoreCase(ADMgram[k]))
                              {
                                  admOriginal=term;
                                  term="ADM";
                              }
                          }
                        }

                       /*String drmOriginal="";
                        if (!DRMSet.equals("")){
                         String[] DRMgram = DRMSet.split("\\|");
                         for (int k=0; k<DRMgram.length; k++)
                         {
                             if (term.equalsIgnoreCase(DRMgram[k]))
                             {
                                 term="DRM";
                             }
                         }
                       }*/


                        //for testing output
                        if (term.equals("ADM")){
                            testoutAD+="|"+ term+"("+admOriginal+")";
                        }
                        else{
                            testoutAD+="|"+ term;
                        }
                        //end


                        str += "|" + term;
//                        System.out.println("str:"+str);
                    }
                }
                if(nonBlank == true){
                    if(!str.equals("")){
                        // by Laura Nov 30, 2010
                       /*if ( str.contains(DsarmdDATag.DR) && commActType.contains("response-to")){
                          str=str.replace("|"+DsarmdDATag.DR, "");
                          bw.write(tag + ", '" + str + "|" + "DRRT" + "'\n");
                       }else*/
                        bw.write(tag + ", '" + str + "|" + commActType + "'\n");
                    }
//                        bw.write(tag + ", '" + str + "'\n");
                        testoutAD+=  "|" +commActType +"\n";
                }
                else{
                    // by Laura Nov 30, 2010
                    /*if ( str.contains(DsarmdDATag.DR) && commActType.contains("response-to")){
                          str=str.replace("|"+DsarmdDATag.DR, "");
                          bw.write(tag + ", '" + str + "|" + "DRRT" + "'\n");
                       }else*/
                    bw.write(tag + ", '" + str + "|" + commActType + "'\n");

                    testoutAD+=  "|" +commActType +"\n";
//                    bw.write(tag + ", '" + str + "'\n");
                }
            }
             bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

         
    }

    /**
     * Set up ngram's term frequency with the training set data
     *
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingFrequency(ArrayList tr_utts) {
        for (int i = 0; i < tr_utts.size(); i++) {
            Utterance utterance = (Utterance) tr_utts.get(i); // Utterance turn node
            String tag = utterance.getTag(); // utterance dialog_act
            String daTag = "";
            daTag = TaggingType.getTag(utterance, tagType);
            String utt = utterance.getContent(); // utterance content



            if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")) {
                utt = utterance.getSpaceTagContent();
            }
            utt = Ngram.urlNormalize(utt);
            utt = Ngram.filterUtterance(utt);
            ArrayList<String> ngrams = Ngram.generateNgramList(utt);

            if (daTag.toLowerCase().contains(DsarmdDATag.AD)) { // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
            }
            // by Laura Jan 25, 2011
            if (daTag.toLowerCase().contains(DsarmdDATag.DR)) { // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                ngrams.add(DsarmdDATag.DR);
            }

            for (int j = 0; j < ngrams.size(); j++) {
                if (term_frequency_.containsKey(ngrams.get(j))) {
                    int freq = term_frequency_.get(ngrams.get(j));
                    term_frequency_.put(ngrams.get(j), freq + 1);
                } else {
                    term_frequency_.put(ngrams.get(j), Integer.valueOf(1));
                }
            }

        }
        //System.out.println("Size of the term_frequency_ = " + term_frequency_.size());
    }

    /**
     * Set up ngram's tag frequency with the training set data
     *
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingTagFrequency(ArrayList tr_utts) {
        TagNumber tn = new TagNumber();
        int da_tn = -1;
        int dr_tn = -1;
        for (int i = 0; i < tr_utts.size(); i++) {
            Utterance utterance = (Utterance) tr_utts.get(i); // Utterance turn node
            String tag = utterance.getTag(); // utterance dialog_act tag
            String daTag = "";
            daTag = TaggingType.getTag(utterance, tagType);

            if (Settings.getValue("tagNum").equals("3")) {
                tag = utterance.getMTag();
            }
            int tag_num = tn.tagNumberDialogAct(tag.toLowerCase(), Integer.parseInt(Settings.getValue("tagNum")));
            String utt = utterance.getContent(); // utterance content

            if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")) {
                utt = utterance.getSpaceTagContent();
            }
            utt = Ngram.urlNormalize(utt);
            utt = Ngram.filterUtterance(utt);
            ArrayList<String> ngrams = Ngram.generateNgramList(utt);


            if (daTag.toLowerCase().contains(DsarmdDATag.AD)) { // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                ngrams.add(DsarmdDATag.AD);
                if (da_tn == -1) {
                    da_tn = tag_num;
                }
                //System.out.println("action-directive: " + Arrays.toString(ngrams.toArray()));
            }
            // by Laura Jan 25, 2011
            if (daTag.toLowerCase().contains(DsarmdDATag.DR)) { // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                ngrams.add(DsarmdDATag.DR);
                if (dr_tn == -1) {
                    dr_tn = tag_num;
                }
            }


            for (int j = 0; j < ngrams.size(); j++) {
                if (tag_frequency_.containsKey(ngrams.get(j))) {
                    int[] tag_f_array = tag_frequency_.get(ngrams.get(j));
                    tag_f_array[tag_num - 1] += 1;
                    tag_frequency_.put(ngrams.get(j), tag_f_array);
                } else {
                    int tagNum = Integer.parseInt(Settings.getValue("tagNum"));
                    int[] tag_f_array = new int[tagNum + 1];
                    tag_f_array[tag_num - 1] += 1;
                    tag_frequency_.put(ngrams.get(j), tag_f_array);
                }
            }
        }
        //System.out.println("Size of the tag_frequency_ = " + tag_frequency_.size());
//        System.out.println("AD ngrams:");
        ArrayList<String> ngrams = new ArrayList(Arrays.asList(tag_frequency_.keySet().toArray()));
        HashMap<String, double[]> ad_ngrams = new HashMap<String, double[]>();
        for (String ngram : ngrams) {
            int[] ngram_freq = tag_frequency_.get(ngram);
            if (ngram_freq[da_tn - 1] > 0) {
                int i = 0;
                double ad_freq = 0;
                int total_freq = 0;
                for (int freq : ngram_freq) {
                    if (i == da_tn - 1) {
                        //System.out.print("*");
                        ad_freq = freq;
                    }
//                    System.out.print(freq + " ");
                    total_freq += freq;
                    i++;
                }
                if (ad_freq / total_freq >= 0.6
                        && ad_freq > 1) {
//                    System.out.print(ngram + ": " + ad_freq + " ");
//                    System.out.println("---frequency: " + ad_freq / total_freq);
                    double[] ngram_info = new double[2];
                    ngram_info[0] = ad_freq;
                    ngram_info[1] = ad_freq / total_freq;
                    if (!ad_ngrams.containsKey(ngram)) {
                        ad_ngrams.put(ngram, ngram_info);
                    }
                }
            }
        }
        Util.writeToFile("ad_ngrams", ad_ngrams);
        ad_ngrams_ = ad_ngrams;
//        System.out.println("DR ngrams:");
        ngrams = new ArrayList(Arrays.asList(tag_frequency_.keySet().toArray()));
        HashMap<String, double[]> dr_ngrams = new HashMap<String, double[]>();
        for (String ngram : ngrams) {
            int[] ngram_freq = tag_frequency_.get(ngram);
            if (ngram_freq[dr_tn - 1] > 0) {
                int i = 0;
                double dr_freq = 0;
                int total_freq = 0;
                for (int freq : ngram_freq) {
                    if (i == dr_tn - 1) {
                        //System.out.print("*");
                        dr_freq = freq;
                    }
//                    System.out.print(freq + " ");
                    total_freq += freq;
                    i++;
                }
                if (dr_freq / total_freq >= 0.6
                        && dr_freq > 1) {
//                    System.out.print(ngram + ": " + dr_freq + " ");
//                    System.out.println("---frequency: " + dr_freq / total_freq);
                    double[] ngram_info = new double[2];
                    ngram_info[0] = dr_freq;
                    ngram_info[1] = dr_freq / total_freq;
                    if (!dr_ngrams.containsKey(ngram)) {
                        dr_ngrams.put(ngram, ngram_info);
                    }
                }
            }
        }
        Util.writeToFile("dr_ngrams", dr_ngrams);
        dr_ngrams_ = dr_ngrams;
    }

    private String ADMSet="";
    private String DRMSet="";
    private String AllGramsSet="";
    private ArrayList WikiFeature=new ArrayList();
            
}