package com.may.paperleap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.may.paperleap.model.domain.Tag;
import com.may.paperleap.service.TagService;
import com.may.paperleap.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 28789
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2025-04-27 23:23:44
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




