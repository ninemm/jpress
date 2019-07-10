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
