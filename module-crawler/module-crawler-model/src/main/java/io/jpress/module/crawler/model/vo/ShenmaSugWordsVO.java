package io.jpress.module.crawler.model.vo;

import java.util.List;

/**
 * 神马下拉相关关键词VO
 * <p></p>
 * window.shenma.sug({"q":"设计","r":[{"w":"设计师"},{"w":"设计图纸"},{"w":"设计签名"},{"w":"设计本"},{"w":"设计图"},{"w":"设计专业"}]});
 * </p>
 * @author Eric.Huang
 * @date 2019-05-20 15:46
 * @package io.jpress.module.crawler.model.vo
 **/

public class ShenmaSugWordsVO {

    private String q;
    private List<Word> r;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public List<Word> getR() {
        return r;
    }

    public void setR(List<Word> r) {
        this.r = r;
    }

    public class Word {
        private String w;

        public String getW() {
            return w;
        }

        public void setW(String w) {
            this.w = w;
        }
    }

    @Override
    public String toString() {
        return "ShenmaSugWordsVO{" +
                "q='" + q + '\'' +
                ", r=" + r +
                '}';
    }
}
