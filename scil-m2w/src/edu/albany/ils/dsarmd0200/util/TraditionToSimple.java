/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.albany.ils.dsarmd0200.util;
import com.rupeng.jero.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author ils-Stanislaus
 */
public class TraditionToSimple {
    ArrayList list=new ArrayList(); 
    String value=null;
    /**
     * @param args the command line arguments
     */
    /*private static String outPutFileName = "/home/ils/result.txt";
    private static BufferedWriter bw;
    private static String inPutFileName="/home/ils/test.txt";
    private static BufferedReader br;*/
    
    public static String Convert(ConvertInto con,ArrayList list){
        String word=con.getString(list);
        //System.out.println(word);
        return word;
        
    }
    public String Big5ToGb (String utterence){
        
           ConvertInto con=new ConvertIntoFDao();
           list=(ArrayList)con.getIndex(utterence);
           this.value=Convert(new ConvertIntoJDao(),list);
           //Convert(new ConvertIntoHDao(), list);
           
           return value;
    }
    public static void main(String[] args) {
          //new Test().Big5ToGb("葉劉淑儀d#$%,87");
    }
    
}
