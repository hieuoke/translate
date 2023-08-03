package com.interview.translate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.interview.translate.entity.TranslationCsv;
import com.interview.translate.service.TranslateService;

@RestController
@RequestMapping("/api/")
public class TranslateController {
	
	@Autowired
	TranslateService translateService;
	
	@GetMapping("translate/{lang}")
	public ResponseEntity<?> translate(@PathVariable String lang) {
		translateService.translatesAndWhiteToCsv(lang);
		return ResponseEntity.ok("Đã xong");
	}
	
	@GetMapping("translates")
	public ResponseEntity<List<TranslationCsv>> getData(
            @RequestParam(defaultValue = "0") Integer pageNo, 
            @RequestParam(defaultValue = "10") Integer pageSize) {
		
		List<TranslationCsv> list = translateService.getTranslate(pageNo, pageSize);
		return ResponseEntity.ok().body(list);
	}

}
