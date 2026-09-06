package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Language;
import com.antarang.cap.dto.request.CreateLanguageRequest;
import com.antarang.cap.dto.response.LanguageResponse;
import com.antarang.cap.exception.BusinessException;
import com.antarang.cap.repository.LanguageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LanguageService {

    private final LanguageRepository languageRepository;

    public LanguageService(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @Transactional(readOnly = true)
    public List<LanguageResponse> listLanguages() {
        return languageRepository.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LanguageResponse createLanguage(CreateLanguageRequest request) {
        languageRepository.findByCodeAndIsDeletedFalse(request.code())
                .ifPresent(existing -> {
                    throw new BusinessException("Language code already exists", "DUPLICATE_RESOURCE");
                });

        Language language = new Language();
        language.setCode(request.code().trim());
        language.setName(request.name().trim());
        language.setNativeName(request.name().trim());
        if (request.isActive() != null) {
            language.setActive(request.isActive());
        }

        return toResponse(languageRepository.save(language));
    }

    private LanguageResponse toResponse(Language language) {
        return new LanguageResponse(
                language.getId(),
                language.getCode(),
                language.getName(),
                language.getNativeName(),
                language.isDefault()
        );
    }
}
