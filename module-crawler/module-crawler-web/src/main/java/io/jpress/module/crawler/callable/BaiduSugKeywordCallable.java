package io.jpress.module.crawler.callable;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.Ret;
import io.jboot.Jboot;
import io.jboot.utils.HttpUtil;
import io.jpress.module.crawler.model.status.KeywordSourceStatus;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.BaiduSugWordsVO;
import io.jpress.module.crawler.model.vo.KeywordParamVO;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * 百度关键词验证
 *
 * @author Eric.Huang
 * @date 2019-05-20 15:37
 * @package io.jpress.module.crawler.callable
 **/

public class BaiduSugKeywordCallable implements Callable<KeywordParamVO> {

    private static final String REL_KEYWORDS_URL = "http://suggestion.baidu.com/su?wd=${content}&cb=window.baidu.sug";

    private KeywordParamVO keyword;

    public BaiduSugKeywordCallable(KeywordParamVO keyword) {
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
        String content = HttpUtil.httpGet(url);
        content = content.replace("window.baidu.sug(", "")
                .replace(");", "");

        BaiduSugWordsVO baidu = JSON.parseObject(content, BaiduSugWordsVO.class);
        if (baidu.getS() != null && baidu.getS().length > 0) {

            /** 下拉提示关键词保存 */
            Ret ret = Ret.create();
            ret.put("parentId", keyword.getId());
            ret.put("source", KeywordSourceStatus.SUG_COLLECTOR);
            ret.put("relWordList", Arrays.asList(baidu.getS()));
            Jboot.sendEvent(CrawlerConsts.ADD_KEYWORD_EVENT_NAME, ret);

            keyword.setValid(true);
            return keyword;
        }

        keyword.setValid(false);
        return keyword;
    }

}
