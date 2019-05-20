package io.jpress.module.crawler.model.vo;

import java.util.Arrays;

/**
 * 百度下拉相关词VO
 * <p>
 * window.baidu.sug({q:"关键字",p:false,s:["关键字搜索排名","关键字怎么优化","关键字查询工具","关键字推广","关键词优化","关键词排名","关键字 英文","关键词挖掘","关键词查询","关键词搜索"]});
 * </p>
 * @author Eric.Huang
 * @date 2019-05-20 15:43
 * @package io.jpress.module.crawler.model.vo
 **/

public class BaiduSugWordsVO {

    private String q;
    private Boolean p;
    private String[] s;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Boolean getP() {
        return p;
    }

    public void setP(Boolean p) {
        this.p = p;
    }

    public String[] getS() {
        return s;
    }

    public void setS(String[] s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return "BaiduSugWordsVO: {" +
                "q='" + q + '\'' +
                ", p=" + p +
                ", s=" + Arrays.toString(s) +
                '}';
    }
}
