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

package io.jpress.module.crawler.service.provider;

import com.jfinal.plugin.activerecord.Model;
import io.jboot.Jboot;
import io.jboot.aop.annotation.Bean;
import io.jpress.model.Dict;
import io.jpress.module.crawler.service.ScheduleTaskService;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jboot.service.JbootServiceBase;

@Bean
public class ScheduleTaskServiceProvider extends JbootServiceBase<ScheduleTask> implements ScheduleTaskService {

    @Override
    public void shouldUpdateCache(int action, Object data) {

        switch (action) {
            case ACTION_UPDATE :
            case ACTION_DEL :
                if (data instanceof Model) {
                    ScheduleTask task = (ScheduleTask) data;
                    Jboot.getCache().remove(ScheduleTask.CACHE_NAME, task.getId());
                } else {
                    Jboot.getCache().remove(Dict.CACHE_NAME, data);
                }
                break;
            default :
                ;
        }
    }

}