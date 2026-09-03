package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.domain.AiAnswer;
import cn.wolfcode.wolf2w.business.domain.AiAskRequest;
import cn.wolfcode.wolf2w.business.service.IAiAnswerService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * RAG 智能问答接口。
 */
@Validated
@RestController
@RequestMapping({"/ai/ask", "/ask"})
public class AiAskController {

    private final IAiAnswerService answerService;

    public AiAskController(IAiAnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping
    public R<AiAnswer> ask(@Valid @RequestBody AiAskRequest request) {
        return R.ok(answerService.ask(request.getQuestion(), request.getTopK()));
    }
}
