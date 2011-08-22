/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

import edu.albany.ils.dsarmd0200.evaltag.Utterance;

/**
 *
 * @author gjiao
 */
public class TaggingType {

    public static String getTag(Utterance utterance, String tagType){
        String tag = "";
        
        if(tagType.equals("da15")){
            tag = utterance.getTag().toLowerCase().trim();
            tag = tag.replace("--", "");
        }
        else if(tagType.equals("da3")){
            tag = utterance.getMTag().toLowerCase().trim();
        }
        else if(tagType.equals("polarity")){
            tag = utterance.getPolarity().toLowerCase().trim();
            if(tag.equals("")){
                tag = "neutral";
            }
        }
        else{
            System.err.println("unrecognized tag type");
            return null;
        }

        if(tag == null || tag.equals("")){
                tag = "unknown";
        }
        
        return tag;
    }

    public static int getTagNum(String tagType){

        if(tagType.equals("da15")){
            return 15;
        }
        else if(tagType.equals("da3")){
            return 3;
        }
        else if(tagType.equals("polarity")){
            return 3;
        }
        else{
            System.out.println("unrecognized tag type");
            return 0;
        }
    }


}
