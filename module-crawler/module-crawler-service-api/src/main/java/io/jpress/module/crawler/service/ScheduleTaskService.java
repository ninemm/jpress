/**
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jpress.module.crawler.service;

import com.jfinal.plugin.activerecord.Page;
import io.jpress.module.crawler.model.ScheduleTask;

import java.util.List;

public interface ScheduleTaskService  {

    /**
     * find model by primary key
     *
     * @param id
     * @return
     */
    public ScheduleTask findById(Object id);

    /**
     * find model by spider id
     *
     * @param spiderId
     * @return
     */
    public ScheduleTask findBySpiderId(Object spiderId);

    /**
     * find all model
     *
     * @return all <ScheduleTask
     */
    public List<ScheduleTask> findAll();


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
    public boolean delete(ScheduleTask model);


    /**
     * save model to database
     *
     * @param model
     * @return  id value if save success
     */
    public Object save(ScheduleTask model);


    /**
     * save or update model
     *
     * @param model
     * @return id value if saveOrUpdate success
     */
    public Object saveOrUpdate(ScheduleTask model);


    /**
     * update data model
     *
     * @param model
     * @return
     */
    public boolean update(ScheduleTask model);


    /**
     * paginate query
     *
     * @param page
     * @param pageSize
     * @return
     */
    public Page<ScheduleTask> paginate(int page, int pageSize);


}