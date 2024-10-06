package com.springboot.publicplace.dto.request;

import com.springboot.publicplace.entity.Message;
import lombok.Data;

import java.util.List;

@Data
public class GPTRequest {
    private String model;
    private List<Message> messages;

    //    일회성 질문
    public GPTRequest(String model, String prompt) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt));
    }


    // 이전 질문들 저장후 보냄
    public GPTRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
}
