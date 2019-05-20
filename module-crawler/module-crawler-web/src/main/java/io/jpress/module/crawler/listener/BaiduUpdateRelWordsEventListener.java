package io.jpress.module.crawler.listener;

import com.google.common.collect.Lists;
import com.jfinal.kit.Ret;
import io.jboot.components.event.JbootEvent;
import io.jboot.components.event.JbootEventListener;
import io.jboot.components.event.annotation.EventConfig;
import io.jpress.commons.utils.WebSocketUtil;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.KeywordParamVO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 处理百度相关词汇存储
 *
 * @author Eric.Huang
 * @date 2019-05-20 16:44
 * @package io.jpress.module.crawler.listener
 **/

@EventConfig(action = CrawlerConsts.UPDATE_REL_WORD_EVENT_NAME)
public class BaiduUpdateRelWordsEventListener implements JbootEventListener {

    public static final Map<String, List<KeywordParamVO>> data = new ConcurrentHashMap<>();

    @Override
    public void onEvent(JbootEvent event) {

        Ret ret = event.getData();
        String searchType = ret.getStr("searchType");
        KeywordParamVO keyword = (KeywordParamVO) ret.get("keyword");
        List<KeywordParamVO> list = data.get(searchType);

        if (list == null) {
            list = Lists.newArrayList();
        }

        int size = list.size();
        if (size == 0) {
            list.add(keyword);
            return ;
        }

        list.add(keyword);
        if (size >= 100) {
            List<String> sqlList = Lists.newArrayList();
            list.stream().forEach(k -> {
                StringBuilder sql = new StringBuilder("update c_keyword set ");

                if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_BAIDU)) {
                    sql.append("is_baidu_enabled = 1, is_baidu_checked = 1 where id = ").append(k.getId());
                } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_BAIDU)) {
                    sql.append("is_sogo_enabled = 1, is_sogo_checked = 1 where id = ").append(k.getId());
                } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_360)) {
                    sql.append("is_soso_enabled = 1, is_soso_checked = 1 where id = ").append(k.getId());
                } else if (searchType.equals(CrawlerConsts.SEARCH_ENGINE_SHENMA)) {
                    sql.append("is_shenma_enabled = 1, is_shenma_checked = 1 where id = ").append(k.getId());
                }

                sqlList.add(sql.toString());
                StringBuilder message = new StringBuilder();
                message.append("关键词ID: ").append(keyword.getId()).append(", 关键词名称: ").append(keyword.getTitle());
                WebSocketUtil.sendMessage(keyword.getTaskId(), message.toString());
            });
            list.clear();
        }
    }
}
