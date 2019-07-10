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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.Jboot;
import io.jpress.module.crawler.callable.ProxyValidateCallable;
import io.jpress.module.crawler.model.ProxyInfo;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.ProxyVO;
import io.jpress.module.crawler.service.ProxyInfoService;
import io.jpress.module.crawler.service.ScheduleTaskService;
import io.jpress.module.keyword.model.Keyword;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 代理IP 校验
 *
 * @author: Eric Huang
 * @date: 2019/6/27 21:49
 */
public class ProxyValidateTask extends AbstractScheduleTask<ProxyInfo> {

    private static final Log _LOG = Log.getLog(ProxyValidateTask.class);

    public ProxyValidateTask(ScheduleTask scheduleTask) {
        super(scheduleTask);
    }

    @Override
    public void execute(ThreadPoolExecutor threadPoolExecutor, Integer pageNum) {

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(threadPoolExecutor);
        List<ListenableFuture<ProxyVO>> futures = Lists.newArrayList();

        page.getList().stream().forEach(proxyInfo -> {
            ProxyVO proxy = new ProxyVO(proxyInfo.getIp(), proxyInfo.getPort());
            Callable<ProxyVO> callable = new ProxyValidateCallable(proxy);
            futures.add(executorService.submit(callable));
        });

        ListenableFuture<List<ProxyVO>> resultsFuture = Futures.successfulAsList(futures);
        try {
            List<ProxyVO> result = resultsFuture.get();
            if (result != null && result.size() > 0) {
                Jboot.sendEvent(CrawlerConsts.VALIDATE_PROXY_EVENT_NAME, result);
            }
        } catch (InterruptedException e) {
            _LOG.error("validate proxy error", e);
        } catch (ExecutionException e) {
            _LOG.error("validate proxy error", e);
        } finally {
            threadPoolExecutor.shutdown();
            _LOG.info("shut down thread pool");

            doUpdateTaskSchedule(page, pageNum);
        }
        threadPoolExecutor.shutdown();
    }

    @Override
    protected Page<ProxyInfo> getPageData(Integer pageNum) {
        return Aop.get(ProxyInfoService.class).paginate(pageNum, scheduleTask.getPageSize()
                , "is_verified asc, verify_time asc");
    }

    private void doUpdateTaskSchedule(Page<ProxyInfo> page, int curPageNum) {

        int row = page.getList().size();
        int lastPageNum = page.isLastPage() ? 1 : ++ curPageNum;
        long lastTotalNum = row + scheduleTask.getTotalNum();
        Long lastKeywordId = page.getList().get(row - 1).getId();

        scheduleTask.setTotalNum(lastTotalNum);
        scheduleTask.setLastPageNum(lastPageNum);
        scheduleTask.setLastExecuteTime(new Date());
        scheduleTask.setLastKeywordId(lastKeywordId);
        Aop.get(ScheduleTaskService.class).update(scheduleTask);
    }
}
