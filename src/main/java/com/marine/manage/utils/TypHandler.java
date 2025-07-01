package com.marine.manage.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.marine.manage.pojo.Chapter;

import java.util.List;

public class TypHandler {
    public static class ListChapter extends AbstractJsonTypeHandler<List<Chapter>> {
        public ListChapter() {
            super(List.class);
        }

        public ListChapter(Class<?> type) {
            super(type);
        }
        
        @Override
        public List<Chapter> parse(String json) {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return JSON.parseArray(json, Chapter.class);
        }
        
        @Override
        public String toJson(List<Chapter> obj) {
            if (obj == null) {
                return null;
            }
            return JSON.toJSONString(obj);
        }
    }

    public static class ListString extends AbstractJsonTypeHandler<List<String>> {
        public ListString() {
            super(List.class);
        }

        public ListString(Class<?> type) {
            super(type);
        }
        
        @Override
        public List<String> parse(String json) {
            if (json == null || json.trim().isEmpty()) {
                return null;
            }
            return JSON.parseArray(json, String.class);
        }
        
        @Override
        public String toJson(List<String> obj) {
            if (obj == null) {
                return null;
            }
            return JSON.toJSONString(obj);
        }
    }
}
