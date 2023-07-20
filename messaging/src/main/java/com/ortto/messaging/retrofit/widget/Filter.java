// com.ortto.messaging.retrofit.widget package
package com.ortto.messaging.retrofit.widget;

import com.google.gson.annotations.SerializedName;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

class Filter {
    @SerializedName("$or")
    public Or[] or;

    public static class Or {
        @SerializedName("$str::is")
        public StrIs strIs;

        @SerializedName("$str::contains")
        public StrContains strContains;

        @SerializedName("$str::starts")
        public StrStarts strStarts;

        public static class StrIs {
            public String value;
            public String label;
        }

        public static class StrContains {
            public String value;
            public String label;
        }

        public static class StrStarts {
            public String value;
            public String label;
        }
    }
}

