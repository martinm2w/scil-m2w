package edu.albany.ils.dsarmd0200.util;

import java.io.*;
import java.util.*;
import edu.albany.ils.dsarmd0200.lu.Settings;

public class SentenceSplitter implements Serializable
{
//    private static char SEMICOLON = ';';
    private static char SEMICOLON = '?';
    private static char QUESTIONMARK = '?';
    private static char EXCLAMATIONMARK = '!';
    private static char PERIOD = '.';
    private static char NEWLINE = '\n';
    private static char SPACE = ' ';
    
    //Chinese punctuations
    private static char cnSemicolon='；';
    private static char cnQuestionmark='？';
    private static char cnExclamationmark='！';
    private static char cnPeriod='。';
    private static char cnColon='：';
    
    
    // hold list of known abbreviations
    private ArrayList m_abbrev;
    
    public SentenceSplitter()
    {
    	m_abbrev = new ArrayList();
    	
    	// load abbreviations
	try{
	    BufferedReader in = new BufferedReader( new FileReader( new File( Settings.getValue("abbrv_list")))); //"/home/ting/tools/mdl/abbrv.lst" ) ) );
	    
	    String line = new String();
	    while ( (line = in.readLine()) != null )
		{
		    m_abbrev.add( line );
		}
	}
	catch( IOException ioe )
	    { 
		System.out.println( "Error opening or reading: abbrv.lst file" );
		ioe.printStackTrace();
	    }    	
    }
    
