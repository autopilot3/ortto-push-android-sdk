package com.ortto.messaging.retrofit.widget;

import java.util.List;

class Trigger {
    public String selection;
    public Rules rules;
    public List<String> stop;
    public String value;

    public static class Rules {
        public String when;
        public List<Condition> conditions;

        public static class Condition {
            public String id;
            public String value;
        }
    }
}
