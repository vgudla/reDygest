package com.redygest.grok.srl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Senna {
	
	String[] lineArr;

	File sennaInstallationDir = new File("/Users/tejaswi/Documents/StanfordCourses/SRL/senna-v2.0");

	/*
	 * @ 
	 */
	public String getSennaOutput(String line) {
		try {
			String cmd = "echo " + line + " | " + sennaInstallationDir
					+ "/senna ";
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(sennaInstallationDir);
			Process shell = pb.start();
			InputStream shellIn = shell.getInputStream();
			int shellExitStatus = shell.waitFor();
			int c;
			StringBuffer s = new StringBuffer();
			while ((c = shellIn.read()) != -1) {
				s.append((char) c);
			}
			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	public HashMap<String, SennaVerb>  parseSennaLines(String allText, String sentence){	
		lineArr = allText.split("\n");
		HashMap<String, SennaVerb> verbsToArgs = new HashMap<String, SennaVerb>();
		ArrayList<SennaVerb> verbs = new ArrayList<SennaVerb>();
		int verbCount = 0;
		for(int i=0; i < lineArr.length; i++) {
			//String[] line = lineArr[i].trim().split("\t");
			String line = lineArr[i].trim();
			if(line.length()==0)
				continue;
			lineArr[i] = line;
			Pattern p = Pattern.compile("VB[A-Z]?\t");
			//System.out.println(line);
			if(( !line.split("\\s+")[4].trim().equalsIgnoreCase("-"))  ){
				//Verb v = getVerbArguments(++verbCount, line.split("\\s+")[0].trim(), sentence);
				SennaVerb v = getVerbArgumentNPs(++verbCount, line.split("\\s+")[0].trim(), sentence);
				verbs.add(v);
				verbsToArgs.put(v.text, v);
			}
		}
		return verbsToArgs; 
	}
	
	public SennaVerb getVerbArgumentNPs(int index, String verb, String sentence){
		SennaVerb v = new SennaVerb();
		v.text = verb;
		HashMap<String, List<String>> argumentToText = new HashMap<String, List<String>>();
		index = index + 4;
		
		for(int i=0; i < lineArr.length; i++){
			try{
			String[] lineTokens =  lineArr[i].trim().split("\\s+");			
			String token = lineTokens[0].trim();
			String pos = lineTokens[1].trim();
			String value = lineTokens[index].trim();
			
			if(value.equals("O")){
				continue;
			} else if(value.startsWith("S-") && !value.contains("S-V") && pos.contains("NN")) {
				String arg = value.split("S-")[1];
				ArrayList<String> arr_token = new ArrayList<String>();
				arr_token.add(token);
				argumentToText.put(arg, arr_token);
//			} else if(value.startsWith("B-") && Character.isDigit(value.charAt(value.length()-1))) {
			} else if(value.startsWith("B-")) {
				String arg = value.split("B-")[1];
				StringBuilder text = new StringBuilder();
				boolean flag = false;
				if(pos.contains("NN")){
				  text.append(token);
				  flag = true;
				}
				while(!value.startsWith("E-")){
					 i++;
					 lineTokens =  lineArr[i].trim().split("\\s+");						
					 token = lineTokens[0].trim();
					 pos = lineTokens[1].trim();
					 value = lineTokens[index];
					 if(pos.contains("NN")){
						 text.append(" "+token);
						 flag = true;
					 }
					 else if(flag==true){
						 if(argumentToText.containsKey(arg))
							 argumentToText.get(arg).add(text.toString().trim());
						 else{
							 ArrayList<String> nps = new ArrayList<String>();
							 nps.add(text.toString().trim());
							 argumentToText.put(arg, nps);
						 }
						 text = new StringBuilder();
						 flag = false;
					 }
				}
				
				if(value.startsWith("E-") && flag==true){					
					 if(argumentToText.containsKey(arg))
						 argumentToText.get(arg).add(text.toString().trim());
					 else{
						 ArrayList<String> nps = new ArrayList<String>();
						 nps.add(text.toString().trim());
						 argumentToText.put(arg, nps);
					 }
					 flag = false;
				}
				
				if(argumentToText.containsKey(arg))
					arg = arg + "-1";
				
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		v.argumentToNPs = argumentToText;
		return v;

	}
	
	//get the arguments of a SennaVerb
	public SennaVerb getSennaVerbArguments(int index, String verb, String sentence){
		
		List<String> words = Arrays.asList(sentence.trim().split("\\s+"));
		//System.out.println(sentence);
		SennaVerb v = new SennaVerb();
		v.text = verb;
		HashMap<String, String> argumentToText = new HashMap<String, String>();
		index = index + 4;
		//System.out.println("index "+index);
		for(int i=0; i < lineArr.length; i++){
			//String[] arr = lineArr[i].split("\\s+");
			String value =  lineArr[i].trim().split("\\s+")[index].trim();
			if(value.equals("O")){
				continue;
			} else if(value.startsWith("S-") && !value.contains("S-V")){
				String arg = value.split("S-")[1];
				//String text = lineArr[i].trim().split("\\s+")[0].trim();
				String text = words.get(i).trim();
				argumentToText.put(arg, text);
			}
			else if(value.startsWith("B-") && Character.isDigit(value.charAt(value.length()))){
				System.out.println(value);
				String arg = value.split("B-")[1];
				//String text = lineArr[i].trim().split("\\s+")[0].trim();
				String text = words.get(i).trim();
				while(!value.startsWith("E-")){
					 i++;
					 value =  lineArr[i].trim().split("\\s+")[index].trim();
					 //text += (" "+lineArr[i].trim().split("\\s+")[0].trim());
					 text += (" "+words.get(i).trim());
				}
				if(argumentToText.containsKey(arg))
					arg = arg + "-1";
				argumentToText.put(arg, text);
			}
		}
		v.argumentToText = argumentToText;
		return v;
	}

}