    /**
     * will split the given text
     */
    public ArrayList split( String text )
    {
    	ArrayList sentences = new ArrayList();
    	
    	int totalLength = text.length();
    	int wordCount = 0;
    	int sentenceStartIndex = 0;
    	int wordStartIndex = -1;
//    	System.out.println("totalLength"+totalLength);
    	
    	for ( int i = 0; i < totalLength; i++ )
	    {
    		char c = text.charAt( i );
    		
    		if ( wordCount > 0 && ( c == SEMICOLON || c == QUESTIONMARK || c == EXCLAMATIONMARK ) )
		    {
    			// end of sentence unconditionally
    			String s = new String();
    			s = text.substring( sentenceStartIndex, i + 1);
    			sentences.add( s );
    			
    			sentenceStartIndex = i + 1;
    			wordCount = 0;
    			wordStartIndex = -1;
		    }
    		
    		if ( c == SPACE )
		    {
    			if ( i - 1 >= 0 ) 
			    {
    				// if we had no whitespace directly before this its a new word
    				if ( ! Character.isWhitespace( text.charAt( i - 1 ) ) )
				    {
    					wordCount++; //add as new word
				    }	
			    }
		    }
    		
    		if ( Character.isWhitespace( c ) )
		    wordStartIndex = -1;
    		
    		if ( c == NEWLINE && wordCount > 0 )
		    {
    			boolean newSentence = false;
    			boolean stopSearch = false;
    			
    			// may be end of sentence may not be, look at the next word
    			for ( int j = i + 1; j < totalLength && newSentence == false && stopSearch == false ; j++ )
			    {
    				char next = text.charAt( j );
    				
    				if ( Character.isLetter( next ) )
				    {
    					if ( Character.isUpperCase( next ) )
					    newSentence = true;
    					else if ( Character.isLowerCase( next ) )
					    {
    						// need to check for punctuation now to see if its a list of some sort
    						if ( ! Character.isLetter( text.charAt( j + 1 ) ) && ! Character.isDigit( text.charAt( j + 1 ) ) )
						    newSentence = true;
    						else 
						    stopSearch = true;
					    }
					
				    }
    				
    				// most likely if we see a digit as the first thing on the new line its some kind of list
    				if ( Character.isDigit( next ) )
				    {
    					// find the end of the number string and look for a puncuation
    					for ( int k = j + 1; k < totalLength; k++ )
					    {
    						char c2 = text.charAt( k );
    						if ( c2 != PERIOD && c2 != ')' && ! Character.isDigit( c2 ) )
						    stopSearch = true;
    						else if ( c2 == PERIOD || c2 == ')' )
						    {
    							// look at the very next location
    							if ( text.charAt( k + 1 ) == SPACE )
							    newSentence = true;
    							else
							    stopSearch = true;
						    }
					    }
				    }	
			    }
    			
    			if ( newSentence )
			    {
    				String s = new String();
        			s = text.substring( sentenceStartIndex, i + 1 );
        			s = s.trim();
        			sentences.add( s );
        			
        			sentenceStartIndex = i + 1;
        			wordCount = 0;    	
        			wordStartIndex = -1;
			    }
		    }
    		
    		if ( c == PERIOD && wordCount > 0)
		    {
    			// may or may not be end of sentence
    			boolean stopSearch = false;
    			boolean newSentence = false;
    			boolean checkDistanceToNextStopSymbol = false;
    			
    			if ( i + 1 >= totalLength )
			    newSentence = true;
			
    			if ( i - 1 < 0 )
			    stopSearch = true;
    			
    			// if there is something other than a space or newline or number after this then its not an end of sentence. i.e. "i.e." 
    			if ( !newSentence && !stopSearch && text.charAt( i + 1 ) != SPACE && text.charAt( i + 1 ) != NEWLINE && !Character.isDigit( text.charAt( i + 1 ) ) )
			    {
    				stopSearch = true;
			    }
    			else
			    {
    				// check if we are looking at a decimal number i.e. 2.0
    				if ( !newSentence && !stopSearch && Character.isDigit( text.charAt( i + 1 ) ) && Character.isDigit( text.charAt( i - 1 ) ) )
				    {
    					stopSearch = true;
				    }
			    }
    			//letter+any
    			if ( !newSentence && !stopSearch && i-1 >= 0 && ( Character.isLetter( text.charAt( i - 1) ) ) )
			    {
    				// check for abbreviations
    				if ( Character.isUpperCase( text.charAt( i - 1 ) ) )
				    {
    					// this is an abbreviation, but is it the end of a sentence?
    					// for first iterations lets say no
    					stopSearch = true;
				    }
    				else
				    {
					// check for known abbreviations
					String s = text.substring( wordStartIndex, i );
    					s = s.trim();
    					//System.out.println ( "comparing word: " + s + " to abbrev list" );
    					for ( int j = 0; j < m_abbrev.size(); j++ )
					    {
    						String word = ( String ) m_abbrev.get( j );
    						if ( s.equalsIgnoreCase( word ) )
						    stopSearch = true;
					    }
    					
    					if ( !stopSearch )
					    {
	    					// also check for known abbreviations AFTER the period (IE. webaddresses)
	    					int j = i + 1;
	    					while ( j < totalLength && Character.isLetter( text.charAt( j ) ) )
						    j++;
	    					
	    					// get end text
	    					s = text.substring( i + 1, j );
	    					s = s.trim();
	    					for ( j = 0; j < m_abbrev.size(); j++ )
						    {
	    						String word = ( String ) m_abbrev.get( j );
	    						if ( s.equalsIgnoreCase( word ) )
	    							stopSearch = true;
						    }
					    }
    					
    					if ( !stopSearch )
					    {
	    					// not an abbreviation, end of sentence
	    					newSentence = true;
					    }
				    }
			    }
    			else if ( !newSentence && !stopSearch )
			    newSentence = true;
			
    			// look for ... or . . .
    			if ( !stopSearch && i + 2 < totalLength )
			    {   
                                // .+..
    				if ( ( text.charAt( i + 1 ) == PERIOD && text.charAt( i + 2 ) == PERIOD ) )
				    {
    					// adjust variables to jump to the end of this and end the sentence
    					newSentence = true;
    					i = i + 2;  //..+any
				    }
                                //.+ . .
    				else if ( i+4 < totalLength && text.charAt( i + 2 ) == PERIOD && text.charAt( i + 4 ) == PERIOD )
				    {
    					newSentence = true;
    					i = i + 4;
				    }
			    }
    			
    			if ( newSentence )
			    {
    				String s = new String();
        			s = text.substring( sentenceStartIndex, i + 1);
        			s = s.trim();
        			sentences.add( s );
        			
        			sentenceStartIndex = i + 1;
        			wordCount = 0;     
        			wordStartIndex = -1;
			    }
		    }
    		
    		if ( Character.isLetter( c ) )
		    {
    			if ( wordStartIndex == -1 )
			    wordStartIndex = i;
		    }
	    }
    	
    	// do one last add sentence if startindex isn't at the end so we get the last sentence
    	if ( sentenceStartIndex < totalLength )
	    {
		String s = new String();
		//			s = text.substring( sentenceStartIndex, totalLength - 1 );
		s = text.substring( sentenceStartIndex, totalLength );
		//System.out.println("add setence (at end): " + s);
		s = s.trim();
		sentences.add( s );    		
	    }
    	
    	return sentences;
    }
    
//<<<<<<< SentenceSplitter.java
    public ArrayList splitChinese(String content) {
        //String[] items = content.split("[。]+");
        //instance varibles
        ArrayList sentences=new ArrayList();
        int totalLength=content.length();
        
        int sentenceStartIndex=0;
        
        //currentChar
        
        for(int i=0;i<totalLength;i++){
            char c=content.charAt(i);
            
            //check whether it is a sequence unconditionally
            if(c==cnExclamationmark || c==cnQuestionmark || c==cnSemicolon){
                String item=content.substring(sentenceStartIndex, i+1);
               
                sentences.add(item);
                //next Sentence beign
                sentenceStartIndex=i+1;               
                
            }
        }
        
        // do one last add sentence if startindex isn't at the end so we get the last sentence
    	if ( sentenceStartIndex < totalLength )
	    {
		String s = new String();
		//			s = text.substring( sentenceStartIndex, totalLength - 1 );
		s = content.substring( sentenceStartIndex, totalLength );		
		s = s.trim();
		sentences.add( s );    		
	    }
        
        return sentences;
        //return new ArrayList(Arrays.asList(items));
    }
    
//=======
    
    
//>>>>>>> 1.3
    
    public static void main(String[] args) 
    {
	
    }
}
