package io.jpress.module.crawler.callable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.jboot.components.http.JbootHttpManager;
import io.jboot.components.http.JbootHttpRequest;
import io.jpress.module.crawler.crawler.BaiduRelWordsCrawler;
import io.jpress.module.crawler.crawler.SogoRelWordsCrawler;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.BaiduSugWordsVO;
import io.jpress.module.crawler.model.vo.KeywordParamVO;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 搜狗关键词验证
 *
 * @author Eric.Huang
 * @date 2019-05-20 15:37
 * @package io.jpress.module.crawler.callable
 **/

public class SogoKeywordCallable implements Callable<KeywordParamVO> {

    private static final String REL_KEYWORDS_URL = "https://www.sogou.com/suggnew/ajajjson?type=web&key=${content}";

    private KeywordParamVO keyword;

    public SogoKeywordCallable(KeywordParamVO keyword) {
        this.keyword = keyword;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public KeywordParamVO call() throws Exception {

        String url = REL_KEYWORDS_URL.replace("${content}", keyword.getTitle());
        JbootHttpRequest request = JbootHttpRequest.create(url);
        String content = JbootHttpManager.me().getJbootHttp().handle(request).getContent();
        content = content.replace("window.sogou.sug(", "")
                .replace(",-1);", "");

        JSONArray array = JSON.parseArray(content);
        List<String> list = JSON.parseArray(array.get(1).toString(), String.class);
        if (list != null && list.size() > 0) {
            keyword.setValid(true);
            return keyword;
        }

        /** 相关搜索验证关键词是否有效 */
        searchRelKeywords();

        keyword.setValid(false);
        return keyword;
    }

    private void searchRelKeywords() {
        try {
            SogoRelWordsCrawler crawler = new SogoRelWordsCrawler(keyword, CrawlerConsts.SEARCH_ENGINE_SOGO, 1);
            crawler.getConf().setExecuteInterval(1000);
            crawler.getConf().setWaitThreadEndTime(1000);
            crawler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
