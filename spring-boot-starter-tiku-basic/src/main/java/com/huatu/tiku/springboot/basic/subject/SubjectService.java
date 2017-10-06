package com.huatu.tiku.springboot.basic.subject;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.utils.recursion.RecursionUtils;
import com.huatu.tiku.springboot.basic.support.ConfigSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hanchao
 * @date 2017/10/6 10:35
 */
@Service
@Slf4j
public class SubjectService implements ConfigSubscriber{
    private static List<Subject> subjectList = Lists.newArrayList();
    private static Map<Integer,Subject> subjectMapping = Maps.newHashMap();

    private static volatile String _sign = "";
    /**
     * 使用配置
     * @param config
     */
    public SubjectService(String config) throws IOException {
        if(StringUtils.isEmpty(config)){
            throw new IllegalArgumentException("basic info cant be empty");
        }
        load(config);
    }

    /**
     * 订阅配置改变的事件
     * @param configChange
     */
    @Override
    public void update(ConfigChange configChange) {
        log.debug(">>> subject changed,oldValue:{} -> newValue:{}",configChange.getOldValue(),configChange.getNewValue());
        String sign = DigestUtils.md5Hex(configChange.getNewValue());
        if(Objects.equals(sign,_sign)){
            log.debug(">>> subjects not really changed!!!");
            return;
        }
        try {
            load(configChange.getNewValue());
        } catch (IOException e) {
            log.error("",e);
        }
    }


    private void load(String config) throws IOException {
        log.debug(">>> load subjects from config...");
        //
        ObjectMapper objectMapper = new ObjectMapper();
        List<Subject> _subjectList = Lists.newArrayList();
        Map<Integer,Subject> _subjectMapping = Maps.newHashMap();

        List<Subject> tops = objectMapper.readValue(config,new TypeReference<List<Subject>>(){});


        for (Subject subject : tops) {
            //递归组装
            RecursionUtils.recursion(subject,(t) -> {
                _subjectList.add(t);
                return t.getChildrens();
            });
        }

        _subjectMapping = _subjectList.stream().collect(Collectors.toMap(Subject::getId, Function.identity()));

        //直接修改引用地址，避免线程安全问题
        subjectList = _subjectList;
        subjectMapping = _subjectMapping;
        _sign = DigestUtils.md5Hex(config);
    }



    /**
     * 返回顶级节点，不存在返回-1
     * @param id
     * @return
     */
    public int top(int id){
        Subject subject = subjectMapping.get(id);
        if(subject == null){
            return -1;
        }
        if(subject.getParent() == 0){
            return subject.getId();
        }
        return top(subject.getParent());
    }


    /**
     * 返回科目信息
     */
    public Subject get(int id){
        return subjectMapping.get(id);
    }


    /**
     * 获取所有的科目
     * @return
     */
    public List<Subject> total(){
        //返回副本
        return Lists.newArrayList(subjectList);
    }

}
