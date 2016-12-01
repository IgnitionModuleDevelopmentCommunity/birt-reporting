package com.tamakicontrol.modules.utils;

import java.util.HashMap;
import java.util.Map;

public class ArgumentMap extends HashMap<String, Object>{

    public ArgumentMap(){
        super();
    }

    public ArgumentMap(Map<? extends String, ? extends Object>map){
        super(map);
    }

    public Long getLongArg(String arg){
        String value = (String)this.get(arg);

        return value != null ? Long.parseLong(value) : null;
    }

    public Long getLongArg(String arg, Long defaultValue){
        try{
            Long value = getLongArg(arg);
            return value != null ? value : defaultValue;
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    public Integer getIntArg(String arg){
        String value = (String)this.get(arg);

        return value != null ? Integer.parseInt(value) : null;
    }

    public Integer getIntArg(String arg, Integer defaultValue){
        try{
            Integer value = getIntArg(arg);
            return value != null ? value : defaultValue;
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    public Float getFloatArg(String arg){
        String value = (String)this.get(arg);

        return value != null ? Float.parseFloat(value) : null;
    }

    public Float getFloatArg(String arg, Float defaultValue){
        try{
            Float value = getFloatArg(arg);
            return value != null ? value : defaultValue;
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    public Double getDoubleArg(String arg){
        String value = (String)this.get(arg);

        return value != null ? Double.parseDouble(value) : null;
    }

    public Double getDoubleArg(String arg, Double defaultValue){
        try{
            Double value = getDoubleArg(arg);
            return value != null ? value : defaultValue;
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    public Boolean getBooleanArg(String arg){
        String value = (String)this.get(arg);

        return value != null ? Boolean.parseBoolean(value) : null;
    }

    public Boolean getBooleanArg(String arg, Boolean defaultValue){
        try{
            Boolean value = getBooleanArg(arg);
            return value != null ? value : defaultValue;
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }


    public String getStringArg(String arg){
        return (String)this.get(arg);
    }

    public String getStringArg(String arg, String defaultValue){
        try{
            String value = getStringArg(arg);
            return value != null ? value : defaultValue;
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }



}
