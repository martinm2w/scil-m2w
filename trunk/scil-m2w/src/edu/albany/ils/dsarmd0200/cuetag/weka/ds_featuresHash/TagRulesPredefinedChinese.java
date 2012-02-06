/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.lu.Settings;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gjiao
 */
public class TagRulesPredefinedChinese extends TagRulesPredefined {

    String chineseNgram = Settings.getValue("chineseNgram");
    String AllGrams;
    String DRMSet;
    public String DRUtt="";
    public String tag="";
    private boolean bApproved=false;
    private ArrayList<String> arrDR=new ArrayList();

    public TagRulesPredefinedChinese(){        
    }

    public void allGramsAndDRMSet(String AllGrams, String DRMSet){
          this.AllGrams=AllGrams;
          this.DRMSet=DRMSet;
    }

    public void setTag(String tag){
        this.tag=tag;
    }

    public void setApproved(boolean bApproved){
        this.bApproved=bApproved;
    }
    public boolean getApproved(){
        return this.bApproved;
    }

    
    @Override
    public String rules_filtered(String str){
        init();
        String str2 = "<start> " + str.toLowerCase().trim() + " <finish>";

        HashMap<String, Double> AA_Features = 
                tagFeaturesPredefinedWithScore.get(DsarmdDATag.AA);
        HashMap<String, Double> DR_Features =
                tagFeaturesPredefinedWithScore.get(DsarmdDATag.DR);
        HashMap<String, Double> AK_Features =
                tagFeaturesPredefinedWithScore.get(DsarmdDATag.AK);
        HashMap<String, Double> IR_Features =
                tagFeaturesPredefinedWithScore.get(DsarmdDATag.IR);
        HashMap<String, Double> CR_Features =
                tagFeaturesPredefinedWithScore.get(DsarmdDATag.CR);


        /*for(String s : AA_Features.keySet()){ // agree-accept
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.AA);
            }
        }
        for(String s : DR_Features.keySet()){ // disagree-reject
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.DR);
            }
        }
        for(String s : AK_Features.keySet()){ // acknowledge
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.AK);
            }
        }
        for(String s : IR_Features.keySet()){ // information-request
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.IR);
            }
        }
        for(String s : CR_Features.keySet()){ // confirmation-request
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.CR);
            }
        }*/
        
        
        /*str=str.toLowerCase().trim();
        String drmOriginal="";
        if (!AllGrams.equals("")){
              String[] gramArray = AllGrams.split("\\|");
              for (int k=0; k<gramArray.length; k++)
              {
                  if (str.equalsIgnoreCase(gramArray[k]))
                  {
                       return str;
                  }
              }
        }

        for(String s : DR_Features.keySet()){ // disagree-reject
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.DR);
            }
        }*/

        Iterator itr=arrDR.iterator();

        while(itr.hasNext()){
            String s=(String)itr.next();
            
            if(str2.contains(s)){// && tag.equalsIgnoreCase("response-to")){
                if (s.contains("<start> ")){
                    if (s.length()>=10)
                       setApproved(true);

                    s= s.replace("<start> ", "");
                   
                }

                str = str.replace(s, DsarmdDATag.DR);
                System.out.print("");
            }
        }

        
        return str;

    }

    @Override
    protected void init(){
        BufferedReader br;
        String str = "";
        try {
            br = new BufferedReader(new FileReader(chineseNgram));
            while((str = br.readLine()) != null){
                String[] array = str.split(":");
                String tag = array[1].trim();
                tag = toDsarmdDATag(tag);
                String ngram = array[2].trim();
                ArrayList<String> ngrams = new ArrayList<String>();
                if(ngram.contains("/")){
                    String[] ngramArray = ngram.split("/");
                    for(String gram : ngramArray){
                        ngrams.add(gram);
                    }
                }
                else{
                    ngrams.add(ngram);
                }
                String s = array[4].trim().replace("]", "");
                double score = Double.parseDouble(s);
                if(!tagFeaturesPredefinedWithScore.containsKey(tag)){
                    HashMap<String, Double> h = new HashMap<String, Double>();
                    for(String gram : ngrams){
                        h.put(gram, score);
                        if (tag.equalsIgnoreCase(DsarmdDATag.DR))
                             arrDR.add(gram);
                    }
                    tagFeaturesPredefinedWithScore.put(tag, h);
                }
                else{
                    HashMap<String, Double> h = tagFeaturesPredefinedWithScore.get(tag);
                    for(String gram : ngrams){
                        h.put(gram, score);
                        if (tag.equalsIgnoreCase(DsarmdDATag.DR))
                             arrDR.add(gram);
                    }
                }
            }
            br.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private String toDsarmdDATag(String tag){
        if(tag.toLowerCase().equals("aa"))
            tag = DsarmdDATag.AA;
        else if(tag.toLowerCase().equals("ar"))
            tag = DsarmdDATag.DR;
        else if(tag.toLowerCase().equals("qw"))
            tag = DsarmdDATag.IR;
        else if(tag.toLowerCase().equals("qy"))
            tag = DsarmdDATag.CR;
        else if(tag.toLowerCase().equals("ba"))
            tag = DsarmdDATag.AK;
        return tag;
    }

}
