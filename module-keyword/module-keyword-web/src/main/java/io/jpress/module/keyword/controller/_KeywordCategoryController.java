/*
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.module.keyword.controller;

import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.ImmutableMap;
import com.jfinal.aop.Inject;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.exception.JbootException;
import io.jboot.utils.StrUtil;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jpress.JPressConsts;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.module.keyword.model.Keyword;
import io.jpress.module.keyword.model.KeywordCategory;
import io.jpress.module.keyword.model.util.KeywordConsts;
import io.jpress.module.keyword.service.KeywordCategoryService;
import io.jpress.module.keyword.service.KeywordService;
import io.jpress.web.base.AdminControllerBase;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RequestMapping(value = "/admin/keyword/category", viewPath = JPressConsts.DEFAULT_ADMIN_VIEW)
public class _KeywordCategoryController extends AdminControllerBase {

    @Inject
    private KeywordService keywordService;
    @Inject
    private KeywordCategoryService categoryService;

    @AdminMenu(text = "关键词类型", groupId = "keyword", order = 2)
    public void index() {
        render("keyword/keyword_category_list.html");
    }

    public void paginate() {
        String categoryName = getPara("categoryName");
        Page<KeywordCategory> page = categoryService.paginate(getPagePara(), getPageSizePara(), categoryName);
        Map<String, Object> map = ImmutableMap.of("total", page.getTotalRow(), "rows", page.getList());
        renderJson(map);
    }

   
    public void edit() {
        int entryId = getParaToInt(0, 0);
        KeywordCategory entry = entryId > 0 ? categoryService.findById(entryId) : new KeywordCategory();
        setAttr("keywordCategory", entry);
        render("keyword/keyword_category_edit.html");
    }
   
    public void doSave() {
        KeywordCategory entry = getModel(KeywordCategory.class,"keywordCategory");
        categoryService.saveOrUpdate(entry);
        renderJson(Ret.ok().set("id", entry.getId()));
    }

    public void doDel() {
        Long id = getIdPara();
        render(categoryService.deleteById(id) ? Ret.ok() : Ret.fail());
    }

    public void doCountAll() {
        if (!categoryService.countAll()) {
            throw new JbootException("分类汇总关键词总数量失败");
        }
        renderOkJson();
    }

    public void doCountById() {
        Object id = getPara(0);
        if (id == null) {
            throw new JbootException("分类ID不能为空");
        }

        if (!categoryService.countById(id)) {
            throw new JbootException("分类汇总关键词总数量失败");
        }

        renderOkJson();
    }

    public void view() {
        keepPara();
        render("keyword/keyword_category_view.html");
    }

    public void loadKeywordData() {
        Object categoryId = getPara("categoryId");
        Page<Keyword> page = keywordService.paginate(getPagePara(), getPageSizePara(), categoryId, "title asc");
        Map<String, Object> map = ImmutableMap.of("total", page.getTotalRow(), "rows", page.getList());
        renderJson(map);
    }

    public void backup() {
        String ids = getPara(0);
        Set<String> categorySet = StrUtil.splitToSet(ids, ",");
        if (categorySet.size() == 0) {
            throw new JbootException("分类不能为空");
        }

        String backupFilePath = PathKit.getWebRootPath() + KeywordConsts.BACKUP_KEYWORDS_PATH;
        File backupDirectory = new File(backupFilePath);
        /** 删除备份文件目录 */
        if (backupDirectory.exists()) {
            File[] files = backupDirectory.listFiles();
            Arrays.stream(files).forEach(file -> {
                file.delete();
            });
        } else {
            backupDirectory.mkdirs();
        }

        /** 按分类备份关键词 */
        categorySet.stream().forEach(id -> {
            List<Keyword> list = keywordService.findListByCategoryId(id);
            if (list.size() > 0) {
                String fileName = list.get(0).getCategoryName();
                backupKeywordTxtFile(backupFilePath, fileName, list);
            }
        });

        String zipPath = PathKit.getWebRootPath() + JFinal.me().getConstants().getBaseUploadPath() + "/备份关键词.zip";
        ZipUtil.zip(backupFilePath, zipPath, Charset.forName("GBK"), false);
        renderFile(new File(zipPath));
    }

    private void backupKeywordTxtFile(String backupFilePath, String fileName, List<Keyword> list) {

        String filePath = backupFilePath + "/" + fileName + ".txt";
        File backupTxtFile = new File(filePath);
        BufferedWriter bufferedWriter = null;
        try {
            backupTxtFile.createNewFile();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupTxtFile), "GBK"));

            if (list != null) {
                for (Keyword keyword : list) {
                    bufferedWriter.write(keyword.getTitle());
                    bufferedWriter.newLine();
                }
            }

            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                    bufferedWriter = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}