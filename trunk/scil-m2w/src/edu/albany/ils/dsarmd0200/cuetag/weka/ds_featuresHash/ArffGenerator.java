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
    private HashMap<String, Integer> term_frequency_ = new HashMap<String, Integer>(); // key: ngram term in training set; value: frequency
    private HashMap<String, int[]> tag_frequency_ = new HashMap<String, int[]>(); // key: ngram term in training set; value: [tag1_frequency][tag2_frequency]...

    static int term_frequency_threshold_ = 3;
    static double fraction_threshold_ = 0.325;


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
            //trp = new TagRulesPredefinedChinese();
            trp = new TagRulesPredefined();
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
                content = Ngram.filterUtterance(content);

//end of
                ArrayList<String> ngrams = Ngram.generateNgramList(content);

                // by Laura Jan 04, 2011
                if(daTag.toLowerCase().contains(DsarmdDATag.AD)){ // Action-Directive System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.AD);
//                    System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                }
                // by Laura Jan 25, 2011
                if(daTag.toLowerCase().contains(DsarmdDATag.DR)){ // Disagree-Reject System.err.println("action-directive: " + Arrays.toString(ngrams.toArray()));
                    ngrams.add(DsarmdDATag.DR);
                }


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
                             fraction >= fraction_threshold_){

                         if (daTag.equalsIgnoreCase("action-directive") &&
                             fraction >= 0.6 &&
                             (Settings.getValue(Settings.LANGUAGE)).equals("chinese") ){
                             str += "|" + "ADM";
                             ADMSet += "|" + term;
                             //outAD+="|" + term+","+tag_freq+"; "+total_freq +strSep;
                             //adString+="|"+term;
                             continue;
                         }

                         //if (daTag.equalsIgnoreCase("disagree-reject"))
                         //    outDR+="|" + term+","+tag_freq+"; "+total_freq +strSep;

                         str += "|" + term;

                      }




                    }
                }
                // end of Lin


                if(!str.equals("")){
                // by Laura Nov 30, 2010
                    bw.write(daTag + ", '" + str + "|" + commActType + "'\n");
//                    bw.write(tag + ", '" + str + "'\n");
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Lin added

            //remove this when testing
           /*try{

               String strSet="/home/cslin/dsarmd0200/ADMSet.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(adString);

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

    private void appendToTestingArff(String arffFileLocation, ArrayList utts, Collection allFeatures, Boolean nonBlank){
         String testoutAD="";
         String testoutDR="";
         String strSep=System.getProperty("line.separator");

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
                if ((Settings.getValue(Settings.LANGUAGE)).equals("chinese"))
		    {content=utterance.getSpaceTagContent();}
                content = Ngram.urlNormalize(content);
                content = Ngram.filterUtterance(content);
                content = trp.rules_filtered(content);

//end of
                ArrayList<String> ngrams = Ngram.generateNgramList(content);
                for(int j = 0; j < ngrams.size(); j++){
                    String term = ngrams.get(j);
                    String[] wordsInTerm = term.split("\\s+");
                    // by Laura Jan 04, 2011
                    if(!term.equals(DsarmdDATag.AD) &&
		       !term.equals(DsarmdDATag.DR) &&
		       wordsInTerm.length < 2)
                        continue;
                    if(allFeatures.contains(term)){

                        //if (tag.equalsIgnoreCase("action-directive"))
                        //     testoutAD+="|" + term+","+strSep;

                        //if (tag.equalsIgnoreCase("disagree-reject"))
                        //     testoutDR+="|" + term+","+strSep;

                        // if term is included in ADMSet, replace it by "ADM" (term.equal(ADMSet[i]))
                       if (!ADMSet.equals("")){
                         String[] ADMgram = ADMSet.split("\\|");
                         for (int k=0; k<ADMgram.length; k++)
                         {
                             if (term.equalsIgnoreCase(ADMgram[k]))
                             {
                                 term="ADM";
                             }
                         }
                       }

                        str += "|" + term;
                    }
                }
                if(nonBlank == true){
                    if(!str.equals(""))
                        // by Laura Nov 30, 2010
                        bw.write(tag + ", '" + str + "|" + commActType + "'\n");
//                        bw.write(tag + ", '" + str + "'\n");
                }
                else{
                    // by Laura Nov 30, 2010
                    bw.write(tag + ", '" + str + "|" + commActType + "'\n");
//                    bw.write(tag + ", '" + str + "'\n");
                }
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

         /*try{

               String strSet="/home/cslin/dsarmd0200/tetsoutAD.txt";
               BufferedWriter bufferedWriterSet = null;
               bufferedWriterSet = new BufferedWriter(new FileWriter(strSet));

               bufferedWriterSet.write(testoutAD);

               bufferedWriterSet.flush();
               bufferedWriterSet.close();
           }
           catch (Exception ioe )
	   {
	   	ioe.printStackTrace ();
	   }

           try{

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

}
