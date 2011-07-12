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
	File dir;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            - the path to the senna directory
	 */
	public Senna(String path) {
		dir = new File(path);
	}
	
	public Senna(){
		
	}

	/**
	 * Get SRL output from Senna
	 * 
	 * @param line
	 *            - the line to process
	 * @return - the output String
	 */
	public String getSennaOutput(String line) {
		try {
			String cmd = "echo " + line + " | " + dir + "/senna";
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.directory(dir);
			Process shell = pb.start();
			InputStream shellIn = shell.getInputStream();
			shell.waitFor();

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

	/**
	 * Parse the senna output
	 * 
	 * @param allText
	 *            - all lines of the senna output
	 * @param sentence
	 *            - the sentence corresponding to this senna output
	 * @return map of SennaVerbs
	 */
	public HashMap<String, SennaVerb> parseSennaLines(String allText,
			String sentence) {
		lineArr = allText.split("\n");
		HashMap<String, SennaVerb> verbsToArgs = new HashMap<String, SennaVerb>();
		ArrayList<SennaVerb> verbs = new ArrayList<SennaVerb>();
		int verbCount = 0;
		for (int i = 0; i < lineArr.length; i++) {
			String line = lineArr[i].trim();
			if (line.length() == 0)
				continue;
			lineArr[i] = line;
			Pattern p = Pattern.compile("VB[A-Z]?\t");
			//System.out.println(line);
			if ((!line.split("\\s+")[4].trim().equalsIgnoreCase("-"))) {
				SennaVerb v = getVerbArgumentNPs(++verbCount,
						line.split("\\s+")[0].trim(), sentence);
				verbs.add(v);
				verbsToArgs.put(v.getText(), v);
			}
		}
		return verbsToArgs;
	}

	/**
	 * Get the arguments of the verbs
	 * 
	 * @param index
	 *            - the index of the argument to extract
	 * @param verb
	 *            - the verb
	 * @param sentence
	 *            - the sentence to extract from
	 * @return the SennaVerb populated with args
	 */
	public SennaVerb getVerbArgumentNPs(int index, String verb, String sentence) {
		SennaVerb v = new SennaVerb(verb);
		HashMap<String, List<String>> argumentToText = new HashMap<String, List<String>>();
		index = index + 4;

		for (int i = 0; i < lineArr.length; i++) {
			String[] lineTokens = lineArr[i].trim().split("\\s+");
			String token = lineTokens[0].trim();
			String pos = lineTokens[1].trim();
			String value = lineTokens[index].trim();

			if (value.equals("O")) {
				continue;
			} else if (value.startsWith("S-") && !value.contains("S-V")
					&& pos.contains("NN")) {
				String arg = value.split("S-")[1];
				ArrayList<String> arr_token = new ArrayList<String>();
				arr_token.add(token);
				argumentToText.put(arg, arr_token);
			} else if (value.startsWith("B-")) {
				String arg = value.split("B-")[1];
				StringBuilder text = new StringBuilder();
				boolean flag = false;
				if (pos.contains("NN")) {
					text.append(token);
					flag = true;
				}
				while (!value.startsWith("E-")) {
					i++;
					lineTokens = lineArr[i].trim().split("\\s+");
					token = lineTokens[0].trim();
					pos = lineTokens[1].trim();
					value = lineTokens[index];
					if (pos.contains("NN")) {
						text.append(" " + token);
						flag = true;
					} else if (flag == true) {
						if (argumentToText.containsKey(arg))
							argumentToText.get(arg).add(text.toString().trim());
						else {
							ArrayList<String> nps = new ArrayList<String>();
							nps.add(text.toString().trim());
							argumentToText.put(arg, nps);
						}
						text = new StringBuilder();
						flag = false;
					}
				}

				if (value.startsWith("E-") && flag == true) {
					if (argumentToText.containsKey(arg))
						argumentToText.get(arg).add(text.toString().trim());
					else {
						ArrayList<String> nps = new ArrayList<String>();
						nps.add(text.toString().trim());
						argumentToText.put(arg, nps);
					}
					flag = false;
				}

				if (argumentToText.containsKey(arg))
					arg = arg + "-1";

			}
		}

		v.setArgumentToNPs(argumentToText);
		return v;

	}

	/**
	 * Get all arguments of the verb
	 * 
	 * @param index
	 * @param verb
	 * @param sentence
	 * @return
	 */
	public SennaVerb getVerbArguments(int index, String verb, String sentence) {
		List<String> words = Arrays.asList(sentence.trim().split("\\s+"));
		SennaVerb v = new SennaVerb(verb);
		HashMap<String, String> argumentToText = new HashMap<String, String>();

		index = index + 4;
		for (int i = 0; i < lineArr.length; i++) {
			String value = lineArr[i].trim().split("\\s+")[index].trim();
			if (value.equals("O")) {
				continue;
			} else if (value.startsWith("S-") && !value.contains("S-V")) {
				String arg = value.split("S-")[1];
				String text = words.get(i).trim();
				argumentToText.put(arg, text);
			} else if (value.startsWith("B-")
					&& Character.isDigit(value.charAt(value.length()))) {
				System.out.println(value);
				String arg = value.split("B-")[1];
				String text = words.get(i).trim();
				while (!value.startsWith("E-")) {
					i++;
					value = lineArr[i].trim().split("\\s+")[index].trim();
					text += (" " + words.get(i).trim());
				}
				if (argumentToText.containsKey(arg))
					arg = arg + "-1";
				argumentToText.put(arg, text);
			}
		}

		v.setArgumentToText(argumentToText);
		return v;
	}
	
	/**
	 * 
	 * @param sennaOutput - senna output for a sentence
	 * @return
	 */
	public ArrayList<String> getNounPhrases(String sennaOutput) {
		//System.out.println(sennaOutput);
		ArrayList<String> nounPhrases = new ArrayList<String>();
		String[] lineArr = sennaOutput.split("\n");

		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> posTags = new ArrayList<String>();
		ArrayList<String> chunkerTokens = new ArrayList<String>();
		try {
			for (String line : lineArr) {
				String[] tokens = line.trim().split("[ \t]+");
				if (tokens.length < 3)
					continue;
				words.add(tokens[0].trim());
				posTags.add(tokens[1].trim());
				chunkerTokens.add(tokens[2].trim());
			}
			
			boolean flag = false;
			StringBuffer npBuf= new StringBuffer();			
			for(int i=0; i < posTags.size(); i++){
				if(posTags.get(i).startsWith("NN") || posTags.get(i).startsWith("JJ")){
					flag = true;
					npBuf.append(words.get(i)+" ");
				} else if(flag == true){
					flag = false;
					nounPhrases.add(npBuf.toString().trim().toLowerCase());
					npBuf = new StringBuffer();
				}
			}
			if(flag==true)
				nounPhrases.add(npBuf.toString().trim().toLowerCase());
			
//			for (int i = 0; i < chunkerTokens.size(); i++) {
//				if (chunkerTokens.get(i).equalsIgnoreCase("s-np")) {
//					nounPhrases.add(words.get(i).toLowerCase());
//				} else if (chunkerTokens.get(i).equalsIgnoreCase("b-np")) {
//					String np = "";
//					while (!chunkerTokens.get(i).equalsIgnoreCase("e-np")) {
//						//System.out.println(chunkerTokens.get(i));
//						if (posTags.get(i).contains("NN")
//								|| posTags.get(i).contains("JJ"))
//							np += (words.get(i) + " ");
//						i++;
//					}
//					np += (words.get(i) + " ");
//					nounPhrases.add(np.trim().toLowerCase());
//				}
//			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return nounPhrases;
	}
	
	public static void main(String args[]){
		Senna s = new Senna("/Users/tejaswi/Documents/StanfordCourses/SRL/senna-v2.0");
		String line = "Bill Clinton has stents placed in coronary of artery";
		System.out.println(s.getNounPhrases(s.getSennaOutput(line)));
	}
}