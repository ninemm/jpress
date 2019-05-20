package io.jpress.module.crawler.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.Jboot;
import io.jpress.module.crawler.callable.BaiduKeywordCallable;
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

public class KeywordValidateTask implements Runnable {

    private static final Log _LOG = Log.getLog(KeywordValidateTask.class);

    private ScheduleTask scheduleTask;

    public KeywordValidateTask(ScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @Override
    public void run() {

        Integer pageNum = scheduleTask.getLastPageNum();
        String searchType = scheduleTask.getSearchType();

        if (pageNum == null) {
            pageNum = 1;
        }

        Page<Keyword> page = Aop.get(KeywordService.class).paginate(pageNum, scheduleTask.getPageSize(), "id asc");
        if (page.getTotalPage() == 0) {
            scheduleTask.setLastPageNum(1);
            scheduleTask.setTotalNum(0);
            scheduleTask.setLastExecuteTime(new Date());
            Aop.get(ScheduleTaskService.class).update(scheduleTask);
            return;
        }

        int processors = Runtime.getRuntime().availableProcessors();
        ThreadFactory threadName = new ThreadFactoryBuilder().setNameFormat("当前线程-%d").build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors * 3,
                processors * 5,
                200L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                threadName,
                new ThreadPoolExecutor.AbortPolicy());

        execute(page.getList(), searchType, executor);

        int totalNum = page.getList().size();
        Long lastKeywordId = page.getList().get(totalNum - 1).getId();
        scheduleTask.setTotalNum(totalNum + scheduleTask.getTotalNum());
        scheduleTask.setLastKeywordId(lastKeywordId.intValue());

        if (page.getTotalPage() == scheduleTask.getLastPageNum()) {
            scheduleTask.setLastPageNum(1);
        } else {
            scheduleTask.setLastPageNum(pageNum + 1);
        }

        Aop.get(ScheduleTaskService.class).update(scheduleTask);
    }

    /**
     *  多个关键词下拉验证后的结果
     */
    private void execute(List<Keyword> list, String searchType, ThreadPoolExecutor executor) {

        /** guava 并发执行 */
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(executor);
        /** Map存储关键词ID，有效状态 */
        List<ListenableFuture<KeywordParamVO>> futures = Lists.newArrayList();

        list.stream().forEach(keyword -> {

            Callable<KeywordParamVO> callable = null;
            KeywordParamVO keywordVO = new KeywordParamVO(keyword.getId(), keyword.getTitle());

            if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_BAIDU)) {
                callable = new BaiduKeywordCallable(keywordVO);
            }

            futures.add(executorService.submit(callable));
        });

        try {
            /** 执行结果 */
            List<KeywordParamVO> result = Futures.successfulAsList(futures).get();
            if (result != null && result.size() > 0) {
                Ret ret = Ret.create();
                ret.put("taskId", scheduleTask.getTaskId());
                Map<String, List<KeywordParamVO>> keywordData = ImmutableMap.of(searchType, result);
                ret.put("keywordData", keywordData);

                Jboot.sendEvent(CrawlerConsts.UPDATE_SUG_WORD_EVENT_NAME, ret);
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
}
