package com.ortto.messaging.retrofit.widget;

import com.google.gson.annotations.SerializedName;
import com.ortto.messaging.retrofit.WidgetType;
import com.ortto.messaging.retrofit.widget.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Widget {
    public String id;
    public WidgetType type;
    public Page page;
    public Where where;
    public When when;
    public Who who;
    public Trigger trigger;
    public String frequency;
    public Date expiry;
    @SerializedName("is_gdpr")
    public boolean isGdpr;
    public Style style;
    public String html;
    @SerializedName("use_slot")
    public String useSlot;
    public List<Font> font;
    @SerializedName("font_urls")
    public List<String> fontUrls;
    public Map<String, String> variables;
    @SerializedName("talk_message_body")
    public String talkMessageBody;
    @SerializedName("talk_message_agent_id")
    public String talkMessageAgentId;
    @SerializedName("talk_message_agent")
    public TalkMessageAgent talkMessageAgent;
    @SerializedName("has_recaptcha")
    public boolean hasRecaptcha;
}

