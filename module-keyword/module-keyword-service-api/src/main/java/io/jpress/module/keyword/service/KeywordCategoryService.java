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

package io.jpress.module.keyword.service;

import com.jfinal.plugin.activerecord.Page;
import io.jboot.service.JbootServiceJoiner;
import io.jpress.module.keyword.model.KeywordCategory;

import java.util.List;

public interface KeywordCategoryService extends JbootServiceJoiner {

    /**
     * find model by primary key
     *
     * @param id
     * @return
     */
    public KeywordCategory findById(Object id);


    /**
     * find all model
     *
     * @return all <KeywordCategory
     */
    public List<KeywordCategory> findAll();

    /**
     * find model by name
     * @param name
     * @return KeywordCategory
     */
    public KeywordCategory findByName(String name);

    /**
     * delete model by primary key
     *
     * @param id
     * @return success
     */
    public boolean deleteById(Object id);

    /**
     * delete model cache by primary key
     *
     * @param id
     * @return success
     */
    public void deleteCacheById(Object id);

    /**
     * delete model
     *
     * @param model
     * @return
     */
    public boolean delete(KeywordCategory model);


    /**
     * save model to database
     *
     * @param model
     * @return  id value if save success
     */
    public Object save(KeywordCategory model);


    /**
     * save or update model
     *
     * @param model
     * @return id value if saveOrUpdate success
     */
    public Object saveOrUpdate(KeywordCategory model);

    /**
     * batch save category
     *
     * @param categoryList
     * @return boolean
     */
    public boolean batchSave(List<String> categoryList);

    /**
     * update data model
     *
     * @param model
     * @return
     */
    public boolean update(KeywordCategory model);

    /**
     * 汇总单个分类关键词总数量
     *
     * @param id
     * @return
     */
    public boolean countById(Object id);

    /**
     * 汇总所有分类关键词总数量
     *
     * @param id
     * @return
     */
    public boolean countAll();

    /**
     * paginate query
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<KeywordCategory> paginate(int page, int pageSize);

    /**
     * paginate query
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    public Page<KeywordCategory> paginate(int pageNum, int pageSize, String name);
}