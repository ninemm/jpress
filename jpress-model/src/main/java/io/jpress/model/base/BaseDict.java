package io.jpress.model.base;

import com.jfinal.plugin.activerecord.IBean;
import io.jboot.db.model.JbootModel;

/**
 * Generated by Jboot, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseDict<M extends BaseDict<M>> extends JbootModel<M> implements IBean {

	public void setId(Long id) {
		set("id", id);
	}

	public Long getId() {
		return get("id");
	}

	public void setTypeId(Long typeId) {
		set("type_id", typeId);
	}

	public Long getTypeId() {
		return getLong("type_id");
	}

	public void setType(String type) {
		set("type", type);
	}

	public String getType() {
		return getStr("type");
	}

	public void setName(String name) {
		set("name", name);
	}

	public String getName() {
		return getStr("name");
	}

	public void setKey(String key) {
		set("key", key);
	}

	public String getKey() {
		return getStr("key");
	}

	public void setValue(String value) {
		set("value", value);
	}

	public String getValue() {
		return getStr("value");
	}

	public void setIcon(String icon) {
		set("icon", icon);
	}

	public String getIcon() {
		return getStr("icon");
	}

	public void setCreateDate(java.util.Date createDate) {
		set("create_date", createDate);
	}
	
	public java.util.Date getCreateDate() {
		return get("create_date");
	}

	public void setModifyDate(java.util.Date modifyDate) {
		set("modify_date", modifyDate);
	}
	
	public java.util.Date getModifyDate() {
		return get("modify_date");
	}

}
