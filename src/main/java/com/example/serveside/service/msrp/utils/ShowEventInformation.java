package com.example.serveside.service.msrp.utils;

import com.example.serveside.service.msrp.entity.EventInformation;

import java.util.ArrayList;

public class ShowEventInformation
{

    /* This function is used to show event in the shell. */
    public static void showEventInShell(ArrayList<ArrayList<EventInformation>> eventInformations)
    {
        /* Generate the dividing line. */
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 25; ++i)
            builder.append('-');
        String splitLine = builder.toString();
        splitLine += "\n\t\t\t|\n\t\t\t|\n" + splitLine + "\n";

        /* Print even information. */
        for (int i = 0; i < eventInformations.size(); ++i)
        {
            System.out.printf("\n\t\tCPU:%d\n", i);
            for (EventInformation event : eventInformations.get(i))
            {
                // print the split line
                System.out.printf(splitLine);
                // Task run time
                System.out.printf("[start time:%d, end time:%d)\n", event.startTime, event.endTime);

                // Task state:spin/ access resource / computing.
                if (event.spin)
                    System.out.printf("Static task id:%d\nDynamic task id:%d\nTask state: spin\nrequest resource id: %d\n", event.staticTaskId, event.dynamicTaskId, event.requestResourceIndex);
                else if (event.isAccessLocalResource || event.isAccessGlobalResource)
                    System.out.printf("Static task id:%d\nDynamic task id:%d\nTask state: access resource\naccess resource id: %d\n", event.staticTaskId, event.dynamicTaskId, event.requestResourceIndex);
                else if (event.staticTaskId != -1 || event.dynamicTaskId != -1)
                    System.out.printf("Static task id:%d\nDynamic task id:%d\nTask state:computing\n", event.staticTaskId, event.dynamicTaskId);
                else
                    System.out.print("CPU spare!\n");
            }
        }
    }

    /* show criticality_indicator in the shell. */
    public static void showIndicatorInShell(ArrayList<EventInformation> indicatorInformations) {

        System.out.print("\n\n\nSystem Criticality Indicator Information\n");

        /* Generate the dividing line. */
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 25; ++i)
            builder.append('-');
        String splitLine = builder.toString();
        splitLine += "\n\t\t\t|\n" + splitLine + "\n";

        /* Print indicator information. */
        for (int i = 0; i < indicatorInformations.size(); ++i){
            // print the split line
            System.out.printf(splitLine);
            EventInformation event = indicatorInformations.get(i);

            if (i != 0){
                if (event.systemCriticality == 1){
                    System.out.printf("At time %d, indicator from 0 to 1 (Static task id:%d, Dynamic task id:%d)\n", event.startTime, event.staticTaskId, event.dynamicTaskId);
                    System.out.printf(splitLine);
                }
                else{
                    /* 这里有问题 */
//                    System.out.printf("indicator from 1 to 0\n", event.endTime);
                    System.out.printf(splitLine);
                }
            }

            System.out.printf("[start time:%d, end time:%d)\n", event.startTime, event.endTime);
            System.out.printf("system criticality indicator:%d\n", event.systemCriticality);
        }
    }
}
