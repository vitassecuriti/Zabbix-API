package application.utils;


import application.constants.Constants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by VSKryukov on 30.01.2016.
 */
public class ParamsReader {

    public Map<String, String> ReadParamsFromFile() throws Exception {
        Map<String, String> hostParamses = new HashMap<>();

        File file = new File(Constants.fileHostParams);

        if (!file.exists() || file.isDirectory()) {
            throw new Exception(String.format("File \"'%s'\" does not exisss!", file.getName()));
        }

        Scanner sc = new Scanner(file);
        int countLine = 0;
        //Ignor header from file
        sc.nextLine();
        while (sc.hasNext()) {
            String[] line = sc.nextLine().split(";");
            countLine++;
            if (line.length != 2) {
                throw new Exception(String.format("Wrong number params in line '%s'!", countLine));
            }


            hostParamses.put(line[0],line[1]);
        }

        return hostParamses;
    }
}
