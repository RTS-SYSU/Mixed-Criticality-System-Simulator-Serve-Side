package sysu.rtsg.analysis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sysu.rtsg.entity.Resource;
import sysu.rtsg.entity.SporadicTask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

public class Test_Dead {

    public static void main(String[] args) throws InterruptedException {

        // 2. 读取filePath下的tasks和resources形成对象
        Gson gson = new Gson();
        Type resourceListType = new TypeToken<ArrayList<Resource>>(){}.getType();
        Type taskListType = new TypeToken<ArrayList<ArrayList<SporadicTask>>>(){}.getType();
        ArrayList<Resource> resources = new ArrayList<>();
        ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("D:\\BADCASE\\HighPerformanceResourceSharingProtocol-master\\Dead_Example\\Case_5\\resources_dead.txt"));
            BufferedReader reader2 = new BufferedReader(new FileReader("D:\\BADCASE\\HighPerformanceResourceSharingProtocol-master\\Dead_Example\\Case_5\\tasks_dead.txt"));
            String re;
            String ta;
            while ((re = reader.readLine()) != null) {
                resources = gson.fromJson(re, resourceListType);
            }
            while ((ta = reader2.readLine()) != null) {
                tasks = gson.fromJson(ta, taskListType);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        HashSet<Resource> shortLocalBlockingResources = new HashSet<>();
        ArrayList<Resource> sss = new ArrayList<>();

        for(int i = 0 ; i < 10; i++){
            shortLocalBlockingResources.add(resources.get(0));
            sss.add(resources.get(0));
        }




        // tasks = new PriorityGeneator().assignPrioritiesByDM(tasks);

//        ResponseTimeAnalysisWithVariousPriorities_B_S analysis = new ResponseTimeAnalysisWithVariousPriorities_B_S();
//        long[][] Ris = analysis.getResponseTimeByDMPO(tasks, resources, 1, false, true, true, true, false);
//
//        ResponseTimeAnalysisWithVariousPriorities_S_B analysis_2 = new ResponseTimeAnalysisWithVariousPriorities_S_B();
//        long[][] Ris_2 = analysis_2.getResponseTimeByDMPO(tasks, resources, 1, false, true, true, true, false);

//        ResponseTimeAnalysisWithVariousPriorities analysis_3 = new ResponseTimeAnalysisWithVariousPriorities();
//        long[][] Ris_3 = analysis_3.getResponseTimeByDMPO(tasks, resources, 1, true, true, true, true, false);

        System.out.println(1);


    }
}
