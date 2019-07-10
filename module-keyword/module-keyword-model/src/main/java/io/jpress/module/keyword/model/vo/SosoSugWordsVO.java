/*
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jpress.module.keyword.model.vo;

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
    private List<Content> result;

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

    public List<Content> getResult() {
        return result;
    }

    public void setResult(List<Content> result) {
        this.result = result;
    }

    public class Content {
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
