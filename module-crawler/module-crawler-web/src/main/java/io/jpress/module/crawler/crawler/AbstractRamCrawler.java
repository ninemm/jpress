package io.jpress.module.crawler.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;
import io.jpress.module.crawler.model.vo.KeywordParamVO;

/**
 * RamCrawler抽象类
 *
 * @author Eric.Huang
 * @date 2019-05-06 23:13
 * @package io.jboot.admin.task
 **/

public abstract class AbstractRamCrawler extends RamCrawler {

    /** 关键词参数 */
    protected KeywordParamVO keyword;
    /** 搜索引擎类型 **/
    protected String searchType;

    public AbstractRamCrawler(KeywordParamVO keyword, String searchType, Integer pageIndex) {

        this.keyword = keyword;
        this.searchType = searchType;

        String url = createUrl(keyword.getTitle(), pageIndex);
        CrawlDatum datum = new CrawlDatum(url)
                .type(searchType)
                .meta("keyword", keyword.getTitle())
                .meta("pageIndex", pageIndex)
                .meta("depth", 1);

        addSeed(datum);
    }

    /**
     * 相关搜索地址
     *
     * @author Eric
     * @date  2019-05-06 23:17
     * @param keyword       关键词
     * @param pageIndex     第一页
     * @return java.lang.String
     */
    public abstract String createUrl(String keyword, Integer pageIndex);

}
