package it.requestassistant.playground;

import it.requestassistant.adapters.outlook.OutlookMailReader;

public class Playground {

    public static void main(String[] args) {

        System.out.println("RequestAssistant Playground");

        OutlookMailReader reader = new OutlookMailReader();
        reader.getLastMessage("");

    }

}
