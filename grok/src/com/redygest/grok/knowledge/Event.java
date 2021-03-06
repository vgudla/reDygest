package com.redygest.grok.knowledge;

import java.util.HashMap;
import java.util.List;

public class Event {

	private int index = -1;
	private String text;
	private HashMap<String, List<String>> argumentToText = new HashMap<String, List<String>>();
	private int startRange = -1;
	private int endRange = -1;
	
	/**
	 * Constructor
	 */
	public Event() {
	}

	/**
	 * Constructor
	 * 
	 * @param text
	 */
	public Event(String text) {
		this.text = text;
	}
	
	/**
	 * Constructor
	 * @param text
	 */
	public Event(String text, int index) {
		this.text = text;
		this.index = index;
	}

	/**
	 * get text
	 * 
	 * @return
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * set argument to text
	 * 
	 * @param map
	 */
	public void setArgumentToText(HashMap<String, List<String>> map) {
		this.argumentToText = map;
	}
	

	public HashMap<String, List<String>> getArgumentToText() {
		return argumentToText;
	}

	
	public int getIndex() {
		return index;
	}
	
	public void setStartRange(int i) {
		this.startRange = i;
	}
	
	public void setEndRange(int i) {
		this.endRange = i;
	}
	
	public int getStartRange() {
		return this.startRange;
	}
	
	public int getEndRange() {
		return this.endRange;
	}

}