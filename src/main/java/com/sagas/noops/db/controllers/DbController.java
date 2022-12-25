package com.sagas.noops.db.controllers;

import com.sagas.noops.db.entities.DbSource;
import com.sagas.noops.db.exceptions.DbQueryException;
import com.sagas.noops.db.inputs.DbParam;
import com.sagas.noops.db.outputs.DbResult;
import com.sagas.noops.db.outputs.ResultModel;
import com.sagas.noops.db.outputs.VersionResult;
import com.sagas.noops.db.services.DbQueryService;
import com.sagas.noops.db.services.DbSourceService;
import com.sagas.noops.db.support.SessionHelper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sql.DataSource;
import java.util.List;

@Log4j2
@Controller
public class DbController {
    public static final String VERSION = "1.0";

    @Autowired
    private SessionHelper sessionHelper;

    @Autowired
    private DbSourceService dbSourceService;

    @Autowired
    private DbQueryService dbQueryService;

    @RequestMapping("/")
    public String execute(DbParam dbParam, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("dbSourceList", dbSourceService.getAll());
        model.addAttribute("dbSourceVersion", dbSourceService.getDataVersion());
        model.addAttribute("dbSourceId", sessionHelper.getDbSourceId());
        if (StringUtils.isBlank(dbParam.getSql())) {
            return "index";
        }
        final String sql = dbParam.getSql().trim();
        redirectAttributes.addFlashAttribute("sql", sql);
        final DataSource dataSource = dbSourceService.findDataSource(dbParam.getDbSourceId());
        Assert.notNull(dataSource, "【数据源】不能为空");
        sessionHelper.setDbSourceId(dbParam.getDbSourceId());
        try {
            List<DbResult> dbResultList = dbQueryService.execute(sql, dataSource);
            redirectAttributes.addFlashAttribute(dbResultList);
        } catch (Throwable t) {
            throw new DbQueryException(t, sql);
        }
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/save")
    public String save(DbSource dbSource, int dataVersion) {
        Assert.hasText(dbSource.getId(), "Id不能为空");
        Assert.notNull(dbSource.getType(), "Type不能为空");
        Assert.hasText(dbSource.getUrl(), "Url不能为空");
        Assert.hasText(dbSource.getUsername(), "Username不能为空");
        dbSourceService.save(dbSource, dataVersion);
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/delete")
    public String delete(String id, int dataVersion) {
        Assert.hasText(id, "Id不能为空");
        dbSourceService.delete(id, dataVersion);
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseBody
    @PostMapping("/test")
    public ResultModel<String> testConnection(@RequestBody DbSource dbSource) {
        try {
            if (StringUtils.isBlank(dbSource.getPassword())
                    && StringUtils.isNotBlank(dbSource.getId())) {
                DbSource ds = dbSourceService.getBy(dbSource.getId());
                dbSource.setPassword(ds.getPassword());
            }
            dbQueryService.testConnection(dbSource);
            return ResultModel.success("连接成功。");
        } catch (Exception ex) {
            log.warn(ex);
            return ResultModel.failure("连接失败！", ex.getMessage());
        }
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
