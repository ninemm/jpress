package io.jpress.module.crawler.model.vo;

import java.util.List;

/**
 * 360下拉相关关键词VO
 *
 * @author Eric.Huang
 * @date 2019-05-20 15:48
 * @package io.jpress.module.crawler.model.vo
 **/

public class SosoSugWordsVO {

    private String ext;
    private String query;
    private String tag;
    private String ssid;
    private String version;
    private List<Conent> result;

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Conent> getResult() {
        return result;
    }

    public void setResult(List<Conent> result) {
        this.result = result;
    }

    class Conent {
        private Double rank;
        private Double resrc;
        private String source;
        private String word;

        public Double getRank() {
            return rank;
        }

        public void setRank(Double rank) {
            this.rank = rank;
        }

        public Double getResrc() {
            return resrc;
        }

        public void setResrc(Double resrc) {
            this.resrc = resrc;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }
    }

    @Override
    public String toString() {
        return "SosoSugWordsVO{" +
                "ext='" + ext + '\'' +
                ", query='" + query + '\'' +
                ", tag='" + tag + '\'' +
                ", ssid='" + ssid + '\'' +
                ", version='" + version + '\'' +
                ", result=" + result +
                '}';
    }
}
