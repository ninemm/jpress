package io.jpress.module.crawler.callable;

import io.jpress.module.crawler.model.vo.KeywordParamVO;

/**
 * verify keyword in shenma
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:15
 * @package io.jpress.module.crawler.callable
 **/

public class SogoRelKeywordCallable extends AbstractRelKeywordCallable {

    public SogoRelKeywordCallable(KeywordParamVO keywordParam) {
        super(keywordParam);
    }

    /**
     * Define search url
     *
     * @return java.lang.String
     */
    @Override
    public String getUrl() {
        return String.format("http://www.sogo.com/web?query=%s", keywordParam.getTitle());
    }

    /**
     * Define css query or xpath
     *
     * @return java.lang.String
     */
    @Override
    public String getXPath() {
        return "#hint_container>tbody>tr>td>p>a";
    }
}
