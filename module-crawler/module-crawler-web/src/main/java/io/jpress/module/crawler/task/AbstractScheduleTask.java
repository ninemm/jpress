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

package io.jpress.module.crawler.task;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.db.model.JbootModel;
import io.jboot.service.JbootServiceBase;
import io.jpress.model.Columns;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.service.ScheduleTaskService;
import io.jpress.module.keyword.model.vo.KeywordParamVO;

import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO <br>
 *
 * @author: Eric Huang
 * @date: 2019/6/27 21:51
 */
public abstract class AbstractScheduleTask<T extends JbootModel> implements Runnable {

    protected ScheduleTask scheduleTask;
    protected Page<T> page;

    public AbstractScheduleTask() {
    }

    public AbstractScheduleTask(ScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @Override
    public void run() {

        Integer pageNum = scheduleTask.getLastPageNum();
        if (pageNum == null || pageNum == 0) {
            pageNum = 1;
        }

        page = getPageData(pageNum);
        ScheduleTaskService taskService = Aop.get(ScheduleTaskService.class);

        if (page.getTotalRow() == 0) {
            scheduleTask.setLastPageNum(1);
            scheduleTask.setTotalNum(0L);
            scheduleTask.setLastExecuteTime(new Date());
            taskService.update(scheduleTask);
            return;
        }

        int processors = Runtime.getRuntime().availableProcessors();
        ThreadFactory threadName = new ThreadFactoryBuilder().setNameFormat("当前线程-%d").build();
        ThreadPoolExecutor executorPool = new ThreadPoolExecutor(processors * 3,
                processors * 5, 200L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                threadName,
                new ThreadPoolExecutor.AbortPolicy());


        execute(executorPool, pageNum);
    }

    /**
     * 多线程验证代理的有效性
     *
     * @author Eric
     * @date 23:25 2019/6/27
     * @param executorService
     * @param pageNum
     * @return
     */
    protected abstract void execute(ThreadPoolExecutor executor, Integer pageNum);

    /**
     * 待处理的数据
     *
     * @author Eric
     * @date 23:26 2019/6/27
     * @return
     */
    protected abstract Page<T> getPageData(Integer pageNum);
}
