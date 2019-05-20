package io.jpress.module.crawler.listener;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import io.jboot.components.event.JbootEvent;
import io.jboot.components.event.JbootEventListener;
import io.jboot.components.event.annotation.EventConfig;
import io.jpress.commons.utils.WebSocketUtil;
import io.jpress.module.crawler.model.Keyword;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.KeywordParamVO;
import io.jpress.module.crawler.service.KeywordService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 处理搜索框下拉提示
 *
 * @author Eric.Huang
 * @date 2019-05-20 22:55
 * @package io.jpress.module.crawler.listener
 **/

@EventConfig(action = CrawlerConsts.UPDATE_SUG_WORD_EVENT_NAME)
public class KeywordSugWordsEventListener implements JbootEventListener {

    private static final Log _LOG = Log.getLog(KeywordSugWordsEventListener.class);

    @Override
    public void onEvent(JbootEvent event) {

        Ret ret = event.getData();
        String taskId = ret.getStr("taskId");
        Map<String, List<KeywordParamVO>> keywordData = (Map<String, List<KeywordParamVO>>) ret.get("keywordData");

        Date modified = new Date();
        List<Keyword> keywordList = Lists.newArrayList();
        KeywordService keywordService = Aop.get(KeywordService.class);

        keywordData.forEach((searchType, list) -> {
            list.stream().forEach(keywordVO -> {

                Keyword keyword = keywordService.findById(keywordVO.getId());
                keyword.setModified(modified);

                if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_BAIDU)) {
                    keyword.setIsBaiduEnabled(keywordVO.getValid());
                    keyword.setIsBaiduChecked(true);
                } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SOGO)) {
                    keyword.setIsSogoEnabled(keywordVO.getValid());
                    keyword.setIsSogoChecked(true);
                } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_360)) {
                    keyword.setIsSosoEnabled(keywordVO.getValid());
                    keyword.setIsSosoChecked(true);
                } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SHENMA)) {
                    keyword.setIsShenmaEnabled(keywordVO.getValid());
                    keyword.setIsShenmaChecked(true);
                }

                _LOG.debug("关键词ID: " + keyword.getId() + ", 关键词名称: " + keyword.getTitle());
                String message = "关键词ID: " + keyword.getId() + ", 关键词名称: " + keyword.getTitle();
                WebSocketUtil.sendMessage(taskId, message);
                keywordList.add(keyword);
            });
        });

        Db.batchUpdate(keywordList, keywordList.size());
    }

}
