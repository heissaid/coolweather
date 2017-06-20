package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/6/9.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;
    @SerializedName("cw")
    public CarWash carWash;
    public Sport sport;
    @SerializedName("drsg")
    public DressSuggestion dressSuggestion;
    @SerializedName("trav")
    public Travel travel;
    @SerializedName("uv")
    public Uv uv;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
    public class DressSuggestion{
        @SerializedName("txt")
        public String info;
    }
    public class Travel{
        @SerializedName("txt")
        public String info;
    }
    public class Uv{
        @SerializedName("txt")
        public String info;
    }
}
