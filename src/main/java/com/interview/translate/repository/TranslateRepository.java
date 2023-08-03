package com.interview.translate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.interview.translate.entity.TranslationCsv;

public interface TranslateRepository extends JpaRepository<TranslationCsv, Long>{

}
