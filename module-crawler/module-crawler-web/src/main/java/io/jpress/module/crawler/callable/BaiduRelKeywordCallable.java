package io.jpress.module.crawler.callable;

import io.jpress.module.crawler.model.vo.KeywordParamVO;

/**
 * verify keyword in baidu
 * <br /> search relate keyword in baidu, if relate keywords exist then return true, and save the relate keyword
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:15
 * @package io.jpress.module.crawler.callable
 **/

public class BaiduRelKeywordCallable extends AbstractRelKeywordCallable {

    public BaiduRelKeywordCallable(KeywordParamVO keywordParam) {
        super(keywordParam);
    }

    /**
     * Define search url
     *
     * @return java.lang.String
     */
    @Override
    public String getUrl() {
        return String.format("http://www.baidu.com/s?wd=%s", keywordParam.getTitle());
    }

    /**
     * Define css query or xpath
     *
     * @return java.lang.String
     */
    @Override
    public String getXPath() {
        return "#rs>table>tbody>tr>th>a";
    }
}
