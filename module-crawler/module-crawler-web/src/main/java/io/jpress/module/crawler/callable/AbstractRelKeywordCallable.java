package io.jpress.module.crawler.callable;

import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jboot.utils.HttpUtil;
import io.jpress.module.crawler.model.status.KeywordSourceStatus;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.KeywordParamVO;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.concurrent.Callable;

/**
 * handle relate keyword of thread
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:36
 * @package io.jpress.module.crawler.callable
 **/

public abstract class AbstractRelKeywordCallable implements Callable<KeywordParamVO> {

    private static final Log _LOG = Log.getLog(AbstractRelKeywordCallable.class);

    protected KeywordParamVO keywordParam;

    public AbstractRelKeywordCallable(KeywordParamVO keywordParam) {
        this.keywordParam = keywordParam;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public KeywordParamVO call() throws Exception {
        String url = String.format(getUrl(), keywordParam.getTitle());
        String content = HttpUtil.httpGet(url);
        Elements elements = Jsoup.parseBodyFragment(content).select(getXPath());

        if (elements != null && elements.size() > 0) {

            _LOG.info("search relate keyword num : " + elements.size());
            /**
             * save relate keywords
             */
            Ret ret = Ret.create();
            ret.put("parentId", keywordParam.getId());
            ret.put("source", KeywordSourceStatus.REL_COLLECTOR);
            ret.put("relWordList", elements.eachText());
            Jboot.sendEvent(CrawlerConsts.ADD_KEYWORD_EVENT_NAME, ret);

            keywordParam.setValid(true);
            return keywordParam;
        }

        keywordParam.setValid(false);
        return keywordParam;
    }

    /**
     * Define search url
     * @param
     * @return java.lang.String
     */
    public abstract String getUrl();

    /**
     * Define css query or xpath
     * @param
     * @return java.lang.String
     */
    public abstract String getXPath();
}
