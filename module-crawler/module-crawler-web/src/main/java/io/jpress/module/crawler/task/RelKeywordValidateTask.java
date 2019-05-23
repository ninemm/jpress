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
import io.jpress.module.crawler.callable.*;
import io.jpress.module.crawler.model.Keyword;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.KeywordParamVO;
import io.jpress.module.crawler.service.KeywordService;
import io.jpress.module.crawler.service.ScheduleTaskService;

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

public class RelKeywordValidateTask implements Runnable {

    private static final Log _LOG = Log.getLog(RelKeywordValidateTask.class);

    private ScheduleTask scheduleTask;

    public RelKeywordValidateTask(ScheduleTask scheduleTask) {
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

        execute(executorPool, page, searchType, pageNum);

    }

    /**
     *  多个关键词下拉验证后的结果
     */
    private void execute(ThreadPoolExecutor executor, Page<Keyword> page, String searchType, int pageNum) {

        /** 监听执行线程池 */
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(executor);
        /** Map存储关键词ID，有效状态 */
        List<ListenableFuture<KeywordParamVO>> futures = Lists.newArrayList();

        page.getList().stream().forEach(keyword -> {

            Callable<KeywordParamVO> callable = null;
            KeywordParamVO keywordParam = new KeywordParamVO(keyword.getId(), keyword.getTitle());

            if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SOGO)) {
                callable = new SogoRelKeywordCallable(keywordParam);
            } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_360)) {
                callable = new SosoRelKeywordCallable(keywordParam);
            } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SHENMA)) {
                callable = new ShenmaRelKeywordCallable(keywordParam);
            } else {
                callable = new BaiduRelKeywordCallable(keywordParam);
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
            _LOG.info("shut down thread pool");

            doUpdateTaskSchedule(page, pageNum);
        }
        executor.shutdown();
    }

    private void doUpdateTaskSchedule(Page<Keyword> page, int curPageNum) {

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
