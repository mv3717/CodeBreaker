import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String USER_AGENT = "application/json;charset=UTF-8";

    private static final String POST_URL = "https://qcvault.herokuapp.com/unlock_safe";


    public static void main(String[] args) throws InterruptedException {

        long startTime = System.nanoTime();

        ExecutorService service = Executors.newFixedThreadPool(1000);

        for (int i = 0; i < 1000; i++) {
            service.submit(new Runner((i/100)%10,  (i/10)%10, i%10, service));
        }

        try {
            service.shutdown();
            service.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            service.shutdownNow();
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000000;  //divide by 1000000000 to get seconds.


        System.out.println("I found it in: "+ duration + "s!");

    }

    private static String sendPOST(String params) throws IOException {
        URL obj = new URL(POST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", USER_AGENT);
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(params.getBytes());
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            System.out.println("POST request not worked");
            return null;
        }

    }

    private static class Runner implements Runnable {

        private final int one;
        private final int two;
        private final int three;
        ExecutorService service;


        Runner(int one, int two, int three, ExecutorService service ) {
            this.one = one;
            this.two = two;
            this.three = three;
            this.service = service;
        }

        @Override
        public void run() {
            String body = "{\"first\":" + one + ",\"second\":" + two + ",\"third\":" + three + "}";
            if (!service.isShutdown()) {
                try {
                    String response = sendPOST(body);
                    if (!response.equals("Wrong code")) {
                        System.out.println("The code is: " + one + two + three);
                        service.shutdownNow();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}



