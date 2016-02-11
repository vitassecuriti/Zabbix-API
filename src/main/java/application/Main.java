package application;
import com.alibaba.fastjson.JSONObject;
import io.github.hengyunabc.zabbix.api.DefaultZabbixApi;
import io.github.hengyunabc.zabbix.api.Request;
import io.github.hengyunabc.zabbix.api.RequestBuilder;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static String EXECUTION_DIR = new File("").getAbsolutePath();
    public static String fileHostParams = EXECUTION_DIR + "/IPReportByHostName.txt";
    public static DefaultZabbixApi zabbixApi;

    /*Поиск Хоста и обновление параметров */
    public static void main(String[] args) throws Exception {
        String url = "http://192.168.45.25/zabbix/api_jsonrpc.php";

        try {
            zabbixApi = new DefaultZabbixApi(url);
            zabbixApi.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean login = zabbixApi.login("vkryukov", "Deboro55");


        Map<String, String> params = ReadhostNameFromFile();
        String hostid = "";
        String interfaceid = "";

        for (Map.Entry hostParam : params.entrySet()) {
            String hostName = hostParam.getKey().toString();
            String ip = hostParam.getValue().toString();

            //Проверка существования хоста

            Request request = RequestBuilder.newBuilder()
                    .method("host.exists")
                    .paramEntry("host", ip)
                    .build();
            JSONObject response = zabbixApi.call(request);
            boolean hostexist =  response.getBooleanValue("result");

            if (hostexist) {
                // Поиск Узла
                JSONObject filter = new JSONObject();
                filter.put("host", ip);
                request = RequestBuilder.newBuilder()
                        .method("host.get")
                        .paramEntry("filter", filter)
                        .build();
                System.out.println(request);
                response = zabbixApi.call(request);

                hostid = response.getJSONArray("result").getJSONObject(0).getString("hostid");

                //переименование хоста
                request = RequestBuilder.newBuilder()
                        .method("host.update")
                        .paramEntry("hostid", hostid)
                        .paramEntry("host", hostName)
                        .paramEntry("name", hostName)
                        .build();

                zabbixApi.call(request);

                //Поиск интерфейса узла
                request = RequestBuilder.newBuilder()
                        .method("hostinterface.get")
                        .paramEntry("output", new String[]{"interfaceid"})
                        .paramEntry("hostids", hostid)
                        .build();

                response = zabbixApi.call(request);

                interfaceid = response.getJSONArray("result").getJSONObject(0).getString("interfaceid");

                //Изменение интерфейса Хоста
                request = RequestBuilder.newBuilder()
                        .method("hostinterface.update")
                        .paramEntry("interfaceid", interfaceid)
                        .paramEntry("dns", hostName.toLowerCase())
                        .paramEntry("useip", 0)
                        .build();

                zabbixApi.call(request);

                } else {
                System.out.println("Host with name - " + ip + " not found;");
            }


        }
    }

    /**
     * Читает из файла DNS узлов
     * @return maping <hostName, IP>
     */
    public static Map<String, String> ReadhostNameFromFile() throws Exception {
        Map<String, String> hostParams = new HashMap<>();

        File file = new File(fileHostParams);
        Scanner sc = new Scanner(file);

        //Ignor header from file
        sc.nextLine();

        while (sc.hasNext()) {
            String[] line = sc.nextLine().split(";");
            hostParams.put(line[0], getIPbyHostName(line[0]));
        }

        return hostParams;
    }

    /**
     * Return IP address by dns host name
     * @param hostName
     * @return IP
     */
    public static String getIPbyHostName(String hostName) {
        String ipAddr = "";
        try {
            InetAddress inetAddr = InetAddress.getByName(hostName);
            byte[] addr = inetAddr.getAddress();
            // Convert to dot representation

            for (int i = 0; i < addr.length; i++) {
                if (i > 0) {
                    ipAddr += ".";
                }
                ipAddr += addr[i] & 0xFF;
            }

        } catch (UnknownHostException e) {
            System.out.println("Host not found: " + e.getMessage());
        }
        return ipAddr;
    }

}

