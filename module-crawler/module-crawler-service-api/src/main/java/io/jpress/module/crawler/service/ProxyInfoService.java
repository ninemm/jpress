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

package io.jpress.module.crawler.service;

import com.jfinal.plugin.activerecord.Page;
import io.jpress.module.crawler.model.ProxyInfo;

import java.util.List;

/**
 * @author ninemm
 */
public interface ProxyInfoService {

    /**
     * find model by primary key
     *
     * @param id
     * @return
     */
    public ProxyInfo findById(Object id);


    /**
     * find all model
     *
     * @return all <ProxyInfo
     */
    public List<ProxyInfo> findAll();

    /**
     * find count by enable status
     *
     * @param isEnable
     *
     * @return Integer
     */
    public Integer findCountByEnableStatus(Integer isEnable);

    /**
     * find proxy by protocol
     *
     * @param protocol
     * @param count
     *
     * @return all <ProxyInfo
     */
    public List<ProxyInfo> findByProtocol(String protocol, Integer count);

    /**
     * delete model by primary key
     *
     * @param id
     * @return success
     */
    public boolean deleteById(Object id);


    /**
     * delete model
     *
     * @param model
     * @return
     */
    public boolean delete(ProxyInfo model);


    /**
     * save model to database
     *
     * @param model
     * @return  id value if save success
     */
    public Object save(ProxyInfo model);


    /**
     * save or update model
     *
     * @param model
     * @return id value if saveOrUpdate success
     */
    public Object saveOrUpdate(ProxyInfo model);

    /**
     * batch save proxy ip
     *
     * @author ninemm
     * @date 15:40
     * @param list
     * @return
     */
    public void batchSave(List<String> list);

    /**
     * update data model
     *
     * @param model
     * @return
     */
    public boolean update(ProxyInfo model);

    /**
     * batch update verify status
     *
     * @param list
     * @return
     */
    public void batchUpdateVerifyStatus(List<ProxyInfo> list);

    /**
     * paginate query
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<ProxyInfo> paginate(int page, int pageSize);

    /**
     * paginate query
     *
     * @param page
     * @param pageSize
     * @param orderBy
     * @return
     */
    public Page<ProxyInfo> paginate(int page, int pageSize, String orderBy);

    /**
     * paginate query with params
     *
     * @param page
     * @param pageSize
     * @param protocol
     * @param anonymity
     * @param response
     * @param isEnable
     * @return
     */
    public Page<ProxyInfo> paginate(int page, int pageSize, String protocol, String anonymity,
            Integer response, Integer isEnable);


}