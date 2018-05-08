package com.example.administrator.screenpet;

public class ScheduleItem {
    public String getSchedule_content() {
        return schedule_content;
    }

    public String getSchedule_time() {
        return schedule_time;
    }

    public boolean isSchedule_on_off() {
        return schedule_on_off;
    }

    public void setSchedule_content(String schedule_content) {
        this.schedule_content = schedule_content;
    }



    public void setSchedule_time(String schedule_time) {
        this.schedule_time = schedule_time;
    }

    public void setSchedule_on_off(boolean schedule_on_off) {
        this.schedule_on_off = schedule_on_off;
    }

    private String schedule_content;
    private String schedule_time;
    private boolean schedule_on_off;


    public ScheduleItem(String s_content, String s_time, boolean on_off) {
        schedule_content = s_content;
        schedule_on_off = on_off;
        schedule_time = s_time;
    }
}
