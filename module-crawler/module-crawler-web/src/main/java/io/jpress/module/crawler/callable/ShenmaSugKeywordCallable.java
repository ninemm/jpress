package io.jpress.module.crawler.callable;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jfinal.kit.Ret;
import io.jboot.Jboot;
import io.jboot.utils.HttpUtil;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.KeywordParamVO;
import io.jpress.module.crawler.model.vo.ShenmaSugWordsVO;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 搜狗关键词验证
 *
 * @author Eric.Huang
 * @date 2019-05-20 15:37
 * @package io.jpress.module.crawler.callable
 **/

public class ShenmaSugKeywordCallable implements Callable<KeywordParamVO> {

    private static final String REL_KEYWORDS_URL = "http://sugs.m.sm.cn/web?t=w&fr=android&q=${content}&callback=window.shenma.sug";

    private KeywordParamVO keyword;

    public ShenmaSugKeywordCallable(KeywordParamVO keyword) {
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
        content = content.replace("window.shenma.sug(", "")
                .replace(");", "");

        ShenmaSugWordsVO shenma = JSON.parseObject(content, ShenmaSugWordsVO.class);
        if (shenma.getR() != null && shenma.getR().size() > 0) {

            List<ShenmaSugWordsVO.Word> result = shenma.getR();
            List<String> keywordList = Lists.newArrayList();
            result.stream().forEach(word -> {
                keywordList.add(word.getW());
            });

            /** 下拉提示关键词保存 */
            Ret ret = Ret.create();
            ret.put("parentId", keyword.getId());
            ret.put("relWordList", keywordList);
            Jboot.sendEvent(CrawlerConsts.ADD_KEYWORD_EVENT_NAME, ret);

            keyword.setValid(true);
            return keyword;
        }

        keyword.setValid(false);
        return keyword;
    }

}
