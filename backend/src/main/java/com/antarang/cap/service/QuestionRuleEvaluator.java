package com.antarang.cap.service;

import com.antarang.cap.domain.entity.Question;
import com.antarang.cap.domain.entity.QuestionRule;
import com.antarang.cap.domain.enums.RuleType;
import com.antarang.cap.repository.QuestionRuleRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class QuestionRuleEvaluator {

    private final QuestionRuleRepository questionRuleRepository;

    public QuestionRuleEvaluator(QuestionRuleRepository questionRuleRepository) {
        this.questionRuleRepository = questionRuleRepository;
    }

    public List<Question> filterVisible(List<Question> questions, Map<UUID, Map<String, Object>> responsesByQuestionId) {
        List<Question> visible = new ArrayList<>();
        for (Question question : questions) {
            if (isQuestionVisible(question, null, responsesByQuestionId)) {
                visible.add(question);
            }
        }
        return visible;
    }

    public boolean isQuestionVisible(
            Question question,
            Map<String, Object> conditionConfig,
            Map<UUID, Map<String, Object>> responsesByQuestionId
    ) {
        if (conditionConfig != null && !conditionConfig.isEmpty()
                && !evaluateRuleConfig(conditionConfig, responsesByQuestionId)) {
            return false;
        }

        List<QuestionRule> rules = questionRuleRepository.findByQuestionIdAndIsActiveTrue(question.getId()).stream()
                .filter(rule -> rule.getRuleType() == RuleType.VISIBILITY || rule.getRuleType() == RuleType.BRANCHING)
                .toList();
        if (rules.isEmpty()) {
            return true;
        }
        for (QuestionRule rule : rules) {
            if (evaluateRuleConfig(rule.getRuleConfig(), responsesByQuestionId)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateRuleConfig(Map<String, Object> ruleConfig, Map<UUID, Map<String, Object>> responsesByQuestionId) {
        if (ruleConfig == null || ruleConfig.isEmpty()) {
            return true;
        }
        Object dependsOn = ruleConfig.get("dependsOnQuestionId");
        if (dependsOn == null) {
            return true;
        }
        UUID dependsOnQuestionId = UUID.fromString(dependsOn.toString());
        Map<String, Object> response = responsesByQuestionId.get(dependsOnQuestionId);
        if (response == null) {
            return false;
        }
        Object requiredOptionIds = ruleConfig.get("requiredOptionIds");
        if (requiredOptionIds instanceof List<?> required) {
            Object selected = response.get("selectedOptionIds");
            if (!(selected instanceof List<?> selectedIds)) {
                return false;
            }
            return selectedIds.stream().map(Object::toString).collect(Collectors.toSet())
                    .containsAll(required.stream().map(Object::toString).toList());
        }
        Object requiredValue = ruleConfig.get("requiredValue");
        if (requiredValue != null) {
            Object text = response.get("responseText");
            return requiredValue.toString().equals(text != null ? text.toString() : null);
        }
        return true;
    }
}
