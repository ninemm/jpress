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

/**
 * 关键词参数VO
 *
 * @author Eric.Huang
 * @date 2019-05-20 16:21
 * @package io.jpress.module.crawler.model.vo
 **/

public class KeywordParamVO {

    /** 关键词ID */
    private Object id;
    /** 关键词 */
    private String title;

    /** 定时任务ID */
    private String taskId;
    /** 关键词是否有效 */
    private Boolean isValid;

    public KeywordParamVO(Object id, String title) {
        this.id = id;
        this.title = title;
    }

    public KeywordParamVO(Object id, String title, String taskId) {
        this.id = id;
        this.title = title;
        this.taskId = taskId;
    }

    public Object getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "KeywordParamVO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", taskId='" + taskId + '\'' +
                ", isValid=" + isValid +
                '}';
    }

}
