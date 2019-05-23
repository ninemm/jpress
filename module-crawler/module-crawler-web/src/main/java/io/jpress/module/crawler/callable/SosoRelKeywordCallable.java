package io.jpress.module.crawler.callable;

import io.jpress.module.crawler.model.vo.KeywordParamVO;

/**
 * verify keyword in shenma
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:15
 * @package io.jpress.module.crawler.callable
 **/

public class SosoRelKeywordCallable extends AbstractRelKeywordCallable {

    public SosoRelKeywordCallable(KeywordParamVO keywordParam) {
        super(keywordParam);
    }

    /**
     * Define search url
     *
     * @return java.lang.String
     */
    @Override
    public String getUrl() {
        return String.format("http://www.so.com/s?q=%s", keywordParam.getTitle());
    }

    /**
     * Define css query or xpath
     *
     * @return java.lang.String
     */
    @Override
    public String getXPath() {
        return "#rs > table > tbody > tr > th > a";
    }
}
