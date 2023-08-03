package com.interview.translate.modal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sentences {
	
	private String id;
	
	private String lang;
	
	private String text;

}
