import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WgerService {
    private static final String BASE = "https://wger.de/api/v2";


    //GET simple que retorna el JSON (ya decodificado)
    private static JSONObject getJson(String endpoint) throws Exception {
        URL url = new URL(BASE + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Finge ser un navegador
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestMethod("GET");

        try (InputStream in = con.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            String body = out.toString(StandardCharsets.UTF_8);
            return new JSONObject(body);
        }
    }

    // Trae una “página” de ejercicios (en inglés language=2). Devuelve el JSON con results/next/previous.
    public static JSONObject fetchExercisesPage(int limit, int offset) throws Exception {
        // Usamos el idioma inglés (id=2) que es el más completo en la API
        String ep = "/exercise/?format=json&language=2&limit=" + limit + "&offset=" + offset;
        return getJson(ep);
    }

    //Devuelve URL de imagen principal (o vacío si no hay).
    public static String fetchMainImageUrl(int exerciseId) throws Exception {
        String ep = "/exerciseimage/?format=json&is_main=True&exercise=" + exerciseId;
        JSONObject json = getJson(ep);
        JSONArray results = json.getJSONArray("results");
        System.out.println(results);
        if (results.length() > 0) {
            return results.getJSONObject(0).getString("image");
        }
        return "";
    }

    //Quita tags HTML simples de wger (descripción)
    public static String stripHtml(String html) {
        if (html == null) return "";
        return html
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}