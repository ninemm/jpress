package io.jpress.module.crawler.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.util.ExceptionUtils;
import cn.hutool.core.util.CharsetUtil;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.KeywordParamVO;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 处理百度相关搜索关键词
 *
 * @author Eric.Huang
 * @date 2019-05-20 16:04
 * @package io.jpress.module.crawler.crawler
 **/

public class ShenmaRelWordsCrawler extends AbstractRamCrawler {

    private static final Log _LOG = Log.getLog(ShenmaRelWordsCrawler.class);

    public ShenmaRelWordsCrawler(KeywordParamVO keyword, String searchType, Integer pageIndex) {
        super(keyword, searchType, pageIndex);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        if (page.code() == 301 || page.code() == 302) {
            try {
                String redirectUrl = new URL(new URL(page.url()), page.location()).toExternalForm();
                next.addAndReturn(redirectUrl).meta(page.copyMeta());
            } catch (MalformedURLException e) {
                //the way to handle exceptions in WebCollector
                ExceptionUtils.fail(e);
                _LOG.error(e.getMessage(), e);
            }
            return;
        }

        Elements results = page.select("#rs>table>tbody>tr>th> a");
        _LOG.info("关键词：" + keyword + ", 在神马中的相关(搜索)关键词个数：" + results.size());

        if (results.size() > 0) {

            Ret ret = Ret.create();
            ret.put("searchType", searchType);
            KeywordParamVO keywordVO = new KeywordParamVO(keyword.getId(), keyword.getTitle(), keyword.getTaskId());
            ret.put("keyword", keywordVO);

            // 更新单个关键词状态
            Jboot.sendEvent(CrawlerConsts.UPDATE_REL_WORD_EVENT_NAME, ret);

            // 保存相关搜索关键词
            ret.clear();
            ret.put("parentId", keyword.getId());
            ret.put("relWordList", results.eachText());
            Jboot.sendEvent(CrawlerConsts.ADD_REL_WORD_EVENT_NAME, ret);
        }
    }

    /**
     * 相关搜索地址
     *
     * @param keyword
     * @param pageIndex
     * @return java.lang.String
     * @author Eric
     * @date 2019-05-06 23:17
     */
    @Override
    public String createUrl(String keyword, Integer pageIndex) {
        try {
            keyword = URLEncoder.encode(keyword, CharsetUtil.UTF_8);
            return String.format("http://www.so.com/s?q=%s", keyword);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
