package com.interview.translate.service.impl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.interview.translate.entity.TranslationCsv;
import com.interview.translate.modal.Links;
import com.interview.translate.modal.SentenceWithAudio;
import com.interview.translate.modal.Sentences;
import com.interview.translate.repository.TranslateRepository;
import com.interview.translate.service.TranslateService;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

@Service
public class TranslateServiceImpl implements TranslateService {

	@Value("${csv.links.path}")
	private String csvFileLinkPath;

	@Value("${csv.sentences.path}")
	private String csvFileSentencesPath;

	@Value("${csv.sentences_with_audio.path}")
	private String csvFileSentencesWithAudioPath;

	@Value("${csv.new.path}")
	private String csvFileNewPath;

	@Autowired
	private TranslateRepository translateRepository;

	@Override
	public void translatesAndWhiteToCsv(String lang) {

		Map<String, String> langSentences = new HashMap<>();
		Map<String, String> vieSentences = new HashMap<>();
		List<Sentences> lstS = new ArrayList<Sentences>();
		List<Links> lstL = new ArrayList<Links>();
		List<SentenceWithAudio> lstSwa = new ArrayList<SentenceWithAudio>();

		ExecutorService executor = Executors.newFixedThreadPool(3);

		CompletableFuture<Void> a = CompletableFuture.runAsync(() -> processCSVFile("s", lstS, csvFileSentencesPath),
				executor);
		CompletableFuture<Void> b = CompletableFuture.runAsync(() -> processCSVFile("l", lstL, csvFileLinkPath),
				executor);
		CompletableFuture<Void> c = CompletableFuture
				.runAsync(() -> processCSVFile("swa", lstSwa, csvFileSentencesWithAudioPath), executor);

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(a, b, c);
		try {
			allFutures.join();
			executor.shutdown();
			lstS.stream().forEach(e -> {
				if (e.getLang().equals(lang)) {
					langSentences.put(e.getId(), e.getText());
				} else if (e.getLang().equals("vie")) {
					vieSentences.put(e.getId(), e.getText());
				}
			});
			List<TranslationCsv> translationCsvs = new ArrayList<TranslationCsv>();

			lstL.stream().filter(i -> Objects.nonNull(langSentences.get(i.getId()))).forEach(e -> {
				String englishText = langSentences.get(e.getId());
				String audioUrl = findAudioUrl(lstSwa, e.getId(), lang);
				String vietnameseText = vieSentences.get(e.getLinkId());

				if (englishText != null && vietnameseText != null) {
					synchronized (translationCsvs) {
						TranslationCsv t = TranslationCsv.builder().en_id(Long.valueOf(e.getId())).en_text(englishText)
								.audio_url(audioUrl).vi_id(Long.valueOf(e.getLinkId())).vi_text(vietnameseText).build();

						translationCsvs.add(t);
					}
				}

			});

			System.out.println("add complete ");
			translateRepository.saveAll(translationCsvs);

			writeTranslationsToCSV(translationCsvs, csvFileNewPath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public <E> void processCSVFile(String type, List<E> lst, String path) {
		try (CSVReader csvReader = new CSVReader(new FileReader(path))) {
			String[] line;
			int batchCount = 0;

			while ((line = csvReader.readNext()) != null) {
				batchCount++;
				String col[] = line[0].split("\t");
				switch (type) {
				case "s":
					if (!"eng".equals(col[1]) && !"vie".equals(col[1])) {
						continue;
					}
					lst.add((E) Sentences.builder().id(col[0]).lang(col[1]).text(col.length > 2 ? col[2] : "").build());
					break;
				case "l":
					lst.add((E) Links.builder().id(col[0]).linkId(col[1]).build());
					break;
				case "swa":
					lst.add((E) SentenceWithAudio.builder().id(col[0]).userName(col[1])
							.license(col.length > 2 ? col[2] : "").attributionUrl(col.length > 3 ? col[3] : "")
							.build());
					break;
				default:
					break;
				}
				if (batchCount % 10000 == 0) {
					System.out.println("Processed " + type + " " + batchCount + " lines");
				}
			}
			System.out.println("Finished processing file " + type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTranslationsToCSV(List<TranslationCsv> translations, String csvFilePath) throws IOException {

		Path path = Paths.get(csvFilePath);
		if (!Files.exists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}
		try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {

			translations.stream().forEach(e -> {
				String[] data = { String.valueOf(e.getEn_id()), e.getEn_text(), e.getAudio_url(),
						String.valueOf(e.getVi_id()), e.getVi_text() };
				writer.writeNext(data);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("white file complete");
	}

	private String findAudioUrl(List<SentenceWithAudio> sentencesWithAudio, String sentenceId, String lang) {
		return "https://audio.tatoeba.org/sentences/" + lang + "/" + sentenceId + ".mp3";
	}

	@Override
	public List<TranslationCsv> getTranslate(Integer pageNo, Integer pageSize) {

		Pageable paging = PageRequest.of(pageNo, pageSize);

		Page<TranslationCsv> pagedResult = translateRepository.findAll(paging);

		if (pagedResult.hasContent()) {
			return pagedResult.getContent();
		} else {
			return new ArrayList<TranslationCsv>();
		}
	}
}
