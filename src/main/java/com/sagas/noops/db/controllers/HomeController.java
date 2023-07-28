package com.sagas.noops.db.controllers;

import com.sagas.noops.db.outputs.ResultModel;
import com.sagas.noops.db.outputs.VersionResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    public static final String VERSION = "1.0";

    @RequestMapping("/")
    public String index() {
        return "redirect:/db";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    @GetMapping("/version")
    public ResultModel<VersionResult> version() {
        String os = System.getProperty("os.name");
        VersionResult versionResult = new VersionResult();
        versionResult.setVersion(VERSION);
        versionResult.setOs(os);
        return ResultModel.success(versionResult);
    }
}
