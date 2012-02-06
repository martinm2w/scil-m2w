/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.util;

/**
 * This class is to create a emotive words list from the emotive word file
 * @author peng
 */
import java.util.*;
import java.io.*;

public class EmotiveWords {
    public EmotiveWords(String ew) {
	readStopWords(new File(ew));
    }
    
    public void readStopWords(File file){
        if( !file.exists() ) {
	    System.out.println("the emotive word file cannot be found!");
	    return ;
        }
        try{
	    String line="";        
	    int fl = (int)file.length();
	    char[] f_chs = new char [fl];
            BufferedReader in = new BufferedReader ( new FileReader (file));
	    in.read(f_chs);
	    String f_str = new String(f_chs);
	    in = new BufferedReader(new StringReader(f_str));
            while ((line = in.readLine()) != null) {
               // line.toLowerCase();
		emotive_words.add(line.trim().toLowerCase());
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void filterIt(ArrayList words) {
	for (int i = 0; i < words.size(); i++) {
	    String word = ((String)words.get(i)).toLowerCase();
	    if (emotive_words.contains(word)) {
		words.remove(i);
		i--;
	    }
	}
    }
    
    public boolean isEmotiveWord(String word) {
	word.toLowerCase();
        return emotive_words.contains(word);
    }
    

    private ArrayList<String> emotive_words = new ArrayList<String>();
}
