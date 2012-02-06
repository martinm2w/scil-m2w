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
    static double fraction_threshold_ = 0.4   ;//0.325;


//    private static PNWords pnw = new PNWords();
    private TagRulesPredefined trp;

    public ArffGenerator(String tr_fileName, String tr_fileLocation, String te_fileName, String te_fileLocation,
            ArrayList tr_utts, ArrayList utts, ArrayList tags, HashMap featuresMap, ArrayList allFeatures){
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
        if(Settings.getValue("language").equals("chinese")){
            System.out.println("Use Chinese Ngram rules ...");
            trp = new TagRulesPredefinedChinese();
            //trp = new TagRulesPredefined();
        }
        else{
            trp = new TagRulesPredefined();
        }
        term_frequency_.clear();
        tag_frequency_.clear();

    }

    public void writeTrainingArff(){
        calculateTrainingFrequency(tr_utts);
        calculateTrainingTagFrequency(tr_utts);
        writeArffHeading(arffTrainingFileLocation, arffTrainingFileName, tags);
        appendToTrainingArff(arffTrainingFileLocation, tr_utts, featuresMap);
    }

    public void writeTestingArff(Boolean nonBlank){
        writeArffHeading(arffTestingFileLocation, arffTestingFileName, tags);
        appendToTestingArff(arffTestingFileLocation, utts, allFeatures, nonBlank);
    }


    /**
     * Prepare arff heading
     * @param tags Arff heading: classes
     */
    private void writeArffHeading(String arffFileLocation, String arffFileName, ArrayList tags){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation));
            /* write the arff heading */
            bw.write("@RELATION " + arffFileName + "\n");
            bw.write("@ATTRIBUTE tag { ");
	    String unknown = "unknown";
	    boolean has_ukn = false;
            for(int i = 0; i < tags.size(); i++){
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


    private void appendToTrainingArff(String arffFileLocation, ArrayList tr_utts, HashMap featuresMap){
        BufferedWriter bw = null;

         String adString="";

         String outAD="";
         String outDR="";
         String strSep=System.getProperty("line.separator");
         HashMap<String, Integer> preDR = new HashMap<String, Integer>();
         HashMap<String, Integer> postDR = new HashMap<String, Integer>();
         HashMap<String, Integer> preNonDR = new HashMap<String, Integer>();
         HashMap<String, Integer> postNonDR = new HashMap<String, Integer>();
         HashMap<String, Integer> triDR = new HashMap<String, Integer>();
         HashMap<String, Integer> triNonDR = new HashMap<String, Integer>();

        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation, true));

            for(int i = 0; i < tr_utts.size(); i++){ // tr_utts
                Utterance utterance = (Utterance)tr_utts.get(i);
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
                ArrayList featuresOfTheTag = (ArrayList)featuresMap.get(daTag);


                // by Laura Nov 30, 2010
                String commActType = utterance.getCommActType().toLowerCase();

                String content = utterance.getContent();

                // by Laura Nov 30, 2010
//LinCommented                content = Ngram.urlNormalize(content);
//LinCommented                content = Ngram.filterUtterance(content);

//                // by Laura Dec 07, 2010
//                content = pnw.replaceSentence(content);

//Lin Added
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese"))
		{content=utterance.getSpaceTagContent();}
                content = Ngram.urlNormalize(content);

                //for computing (n-grams+POS) negative features in D-R
                /*String[] uttsSentence=null;
                String[] uttsPOS=null;
                String[] indPOS=null;
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")){
                    uttsSentence = content.split("[\\s]+");
                    uttsPOS = utterance.getTaggedContent().split("[\\s]+");
                    indPOS=new String[uttsPOS.length];

                    for (int kk=0; kk<uttsPOS.length; kk++){
                        String[] combine=uttsPOS[kk].split("/");
                        if (combine.length>1)
                          indPOS[kk]=combine[1];
                    }
                }*/
                //end

                content = Ngram.filterUtterance(content);

//end of
                ArrayList<String> ngrams = Ngram.generateNgramList(content);

                // by Laura Jan 04, 2011
                if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                }
                // by Laura Jan 25, 2011
                //Commented out by cslin
                if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.DR);
                }

                //Lin added--(n-grams+POS) negative features in D-R
            /*if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")){
                for (int kk=0; kk<uttsSentence.length; kk++){
                    if (uttsSentence[kk].contains("不")){
                        String strComb="";
                        int    iComb=0;
                        // it is a DR
                        if (daTag.toLowerCase().contains(DsarmdDATag.DR) &&
                            commActType.toLowerCase().contains("response-to")    ){
                           //pre bigram
                           if (kk-1>=0)
                               strComb= indPOS[kk-1]+" "+uttsSentence[kk];
                           else
                               strComb= "<start>"+" "+uttsSentence[kk];

                           if (preDR.containsKey(strComb)){
                               iComb= preDR.get(strComb)+1;
                               preDR.put(strComb, iComb);
                           }
                           else{
                               preDR.put(strComb, 1);
                           }
                           
                           //post bigram
                           
                           if (kk+1<uttsSentence.length)
                               strComb= uttsSentence[kk]+" "+indPOS[kk+1];
                           else
                               strComb= uttsSentence[kk]+" "+"<finish>";


                           if (postDR.containsKey(strComb)){
                               iComb= postDR.get(strComb)+1;
                               postDR.put(strComb, iComb);
                           }
                           else{
                               postDR.put(strComb, 1);
                           }
                           
                           //trigram
                           
                           String strFst="", strThird="";
                           if (kk-1>=0)
                              strFst=indPOS[kk-1];
                           else
                              strFst="<start>";

                           if (kk+1<uttsSentence.length)
                              strThird=uttsSentence[kk+1];
                           else
                              strThird="<finish>";


                           strComb= strFst+" "+uttsSentence[kk]+" "+strThird;
                           if (triDR.containsKey(strComb)){
                              iComb= triDR.get(strComb)+1;
                              triDR.put(strComb, iComb);
                           }
                           else{
                              triDR.put(strComb, 1);
                           }



                        }
                        //It is other DA
                        else if (commActType.toLowerCase().contains("response-to")){
                            //pre bigram
                              
                              if (kk-1>=0)
                                 strComb= indPOS[kk-1]+" "+uttsSentence[kk];
                              else
                                 strComb= "<start>"+" "+uttsSentence[kk];

                              if (preNonDR.containsKey(strComb)){
                                 iComb= preNonDR.get(strComb)+1;
                                 preNonDR.put(strComb, iComb);
                              }
                              else{
                                 preNonDR.put(strComb, 1);
                              }
                           
                           //post bigram
                              
                              if (kk+1<uttsSentence.length)
                                  strComb= uttsSentence[kk]+" "+indPOS[kk+1];
                              else
                                  strComb= uttsSentence[kk]+" "+"<finish>";

                              if (postNonDR.containsKey(strComb)){
                                 iComb= postNonDR.get(strComb)+1;
                                 postNonDR.put(strComb, iComb);
                              }
                              else{
                                 postNonDR.put(strComb, 1);
                              }
                           
                           //trigram
                              
                              String strFst="", strThird="";
                               if (kk-1>=0)
                                  strFst=indPOS[kk-1];
                               else
                                  strFst="<start>";

                               if (kk+1<uttsSentence.length)
                                  strThird=uttsSentence[kk+1];
                               else
                                  strThird="<finish>";


                               strComb= strFst+" "+uttsSentence[kk]+" "+strThird;
                              if (triNonDR.containsKey(strComb)){
                                 iComb= triNonDR.get(strComb)+1;
                                 triNonDR.put(strComb, iComb);
                              }
                              else{
                                 triNonDR.put(strComb, 1);
                              }
                           

                        }


                    }
                }

            }*/
                // end of add


                //Lin modified
                TagNumber tn = new TagNumber();
                String tag = utterance.getTag(); // dialog_act tag
                tag = tn.DialogAct(tag.toLowerCase(),
                        Integer.parseInt(Settings.getValue("tagNum"))).trim();
                int tagNum = tn.tagNumberDialogAct(tag.toLowerCase(), Integer.parseInt(Settings.getValue("tagNum")));

                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    int total_freq = (Integer)term_frequency_.get(term);
                    int[] freq = tag_frequency_.get(term);
                    int tag_freq = freq[tagNum-1];
                    double fraction = (double)tag_freq/(double)total_freq;
                    String[] wordsInTerm = term.split("\\s+");
                    // by Laura Jan 04, 2011
                    if(!term.equals(DsarmdDATag.AD) &&
                            !term.equals(DsarmdDATag.DR) &&
                            wordsInTerm.length < 2){
                        continue;
                    }
                    if(featuresOfTheTag != null && featuresOfTheTag.contains(term)){
                         // use else if to avoid repeatly include the "hastopic" and "notopic"

                      if(total_freq >= term_frequency_threshold_ &&
                             fraction >= fraction_threshold_)
                      {

                         if (daTag.equalsIgnoreCase("action-directive") &&
                             fraction >= 0.6 &&
                             (Settings.getValue(Settings.LANGUAGE)).equals("chinese") ){
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



         //Lin added--(n-grams+POS) negative features in D-R
         /*if (bNegativeFeatures){
           writeStat(preDR, "preDR");
           writeStat(postDR, "postDR");
           writeStat(preNonDR, "preNonDR");
           writeStat(postNonDR, "postNonDR");
           writeStat(triDR, "triDR");
           writeStat(triNonDR, "triNonDR");
         }*/
         //end

            //remove this when testing
           /*try{

               String strSet="/home/cslin/tmp/dsarmd0200/ADMSet.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(ADMSet);

               bufferedWriterSet.flush();
               bufferedWriterSet.close();
           }
           catch (Exception ioe )
	   {
	   	ioe.printStackTrace ();
	   }

           try{

               String strSet="/home/cslin/dsarmd0200/trainoutAD.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(outAD);

               bufferedWriterSet.flush();
               bufferedWriterSet.close();
           }
           catch (Exception ioe )
	   {
	   	ioe.printStackTrace ();
	   }

           try{

               String strSet="/home/cslin/dsarmd0200/trainoutDR.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(outDR);

               bufferedWriterSet.flush();
               bufferedWriterSet.close();
           }
           catch (Exception ioe )
	   {
	   	ioe.printStackTrace ();
	   }*/
           //end of Lin

    }

    
    private void writeStat(HashMap<String, Integer> hashMap, String filename){
        //Lin added-- Write (n-grams+POS) negative features in D-R
                try{

                       String strSet="/home/cslin/tmp/dsarmd0200/"+filename+".txt";
                       BufferedWriter bufferedWriterSet = null;
                       bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

                       hashMap=sortHashMapByValuesD(hashMap);
                       for (String grams : hashMap.keySet()){
                           bufferedWriterSet.write(grams + " " + hashMap.get(grams));
                           bufferedWriterSet.write("\n");
                       }
                       bufferedWriterSet.flush();
                       bufferedWriterSet.close();
                }
                catch (Exception ioe )
	        {
	   	       ioe.printStackTrace ();
	        }
    }
    private LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapKeys);

        LinkedHashMap sortedMap =
            new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Integer)val);
                    break;
                }

            }

        }
        return sortedMap;
    }


    private boolean chkNegativeFeatures(Utterance utt, String content){
        String[] preWords={"不 会/AD", "不 知道/<finish>", "不 会/VV", "不 用/VV",
        "不 太/VV", "不 重要/PU", "不 好/AS", "不错/<finish>", "不错/PU", "不/P", "不错/DEC"};
        for (int i=0; i<preWords.length; i++){
            //for preWords
            String term="", pos="";
            String[] preCombine=preWords[i].split("/");
            term=preCombine[0]; pos=preCombine[1];

            //for testing utterance
            String[] uttsPOS = utt.getTaggedContent().split("[\\s]+");
            String[] indPOS=new String[uttsPOS.length];

                //all pos in sentence
                for (int kk=0; kk<uttsPOS.length; kk++){
                    String[] combine=uttsPOS[kk].split("/");
                    if (combine.length>1)
                      indPOS[kk]=combine[1];
                    else
                      indPOS[kk]="";
                }

                int iStart=0, iEnd=0;
                if (content.contains(term)){
                    int iContStart=content.indexOf(term);
                    int iContEnd=iContStart+term.length();
                    
                    if (iContStart!=0){
                        String str=(content.substring(iContStart-1, iContStart));
                        if (!str.equals(" "))
                            continue;
                    }
                    if (iContEnd!=content.length()){
                        String str=(content.substring(iContEnd, iContEnd+1));
                        if (!str.equals(" "))
                            continue;
                    }

                    int iCont=content.indexOf(term);
                    for (int kk=0; kk<=iCont; kk++)
                    {
                        if (content.subSequence(kk, kk+1).equals(" "))
                            iStart++;
                    }
                    iEnd=iStart;
                    for (int kk=iCont; kk<=(iCont+term.length()); kk++)
                    {
                        if (content.subSequence(kk, kk+1).equals(" "))
                            iEnd++;
                    }

                    


                    
                    if (iEnd<uttsPOS.length-1){
                      String postPos=indPOS[iEnd];
                      if (pos.equalsIgnoreCase(postPos))
                         return true;
                    }

                    if (iEnd>=uttsPOS.length-1){
                      if (pos.contains("<finish>"))
                        return true;
                    }
                    
                    

                 
                }

                

        }

        String[] postWords={"VC/不 是", "NN/不 是", "<start>/不 会", "<start>/不 用",
        "<start>/不 知道", "PN/不 知道", "PU/不 知道", "AD/不错", "NR/不", "VA/不", "<start>/不错", "M/不错"};
        
        for (int i=0; i<postWords.length; i++){
            //for preWords
            String term="", pos="";
            String[] postCombine=postWords[i].split("/");
            term=postCombine[1]; pos=postCombine[0];

            //for testing utterance
            String[] uttsPOS = utt.getTaggedContent().split("[\\s]+");
            String[] indPOS=new String[uttsPOS.length];

                //all pos in sentence
                for (int kk=0; kk<uttsPOS.length; kk++){
                    String[] combine=uttsPOS[kk].split("/");
                    if (combine.length>1)
                      indPOS[kk]=combine[1];
                    else
                      indPOS[kk]="";
                }

                int iStart=0, ipreStart=0;
                if (content.contains(term)){
                    int iContStart=content.indexOf(term);
                    int iContEnd=iContStart+term.length();
                    
                    if (iContStart!=0){
                        String str=(content.substring(iContStart-1, iContStart));
                        if (!str.equals(" "))
                            continue;
                    }
                    if (iContEnd!=content.length()){
                        String str=(content.substring(iContEnd, iContEnd+1));
                        if (!str.equals(" "))
                            continue;
                    }

                    int iCont=content.indexOf(term);
                    for (int kk=0; kk<=iCont; kk++)
                    {
                        if (content.subSequence(kk, kk+1).equals(" "))
                            iStart++;
                    }
                    ipreStart=iStart-1;

                    



                    if (ipreStart>=0){
                      String prePos=indPOS[ipreStart];
                      if (pos.equalsIgnoreCase(prePos))
                         return true;
                    }

                    if (ipreStart<0){
                      if (pos.contains("<start>"))
                        return true;
                    }




                }



        }

        return false;
    }
    private boolean chkSpecialWords(String strUtterance, ArrayList featuresOfTheTag, String term){
                         if ( (strUtterance.contains("？") ||
                            strUtterance.contains("么") ||
                            strUtterance.contains("吗") ||
                            strUtterance.contains("可能") ||
                            strUtterance.contains("88") ||
                            strUtterance.contains("bye") ||
                            strUtterance.contains("不客气") ||
                            strUtterance.contains("嗎") ||

                            strUtterance.contains("会不会") ||

                            strUtterance.contains("差不多") ||
                            strUtterance.contains("是不是") ||
                            strUtterance.contains("不清楚") ||
                            strUtterance.contains("不知道") ||
                            strUtterance.contains("来不及")
                            ) &&
                            featuresOfTheTag.contains(term)
                            ){
                            return true;
                        }
                        return false;
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
                    if ( preContent[preContent.length-1].contains(questionMarks[i]) )
                    return 2;

                    /*if ( Content[Content.length-1].contains(questionMarks[i]) ||
                         (Content.length-2>=0 && Content[Content.length-2].contains(questionMarks[i]))
                       )*/
                    if ( Content[Content.length-1].contains(questionMarks[i]) )
                    return 2;
                }

                //check if the utterance is a agreed sentence
                for (int i=0; i<agreeWords.length; i++){
                     if (Content[0].contains(agreeWords[i]))
                         return 2;
                }

                
                //check the repeated concept
                for (int i=0; i<Content.length; i++){
                    for(int j=0; j<preContent.length; j++){

                       if (Content[i].equalsIgnoreCase(preContent[j])){
                          if (Content[i].length()>=2)
                            bRepeatedConcept=true;
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
                        if (iNeg==0)
                          return 2;
                     }
                     if (agreeWords[i].length()>1 && utterance.getContent().contains(agreeWords[i])){
                        if (iNegInPre==0)
                          return 2;
                     }
                }





                //check the begining word
                for (int i=0; i<startWords.length; i++){
                   if (Content[0].contains(startWords[i]))
                       return 1;
                }

                //Check all conditions of Dis-Rej
               if (Content.length>=3){
                if (bRepeatedConcept && iNeg>0)
                    return 1;

                if (iNegInPre>0 && iPos>0)
                    return 1;

                if (iPosInPre>0 && iNeg>0)
                    return 1;

                if (iNegInPre>0 && iNeg>1)
                    return 1;

                if (bRepeatedConcept && iNegInPre>0 && iNeg==0)
                    return 1;
               }
               
                

               

                break;
              }
            }
	}
        return 0;
    }
    private void appendToTestingArff(String arffFileLocation, ArrayList utts, Collection allFeatures, Boolean nonBlank){
         String testoutAD="";
         String testoutDR="";
         String tagGTCAT="";
         String strSep=System.getProperty("line.separator");

         ArrayList featuresOfTheTag = (ArrayList)featuresMap.get(DsarmdDATag.DR);
         BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(arffFileLocation, true));

            for(int i = 0; i < utts.size(); i++){ // utts
                Utterance utterance = (Utterance)utts.get(i);
                String tag = "";
                String str = "";
                tag = TaggingType.getTag(utterance, tagType);
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

                content = Ngram.urlNormalize(content);
                boolean negFeatures=false;
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese"))
                {     negFeatures=chkNegativeFeatures(utterance, content);}
                content = Ngram.filterUtterance(content);

                //trp.allGramsAndDRMSet(AllGramsSet, DRMSet);
                //content = "<start> " + content.toLowerCase().trim() + " <finish>";

                //CSLin added--retrieve the ground truth for Comm Act type
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese")){
                   tagGTCAT=utterance.getGroundTruthCommActType();
                   trp.setApproved(false);
                   trp.setTag(tagGTCAT);
                }

                /*if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese") &&
                        tagGTCAT.equalsIgnoreCase("response-to")){
                   
                   termMatching(utts, utterance, i+1);
                }*/
                
                //end

                content = trp.rules_filtered(content);
                
                ArrayList<String> ngrams = Ngram.generateNgramList(content);

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
                //end

                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    String[] wordsInTerm = term.split("\\s+");
                    // by Laura Jan 04, 2011
                    if(!term.equals(DsarmdDATag.AD) &&
		       !term.equals(DsarmdDATag.DR) &&
                       wordsInTerm.length < 2 )//&&
                       //!term.contains(DsarmdDATag.DR))
                        continue;

                    
                    if(allFeatures.contains(term)){// || term.contains(DsarmdDATag.DR)){

                        //if (tag.equalsIgnoreCase("action-directive"))
                        //     testoutAD+="|" + term+","+strSep;

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
                        //else if ( !chkSpecialWords(strUtterance,featuresOfTheTag,term) && trp.getApproved() && iUttLen>=3 && tagGTCAT.equalsIgnoreCase("response-to"))
                        //{
                        //    ;
                        //}
                        //else if (negFeatures && featuresOfTheTag.contains(term))
                        //    continue;
                        else if (iUttLen<5 && tagGTCAT.equalsIgnoreCase("response-to") && featuresOfTheTag.contains(term))
                        {
                            continue;
                        }
                        else if ( !tagGTCAT.equalsIgnoreCase("response-to") && featuresOfTheTag.contains(term))
                        {
                            continue;
                        }
                        //else if ( chkSpecialWords(strUtterance,featuresOfTheTag,term)) {
                        //    continue;
                        //}
                        //end
                      }

                        String admOriginal="";
                        if (!ADMSet.equals("")){
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
//                        bw.write(tag + ", '" + str + "'\n");
                        testoutAD+=  "|" +commActType +"\n";
                    }
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
        
         /*try{

               String strSet="/home/cslin/tmp/dsarmd0200/tetsoutAD.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(testoutAD);

               bufferedWriterSet.flush();
               bufferedWriterSet.close();
           }
           catch (Exception ioe )
	   {
	   	ioe.printStackTrace ();
	   }*/

           /*try{

               String strSet="/home/cslin/dsarmd0200/testoutDR.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(testoutDR);

               bufferedWriterSet.flush();
               bufferedWriterSet.close();
           }
           catch (Exception ioe )
	   {
	   	ioe.printStackTrace ();
	   }*/
           //end of Lin
    }



        /**
     * Set up ngram's term frequency with the training set data
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingFrequency(ArrayList tr_utts){
        for(int i = 0; i < tr_utts.size(); i++){
            Utterance utterance = (Utterance)tr_utts.get(i); // Utterance turn node
            String tag = utterance.getTag(); // utterance dialog_act
	    String daTag = "";
            daTag = TaggingType.getTag(utterance, tagType);
            String utt = utterance.getContent(); // utterance content


	    
	    if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese"))
		utt=utterance.getSpaceTagContent();
            utt = Ngram.urlNormalize(utt);
            utt = Ngram.filterUtterance(utt);
            ArrayList<String> ngrams = Ngram.generateNgramList(utt);

            if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
            }
                // by Laura Jan 25, 2011
            if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.DR);
            }

            for(int j = 0; j < ngrams.size(); j++){
                 if(term_frequency_.containsKey(ngrams.get(j))){
                     int freq = term_frequency_.get(ngrams.get(j));
                     term_frequency_.put(ngrams.get(j), freq+1);
                 }
                 else{
                     term_frequency_.put(ngrams.get(j), Integer.valueOf(1));
                 }
            }

        }
        //System.out.println("Size of the term_frequency_ = " + term_frequency_.size());
    }


    /**
     * Set up ngram's tag frequency with the training set data
     * @param tr_utts list of traininig Utterance turn nodes
     */
    private void calculateTrainingTagFrequency(ArrayList tr_utts){
        TagNumber tn = new TagNumber();

        for(int i = 0; i < tr_utts.size(); i++){
            Utterance utterance = (Utterance)tr_utts.get(i); // Utterance turn node
            String tag = utterance.getTag(); // utterance dialog_act tag
            String daTag = "";
            daTag = TaggingType.getTag(utterance, tagType);

	    if (Settings.getValue("tagNum").equals("3")) {
		tag = utterance.getMTag();
	    }
            int tag_num = tn.tagNumberDialogAct(tag.toLowerCase()
						, Integer.parseInt(Settings.getValue("tagNum")));
            String utt = utterance.getContent(); // utterance content

	    if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese"))
		utt=utterance.getSpaceTagContent();
            utt = Ngram.urlNormalize(utt);
            utt = Ngram.filterUtterance(utt);
            ArrayList<String> ngrams = Ngram.generateNgramList(utt);


            if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
            }
                // by Laura Jan 25, 2011
            if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.DR);
            }


            for(int j = 0; j < ngrams.size(); j++){
                if(tag_frequency_.containsKey(ngrams.get(j))){
                    int[] tag_f_array = tag_frequency_.get(ngrams.get(j));
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(ngrams.get(j), tag_f_array);
                }
                else{
                    int tagNum = Integer.parseInt(Settings.getValue("tagNum"));
                    int[] tag_f_array = new int[tagNum+1];
                    tag_f_array[tag_num-1] += 1;
                    tag_frequency_.put(ngrams.get(j), tag_f_array);
                }
            }
        }
        //System.out.println("Size of the tag_frequency_ = " + tag_frequency_.size());
    }

    private String ADMSet="";
    private String DRMSet="";
    private String AllGramsSet="";

}
