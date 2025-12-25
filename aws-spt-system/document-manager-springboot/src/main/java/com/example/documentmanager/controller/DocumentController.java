package com.example.documentmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentController {

    /**
     * 显示文档列表页面
     */
    @GetMapping("/")
    public String listDocuments() {
        return "index";
    }
}