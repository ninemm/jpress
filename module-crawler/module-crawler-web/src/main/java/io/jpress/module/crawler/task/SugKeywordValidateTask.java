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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.Jboot;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.service.ScheduleTaskService;
import io.jpress.module.keyword.callable.BaiduSugKeywordCallable;
import io.jpress.module.keyword.callable.ShenmaSugKeywordCallable;
import io.jpress.module.keyword.callable.SogoSugKeywordCallable;
import io.jpress.module.keyword.callable.SosoSugKeywordCallable;
import io.jpress.module.keyword.model.Keyword;
import io.jpress.module.keyword.model.vo.KeywordParamVO;
import io.jpress.module.keyword.service.KeywordService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 在搜索引擎中验证关键词的有效性
 *
 * @author Eric.Huang
 * @date 2019-05-20 14:42
 * @package io.jpress.module.crawler.schedule
 **/

public class SugKeywordValidateTask implements Runnable {

    private static final Log _LOG = Log.getLog(SugKeywordValidateTask.class);

    private ScheduleTask scheduleTask;

    public SugKeywordValidateTask(ScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @Override
    public void run() {

        Integer pageNum = scheduleTask.getLastPageNum();
        String searchType = scheduleTask.getSearchType();

        if (pageNum == null) {
            pageNum = 1;
        }
        ScheduleTaskService taskService = Aop.get(ScheduleTaskService.class);
        Page<Keyword> page = Aop.get(KeywordService.class).paginate(pageNum, scheduleTask.getPageSize(), "id asc");
        if (page.getTotalPage() == 0) {
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

        doUpdateTaskSchedule(page.getList(), page.getTotalPage(), pageNum, scheduleTask.getTotalNum(), scheduleTask.getLastPageNum());

        execute(page.getList(), searchType, executorPool);
    }

    /**
     *  多个关键词下拉验证后的结果
     */
    private void execute(List<Keyword> list, String searchType, ThreadPoolExecutor executor) {

        /** 监听执行线程池 */
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(executor);
        /** Map存储关键词ID，有效状态 */
        List<ListenableFuture<KeywordParamVO>> futures = Lists.newArrayList();

        list.stream().forEach(keyword -> {

            Callable<KeywordParamVO> callable = null;
            KeywordParamVO keywordParam = new KeywordParamVO(keyword.getId(), keyword.getTitle());

            if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SOGO)) {
                callable = new SogoSugKeywordCallable(keywordParam);
            } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_360)) {
                callable = new SosoSugKeywordCallable(keywordParam);
            } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SHENMA)) {
                callable = new ShenmaSugKeywordCallable(keywordParam);
            } else {
                callable = new BaiduSugKeywordCallable(keywordParam);
            }

            futures.add(executorService.submit(callable));
        });

        final ListenableFuture<List<KeywordParamVO>> resultsFuture = Futures.successfulAsList(futures);
        try {
            /** 执行结果 */
            List<KeywordParamVO> result = resultsFuture.get();
            if (result != null && result.size() > 0) {
                Ret ret = Ret.create();
                ret.put("taskId", scheduleTask.getTaskId());
                Map<String, List<KeywordParamVO>> keywordData = ImmutableMap.of(searchType, result);
                ret.put("keywordData", keywordData);

                Jboot.sendEvent(CrawlerConsts.UPDATE_KEYWORD_EVENT_NAME, ret);
            }
        } catch (InterruptedException e) {
            _LOG.error("下拉搜索执行错误", e);
        } catch (ExecutionException e) {
            _LOG.error("下拉搜索执行错误", e);
        } finally {
            executor.shutdown();
        }
        executor.shutdown();
    }

    private void doUpdateTaskSchedule(List<Keyword> list, int totalPage, int pageNum, Long totalNum, int lastPageNum) {
        int size = list.size();
        Long lastKeywordId = list.get(size - 1).getId();
        totalNum = size + totalNum;

        if (totalPage > lastPageNum) {
            lastPageNum = pageNum + 1;
        } else {
            lastPageNum = 1;
        }

        String sql = "update c_schedule_task set last_keyword_id = ?, last_execute_time = ?, last_page_num = ?, total_num = ? where id = ?";
        Db.update(sql, lastKeywordId, new Date(), lastPageNum, totalNum, scheduleTask.getId());
    }
}
