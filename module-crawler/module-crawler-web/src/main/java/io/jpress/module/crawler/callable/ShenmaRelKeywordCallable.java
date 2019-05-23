package io.jpress.module.crawler.callable;

import io.jpress.module.crawler.model.vo.KeywordParamVO;

/**
 * verify keyword in shenma
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:15
 * @package io.jpress.module.crawler.callable
 **/

public class ShenmaRelKeywordCallable extends AbstractRelKeywordCallable {

    public ShenmaRelKeywordCallable(KeywordParamVO keywordParam) {
        super(keywordParam);
    }

    /**
     * Define search url
     *
     * @return java.lang.String
     */
    @Override
    public String getUrl() {
        return String.format("https://m.sm.cn/s?q=%s&from=smor&safe=1", keywordParam.getTitle());
    }

    /**
     * Define css query or xpath
     *
     * @return java.lang.String
     */
    @Override
    public String getXPath() {
        return ".ali_rel>ul>li>a";
    }
}
