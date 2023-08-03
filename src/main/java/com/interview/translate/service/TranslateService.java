package com.interview.translate.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.interview.translate.entity.TranslationCsv;

@Service
public interface TranslateService {
	void translatesAndWhiteToCsv(String lang);

	List<TranslationCsv> getTranslate(Integer pageNo, Integer pageSize);
}
