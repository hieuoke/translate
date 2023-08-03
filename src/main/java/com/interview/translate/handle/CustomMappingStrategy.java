package com.interview.translate.handle;

import com.opencsv.bean.CsvToBeanFilter;

public class CustomMappingStrategy implements CsvToBeanFilter{
	
	private String lang;
	
	

	public CustomMappingStrategy(String lang) {
		super();
		this.lang = lang;
	}



	@Override
	public boolean allowLine(String[] line) {
		 return this.lang.equalsIgnoreCase(line[1]) || "vie".equalsIgnoreCase(line[1]);
	}

}
