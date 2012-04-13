
package edu.albany.ils.dsarmd0200.cuetag.weka.ds_featuresHash;

// author Laura G.H. Jiao

import java.util.ArrayList;
import java.util.HashMap;

public class TagRulesPredefined {

    protected HashMap<String, ArrayList<String>> tagFeaturesPredefined =
            new HashMap<String, ArrayList<String>>();
    protected HashMap<String, HashMap<String, Double>> tagFeaturesPredefinedWithScore =
            new HashMap<String, HashMap<String, Double>>();
    protected HashMap<String, ArrayList<String>> tagFeaturesPredefinedWiki =
            new HashMap<String, ArrayList<String>>();
    private  ArrayList<String> Wiki_AD_Features = new ArrayList<String>();
    private String Wiki_AD="";
    private ArrayList<String> filters=new ArrayList<String>();
    public TagRulesPredefined(){        
    }

    public void allGramsAndDRMSet(String AllGrams, String DRMSet){
        
    }
    
    public void setfilter(String f){
        if(!f.equals(""))
     filters.add(f);
    }
   
    public void setTag(String tag){
     
    }
    
    public void setApproved(boolean bApproved){
        ;
    }
    public boolean getApproved(){
        return false;
    }


    public String rules_filtered(String str){
        init();
        
        String str2 = "<start> " + str.toLowerCase().trim() + " <finish>";
        ArrayList<String> AD_Feature = tagFeaturesPredefined.get(DsarmdDATag.AD);
        ArrayList<String> Wiki_AD_Feature = new ArrayList<String>() ;
        ArrayList<String> DR_Feature = tagFeaturesPredefined.get(DsarmdDATag.DR);
        if(!tagFeaturesPredefinedWiki.isEmpty() )
        Wiki_AD_Feature= tagFeaturesPredefinedWiki.get(Wiki_AD);

        if(!Wiki_AD_Feature.isEmpty() ){
        for(String s : Wiki_AD_Feature){
            if(!s.equals("")|| s!=null)
            if(str2.contains(s) ){
                str = str.replace(s, Wiki_AD);
            }
            continue;
        }
        }
        if(!filters.isEmpty()){
         for(String se:filters){
            if(str2.contains(se) && !se.isEmpty()){

                str2=str2.replace(se,"");
//                System.out.println("STR2:"+str2);
            }
         }
        }
        for(String s : AD_Feature){
           if(str2.contains(s)){
//                 System.out.println("STR3:"+str2);
                str = str.replace(s, DsarmdDATag.AD);
               
            }
            continue;
        }
        for(String s : DR_Feature){
            if(str2.contains(s)){
                str = str.replace(s, DsarmdDATag.DR);
            }
        }

        return str;
        
    }
  
    //new rules for wiki added to AD-3/13/12  
    public void addmoreADfeature(String req){
        
      if(!req.equals("") | (req!=null) ){
         Wiki_AD="wiki_ad";
//         System.out.println("AddWiki!="+req);
         if(!Wiki_AD_Features.contains(req))
     Wiki_AD_Features.add(req);
                  if(!(Wiki_AD_Features.isEmpty()))
     tagFeaturesPredefinedWiki.put(Wiki_AD, Wiki_AD_Features);
     }
     return;
    }
  
    protected void init(){
        /* Action-Directive pre-defined features */
        ArrayList<String> AD_Features = new ArrayList<String>();
        /* Disagree-Reject pre-defined features */
        ArrayList<String> DR_Features = new ArrayList<String>();

        AD_Features.add("hyperlink");
        AD_Features.add("hyperlinks");

        AD_Features.add("<start> hold on");
        AD_Features.add("<start> go");
        AD_Features.add("<start> i think we should");
        AD_Features.add("<start> why don\'t");
        AD_Features.add("<start> then pick");
        AD_Features.add("<start> and figure out");
        AD_Features.add("<start> look");
        AD_Features.add("<start> you may");
        AD_Features.add("<start> please");
        AD_Features.add("<start> compare");
        AD_Features.add("<start> remove");
        AD_Features.add("<start> we need to");
        AD_Features.add("<start> we can");

        AD_Features.add("please");
        AD_Features.add("you may");
        AD_Features.add("you could");
        AD_Features.add("google it");
        AD_Features.add("i request you to");
        AD_Features.add("we could");
        AD_Features.add("we should");
        AD_Features.add("we have to");
        AD_Features.add("we need to");
        AD_Features.add("let\'s");
        AD_Features.add("lets");
        AD_Features.add("let");
        AD_Features.add("please remember to");
        AD_Features.add("i think you can");
        AD_Features.add("you guys can");
        AD_Features.add("you guys may");
        AD_Features.add("everyone should");
        AD_Features.add("maybe we should");
        AD_Features.add("remind you to");
        AD_Features.add("remind everyone to");
        AD_Features.add("don\'t pick on");
        AD_Features.add("so decide now");
        
        


        DR_Features.add("though <finish>");
        DR_Features.add("tho <finish>");
        DR_Features.add("<start> but");
        DR_Features.add("<start> yes but");
        DR_Features.add("<start> yeah but");
        DR_Features.add("<start> nah");
        DR_Features.add("<start> no");
        DR_Features.add("<start> not really");
        DR_Features.add("<start> i\'m not");
        DR_Features.add("<start> that\'s not");
        DR_Features.add("<start> but they");
        DR_Features.add("<start> but you");
        DR_Features.add("<start> but we");
        DR_Features.add("<start> but i");
        DR_Features.add("<start> but he");
        DR_Features.add("<start> but she");
        DR_Features.add("<start> but it");
        DR_Features.add("<start> wait");
        DR_Features.add("i disagree");
        DR_Features.add("<start> i disagree");
        DR_Features.add("not really");
        DR_Features.add("but i think");
        DR_Features.add("i don\'t");
        DR_Features.add("but");
        DR_Features.add("but if");
        DR_Features.add("i disagree");
        


        tagFeaturesPredefined.put(DsarmdDATag.AD, AD_Features);
        tagFeaturesPredefined.put(DsarmdDATag.DR, DR_Features);

    }

}
