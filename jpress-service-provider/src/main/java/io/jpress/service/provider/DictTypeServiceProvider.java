package io.jpress.service.provider;

import com.jfinal.plugin.activerecord.Model;
import io.jboot.Jboot;
import io.jboot.aop.annotation.Bean;
import io.jboot.service.JbootServiceBase;
import io.jpress.model.Dict;
import io.jpress.model.DictType;
import io.jpress.service.DictTypeService;

@Bean
public class DictTypeServiceProvider extends JbootServiceBase<DictType> implements DictTypeService {

    @Override
    public void shouldUpdateCache(int action, Object data) {

        switch (action) {
            case ACTION_UPDATE :
            case ACTION_DEL :
                if (data instanceof Model) {
                    DictType dictType = (DictType) data;
                    Jboot.getCache().remove(Dict.CACHE_NAME, dictType.getId());
                } else {
                    Jboot.getCache().remove(Dict.CACHE_NAME, data);
                }
                break;
            default :
                ;
        }
    }
}