package kg.akoikelov.prbubble.pypi;

import kg.akoikelov.prbubble.exception.PackageNotFoundException;
import kg.akoikelov.prbubble.exception.PypiUnknownException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by akoikelov
 */
public class PackageInfoRetriever {

    private static final String PYPI_RETRIEVE_JSON_URL = "https://pypi.python.org/pypi/%s/json";

    public static JSONObject getInfo(String packageName) throws IOException, PackageNotFoundException, PypiUnknownException, JSONException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(String.format(PYPI_RETRIEVE_JSON_URL, packageName));
        String info = "";

        try (CloseableHttpResponse response = client.execute(get)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 404) {
                throw new PackageNotFoundException();
            } else if (statusCode == 200) {
                Scanner scanner = new Scanner(response.getEntity().getContent());

                while (scanner.hasNextLine()) {
                    info += scanner.nextLine();
                }
            } else {
                throw new PypiUnknownException();
            }
        }

        return new JSONObject(info);
    }

}
