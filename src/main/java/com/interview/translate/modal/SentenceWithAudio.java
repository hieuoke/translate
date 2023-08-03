package com.interview.translate.modal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SentenceWithAudio {
	
	private String id;
	
	private String userName;
	
	private String license;
	
	private String attributionUrl;

}
